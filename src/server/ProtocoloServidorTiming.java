package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import cipherLogic.Simetricos;

public class ProtocoloServidorTiming {
     public static void procesar(BufferedReader pIn, PrintWriter pOut, PrivateKey privateKey, PublicKey publicKey)
               throws Exception {
          String inputLine;
          String outputLine;
          int estado = 0;
          Simetricos dh = new Simetricos();
          SecretKey kAB1 = null;
          SecretKey kAB2 = null;
          IvParameterSpec ivSpec = null;

          while (estado < 7 && (inputLine = pIn.readLine()) != null) {
               // System.out.println("Entrada a procesar: " + inputLine);
               switch (estado) {
                    case 0:
                         if (inputLine.equalsIgnoreCase("SECINIT")) {
                              outputLine = "Listo";
                              estado++;
                         } else {
                              outputLine = "ERROR. Esperaba SECINIT";
                              estado = 0;
                         }
                         break;

                    case 1:
                         try {
                              String numeroCifradoBase64 = inputLine;
                              System.out.println(
                                        "Thread " + Thread.currentThread().getId() + " comenzando descifrado.");
                              byte[] numeroDescifrado = null;
                              long startTime = System.nanoTime();
                              for (int i = 0; i < 50; i++) {
                                   numeroDescifrado = cipherLogic.Asimetricos.descifrarConClavePrivada(
                                             numeroCifradoBase64,
                                             privateKey);
                              }
                              long endTime = System.nanoTime();
                              long duration = endTime - startTime;
                              System.out.println(
                                        "################# El tiempo de descifrar el reto fue: " + (duration / 50) +
                                                  " nanosegundos");
                              outputLine = "" + new BigInteger(numeroDescifrado).toString();
                              estado++;
                         } catch (Exception e) {
                              outputLine = "ERROR en argumento esperado del reto";
                              estado = 0;
                         }
                         break;

                    case 2:
                         if (inputLine.equalsIgnoreCase("OK")) {
                              BigInteger G = dh.getG();
                              BigInteger P = dh.getP();
                              BigInteger Gx = dh.getGx();

                              String mensaje = "" + G + " " + P + " " + Gx;
                              String Firma = cipherLogic.Asimetricos.firmarSHA1withRSA(mensaje, privateKey);
                              outputLine = mensaje + ":::" + Firma;
                              estado++;
                         } else {
                              outputLine = "ERROR. Esperaba OK";
                              estado = 0;
                         }
                         break;
                    case 3:
                         if (inputLine.equalsIgnoreCase("OK")) {
                              outputLine = "A mitad de camino";
                              estado++;
                         } else {
                              outputLine = "ERROR. Esperaba OK";
                              estado = 0;
                         }
                         break;
                    case 4:
                         try {
                              BigInteger Gy = new BigInteger(inputLine, 10);
                              BigInteger x = dh.getX();
                              BigInteger P = dh.getP();
                              BigInteger GYx = Gy.modPow(x, P);
                              // System.out.println("USADOS: " + Gy + " "+ x + " "+ P + " "+ GYx);
                              SecretKey[] kABs = cipherLogic.Simetricos.getKABs(GYx);
                              kAB1 = kABs[0];
                              kAB2 = kABs[1];
                              ivSpec = cipherLogic.Simetricos.generarIV();
                              // System.out.println("KAB1 EN SERVER: " + kAB1.toString());
                              // System.out.println("KAB2 EN SERVER: " + kAB2.toString());
                              String ivBase64 = cipherLogic.Simetricos.ivToBase64(ivSpec);
                              // System.out.println("IV EN SERVER: " + ivBase64);
                              outputLine = "" + ivBase64;
                              estado++;
                         } catch (Exception e) {
                              outputLine = "ERROR en argumento esperado del G^y";
                              estado = 0;
                         }
                         break;
                    case 5:
                         if (inputLine.equals("TERMINAR")) {
                              outputLine = "Adiós :)";
                              estado++;
                         } else {
                              try {
                                   System.out.println(
                                             "Thread " + Thread.currentThread().getId()
                                                       + " comenzando verificación de consulta.");
                                   int valor = 99;
                                   long startTime = System.nanoTime();
                                   for (int i = 0; i < 50; i++) {
                                        String[] partesCifradas = inputLine.split(";;;");
                                        String[] partesUId = partesCifradas[0].split(":::");
                                        String[] partesPaqueteId = partesCifradas[1].split(":::");
                                        String CUId = partesUId[0];
                                        String HMACUId = partesUId[1];
                                        String CPaqueteId = partesPaqueteId[0];
                                        String HMACPaqueteId = partesPaqueteId[1];
                                        if (!cipherLogic.Simetricos.verifyHMAC(CUId, HMACUId, kAB2)) {
                                             throw new SecurityException(
                                                       "El HMAC del UId no coincide. El mensaje podría haber sido alterado.");
                                        }
                                        int UId = Integer
                                                  .parseInt(cipherLogic.Simetricos.decryptAES(CUId, kAB1, ivSpec));
                                        if (!cipherLogic.Simetricos.verifyHMAC(CPaqueteId, HMACPaqueteId, kAB2)) {
                                             throw new SecurityException(
                                                       "El HMAC del PaqueteIdno coincide. El mensaje podría haber sido alterado.");
                                        }
                                        int PaqueteId = Integer
                                                  .parseInt(cipherLogic.Simetricos.decryptAES(CPaqueteId, kAB1,
                                                            ivSpec));
                                        // System.out.println("CONSULTALNDO: " + UId + " - " + PaqueteId);
                                        valor = Servidor.tablaInfo[UId][PaqueteId];
                                   }
                                   long endTime = System.nanoTime();
                                   long duration = endTime - startTime;
                                   System.out.println(
                                             "################# El tiempo de verificar la consulta fue: " + (duration /
                                                       50)
                                                       + " nanosegundos");

                                   System.out.println(
                                             "Thread " + Thread.currentThread().getId()
                                                       + " comenzando cifrado asimétrico de estado.");
                                   long startTime2 = System.nanoTime();
                                   for (int i = 0; i < 50; i++) {
                                        byte[] bytesValor = ByteBuffer.allocate(4).putInt(valor).array();
                                        byte[] cipherAsimetrico = cipherLogic.Asimetricos
                                                  .cifrarConClavePublica(bytesValor, publicKey);
                                   }
                                   long endTime2 = System.nanoTime();
                                   long duration2 = endTime2 - startTime2;
                                   System.out.println(
                                             "################# El tiempo de cifrar asimétricamente el estado fue: "
                                                       + (duration2 / 50)
                                                       + " nanosegundos");

                                   System.out.println(
                                   "Thread " + Thread.currentThread().getId()
                                   + " comenzando cifrado simétrico de estado.");
                                   String cipherEstado = "";
                                   long startTime3 = System.nanoTime();
                                   for (int i = 0; i < 50; i++) {
                                   cipherEstado = cipherLogic.Simetricos.encryptAndSign("" + valor, kAB1,
                                   kAB2,
                                   ivSpec);
                                   }
                                   long endTime3 = System.nanoTime();
                                   long duration3 = endTime3 - startTime3;
                                   System.out.println(
                                   "################# El tiempo de cifrar simétricamente el estado fue: "
                                   + (duration3 / 50)
                                   + " nanosegundos");
                                   outputLine = "" + cipherEstado;
                              } catch (Exception e) {
                                   outputLine = "ERROR en argumento esperado de la solicutud de estado";
                                   estado = 0;
                              }
                         }
                         break;
                    case 6:
                         if (inputLine.equalsIgnoreCase("TERMINAR")) {
                              outputLine = "Adiós :)";
                              estado++;
                         } else {
                              outputLine = "ERROR. Esperaba TERMINAR";
                              estado = 0;
                         }
                         break;
                    default:
                         outputLine = "ERROR";
                         estado = 0;
                         break;
               }
               pOut.println(outputLine);
          }
     }

}

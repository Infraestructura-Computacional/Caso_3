package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import Llaves.DiffieHellman;

public class ProtocoloServidor {
     public static void procesar(BufferedReader pIn, PrintWriter pOut, PrivateKey privateKey, PublicKey publicKey)
               throws Exception {
          String inputLine;
          String outputLine;
          int estado = 0;
          DiffieHellman dh = new DiffieHellman();
          SecretKey kAB1;
          SecretKey kAB2;
          IvParameterSpec ivSpec;

          while (estado < 5 && (inputLine = pIn.readLine()) != null) {
               System.out.println("Entrada a procesar: " + inputLine);
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
                              byte[] numeroDescifrado = Llaves.RSA.descifrarConClavePrivada(numeroCifradoBase64,
                                        privateKey);
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
                              String Firma = Llaves.RSA.firmarSHA1withRSA(mensaje, privateKey);
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
                              kAB1 = Llaves.DiffieHellman.getKAB(GYx, "AES");
                              kAB2 = Llaves.DiffieHellman.getKAB(GYx, "HmacSHA384");
                              ivSpec = Llaves.DiffieHellman.generarIV();
                              System.out.println("KAB1 EN SERVER: " + kAB1.toString());
                              System.out.println("KAB2 EN SERVER: " + kAB2.toString());
                              String ivBase64 = Llaves.DiffieHellman.ivToBase64(ivSpec);
                              System.out.println("IV EN SERVER: " + ivBase64);
                              outputLine = "" + ivBase64;
                              estado++;
                         } catch (Exception e) {
                              outputLine = "ERROR en argumento esperado del reto";
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

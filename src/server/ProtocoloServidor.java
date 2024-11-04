package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

import Llaves.DiffieHellman;

public class ProtocoloServidor {
     public static void procesar(BufferedReader pIn, PrintWriter pOut, PrivateKey privateKey, PublicKey publicKey)
               throws Exception {
          String inputLine;
          String outputLine;
          int estado = 0;

          while (estado < 4 && (inputLine = pIn.readLine()) != null) {
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
                              byte[] numeroDescifrado = Llaves.RSA.descifrarConClavePrivada(numeroCifradoBase64, privateKey);
                              outputLine = "" + new BigInteger(numeroDescifrado).toString();
                              estado++;
                         } catch (Exception e) {
                              outputLine = "ERROR en argumento esperado del reto";
                              estado = 0;
                         }
                         break;

                    case 2:
                         if (inputLine.equalsIgnoreCase("OK")) {
                              DiffieHellman df = new DiffieHellman();
                              BigInteger G = df.getG();
                              BigInteger P = df.getP();
                              BigInteger Gx = df.getGx();
                              String mensaje = "" + G + " " + P + " " + Gx;
                              String Firma = Llaves.RSA.firmarSHA1withRSA(mensaje,privateKey);
                              outputLine = mensaje + ":::" + Firma;
                              estado++;
                         } else {
                              outputLine = "ERROR. Esperaba OK";
                              estado = 0;
                         }
                         break;
                    case 3:
                    if (inputLine.equalsIgnoreCase("OK")) {
                         outputLine = "Adios :)";
                         estado++;
                    } else {
                         outputLine = "ERROR. Esperaba OK";
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

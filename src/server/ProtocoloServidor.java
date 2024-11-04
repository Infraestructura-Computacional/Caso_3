package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

public class ProtocoloServidor {
     public static void procesar(BufferedReader pIn, PrintWriter pOut, PrivateKey privateKey, PublicKey publicKey)
               throws IOException {
          String inputLine;
          String outputLine;
          int estado = 0;

          while (estado < 3 && (inputLine = pIn.readLine()) != null) {
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
                              outputLine = "ADIOS";
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

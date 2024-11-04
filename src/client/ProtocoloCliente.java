package client;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class ProtocoloCliente {
     public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, PublicKey serverPublicKey,
               int idCliente, int numPeticiones)
               throws Exception {
          String fromServer;

          System.out.println("El usuario " + idCliente + " escribió: SECINIT");
          pOut.println("SECINIT"); // Enviar mensaje al servidor // Estado 0 paso 1
          // Leer respuesta del servidor
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor para el usuario " + idCliente + ": " + fromServer);
          }

          BigInteger reto = generarReto();
          String R = calcularReto(reto, serverPublicKey);
          System.out.println("El usuario " + idCliente + " escribió su reto: " + R);
          pOut.println(R); // Estado 1 paso 2.a
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor para el usuario " + idCliente + ": " + fromServer);
          }

          boolean checkReto = reto.equals(new BigInteger(fromServer));
          String verificacionReto = (checkReto) ? " verificó el reto con éxito" : " falló al verificar el reto";
          System.out.println("El usuario " + idCliente + verificacionReto);
          if (checkReto)
               pOut.println("OK"); // Estado 2 paso 6
          else
               pOut.println("ERROR");
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor para el usuario " + idCliente + ": " + fromServer);
          }

          String[] partes = fromServer.split(":::");
          String mensaje = partes[0];
          String firma = partes[1];
          boolean checkFirma = cipherLogic.Asimetricos.verificarFirmaSHA1withRSA(mensaje, firma, serverPublicKey);
          String verificacionFirma = (checkFirma) ? " verificó la firma con éxito" : " falló al verificar la firma";
          System.out.println("El usuario " + idCliente + verificacionFirma);
          if (checkFirma)
               pOut.println("OK"); // Estado 3 paso 10
          else
               pOut.println("ERROR");
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor para el usuario " + idCliente + ": " + fromServer);
          }

          String[] valoresDh = mensaje.split(" ");
          BigInteger G = new BigInteger(valoresDh[0], 10);
          BigInteger P = new BigInteger(valoresDh[1], 10);
          BigInteger Gx = new BigInteger(valoresDh[2], 10);
          SecureRandom random = new SecureRandom();
          BigInteger y = new BigInteger(P.bitLength() - 1, random).add(BigInteger.ONE);
          BigInteger Gy = G.modPow(y, P);
          System.out.println("El usuario " + idCliente + " escribió (G^y): " + Gy);
          pOut.println("" + Gy); // Estado 4 paso 11
          // Leer respuesta del servidor
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor para el usuario " + idCliente + ": " + fromServer);
          }

          BigInteger GXy = Gx.modPow(y, P);
          // System.out.println("El usuario calculó (G^x)^y: " + GXy);
          // System.out.println("USANDO: " + Gy + " "+ y + " "+ P + " "+ GXy);
          SecretKey[] kABs = cipherLogic.Simetricos.getKABs(GXy);
          SecretKey kAB1 = kABs[0];
          SecretKey kAB2 = kABs[1];
          IvParameterSpec ivSpec = cipherLogic.Simetricos.base64ToIv(fromServer);
          System.out.println("El usuario " + idCliente + " calculó sus llaves simétricas");
          // System.out.println("KAB1 EN CLIENTE: " + kAB1.toString());
          // System.out.println("KAB2 EN CLIENTE: " + kAB2.toString());
          // System.out.println("IV EN CLIENTE: " + fromServer);

          String cipherUId = cipherLogic.Simetricos.encryptAndSign("" + idCliente, kAB1, kAB2, ivSpec);
          int idPaquete = (numPeticiones == 1) ? idCliente : 0;
          for (int i = 0; i < numPeticiones; i++) {
               System.out.println("Soy el usuario: " + idCliente + " y quiero el paquete: " + idPaquete);
               String cipherPaqueteId = cipherLogic.Simetricos.encryptAndSign("" + idPaquete, kAB1, kAB2, ivSpec);
               String solicitud = "" + cipherUId + ";;;" + cipherPaqueteId;
               // System.out.println("El usuario solicitó el estado del paquete: " +
               // idPaquete);
               pOut.println(solicitud); // Estado 5 pasos 13-14
               // Leer respuesta del servidor
               if ((fromServer = pIn.readLine()) != null) {
                    System.out.println("Respuesta del Servidor para el usuario " + idCliente + ": " + fromServer);
                    String[] partesEstado = fromServer.split(":::");
                    String CEstado = partesEstado[0];
                    String HMACEstado = partesEstado[1];
                    if (!cipherLogic.Simetricos.verifyHMAC(CEstado, HMACEstado, kAB2)) {
                         throw new SecurityException(
                                   "El HMAC del Estado no coincide. El mensaje podría haber sido alterado.");
                    }
                    int estado = Integer.parseInt(cipherLogic.Simetricos.decryptAES(CEstado, kAB1, ivSpec));
                    String estadoString = server.Servidor.getEstado(estado);
                    System.out.println("El usuario " + idCliente + " recibió que el estado del paquete " + idPaquete + " es: " + estadoString);
               }
               idPaquete++;
          }

          System.out.println("El usuario " + idCliente + " escribió: TERMINAR");
          pOut.println("TERMINAR"); // Enviar mensaje al servidor // Estado 0 paso 1
          // Leer respuesta del servidor
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor para el usuario " + idCliente + ": " + fromServer);
          }

     }

     public static BigInteger generarReto() {
          SecureRandom random = new SecureRandom();
          BigInteger numeroAleatorio = new BigInteger(128, random);
          // System.out.println("Número aleatorio generado: " + numeroAleatorio);
          return numeroAleatorio;
     }

     public static String calcularReto(BigInteger reto, PublicKey publicKey) throws Exception {

          // Cifrar el número aleatorio con la clave pública del servidor
          byte[] numeroCifrado = cipherLogic.Asimetricos.cifrarConClavePublica(reto.toByteArray(), publicKey);
          String numeroCifradoBase64 = Base64.getEncoder().encodeToString(numeroCifrado);

          return numeroCifradoBase64;
     }

}

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

          System.out.println("El usuario escribió: SECINIT");
          pOut.println("SECINIT"); // Enviar mensaje al servidor // Estado 0 paso 1
          // Leer respuesta del servidor
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor: " + fromServer);
          }

          BigInteger reto = generarReto();
          String R = calcularReto(reto, serverPublicKey);
          System.out.println("El usuario escribió: " + R);
          pOut.println(R); // Estado 1 paso 2.a
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor: " + fromServer);
          }

          boolean checkReto = reto.equals(new BigInteger(fromServer));
          System.out.println("El usuario verificó el reto: " + checkReto);
          if (checkReto)
               pOut.println("OK"); // Estado 2 paso 6
          else
               pOut.println("ERROR");
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor: " + fromServer);
          }

          String[] partes = fromServer.split(":::");
          String mensaje = partes[0];
          String firma = partes[1];
          boolean checkFirma = Llaves.RSA.verificarFirmaSHA1withRSA(mensaje, firma, serverPublicKey);
          System.out.println("El usuario verificó la firma: " + checkFirma);
          if (checkReto)
               pOut.println("OK"); // Estado 3 paso 10
          else
               pOut.println("ERROR");
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor: " + fromServer);
          }

          String[] valoresDh = mensaje.split(" ");
          BigInteger G = new BigInteger(valoresDh[0], 10);
          BigInteger P = new BigInteger(valoresDh[1], 10);
          BigInteger Gx = new BigInteger(valoresDh[2], 10);
          SecureRandom random = new SecureRandom();
          BigInteger y = new BigInteger(P.bitLength() - 1, random).add(BigInteger.ONE);
          BigInteger Gy = G.modPow(y, P);
          System.out.println("El usuario escribió (G^y): " + Gy);
          pOut.println("" + Gy); // Estado 4 paso 11
          // Leer respuesta del servidor
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor: " + fromServer);
          }

          BigInteger GXy = Gx.modPow(y, P);
          // System.out.println("El usuario calculó (G^x)^y: " + GXy);
          // System.out.println("USANDO: " + Gy + " "+ y + " "+ P + " "+ GXy);
          SecretKey[] kABs = Llaves.DiffieHellman.getKABs(GXy);
          SecretKey kAB1 = kABs[0];
          SecretKey kAB2 = kABs[1];
          IvParameterSpec ivSpec = Llaves.DiffieHellman.base64ToIv(fromServer);
          // System.out.println("KAB1 EN CLIENTE: " + kAB1.toString());
          // System.out.println("KAB2 EN CLIENTE: " + kAB2.toString());
          // System.out.println("IV EN CLIENTE: " + fromServer);

          String cipherUId = Llaves.DiffieHellman.encryptAndSign("" + idCliente, kAB1, kAB2, ivSpec);
          int idPaquete = (numPeticiones == 1) ? idCliente : 0;
          for (int i = 0; i < numPeticiones; i++) {
               System.out.println("Soy id: " + idCliente + " y quiero el paquete: " + idPaquete);
               String cipherPaqueteId = Llaves.DiffieHellman.encryptAndSign("" + idPaquete, kAB1, kAB2, ivSpec);
               String solicitud = "" + cipherUId + ";;;" + cipherPaqueteId;
               // System.out.println("El usuario solicitó el estado del paquete: " +
               // idPaquete);
               pOut.println(solicitud); // Estado 5 pasos 13-14
               // Leer respuesta del servidor
               if ((fromServer = pIn.readLine()) != null) {
                    System.out.println("Respuesta del Servidor: " + fromServer);
                    String[] partesEstado = fromServer.split(":::");
                    String CEstado = partesEstado[0];
                    String HMACEstado = partesEstado[1];
                    if (!Llaves.DiffieHellman.verifyHMAC(CEstado, HMACEstado, kAB2)) {
                         throw new SecurityException(
                                   "El HMAC del Estado no coincide. El mensaje podría haber sido alterado.");
                    }
                    int estado = Integer.parseInt(Llaves.DiffieHellman.decryptAES(CEstado, kAB1, ivSpec));
                    String estadoString = server.Servidor.getEstado(estado);
                    System.out.println("El estado del paquete es: " + estadoString);
               }
               idPaquete++;
          }

          System.out.println("El usuario escribió: TERMINAR");
          pOut.println("TERMINAR"); // Enviar mensaje al servidor // Estado 0 paso 1
          // Leer respuesta del servidor
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor: " + fromServer);
          }

     }

     public static BigInteger generarReto() {
          SecureRandom random = new SecureRandom();
          BigInteger numeroAleatorio = new BigInteger(128, random);
          System.out.println("Número aleatorio generado: " + numeroAleatorio);
          return numeroAleatorio;
     }

     public static String calcularReto(BigInteger reto, PublicKey publicKey) throws Exception {

          // Cifrar el número aleatorio con la clave pública del servidor
          byte[] numeroCifrado = Llaves.RSA.cifrarConClavePublica(reto.toByteArray(), publicKey);
          String numeroCifradoBase64 = Base64.getEncoder().encodeToString(numeroCifrado);

          return numeroCifradoBase64;
     }

}

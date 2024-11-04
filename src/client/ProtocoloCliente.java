package client;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;

public class ProtocoloCliente {
     public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, PublicKey serverPublicKey)
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
          if (checkReto) pOut.println("OK"); // Estado 2 paso 6
          else pOut.println("ERROR");
          if ((fromServer = pIn.readLine()) != null) {
               System.out.println("Respuesta del Servidor: " + fromServer);
          }

          String[] partes = fromServer.split(":::");
          String mensaje = partes[0];
          String firma = partes[1];
          boolean checkFirma = Llaves.RSA.verificarFirmaSHA1withRSA(mensaje, firma, serverPublicKey);
          System.out.println("El usuario verificó la firma: " + checkFirma);
          if (checkReto) pOut.println("OK"); // Estado 2 paso 6
          else pOut.println("ERROR");
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

package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ProtocoloCliente {
     public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws IOException {
          String[] mensajes = {"Hola", "5", "OK"};
          String fromServer;
          boolean ejecutar = true;

          for (String fromUser : mensajes) {
               System.out.println("El usuario escribió: " + fromUser);
               pOut.println(fromUser); // Enviar mensaje al servidor

               // Leer respuesta del servidor
               if ((fromServer = pIn.readLine()) != null) {
                    System.out.println("Respuesta del Servidor: " + fromServer);
               }
          }
     }
}

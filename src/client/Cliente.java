package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;

public class Cliente {
     public static final int PUERTO = 3400;
     public static final String SERVIDOR = "localhost";

     public static void runClient(int peticiones, PublicKey serverPublicKey) throws IOException {
          try (Socket socket = new Socket(SERVIDOR, PUERTO);
                    PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

               ProtocoloCliente.procesar(stdIn, lector, escritor, serverPublicKey);

          } catch (Exception e) {
               e.printStackTrace();
          }
     }
}

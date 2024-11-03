package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import client.Cliente;

public class Servidor {
     public static void main(String args[]) throws IOException {
          ServerSocket ss = null;
          boolean continuar = true;
          int numeroThreads = 0;

          System.out.println("Main Server ...");

          try {
               ss = new ServerSocket(Cliente.PUERTO);
          } catch (IOException e) {
               System.err.println("No se pudo crear el socket en el puerto: "
                         + Cliente.PUERTO);
               System.exit(-1);
          }

          while (continuar) {
               // crear el thread y lanzarlo.

               // crear el socket
               Socket socket = ss.accept();

               // crear el thread con el socket y el id
               ThreadServidor thread = new ThreadServidor(socket,
                         numeroThreads);
               numeroThreads++;

               // start
               thread.start();
          }

          ss.close();
     }

}

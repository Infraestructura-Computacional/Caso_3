package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

import client.Cliente;

public class Servidor {

     public static int[][] tablaInfo = generarMatriz(); //fila usuario columna paquete

     private static int[][] generarMatriz() {
        int[][] matriz = new int[32][32];
        Random random = new Random();

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                matriz[i][j] = random.nextInt(7); // Genera un número entre 0 y 7
            }
        }

        return matriz;
    }

     public static void runServer(int numClientes, PrivateKey privateKey, PublicKey publicKey) throws IOException {
          ServerSocket ss = null;
          int numeroThreads = 0;

          System.out.println("Main Server ...");

          try {
               ss = new ServerSocket(Cliente.PUERTO);
          } catch (IOException e) {
               System.err.println("No se pudo crear el socket en el puerto: "
                         + Cliente.PUERTO);
               System.exit(-1);
          }

          while (numClientes > 0) {
               // crear el thread y lanzarlo.

               // crear el socket
               Socket socket = ss.accept();

               // crear el thread con el socket y el id
               ThreadServidor thread = new ThreadServidor(socket,
                         numeroThreads, privateKey, publicKey);
               numeroThreads++;

               // start
               thread.start();
               numClientes--;
          }

          //ss.close();
     }

     public static String getEstado(int valor) {
          return Estado.obtenerDescripcion(valor);
      }
}

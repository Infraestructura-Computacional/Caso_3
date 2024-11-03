import java.io.IOException;
import java.util.Scanner;

import client.Cliente;
import server.Servidor;

public class Main {
     public static void main(String[] args) {
          Scanner scanner = new Scanner(System.in);
          Main mainInstance = new Main(); // Crear una instancia de Main

          System.out.println("1) Generar pareja de llaves");
          System.out.println("2) Ejecutar");
          int opcion = scanner.nextInt();

          if (opcion == 1) {
               mainInstance.generarLlaves();
          } else if (opcion == 2) {
               try {
                    // mainInstance.ejecutar(1,32);
                    mainInstance.ejecutar(4, 1);
                    // mainInstance.ejecutar(8,1);
                    // mainInstance.ejecutar(32,1);
               } catch (IOException e) {
                    e.printStackTrace();
               }
          } else {
               System.out.println("Opción inválida. Por favor, digite 1 o 2.");
          }

          scanner.close();
     }

     // TODO
     public void generarLlaves() {

     }

     // TODO
     public void ejecutar(int numClientes, int peticionesPorCliente) throws IOException {
          // Ejecutar el servidor en un hilo separado
          Thread servidorThread = new Thread(() -> {
              try {
                  Servidor.main(null);
              } catch (IOException e) {
                  e.printStackTrace();
              }
          });
          servidorThread.start(); // Iniciar el hilo del servidor
      
          // Esperar un momento para asegurarse de que el servidor esté listo antes de iniciar los clientes
          try {
              Thread.sleep(3000); // Aumenta el tiempo de espera si es necesario
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
      
          // Ejecutar los clientes de forma secuencial con pausa adicional
          for (int i = 0; i < numClientes; i++) {
              try {
                  Cliente.main(null);
                  Thread.sleep(2000); // Espera más tiempo entre conexiones de clientes
              } catch (IOException | InterruptedException e) {
                  e.printStackTrace();
              }
          }
      }
            
}

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import client.Cliente;
import server.Servidor;

public class Main {
     public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
          Scanner scanner = new Scanner(System.in);
          Main mainInstance = new Main();

          System.out.println("---------------MENU---------------");
          System.out.println("1) Generar pareja de llaves");
          System.out.println("2) Ejecutar");
          System.out.println("3) Salir");
          int opcion = scanner.nextInt();

          if (opcion == 1) {
              mainInstance.generarLlaves(); // Llamar a través de la instancia
              System.out.println("Llaves generadas correctamente.");
          } else if (opcion == 2) {
               try {
                    // mainInstance.ejecutar(1,"32");
                    mainInstance.ejecutar(1, "1");
                    // mainInstance.ejecutar(4, "1");
                    // mainInstance.ejecutar(8,"1");
                    // mainInstance.ejecutar(32,"1");
               } catch (IOException e) {
                    e.printStackTrace();
               }
          } else if (opcion == 3) {
               System.out.println("Adiós :)");
          } else {
               System.out.println("Opción inválida. Por favor, digite 1, 2 o 3.");
          }

          scanner.close();
     }

     // TODO
     public void ejecutar(int numClientes, String peticionesPorCliente) throws IOException {
          Thread servidorThread = new Thread(() -> {
               try {
                    Servidor.main(null);
               } catch (IOException e) {
                    e.printStackTrace();
               }
          });
          servidorThread.start();

          // Esperar un momento para asegurarse de que el servidor esté listo antes de
          // iniciar los clientes
          // try {
          // Thread.sleep(500);
          // } catch (InterruptedException e) {
          // e.printStackTrace();
          // }

          for (int i = 0; i < numClientes; i++) {
               try {
                    String[] args = {peticionesPorCliente};
                    Cliente.main(args);
                    // Thread.sleep(50);
               } catch (IOException e) {
                    e.printStackTrace();
               }
               // catch (InterruptedException e) {
               // e.printStackTrace();
               // }
          }
     }
    //TODO
    public void generarLlaves() throws NoSuchAlgorithmException, IOException{
        Llaves.RSA.guardarLlaves();
    }

}

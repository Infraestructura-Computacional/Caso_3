import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Scanner;

import client.Cliente;
import server.Servidor;

public class Main {
     public static String rutaLlavePrivada = "src/server/K_w-.txt";
     public static String rutaLlavePublica = "src/K_w+.txt";

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
                    System.out.println("Escenario 1: 1 Cliente, 32 Peticiones");
                    System.out.println("Escenario 2: 4 Clientes, 1 Petición C/U");
                    System.out.println("Escenario 3: 8 Clientes, 1 Petición C/U");
                    System.out.println("Escenario 4: 32 Clientes, 1 Petición C/U");
                    int escenario = scanner.nextInt();
                    if (escenario == 1) mainInstance.ejecutar(1,32);
                    else if (escenario == 2) mainInstance.ejecutar(4,1);
                    else if (escenario == 3) mainInstance.ejecutar(8,1);
                    else if (escenario == 4) mainInstance.ejecutar(32, 1);
                    else System.out.println("Opción inválida. Por favor, digite 1, 2, 3 o 4");
               } catch (Exception e) {
                    e.printStackTrace();
               }
          } else if (opcion == 3) {
               System.out.println("Adiós :)");
          } else {
               System.out.println("Opción inválida. Por favor, digite 1, 2 o 3");
          }

          scanner.close();
     }

     // TODO
     public void ejecutar(int numClientes, int peticionesPorCliente) throws Exception {
          PrivateKey serverPrivateKey = cipherLogic.Asimetricos.leerClavePrivada(rutaLlavePrivada);
          PublicKey serverPublicKey = cipherLogic.Asimetricos.leerClavePublica(rutaLlavePublica);
         
          // String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
          // String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
          // System.out.println("Clave Privada (Base64): " + privateKeyBase64);
          // System.out.println("Clave Pública (Base64): " + publicKeyBase64);

          Thread servidorThread = new Thread(() -> {
               try {
                    Servidor.runServer(numClientes, serverPrivateKey, serverPublicKey);
               } catch (IOException e) {
                    e.printStackTrace();
               }
          });
          servidorThread.start();

          // Esperar un momento para asegurarse de que el servidor esté listo antes de
          // iniciar los clientes
          try {
          Thread.sleep(500);
          } catch (InterruptedException e) {
          e.printStackTrace();
          }

          for (int i = 0; i < numClientes; i++) {
               int idCliente = i;
               Thread clienteThread = new Thread(() -> {
                    try {
                        Cliente.runClient(idCliente, peticionesPorCliente, serverPublicKey);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
               // catch (InterruptedException e) {
               // e.printStackTrace();
               // }
               clienteThread.start();
          }
     }

     public void generarLlaves() throws NoSuchAlgorithmException, IOException {
          cipherLogic.Asimetricos.guardarLlaves(rutaLlavePrivada, rutaLlavePublica);
     }

}

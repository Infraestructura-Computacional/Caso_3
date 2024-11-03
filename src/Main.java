import java.util.Scanner;

public class Main {
     public static void main(String[] args) {
          Scanner scanner = new Scanner(System.in);
          Main mainInstance = new Main(); // Crear una instancia de Main
          
          System.out.println("1) Generar pareja de llaves");
          System.out.println("2) Ejecutar");
          int opcion = scanner.nextInt();
          
          if (opcion == 1) {
              mainInstance.generarLlaves(); // Llamar a través de la instancia
          } else if (opcion == 2) {
              mainInstance.ejecutar(); // Llamar a través de la instancia
          } else {
              System.out.println("Opción inválida. Por favor, digite 1 o 2.");
          }
  
          scanner.close();
      }

    //TODO
    public void generarLlaves(){

    }

    //TODO
    public void ejecutar(){

    }
}

package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

public class ThreadServidor extends Thread {
    private final Socket sktCliente;  // Socket para el cliente específico
    private final int id;  // Identificador único del hilo
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public ThreadServidor(Socket pSocket, int pId, PrivateKey privateKey, PublicKey publicKey) {
        this.sktCliente = pSocket;
        this.id = pId;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public void run() {
        System.out.println("Inicio de un nuevo thread: " + id);

        // Manejo de recursos usando try-with-resources
        try (
            PrintWriter escritor = new PrintWriter(sktCliente.getOutputStream(), true);
            BufferedReader lector = new BufferedReader(new InputStreamReader(sktCliente.getInputStream()))
        ) {
            // Procesar la comunicación con el cliente usando el protocolo del servidor
            ProtocoloServidor.procesar(lector, escritor, privateKey, publicKey);

        } catch (IOException e) {
            System.err.println("Error en el thread " + id + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Asegurarse de que el socket del cliente se cierre al final
            try {
                if (sktCliente != null && !sktCliente.isClosed()) {
                    sktCliente.close();
                }
                System.out.println("Conexión cerrada para el cliente en thread: " + id);
            } catch (IOException e) {
                System.err.println("Error cerrando el socket del cliente en thread " + id + ": " + e.getMessage());
            }
        }
    }
}

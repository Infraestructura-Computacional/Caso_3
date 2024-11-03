package Llaves;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSA {
    
    public static Key[] generarLlaves() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keyPair = keyGen.generateKeyPair();

        Key privateKey = keyPair.getPrivate();
        Key publicKey = keyPair.getPublic();

        return new Key[] { privateKey, publicKey };
    }

    public static void guardarLlaves() throws NoSuchAlgorithmException, IOException {
        Key[] llaves = generarLlaves();

        String publicKeyPath = "Llaves/publica";
        String privateKeyPath = "Llaves/privada";

        Files.write(Paths.get(publicKeyPath), llaves[1].getEncoded());
        Files.write(Paths.get(privateKeyPath), llaves[0].getEncoded());

    }

}

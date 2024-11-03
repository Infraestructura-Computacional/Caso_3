package Llaves;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSA {
    
    public static Key[] generarLlaves() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keyPair = keyGen.generateKeyPair();

        Key privateKey = keyPair.getPrivate();
        Key publicKey = keyPair.getPublic();

        return new Key[] { privateKey, publicKey };
    }

    public static void guardarLlaves(String rutaPrivada, String rutaPublica) throws NoSuchAlgorithmException, IOException {
        Key[] llaves = generarLlaves();

        String publicKeyPath = rutaPublica;
        String privateKeyPath = rutaPrivada;

        Files.write(Paths.get(publicKeyPath), llaves[1].getEncoded());
        Files.write(Paths.get(privateKeyPath), llaves[0].getEncoded());

    }

    public static PrivateKey leerClavePrivada(String filePath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filePath));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    public static PublicKey leerClavePublica(String filePath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filePath));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

}

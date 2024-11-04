package Llaves;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSA {

    public static Key[] generarLlaves() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keyPair = keyGen.generateKeyPair();

        Key privateKey = keyPair.getPrivate();
        Key publicKey = keyPair.getPublic();

        return new Key[] { privateKey, publicKey };
    }

    public static void guardarLlaves(String rutaPrivada, String rutaPublica)
            throws NoSuchAlgorithmException, IOException {
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

    public static byte[] cifrarConClavePublica(byte[] datos, PublicKey clavePublica) throws Exception {
        // Crear un objeto Cipher para cifrar usando RSA
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, clavePublica);

        // Cifrar los datos (el número aleatorio en este caso)
        return cipher.doFinal(datos);
    }

    public static byte[] descifrarConClavePrivada(String datosCifradosBase64, PrivateKey clavePrivada)
            throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, clavePrivada);

        byte[] datosCifrados = Base64.getDecoder().decode(datosCifradosBase64);
        return cipher.doFinal(datosCifrados);
    }

    public static String firmarSHA1withRSA(String mensaje, PrivateKey key) throws Exception {

        // Crear la instancia de Signature con el algoritmo SHA1withRSA
        Signature firma = Signature.getInstance("SHA1withRSA");
        
        // Inicializar la firma con la clave privada del servidor
        firma.initSign(key);
        
        // Actualizar la firma con los bytes del mensaje concatenado
        firma.update(mensaje.getBytes());

        // Firmar el mensaje y codificarlo en Base64
        byte[] firmaBytes = firma.sign();
        return Base64.getEncoder().encodeToString(firmaBytes);
    }

    public static boolean verificarFirmaSHA1withRSA(String mensaje, String firmaBase64, PublicKey clavePublica) {
        try {
            // Decodificar la firma desde Base64
            byte[] firmaBytes = Base64.getDecoder().decode(firmaBase64);
            
            // Crear una instancia de Signature con el algoritmo SHA1withRSA
            Signature verificadorFirma = Signature.getInstance("SHA1withRSA");

            // Inicializar el verificador con la clave pública
            verificadorFirma.initVerify(clavePublica);

            // Actualizar el verificador con el mensaje
            verificadorFirma.update(mensaje.getBytes());

            // Verificar la firma
            return verificadorFirma.verify(firmaBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Utilidad para convertir bytes a formato hexadecimal para imprimir
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

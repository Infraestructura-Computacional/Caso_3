package cipherLogic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Simetricos {

    public BigInteger p;
    public BigInteger g;
    public BigInteger x;
    public BigInteger gx;

    public Simetricos() {
        String output = generarOutput();
        // System.out.println(output);
        String[] pg = getPG(output);
        this.p = new BigInteger(pg[0], 16);
        this.g = new BigInteger(pg[1], 16);
        // System.out.println("P: " + p);
        // System.out.println("G: " + g);
        SecureRandom random = new SecureRandom();
        this.x = new BigInteger(p.bitLength() - 1, random).add(BigInteger.ONE);
        this.gx = g.modPow(x, p);

        // System.out.println("Thread " + Thread.currentThread().getId() + " comenzando calculo.");
        // long startTime = System.nanoTime();
        // for (int i = 0; i<5; i++) {
        //     String output = generarOutput();
        //     // System.out.println(output);
        //     String[] pg = getPG(output);
        //     this.p = new BigInteger(pg[0], 16);
        //     this.g = new BigInteger(pg[1], 16);
        //     // System.out.println("P: " + p);
        //     // System.out.println("G: " + g);
        //     SecureRandom random = new SecureRandom();
        //     this.x = new BigInteger(p.bitLength() - 1, random).add(BigInteger.ONE);
        //     this.gx = g.modPow(x, p);
        // }
        // long endTime = System.nanoTime();
        // long duration = endTime - startTime;
        // System.out.println(
        // "################# El tiempo de calcular dh fue: " + (duration / 5) + " nanosegundos");
    }

    public static String generarOutput() {
        String outputString = "";
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "src/OpenSSL-1.1.1h_win32/openssl", "dhparam", "-text", "1024");
            // Iniciar el proceso
            Process process = processBuilder.start();
            // Leer la salida del commando
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            // Almacena toda la salida para procesarla después
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
            process.waitFor();
            outputString = output.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputString;
    }

    public String[] getPG(String output) {
        // Expresiones regulares para extraer los valores de prime y generator
        Pattern primePattern = Pattern.compile("prime:\\s*((?:[0-9a-f]{2}:)+[0-9a-f]{2})", Pattern.MULTILINE);
        Pattern generatorPattern = Pattern.compile("generator:\\s*(\\d+)", Pattern.MULTILINE);

        // Buscar el valor de prime
        Matcher primeMatcher = primePattern.matcher(output);
        String primeHex = "";
        if (primeMatcher.find()) {
            primeHex = primeMatcher.group(1).replace(":", ""); // Eliminar los dos puntos
        }

        // Buscar el valor de generator
        Matcher generatorMatcher = generatorPattern.matcher(output);
        String generator = "";
        if (generatorMatcher.find()) {
            generator = generatorMatcher.group(1);
        }

        // Imprimir resultados
        // System.out.println("Prime (hex): " + primeHex);
        // System.out.println("Generator: " + generator);

        // Convertir prime de hexadecimal a BigInteger
        // if (!primeHex.isEmpty()) {
        // BigInteger prime = new BigInteger(primeHex, 16);
        // System.out.println("Prime (decimal): " + prime);
        // }
        String[] pg = { primeHex, generator };
        return pg;
    }

    public static byte[] generarSHA512(BigInteger valor) throws NoSuchAlgorithmException {
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        return sha512.digest(valor.toByteArray());
    }

    public static SecretKey[] getKABs(BigInteger sharedSecret) throws NoSuchAlgorithmException {
        // Convertir el secreto compartido a un array de bytes
        byte[] sharedSecretBytes = sharedSecret.toByteArray();

        // Calcular SHA-512 del secreto compartido
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        byte[] hash = sha512.digest(sharedSecretBytes);

        // Separar el hash en dos mitades
        byte[] kAB1Bytes = new byte[32];
        byte[] kAB2Bytes = new byte[32];

        System.arraycopy(hash, 0, kAB1Bytes, 0, 32); // Segunda mitad (K_AB2)
        System.arraycopy(hash, 32, kAB2Bytes, 0, 32); // Segunda mitad (K_AB2)

        // Generar las llaves simétricas
        SecretKey kAB1 = new SecretKeySpec(kAB1Bytes, "AES");
        SecretKey kAB2 = new SecretKeySpec(kAB2Bytes, "HmacSHA384");

        // Imprimir en hexadecimal para verificar
        System.out.println("Clave K_AB1: " + Asimetricos.bytesToHex(kAB1.getEncoded()));
        System.out.println("Clave K_AB2: " + Asimetricos.bytesToHex(kAB2.getEncoded()));

        SecretKey[] keyPair = { kAB1, kAB2 };
        return keyPair;
    }

    public static IvParameterSpec generarIV() {
        byte[] iv = new byte[16]; // 16 bytes para AES (128 bits)
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String ivToBase64(IvParameterSpec ivSpec) {
        return Base64.getEncoder().encodeToString(ivSpec.getIV()); // Convertir IV a Base64
    }

    public static IvParameterSpec base64ToIv(String ivBase64) {
        byte[] ivBytes = Base64.getDecoder().decode(ivBase64);
        return new IvParameterSpec(ivBytes); // Reconstruir el IvParameterSpec a partir de los bytes
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getGx() {
        return gx;
    }

    public static String encryptAndSign(String num, SecretKey KAB1, SecretKey KAB2, IvParameterSpec iv)
            throws Exception {
        // 1. Inicializar el cifrador AES en modo CBC con PKCS5Padding
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, KAB1, iv);

        // 2. Cifrar el número num
        byte[] encryptedNum = aesCipher.doFinal(num.getBytes(StandardCharsets.UTF_8));
        String encryptedNumBase64 = Base64.getEncoder().encodeToString(encryptedNum);

        // 3. Inicializar HMACSHA384 con la llave KAB2
        Mac mac = Mac.getInstance("HmacSHA384");
        mac.init(KAB2);

        // 4. Calcular el HMACSHA384 del texto cifrado (encryptedNum)
        byte[] hmac = mac.doFinal(encryptedNum);
        String hmacBase64 = Base64.getEncoder().encodeToString(hmac);

        // 5. Concatenar ambos resultados con ":::" como separador
        return encryptedNumBase64 + ":::" + hmacBase64;
    }

    public static String decryptAES(String encryptedText, SecretKey KAB1, IvParameterSpec iv) throws Exception {
        // Convertir el texto cifrado de Base64 a bytes
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

        // Inicializar el cifrador AES en modo CBC con PKCS5Padding para descifrado
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, KAB1, iv);

        // Descifrar los bytes
        byte[] decryptedBytes = aesCipher.doFinal(encryptedBytes);

        // Convertir los bytes descifrados a String
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static boolean verifyHMAC(String encryptedText, String receivedHMAC, SecretKey KAB2) throws Exception {
        // Convertir el texto cifrado de Base64 a bytes
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

        // Inicializar HMACSHA384 con la llave KAB2
        Mac mac = Mac.getInstance("HmacSHA384");
        mac.init(KAB2);

        // Calcular el HMAC del texto cifrado
        byte[] computedHMAC = mac.doFinal(encryptedBytes);
        String computedHMACBase64 = Base64.getEncoder().encodeToString(computedHMAC);

        // Comparar el HMAC recibido con el calculado
        return computedHMACBase64.equals(receivedHMAC);
    }

}

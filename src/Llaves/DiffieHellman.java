package Llaves;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffieHellman {

    public BigInteger p;
    public BigInteger g;
    public BigInteger x;
    public BigInteger gx;

    public DiffieHellman() {
        String output = generarOutput();
        //System.out.println(output);
        String[] pg = getPG(output);
        this.p = new BigInteger(pg[0],16);
        this.g = new BigInteger(pg[1],16);
        // System.out.println("P: " + p);
        // System.out.println("G: " + g);
        SecureRandom random = new SecureRandom();
        this.x = new BigInteger(p.bitLength() - 1, random).add(BigInteger.ONE);
        this.gx = g.modPow(x, p);
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
        //     BigInteger prime = new BigInteger(primeHex, 16);
        //     System.out.println("Prime (decimal): " + prime);
        // }
        String[] pg = {primeHex,generator}; 
        return pg;
    }

    public static byte[] generarSHA512(BigInteger valor) throws NoSuchAlgorithmException {
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        return sha512.digest(valor.toByteArray());
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

    
}

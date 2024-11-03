package Llaves;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DiffieHellman {
    
    public static BigInteger p = new BigInteger("00fb124e1b9a94fc3a9b29c704e8289eed2482ad8e36921bc4cbb15de87ab41ad7e11a41c5d29f70592ae14530af44a24d60f78532ed552c72b0fcda147c37bb309141669974ffed24c390c5748a7c594c8516bd0314cc2e454f39e3b56cb974dfe836f4b12d47489ab4197f456855e4d6dcbdb5fbe367555a657b53ee54cb3760f", 16);
    public static BigInteger g = new BigInteger("2", 16);
    public static BigInteger gx;
    public static BigInteger clavePrivada;

    public static void generarGx() {
        gx = g.modPow(g, clavePrivada);
    }

    public static void generarClavePrivada() {
        clavePrivada = new BigInteger(1024, 100, new java.security.SecureRandom());
        while (clavePrivada.compareTo(p) >= 0) {
            clavePrivada = new BigInteger(1024, 100, new java.security.SecureRandom());
        }
    }

    public static byte[] generarSHA512(BigInteger valor) throws NoSuchAlgorithmException {
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        return sha512.digest(valor.toByteArray());
    }


}


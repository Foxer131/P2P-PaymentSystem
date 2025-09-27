package com.p2ppayment.network.authprocess;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class RSAUtils {
    /**
     * Gera um desafio aleatório e criptograficamente seguro.
     * Para garantir que pode ser criptografado como texto pela sua classe RSA,
     * geramos bytes aleatórios e codificamo-los em Base64, resultando numa string aleatória.
     * @return Uma string de desafio aleatória.
     */
    public static String generateChallenge() {
        SecureRandom random = new SecureRandom();
        byte[] challengeBytes = new byte[24];
        random.nextBytes(challengeBytes);
        return Base64.getEncoder().encodeToString(challengeBytes);
    }

    public static String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo MD5 não encontrado", e);
        }
    }
}

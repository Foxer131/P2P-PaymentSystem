package com.p2ppayment.network;

import com.p2ppayment.security.RSA;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Lida com o protocolo de autenticação RSA de desafio-resposta.
 * Esta versão é robusta, usando um desafio aleatório e codificação Base64 para
 * garantir a integridade dos dados durante a transmissão.
 */
public class SecureAuthenticator {

    public boolean authenticateAsClient(BufferedReader in, PrintWriter out, RSA.PrivateKey privatekey) {
        try {
            String challengeMessage = in.readLine();
            if (challengeMessage == null || !challengeMessage.startsWith("AUTH_CHALLENGE|")) {
                System.err.println("Protocolo inválido: Desafio não recebido do servidor.");
                return false;
            }
            String encryptedChallengeBase64 = challengeMessage.split("\\|")[1];

            String decryptedChallenge = RSA.decrypt(encryptedChallengeBase64, privatekey);
            
            String responseHash = md5Hash(decryptedChallenge);
            out.println("AUTH_RESPONSE|" + responseHash);
            String serverReply = in.readLine();
            return "AUTH_SUCCESS".equals(serverReply);
        } catch (Exception e) {
            System.err.println("Erro durante a autenticação como cliente: " + e.getMessage());
            return false;
        }
    }

    public boolean authenticateAsServer(BufferedReader in, PrintWriter out, RSA.PublicKey publickey) {
        try {
            String challenge = generateChallenge();
            String encryptedChallengeBase64 = RSA.encrypt(challenge, publickey);
            out.println("AUTH_CHALLENGE|" + encryptedChallengeBase64);
            String clientResponse = in.readLine();
            if (clientResponse == null || !clientResponse.startsWith("AUTH_RESPONSE|")) {
                out.println("AUTH_FAILURE");
                return false;
            }
            String clientHash = clientResponse.split("\\|")[1];
            String expectedHash = md5Hash(challenge);
            if (clientHash.equals(expectedHash)) {
                out.println("AUTH_SUCCESS");
                return true;
            }
            out.println("AUTH_FAILURE");
            return false;
        } catch (Exception e) {
            System.err.println("Erro durante a autenticação como servidor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gera um desafio aleatório e criptograficamente seguro.
     * Para garantir que pode ser criptografado como texto pela sua classe RSA,
     * geramos bytes aleatórios e codificamo-los em Base64, resultando numa string aleatória.
     * @return Uma string de desafio aleatória.
     */
    private String generateChallenge() {
        SecureRandom random = new SecureRandom();
        byte[] challengeBytes = new byte[24];
        random.nextBytes(challengeBytes);
        return Base64.getEncoder().encodeToString(challengeBytes);
    }
    
    private String md5Hash(String input) {
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


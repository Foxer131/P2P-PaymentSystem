package com.p2ppayment.network.authprocess;

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
public class SecureAuthenticator implements IauthProcess {
    private BufferedReader in;
    private PrintWriter out;
    RSA.PrivateKey privateKey;
    RSA.PublicKey publicKey;

    public SecureAuthenticator(BufferedReader in, PrintWriter out, RSA.PrivateKey privateKey, RSA.PublicKey publicKey) {
        this.in = in;
        this.out = out;
        if  (privateKey != null) {
            this.privateKey = privateKey;
        }
        if (publicKey != null) {
            this.publicKey = publicKey;
        }
    }

    @Override
    public boolean authenticateAsClient() {
        try {
            String challengeMessage = this.in.readLine();
            if (challengeMessage == null || !challengeMessage.startsWith("AUTH_CHALLENGE|")) {
                System.err.println("Protocolo inválido: Desafio não recebido do servidor.");
                return false;
            }
            String encryptedChallengeBase64 = challengeMessage.split("\\|")[1];

            String decryptedChallenge = RSA.decrypt(encryptedChallengeBase64, this.privateKey);
            
            String responseHash = md5Hash(decryptedChallenge);
            out.println("AUTH_RESPONSE|" + responseHash);
            String serverReply = in.readLine();
            return "AUTH_SUCCESS".equals(serverReply);
        } catch (Exception e) {
            System.err.println("Erro durante a autenticação como cliente: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean authenticateAsServer() {
        try {
            String challenge = generateChallenge();
            String encryptedChallengeBase64 = RSA.encrypt(challenge, publicKey);
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
    private static String generateChallenge() {
        SecureRandom random = new SecureRandom();
        byte[] challengeBytes = new byte[24];
        random.nextBytes(challengeBytes);
        return Base64.getEncoder().encodeToString(challengeBytes);
    }
    
    private static String md5Hash(String input) {
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


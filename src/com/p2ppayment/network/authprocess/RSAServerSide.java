package com.p2ppayment.network.authprocess;

import com.p2ppayment.security.RSA;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class RSAServerSide implements IauthProcess{
    private BufferedReader in;
    private PrintWriter out;
    RSA.PublicKey publicKey;

    public RSAServerSide(BufferedReader in, PrintWriter out, RSA.PublicKey publicKey) {
        this.in = in;
        this.out = out;
        this.publicKey = publicKey;
    }

    /**
     * Gera uma challenge e envia ela ao usuário que quer autenticar.
     * Recebe o AUTH_RESPONSE com um hash md5 da challenge enviada
     * @return Um booleano e envia o AUTH_SUCESS para o
     */
    @Override
    public boolean authenticate() {
        try {
            String challenge = RSAUtils.generateChallenge();
            String encryptedChallengeBase64 = RSA.encrypt(challenge, publicKey);
            out.println("AUTH_CHALLENGE|" + encryptedChallengeBase64);
            String clientResponse = in.readLine();
            if (clientResponse == null || !clientResponse.startsWith("AUTH_RESPONSE|")) {
                out.println("AUTH_FAILURE");
                return false;
            }
            String clientHash = clientResponse.split("\\|")[1];
            String expectedHash = RSAUtils.md5Hash(challenge);
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
}

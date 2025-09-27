package com.p2ppayment.network.authprocess;

import com.p2ppayment.security.RSA;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class RSAClientSide implements IauthProcess {
    private final BufferedReader in;
    private final PrintWriter out;
    RSA.PrivateKey privateKey;

    public RSAClientSide(BufferedReader in, PrintWriter out, RSA.PrivateKey privateKey) {
        this.in = in;
        this.out = out;
        this.privateKey = privateKey;
    }

    /**
    * Autentica por parte do cliente
    * Lê a entrada de challenge do servidor, decodifica com chave privada do usuário
    * Faz um hash de md5 na challenge decodificada e envia o hash ao servidor.
    * @return Um boolean caso o servidor respondeu AUTH_SUCESS
    */
    @Override
    public boolean authenticate() {
        try {
            String challengeMessage = this.in.readLine();
            if (challengeMessage == null || !challengeMessage.startsWith("AUTH_CHALLENGE|")) {
                System.err.println("Protocolo inválido: Desafio não recebido do servidor.");
                return false;
            }
            String encryptedChallengeBase64 = challengeMessage.split("\\|")[1];

            String decryptedChallenge = RSA.decrypt(encryptedChallengeBase64, this.privateKey);

            String responseHash = RSAUtils.md5Hash(decryptedChallenge);
            out.println("AUTH_RESPONSE|" + responseHash);
            String serverReply = in.readLine();
            return "AUTH_SUCCESS".equals(serverReply);
        } catch (Exception e) {
            System.err.println("Erro durante a autenticação como cliente: " + e.getMessage());
            return false;
        }
    }
}

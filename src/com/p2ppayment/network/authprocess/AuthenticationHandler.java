package com.p2ppayment.network.authprocess;

import com.p2ppayment.security.RSA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class AuthenticationHandler {
    public static boolean handleServerAuthentication(String handshake, BufferedReader in, PrintWriter out, Socket clientSocket) {
        if (handshake == null) 
                return false;

        boolean autenticado = false;
        String nomeRemetente;

        String[] handshakeParts = handshake.split("\\|");
        String authType = handshakeParts[0];

        switch (authType) {
            case "RSA_AUTH_REQUEST" -> {
                if (handshakeParts.length > 1) {
                    nomeRemetente = handshakeParts[1];
                    System.out.println("Recebido pedido de autenticação RSA de: " + nomeRemetente);
                    try {
                        RSA.PublicKey chavePublicaRemetente = RSA.loadPublicKey("keys/" + nomeRemetente);
                        RSAServerSide authenticator = new RSAServerSide(in, out, chavePublicaRemetente);
                        autenticado = authenticator.authenticate();
                    } catch (Exception e) {
                        System.err.println("Chave pública para '" + nomeRemetente + "' não encontrada. Recusando autenticação.");
                    }
                }
            }
            case "PASS_AUTH_REQUEST" -> {
                System.out.println("Recebido pedido de autenticação por senha");
                autenticado = acceptConnectionManually(clientSocket);
            }
        }

        if (!autenticado) {
            System.out.println("Conexão não autenticada/rejeitada.");
            return false;
        }
        return true;
    }

    public static boolean handleClientAuthentication(String nomeRemetente, String caminhoChavePrivada, BufferedReader in, PrintWriter out) {
        boolean autenticado = false;
        if (caminhoChavePrivada != null) {
            out.println("RSA_AUTH_REQUEST|" + nomeRemetente);
            try {
                RSA.PrivateKey minhaChavePrivada = RSA.loadPrivateKey(caminhoChavePrivada);
                IauthProcess authenticator = new RSAClientSide(in, out, minhaChavePrivada);
                autenticado = authenticator.authenticate();
            } catch (IOException e) {
                System.err.println("Erro ao carregar a chave privada: " + e.getMessage());
            }
        } else {
            out.println("PASS_AUTH_REQUEST");
            autenticado = true;
        }

        if (!autenticado) {
            System.err.println("Autenticação RSA falhou. A cancelar transação.");
            return false;
        }
        return true;
    }

    private static boolean acceptConnectionManually(Socket clientSocket) {
        System.out.print("Deseja aceitar esta conexão não segura de " + clientSocket.getInetAddress() + "? (s/n): ");
        Scanner scanner = new Scanner(System.in);
        String resposta = scanner.nextLine().trim().toLowerCase();
        return resposta.equals("s");
    }
}
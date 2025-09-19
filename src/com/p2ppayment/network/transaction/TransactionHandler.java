package com.p2ppayment.network.transaction;

import com.p2ppayment.domain.Carteira;
import com.p2ppayment.network.authprocess.AuthenticationHandler;
import com.p2ppayment.security.RSA;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class TransactionHandler implements Runnable {

    private final Socket clientSocket;
    private final Carteira carteira;
    private final Map<String, RSA.PublicKey> chavesPublicasConhecidas;

    public TransactionHandler(Socket socket, Carteira carteira, Map<String, RSA.PublicKey> chavesPublicas) {
        this.clientSocket = socket;
        this.carteira = carteira;
        this.chavesPublicasConhecidas = chavesPublicas;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String handshake = in.readLine();
            boolean autenticado = AuthenticationHandler.handleServerAuthentication(handshake, in, out, chavesPublicasConhecidas, clientSocket);
            if (!autenticado) {
                clientSocket.close();
                return;
            }

            String nomeRemetente = "desconhecido";
            
            String inputLine = in.readLine();
            if (inputLine != null && inputLine.startsWith("TRANSFER")) {
                String[] parts = inputLine.split("\\|");
                double valor = 0;
                for (String part : parts) {
                    if (part.startsWith("valor:")) {
                        valor = Double.parseDouble(part.split(":")[1]);
                    } else if (part.startsWith("remetente:")) {
                        nomeRemetente = part.split(":")[1];
                    }
                }
                if (valor > 0) {
                    carteira.depositar(valor);
                    System.out.println("Recebido " + valor + " de " + nomeRemetente + ".");
                    carteira.showCarteira();
                    out.println("SUCCESS");
                } else {
                    System.err.println("Valor inválido recebido: " + valor);
                    out.println("ERROR");
                }                
            } else {
                 out.println("ERROR");
            }

        } catch (Exception e) {
            System.err.println("Erro no TransactionHandler: " + e.getMessage());
        }
    }
}
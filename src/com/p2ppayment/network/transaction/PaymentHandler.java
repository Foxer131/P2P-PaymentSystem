package com.p2ppayment.network.transaction;

import com.p2ppayment.domain.Carteira;
import com.p2ppayment.network.BlankCardHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PaymentHandler extends BlankCardHandler implements Runnable {

    private final Socket clientSocket;
    private final Carteira carteira;

    public PaymentHandler(Socket socket, Carteira carteira) {
        this.clientSocket = socket;
        this.carteira = carteira;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            if (!performAuthentication(in, out, clientSocket)) {
                return;
            }

            String nomeRemetente = "desconhecido";
            
            String inputLine = in.readLine();
            if (inputLine != null && inputLine.startsWith("TRANSFER")) {
                String[] parts = inputLine.split("\\|");
                double valor = 0;
                for (String part : parts) {
                    String[] _p = part.split(":");
                    switch (_p[0]) {
                        case "valor" ->
                            valor = Double.parseDouble(_p[1]);
                        case "remetente" ->
                            nomeRemetente = _p[1];
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
                System.out.println("Invalid protocol.");
                 out.println("ERROR");
            }

        } catch (Exception e) {
            System.err.println("Erro no TransactionHandler: " + e.getMessage());
        }
    }
}
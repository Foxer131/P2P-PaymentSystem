package com.p2ppayment.network.transaction;

import com.p2ppayment.domain.Carteira;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PaymentListener implements Runnable {

    private final int port;
    private final Carteira carteira;

    public PaymentListener(Carteira carteira, int port) {
        this.carteira = carteira;
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Servidor de pagamentos iniciado. A ouvir na porta: " + this.port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                PaymentHandler handler = new PaymentHandler(clientSocket, this.carteira);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Erro crítico no servidor de pagamentos na porta " + this.port + ": " + e.getMessage());
        }
    }
}
package com.p2ppayment.network;

import com.p2ppayment.domain.Carteira;
import com.p2ppayment.security.RSA;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class PaymentListener implements Runnable {

    private final int port;
    private final Carteira carteira;
    private final Map<String, RSA.PublicKey> chavesPublicasConhecidas;

    public PaymentListener(Carteira carteira, int port, Map<String, RSA.PublicKey> chavesPublicas) {
        this.carteira = carteira;
        this.port = port;
        this.chavesPublicasConhecidas = chavesPublicas;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Servidor de pagamentos iniciado. A ouvir na porta: " + this.port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                TransactionHandler handler = new TransactionHandler(clientSocket, this.carteira, this.chavesPublicasConhecidas);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Erro crítico no servidor de pagamentos na porta " + this.port + ": " + e.getMessage());
        }
    }
}
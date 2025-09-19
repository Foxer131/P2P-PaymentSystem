package com.p2ppayment.network.exchange;

import com.p2ppayment.domain.Carteira;
import com.p2ppayment.security.RSA;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class ExchangeListener implements Runnable {
    private final Carteira carteira;
    private final Map<String, RSA.PublicKey> chavesPublicasConhecidas;
    private final int port = 6050;

    // A proposta do anfitrião, guardada como parâmetros individuais.
    private final String anfitriaoOfereceBem;
    private final double anfitriaoOfereceValor;
    private final String anfitriaoPedeBem;
    private final double anfitriaoPedeValor;

    public ExchangeListener(Carteira carteira, Map<String, RSA.PublicKey> chavesPublicas, 
                            String oferecerBem, double oferecerValor, String pedirBem, double pedirValor) {
        this.carteira = carteira;
        this.chavesPublicasConhecidas = chavesPublicas;
        this.anfitriaoOfereceBem = oferecerBem;
        this.anfitriaoOfereceValor = oferecerValor;
        this.anfitriaoPedeBem = pedirBem;
        this.anfitriaoPedeValor = pedirValor;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Servidor de trocas iniciado. A ouvir na porta: " + this.port);

            Socket clientSocket = serverSocket.accept();

            ExchangeHandler handler = new ExchangeHandler(
                    clientSocket, 
                    this.carteira, 
                    this.chavesPublicasConhecidas,
                    this.anfitriaoOfereceBem,
                    this.anfitriaoOfereceValor,
                    this.anfitriaoPedeBem,
                    this.anfitriaoPedeValor
            );
            handler.run();
        } catch (Exception e) {
            System.err.println("Erro crítico no servidor de trocas: " + e.getMessage());
        }
    }
}
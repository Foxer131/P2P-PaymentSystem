package com.p2ppayment.network;

import com.p2ppayment.domain.Carteira;
import com.p2ppayment.security.RSA;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;

public class ExchangeHandler implements Runnable {
    private final Socket clientSocket;
    private final Carteira carteira;
    private final Map<String, RSA.PublicKey> chavesPublicasConhecidas;
    
    private final String anfitriaoOfereceBem;
    private final double anfitriaoOfereceValor;
    private final String anfitriaoPedeBem;
    private final double anfitriaoPedeValor;

    public ExchangeHandler(Socket socket, Carteira carteira, Map<String, RSA.PublicKey> chavesPublicas,
                           String anfitriaoOfereceBem, double anfitriaoOfereceValor, String anfitriaoPedeBem, double anfitriaoPedeValor) {
        this.clientSocket = socket;
        this.carteira = carteira;
        this.chavesPublicasConhecidas = chavesPublicas;
        this.anfitriaoOfereceBem = anfitriaoOfereceBem;
        this.anfitriaoOfereceValor = anfitriaoOfereceValor;
        this.anfitriaoPedeBem = anfitriaoPedeBem;
        this.anfitriaoPedeValor = anfitriaoPedeValor;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String handshake = in.readLine();
            if (handshake == null) 
                return;
            
            String nomeRemetente = "desconhecido";
            String[] handshakeParts = handshake.split("\\|");
            if (handshakeParts[0].equals("RSA_AUTH_REQUEST") && handshakeParts.length > 1) {
                nomeRemetente = handshakeParts[1];
            }

            boolean autenticado = AuthenticationHandler.handleServerAuthentication(handshake, in, out, chavesPublicasConhecidas, clientSocket);
            if (!autenticado) {
                return; 
            }

            String inputLine = in.readLine();
            if (inputLine == null || !inputLine.startsWith("EXCHANGE")) 
                return;
            
            double clienteOfereceValor = 0;
            String clienteOfereceBem = null;
            double clientePedeValor = 0;
            String clientePedeBem = null;
            String currentMode = null;

            String[] parts = inputLine.split("\\|");
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                if (part.equals("enviar") || part.equals("esperar")) {
                    currentMode = part;
                    continue;
                }
                if (part.startsWith("valor:")) {
                    double valor = Double.parseDouble(part.substring(6));
                    if ("enviar".equals(currentMode)) 
                        clienteOfereceValor = valor;
                    else if ("esperar".equals(currentMode)) 
                        clientePedeValor = valor;
                } else if (part.startsWith("bem:")) {
                    String bem = part.substring(4);
                    if ("enviar".equals(currentMode)) 
                        clienteOfereceBem = bem;
                    else if ("esperar".equals(currentMode)) 
                        clientePedeBem = bem;
                }
            }
            
            boolean ofertaCorresponde = Objects.equals(this.anfitriaoOfereceBem, clientePedeBem) && this.anfitriaoOfereceValor == clientePedeValor;
            boolean pedidoCorresponde = Objects.equals(this.anfitriaoPedeBem, clienteOfereceBem) && this.anfitriaoPedeValor == clienteOfereceValor;

            if (!ofertaCorresponde || !pedidoCorresponde) {
                System.err.println("Recusado: A proposta de '" + nomeRemetente + "' não corresponde à do anfitrião.");
                out.println("ABORT|PROPOSTA_INCOMPATIVEL");
                return;
            }
            
            System.out.println("Propostas correspondem. A verificar recursos para a troca com " + nomeRemetente);

            boolean possoCumprirTroca = false;
            if (this.anfitriaoOfereceBem != null) {
                possoCumprirTroca = carteira.temBem(this.anfitriaoOfereceBem);
            } else if (this.anfitriaoOfereceValor > 0) {
                possoCumprirTroca = carteira.temSaldoSuficiente(this.anfitriaoOfereceValor);
            }

            if (!possoCumprirTroca) {
                System.err.println("Troca recusada: Eu não tenho os recursos necessários.");
                out.println("ABORT|RECURSOS_INSUFICIENTES_DO_ANFITRIAO");
                return;
            }

            out.println("PREPARE_COMMIT");
            String respostaCliente = in.readLine();

            if ("AGREED".equals(respostaCliente)) {
                System.out.println("Ambos os lados concordaram. A executar a troca com " + nomeRemetente);
                
                if (this.anfitriaoOfereceBem != null) {
                    carteira.removerBem(this.anfitriaoOfereceBem);
                    carteira.depositar(this.anfitriaoPedeValor);
                } else {
                    carteira.sacar(this.anfitriaoOfereceValor);
                    carteira.adicionarBem(this.anfitriaoPedeBem);
                }

                System.out.println("Troca concluída com sucesso.");
                carteira.showCarteira();
                out.println("COMMIT_SUCCESS");
            } else {
                System.out.println("Troca abortada pelo parceiro: " + nomeRemetente);
            }
        } catch (Exception e) {
            System.err.println("Erro no ExchangeHandler: " + e.getMessage());
        }
    }
}


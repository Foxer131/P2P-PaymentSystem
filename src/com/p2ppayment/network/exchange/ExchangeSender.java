package com.p2ppayment.network.exchange;

import com.p2ppayment.domain.Carteira;
import com.p2ppayment.network.authprocess.AuthenticationHandler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;

public class ExchangeSender {
    private final Carteira carteira;

    public ExchangeSender(Carteira carteira) {
        this.carteira = carteira;
    }

    public void enviar(String destinoHost, int port, String nomeRemetente, String caminhoChavePrivada,
                       String bem, double valor, String bemEsperado, double valorEsperado) {

        boolean euEnvioBem = (bem != null && valorEsperado > 0);
        boolean euEnvioValor = (valor > 0 && bemEsperado != null);

        if (!euEnvioBem && !euEnvioValor || euEnvioBem && euEnvioValor) {
            System.err.println("Falha: Proposta de troca inconsistente.");
            return;
        }

        try (Socket socket = new Socket(destinoHost, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            System.out.println("Conexão estabelecida com " + destinoHost + ":" + port);

            boolean autenticado = AuthenticationHandler.handleClientAuthentication(nomeRemetente, caminhoChavePrivada, in, out);
            if (!autenticado) {
                System.err.println("Falha na autenticação. A fechar conexão.");
                return;
            }
            System.out.println("Autenticação bem-sucedida. A enviar proposta de troca.");

            String mensagemTroca;
            if (euEnvioBem) {
                mensagemTroca = String.format(Locale.US, "EXCHANGE|enviar|bem:%s|esperar|valor:%d|remetente:%s", bem, valorEsperado, nomeRemetente);
            } else { 
                mensagemTroca = String.format(Locale.US, "EXCHANGE|enviar|valor:%d|esperar|bem:%s|remetente:%s", valor, bemEsperado, nomeRemetente);
            }
            out.println(mensagemTroca);

            String respostaServidor = in.readLine();
            if (!"PREPARE_COMMIT".equals(respostaServidor)) {
                System.err.println("Falha: O parceiro não pôde preparar a troca. Motivo: " + respostaServidor);
                return;
            }

            boolean possoCumprirTroca = euEnvioBem ? carteira.temBem(bem) : carteira.temSaldoSuficiente(valor);
            
            if (possoCumprirTroca) {
                out.println("AGREED");
            } else {
                System.err.println("Falha: Eu não tenho os recursos necessários para esta troca.");
                out.println("ABORT");
                return;
            }

            String confirmacaoFinal = in.readLine();
            if ("COMMIT_SUCCESS".equals(confirmacaoFinal)) {
                if (euEnvioBem) {
                    carteira.removerBem(bem);
                    carteira.depositar(valorEsperado);
                } else {
                    carteira.sacar(valor);
                    carteira.adicionarBem(bemEsperado);
                }
                System.out.println("Troca confirmada e concluída com sucesso!");
                carteira.showCarteira();
            } else {
                System.err.println("A troca foi abortada pelo parceiro na fase final.");
            }

        } catch (Exception e) {
            System.err.println("Erro crítico ao enviar troca: " + e.getMessage());
        }
    }
}
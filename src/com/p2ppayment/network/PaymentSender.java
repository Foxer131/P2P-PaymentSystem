package com.p2ppayment.network;

import com.p2ppayment.domain.Carteira;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;

public class PaymentSender {

    private final Carteira carteira;

    public PaymentSender(Carteira carteira) {
        this.carteira = carteira;
    }

    public void enviar(String destinoHost, int destinoPort, double valor, String nomeRemetente, String caminhoChavePrivada
    ) {
        if (carteira.getSaldo() < valor) {
            System.err.println("Falha: Saldo insuficiente. Saldo atual: " + carteira.getSaldo());
            return;
        }

        try (Socket socket = new Socket(destinoHost, destinoPort)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            System.out.println("Conexão estabelecida com " + destinoHost + ":" + destinoPort);

            boolean autenticado = AuthenticationHandler.handleClientAuthentication(nomeRemetente, caminhoChavePrivada, in, out);
            if (!autenticado) {
                System.err.println("Falha na autenticação. A fechar conexão.");
                socket.close();
                return;
            }

            System.out.println("Autenticação bem-sucedida. A enviar detalhes do pagamento.");

            String mensagemPagamento = String.format(Locale.US, "TRANSFER|valor:%.2f|remetente:%s", valor, nomeRemetente);
            out.println(mensagemPagamento);

            String resposta = in.readLine();
            if ("SUCCESS".equals(resposta)) {
                carteira.sacar(valor);
                System.out.println("Pagamento confirmado.");
                carteira.showCarteira();
            } else {
                System.err.println("O destinatário rejeitou o pagamento ou ocorreu um erro.");
            }

        } catch (Exception e) {
            System.err.println("Erro crítico ao enviar pagamento: " + e.getMessage());
        }
    }
}
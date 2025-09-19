package com.p2ppayment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.p2ppayment.cli.ArgumentParser;
import com.p2ppayment.config.UserConfig;
import com.p2ppayment.domain.Pessoa;
import com.p2ppayment.network.transaction.PaymentListener;
import com.p2ppayment.network.transaction.PaymentSender;
import com.p2ppayment.security.RSA;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import com.p2ppayment.network.exchange.ExchangeSender;
import com.p2ppayment.network.exchange.ExchangeListener;

public class Main {

    private static final Map<String, Pessoa> utilizadoresConhecidos = new HashMap<>();
    private static final Map<String, RSA.PublicKey> chavesPublicasConhecidas = new HashMap<>();

    public static void main(String[] args) {
        inicializarUtilizadores();

        try {
            ArgumentParser parser = new ArgumentParser(args);
            String nomeUtilizador = parser.getUsername();
            String comando = parser.getCommand();

            Pessoa utilizadorAtual = utilizadoresConhecidos.get(nomeUtilizador);
            if (utilizadorAtual == null) {
                System.err.println("Erro: Utilizador '" + nomeUtilizador + "' não encontrado.");
                return;
            }

            switch (comando) {
                case "enviar" -> {
                    executarEnvio(utilizadorAtual, parser);
                }
                case "receber" -> {
                    System.out.println("--- A INICIAR MODO RECETOR PARA " + utilizadorAtual.getNome().toUpperCase() + " ---");
                    PaymentListener listener = new PaymentListener(utilizadorAtual.getCarteira(), parser.getPort(), Main.chavesPublicasConhecidas);
                    new Thread(listener).start();
                }
                case "gerar-chaves" -> {
                    utilizadorAtual.gerarEsalvarChaves();
                }
                case "troca" -> {
                    if (parser.getHost() == null) {
                        System.out.println("--- A INICIAR MODO DE ANFITRIÃO DE TROCA PARA " + utilizadorAtual.getNome().toUpperCase() + " ---");
                        
                        String oferecerBem = parser.getOferecerBem();
                        double oferecerValor = parser.getOferecerValor();
                        String pedirBem = parser.getPedirBem();
                        double pedirValor = parser.getPedirValor();

                        ExchangeListener listener = new ExchangeListener(
                            utilizadorAtual.getCarteira(), 
                            chavesPublicasConhecidas,
                            oferecerBem,
                            oferecerValor,
                            pedirBem,
                            pedirValor
                        );
                        new Thread(listener).start();
                    } else {
                        // Se a flag --destino for usada, o utilizador está a juntar-se a uma troca.
                        executarTroca(utilizadorAtual, parser);
                    }
                }
                default -> {
                    System.err.println("Comando desconhecido: " + comando);
                    ArgumentParser.printHelp();
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Erro nos argumentos: " + e.getMessage());
            ArgumentParser.printHelp();
        }
    }

    private static void executarEnvio(Pessoa utilizadorAtual, ArgumentParser parser) {
        String caminhoChavePrivada = parser.getPrivateKeyPath();
        
        if (caminhoChavePrivada == null) {
            System.out.printf("Autenticação por senha para '%s': ", utilizadorAtual.getNome());
            Scanner scanner = new Scanner(System.in);
            String inputPassword = scanner.nextLine();
            if (!utilizadorAtual.autenticarComSenha(inputPassword)) {
                System.err.println("Autenticação por senha falhou. A encerrar.");
                return;
            }
            System.out.println("Autenticação bem-sucedida.");
        }

        PaymentSender sender = new PaymentSender(utilizadorAtual.getCarteira());
        sender.enviar(
            parser.getHost(),
            parser.getPort(),
            parser.getValue(),
            utilizadorAtual.getNome(),
            caminhoChavePrivada        
            );
    }

    private static void inicializarUtilizadores() {
        Gson gson = new Gson();
        String JsonFile = "users.json";

        try (Reader leitor = new FileReader(JsonFile)) {
            Type tipoLista = new TypeToken<ArrayList<UserConfig>>() {}.getType();

            ArrayList<UserConfig> configs = gson.fromJson(leitor, tipoLista);

            for (UserConfig config : configs) {
                Pessoa novoUtilizador = new Pessoa.Builder()
                        .nome(config.getNome())
                        .password(config.getPassword())
                        .saldoInicial(config.getSaldoInicial())
                        .bensIniciais(config.getBens())
                        .build();
                utilizadoresConhecidos.put(novoUtilizador.getNome(), novoUtilizador);
            }

            for (String nome : utilizadoresConhecidos.keySet()) {
                try {
                    RSA.PublicKey pubKey = RSA.loadPublicKey("keys/" + nome);
                    chavesPublicasConhecidas.put(nome, pubKey);
                } catch (Exception e) {
                    System.out.println("Aviso: Não foi possível carregar a chave pública para '" + nome + "'.");
                }
            }

        } catch (java.io.FileNotFoundException e) {
            System.err.println("ERRO CRÍTICO: O ficheiro de configuração '" +  JsonFile + "' não foi encontrado. A encerrar.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO: Ocorreu um erro ao ler ou analisar o ficheiro de configuração: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void executarTroca(Pessoa utilizadorAtual, ArgumentParser parser) {
        System.out.println("--- A INICIAR PEDIDO DE TROCA PARA " + parser.getDestinoUsername().toUpperCase() + " ---");
        ExchangeSender sender = new ExchangeSender(utilizadorAtual.getCarteira());
        
        sender.enviar(
            parser.getHost(),
            parser.getPort(),
            utilizadorAtual.getNome(),
            parser.getPrivateKeyPath(),
            parser.getOferecerBem(),
            (int) parser.getOferecerValor(),
            parser.getPedirBem(),
            (int) parser.getPedirValor()
        );
    }
}
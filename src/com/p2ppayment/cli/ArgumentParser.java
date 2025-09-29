package com.p2ppayment.cli;

public class ArgumentParser {

    private String command;
    private String username;
    private String destinoUsername;
    private String host;
    private int port = -1;
    private double value;
    private String privateKeyPath;

    // Novos campos para a troca
    private String oferecerBem;
    private double oferecerValor;
    private String pedirBem;
    private double pedirValor;

    // Novos campos para arquivos
    private String inputFilePath;

    public ArgumentParser(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Argumentos insuficientes.");
        }

        this.username = args[0];
        this.command = args[1];

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "--destino":
                    if (i + 1 < args.length) {
                        String destinoCompleto = args[++i];
                        String[] userAndHostParts = destinoCompleto.split("@");
                        if (userAndHostParts.length != 2) throw new IllegalArgumentException("Formato de destino inválido. Use utilizador@host:porta");
                        
                        this.destinoUsername = userAndHostParts[0];
                        String[] hostAndPortParts = userAndHostParts[1].split(":");
                        this.host = hostAndPortParts[0];
                        if (hostAndPortParts.length > 1) this.port = Integer.parseInt(hostAndPortParts[1]);
                    }
                    break;
                case "--valor":
                    if (i + 1 < args.length) this.value = Double.parseDouble(args[++i]);
                    break;
                case "--port":
                     if (i + 1 < args.length && this.port == -1) this.port = Integer.parseInt(args[++i]);
                    break;
                case "--chave":
                    if (i + 1 < args.length) this.privateKeyPath = args[++i];
                    break;
                case "--oferecer-bem":
                    if (i + 1 < args.length) this.oferecerBem = args[++i];
                    break;
                case "--oferecer-valor":
                    if (i + 1 < args.length) this.oferecerValor = Double.parseDouble(args[++i]);
                    break;
                case "--pedir-bem":
                    if (i + 1 < args.length) this.pedirBem = args[++i];
                    break;
                case "--pedir-valor":
                    if (i + 1 < args.length) this.pedirValor = Double.parseDouble(args[++i]);
                    break;
                case "--arquivo":
                    if (i + 1 < args.length) this.inputFilePath = args[++i];
            }
        }
    }

    public static void printHelp() {
        System.out.println("Uso: java com.p2ppayment.Main <utilizador> <comando> [opções]");
        System.out.println("\nComandos:");
        System.out.println("  enviar            Envia um pagamento para outro utilizador.");
        System.out.println("  receber           Inicia o servidor para receber pagamentos.");
        System.out.println("  troca             Inicia ou junta-se a uma troca de bens/valores.");
        System.out.println("  gerar-chaves      Gera e salva um novo par de chaves RSA para o utilizador.");
        System.out.println("  gerar-cobranca    Gera um arquivo CNAB400 a partir de um arquivo CSV");
        System.out.println("  processa-cobranca Lê um arquivo cnab400 e gera um output sobre o conteudo");
        System.out.println("\nOpções de Pagamento:");
        System.out.println("  --destino <utilizador@host:porta>  O parceiro da troca.");
        System.out.println("  --port <port>                      O numero da porta utilizada para esperar conexão");
        System.out.println("  --enviar <valor>                   O valor a ser enviado no modo enviar");
        System.out.println("  --chave <arquivo>                  O caminho para chave RSA privada do usuário");
        System.out.println("\nOpções de Troca:");
        System.out.println("  --oferecer-bem <nome_do_bem>       O bem que você oferece.");
        System.out.println("  --oferecer-valor <quantia>         O valor que você oferece.");
        System.out.println("  --pedir-bem <nome_do_bem>          O bem que você quer receber.");
        System.out.println("  --pedir-valor <quantia>            O valor que você quer receber.");
        System.out.println("\nOpções de geração de arquivos");
        System.out.println("  --arquivo <arquivo>                O caminho para o arquivo");
    }

    // --- Getters ---
    public String getCommand() { return command; }
    public String getUsername() { return username; }
    public String getDestinoUsername() { return destinoUsername; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public double getValue() { return value; }
    public String getPrivateKeyPath() { return privateKeyPath; }
    public String getOferecerBem() { return oferecerBem; }
    public double getOferecerValor() { return oferecerValor; }
    public String getPedirBem() { return pedirBem; }
    public double getPedirValor() { return pedirValor; }
    public String getInputFilePath() { return inputFilePath; }
}
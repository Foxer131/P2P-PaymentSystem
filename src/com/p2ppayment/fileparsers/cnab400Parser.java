package com.p2ppayment.fileparsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class cnab400Parser implements FileParser<TransacaoCobranca>{
    private final String cnab400FilePath;

    public cnab400Parser(String cnab400FilePath) {
        this.cnab400FilePath = cnab400FilePath;
    }

    @Override
    public List<TransacaoCobranca> parse() throws Exception {
        List<TransacaoCobranca> transacoes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(this.cnab400FilePath))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                char tipo = linha.charAt(0);

                switch (tipo) {
                    case '0' -> {
                        //Linha de Header, agora não é interessante extrair dados daqui
                    }
                    case '1' -> {
                        TransacaoCobranca t = parseDataLine(linha);
                        transacoes.add(t);
                    }
                    case '9' ->
                        // Trailer
                        System.out.println("Valor acumulado guardado no trailer: " + parseTrailerData(linha));
                    default ->
                        throw new Exception("Tipo de coluna não existe");
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + cnab400FilePath);
        }
        return transacoes;
    }

    private TransacaoCobranca parseDataLine(String linha) {
        String nossoNumero = linha.substring(62, 70).trim();
        String dataVencimento = linha.substring(120,126);
        double valor = Double.parseDouble(linha.substring(126, 139)) / 100.0;
        String nomeSacado = linha.substring(218, 248).trim();
        String cpf = linha.substring(203, 217).trim();
        /*
        Para funções futuras as faturas tem Status, para analise do que está a ocorrer com a transação
        */
        String status = "Pendente";
        return new TransacaoCobranca(nossoNumero, nomeSacado, valor, dataVencimento, cpf, status);
    }

    private double parseTrailerData(String linha) {
        return Double.parseDouble(linha.substring(26, 39)) / 100.0;
    }
}

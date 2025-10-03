package com.p2ppayment.fileparsers;

import com.p2ppayment.transactiontypes.TransacaoCobranca;
import com.p2ppayment.transactiontypes.TransacaoPagamento;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {
    public static List<TransacaoCobranca> lerCobrancasDeCsv(String filePath) throws IOException {
        List<TransacaoCobranca> cobrancas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Pula a linha do cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(",");
                // Cria um objeto TransacaoCobranca a partir dos dados do CSV.
                TransacaoCobranca t = new TransacaoCobranca(
                        dados[4], // nossoNumero
                        dados[0], // nomeSacado
                        Double.parseDouble(dados[2]), // valor
                        dados[3], // dataVencimento
                        dados[1], // CPF
                        null    // status (não aplicável para remessa)
                );
                cobrancas.add(t);
            }
        }
        return cobrancas;
    }

    public static List<TransacaoPagamento> lerPagamentoDeCsv(String filePath) throws IOException {
        List<TransacaoPagamento> pagamentos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Pula a linha do cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(",");
                // Usa o padrão Builder para criar o objeto TransacaoPagamento
                TransacaoPagamento p = new TransacaoPagamento.Builder()
                        .nomeFavorecido(dados[0])
                        .bancoFavorecido(dados[1])
                        .agenciaFavorecida(dados[2])
                        .contaFavorecida(dados[3])
                        .cpfFavorecido(dados[4])
                        .valor(Double.parseDouble(dados[5]))
                        .dataPagamento(dados[6])
                        .seuNumero(dados[7])
                        .build();
                pagamentos.add(p);
            }
        }
        return pagamentos;
    }
}

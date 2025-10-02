package com.p2ppayment.fileparsers;

import com.p2ppayment.transactiontypes.TransacaoCobranca;
import com.p2ppayment.transactiontypes.TransacaoPagamento;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class cnab240Parser implements FileParser<TransacaoPagamento> {
    private final String cnab240FilePath;

    public cnab240Parser(String filePath) { this.cnab240FilePath = filePath; }

    public List<TransacaoPagamento> parse() throws IOException {
        List<TransacaoPagamento> pagamentos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(this.cnab240FilePath))) {
            String linha;
            TransacaoPagamento.Builder pagamentoBuilder = null;

            while ((linha = br.readLine()) != null) {
                if (linha.length() < 240) continue;

                char tipoRegistro = linha.charAt(7);

                if (tipoRegistro == '3') { // É um registo de detalhe
                    char segmento = linha.charAt(13);

                    if (segmento == 'A') {
                        pagamentoBuilder = new TransacaoPagamento.Builder();
                        parseSegmentoA(linha, pagamentoBuilder);
                    } else if (segmento == 'B' && pagamentoBuilder != null) {
                        parseSegmentoB(linha, pagamentoBuilder);
                        pagamentos.add(pagamentoBuilder.build());
                        pagamentoBuilder = null;
                    }
                }
            }
        }
        return pagamentos;
    }

    /**
     * Extrai os dados de um Segmento A e preenche o Builder.
     */
    private void parseSegmentoA(String linha, TransacaoPagamento.Builder builder) {
        String nomeFavorecido = linha.substring(43, 73).trim();
        String dataPagamento = linha.substring(93, 101); // Formato DDMMAAAA
        String valorStr = linha.substring(119, 134);
        double valor = Double.parseDouble(valorStr) / 100.0;
        String seuNumero = linha.substring(73, 93).trim();
        String banco = linha.substring(20, 23);
        String agencia = linha.substring(23, 28);
        String conta = linha.substring(29, 41) + "-" + linha.charAt(41);

        builder.nomeFavorecido(nomeFavorecido)
                .dataPagamento(dataPagamento)
                .valor(valor)
                .seuNumero(seuNumero)
                .bancoFavorecido(banco)
                .agenciaFavorecida(agencia)
                .contaFavorecida(conta);
    }

    /**
     * Extrai os dados de um Segmento B e preenche o Builder.
     */
    private void parseSegmentoB(String linha, TransacaoPagamento.Builder builder) {
        // Extrai o CPF/CNPJ do favorecido do Segmento B.
        String cpfCnpj = linha.substring(18, 32).trim();
        builder.cpfFavorecido(cpfCnpj);
    }
}

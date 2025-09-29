package com.p2ppayment.filegenerators;

import com.p2ppayment.transactiontypes.TransacaoCobranca;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class cnab400Generator implements FileGenerator<TransacaoCobranca> {

    public cnab400Generator() {}

    @Override
    public void generate(List<TransacaoCobranca> transacoes, String ficheiroSaida) throws IOException {
        List<String> linhasCnab = new ArrayList<>();
        double somaValores = 0.0;
        int sequencial = 1;

        linhasCnab.add(construirHeader(sequencial++));

        for (TransacaoCobranca t : transacoes) {
            linhasCnab.add(construirLinhaDetalhe(t, sequencial++));
            somaValores += t.valor();
        }

        linhasCnab.add(construirTrailer(sequencial, somaValores));

        Files.write(Path.of(ficheiroSaida), linhasCnab);
        if (!Files.exists(Path.of(ficheiroSaida))) {
            throw new IOException("Não foi possivel gerar o arquivo.");
        }
        System.out.println("Ficheiro CNAB 400 gerado com sucesso em: " + ficheiroSaida);
    }

    /**
     * Constrói a primeira linha do ficheiro (Header).
     * @param sequencial O número sequencial desta linha (sempre 1).
     * @return Uma String formatada de 400 caracteres.
     */
    private String construirHeader(int sequencial) {
        StringBuilder header = new StringBuilder(" ".repeat(400));
        DateTimeFormatter ddmmaa = DateTimeFormatter.ofPattern("ddMMyy");

        header.replace(0, 1, "0");                                     // Posição 1
        header.replace(1, 2, "1");                                     // Posição 2
        header.replace(2, 9, padRight("REMESSA", 7));                  // Posições 3-9
        header.replace(9, 11, "01");                                    // Posições 10-11
        header.replace(11, 26, padRight("COBRANCA", 15));               // Posições 12-26
        header.replace(26, 46, padRight("01230012345-6", 20));       // Posições 27-46
        header.replace(46, 76, padRight("LOJA GENESIS LTDA", 30));      // Posições 47-76
        header.replace(76, 94, padRight("033BANCO EXEMPLO S A", 18));   // Posições 77-94
        header.replace(94, 100, LocalDate.now().format(ddmmaa));        // Posições 95-100
        header.replace(394, 400, padLeft(String.valueOf(sequencial), 6)); // Posições 395-400

        return header.toString();
    }

    /**
     * Constrói uma única linha de detalhe (400 caracteres).
     * @param t Um array de TransaçãoCobranca vindo de uma linha do CSV.
     * @param sequencial O número sequencial desta linha.
     * @return Uma String formatada de 400 caracteres.
     */
    private String construirLinhaDetalhe(TransacaoCobranca t, int sequencial) {
        StringBuilder linha = new StringBuilder(" ".repeat(400));

        String nomeSacado = t.nomeSacado();
        String cpf = t.cpf();
        double valor = t.valor();
        String vencimento = t.dataVencimento();
        String nossoNumero = t.nossoNumero();

        String tipoInscricao = cpf.length() == 11 ? "01" : "02";
        DateTimeFormatter ddmmaaaa = DateTimeFormatter.ofPattern("ddMMyyyy");
        DateTimeFormatter ddmmaa = DateTimeFormatter.ofPattern("ddMMyy");
        String vencimentoFormatado = LocalDate.parse(vencimento, ddmmaaaa).format(ddmmaa);

        long valorEmCentavos = (long) (valor * 100);

        linha.replace(0, 1, "1");
        linha.replace(37, 62, padRight("NF-" + nossoNumero, 25)); // Uso da Empresa
        linha.replace(62, 70, padLeft(nossoNumero, 8));
        linha.replace(120, 126, vencimentoFormatado);
        linha.replace(126, 139, padLeft(String.valueOf(valorEmCentavos), 13));
        linha.replace(201, 203, tipoInscricao);
        linha.replace(203, 217, padLeft(cpf, 14));
        linha.replace(218, 248, padRight(nomeSacado, 30));
        linha.replace(394, 400, padLeft(String.valueOf(sequencial), 6));

        return linha.toString();
    }

    /**
     * Constrói a última linha do ficheiro (Trailer).
     * @param sequencial O número sequencial desta linha (o último).
     * @param somaValores A soma total dos valores dos  boletos.
     * @return Uma String formatada de 400 caracteres.
     */
    private String construirTrailer(int sequencial, double somaValores) {
        StringBuilder trailer = new StringBuilder(" ".repeat(400));
        long somaEmCentavos = (long) (somaValores * 100);

        trailer.replace(0, 1, "9");
        trailer.replace(26, 39, padLeft(String.valueOf(somaEmCentavos), 13));
        trailer.replace(394, 400, padLeft(String.valueOf(sequencial), 6));

        return trailer.toString();
    }

    private String padRight(String input, int length) {
        if (input.length() > length) {
            return input.substring(0, length);
        }
        return String.format("%-" + length + "s", input);
    }

    private String padLeft(String input, int length) {
        if (input.length() > length) {
            return input.substring(0, length);
        }
        return String.valueOf('0').repeat(length - input.length()) + input;
    }
}


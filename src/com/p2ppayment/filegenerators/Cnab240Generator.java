package com.p2ppayment.filegenerators;

import com.p2ppayment.transactiontypes.TransacaoPagamento;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Cnab240Generator extends cnabBlank implements FileGenerator<TransacaoPagamento> {

    @Override
    public void generate(List<TransacaoPagamento> pagamentos, String ficheiroSaida) throws IOException {
        List<String> linhasCnab = new ArrayList<>();
        double somaValoresLote = 0.0;

        linhasCnab.add(construirHeaderArquivo());
        linhasCnab.add(construirHeaderLote());

        int sequencialNoLote = 1;
        for (TransacaoPagamento pagamento : pagamentos) {
            linhasCnab.add(construirSegmentoA(pagamento, sequencialNoLote++));
            linhasCnab.add(construirSegmentoB(pagamento, sequencialNoLote++));
            somaValoresLote += pagamento.valor();
        }

        int totalRegistosLote = pagamentos.size() * 2 + 2;
        linhasCnab.add(construirTrailerLote(totalRegistosLote, somaValoresLote));

        int totalLotes = 1;
        int totalRegistosArquivo = linhasCnab.size() + 1;
        linhasCnab.add(construirTrailerArquivo(totalLotes, totalRegistosArquivo));

        Files.write(Path.of(ficheiroSaida), linhasCnab);
        System.out.println("Ficheiro CNAB 240 de pagamentos gerado com sucesso em: " + ficheiroSaida);
    }

    private String construirHeaderArquivo() {
        StringBuilder sb = new StringBuilder(" ".repeat(240));
        DateTimeFormatter ddmmaaaa = DateTimeFormatter.ofPattern("ddMMyyyy");
        DateTimeFormatter hhmmss = DateTimeFormatter.ofPattern("HHmmss");

        sb.replace(0, 3, "033");                                           // Posição 1-3
        sb.replace(17, 18, "2");                                          // Posição 18
        sb.replace(18, 32, padLeft("12345678000199", 14));            // Posição 19-32
        sb.replace(52, 72, padRight("012300012345-6", 20));                // Posição 53-72
        sb.replace(72, 102, padRight("LOJA GENESIS LTDA", 30));            // Posição 73-102
        sb.replace(102, 132, padRight("BANCO EXEMPLO S A", 30));           // Posição 103-132
        sb.replace(143, 151, LocalDate.now().format(ddmmaaaa));           // Posição 144-151
        sb.replace(151, 157, LocalTime.now().format(hhmmss));             // Posição 152-157
        sb.replace(157, 163, padLeft("1", 6));                        // Posição 158-163

        return sb.toString();
    }

    private String construirHeaderLote() {
        StringBuilder sb = new StringBuilder(" ".repeat(240));

        sb.replace(0, 3, "033");                                           // Posição 1-3
        sb.replace(3, 7, "0001");                                         // Posição 4-7
        sb.replace(7, 8, "1");                                            // Posição 8
        sb.replace(8, 9, "C");                                            // Posição 9
        sb.replace(9, 11, "20");                                          // Posição 10-11
        sb.replace(17, 18, "2");                                          // Posição 18
        sb.replace(18, 32, padLeft("12345678000199", 14));            // Posição 19-32
        sb.replace(52, 72, padRight("012300012345-6", 20));                // Posição 53-72
        sb.replace(72, 102, padRight("LOJA GENESIS LTDA", 30));            // Posição 73-102

        return sb.toString();
    }

    private String construirSegmentoA(TransacaoPagamento p, int sequencialNoLote) {
        StringBuilder sb = new StringBuilder(" ".repeat(240));
        DateTimeFormatter ddmmaaaa = DateTimeFormatter.ofPattern("ddMMyyyy");
        long valorEmCentavos = (long) (p.valor() * 100);

        sb.replace(0, 3, "033");
        sb.replace(3, 7, "0001");
        sb.replace(7, 8, "3");
        sb.replace(8, 13, padLeft(String.valueOf(sequencialNoLote), 5));
        sb.replace(13, 14, "A");
        sb.replace(20, 23, p.bancoFavorecido());
        sb.replace(23, 28, padLeft(p.agenciaFavorecida(), 5));
        sb.replace(28, 29, " "); // Dígito da Agência
        sb.replace(29, 41, padLeft(p.contaFavorecida().split("-")[0], 12));
        sb.replace(41, 42, p.contaFavorecida().split("-")[1]);
        sb.replace(42, 43, " "); // Dígito Verificador Ag/Conta
        sb.replace(43, 73, padRight(p.nomeFavorecido(), 30));
        sb.replace(73, 93, padRight(p.seuNumero(), 20));
        sb.replace(93, 101, LocalDate.parse(p.dataPagamento(), ddmmaaaa).format(ddmmaaaa));
        sb.replace(119, 134, padLeft(String.valueOf(valorEmCentavos), 15));

        return sb.toString();
    }

    private String construirSegmentoB(TransacaoPagamento p, int sequencialNoLote) {
        StringBuilder sb = new StringBuilder(" ".repeat(240));

        sb.replace(0, 3, "033");
        sb.replace(3, 7, "0001");
        sb.replace(7, 8, "3");
        sb.replace(8, 13, padLeft(String.valueOf(sequencialNoLote), 5));
        sb.replace(13, 14, "B");
        sb.replace(17, 18, (p.cpfFavorecido() != null && p.cpfFavorecido().length() == 11) ? "1" : "2");
        sb.replace(18, 32, padLeft(p.cpfFavorecido(), 14));

        return sb.toString();
    }

    private String construirTrailerLote(int totalRegistosLote, double somaValores) {
        StringBuilder sb = new StringBuilder(" ".repeat(240));
        long somaEmCentavos = (long) (somaValores * 100);

        sb.replace(0, 3, "033");
        sb.replace(3, 7, "0001");
        sb.replace(7, 8, "5");
        sb.replace(17, 23, padLeft(String.valueOf(totalRegistosLote), 6));
        sb.replace(23, 41, padLeft(String.valueOf(somaEmCentavos), 18));

        return sb.toString();
    }

    private String construirTrailerArquivo(int totalLotes, int totalRegistos) {
        StringBuilder sb = new StringBuilder(" ".repeat(240));

        sb.replace(0, 3, "033");
        sb.replace(3, 7, "9999");
        sb.replace(7, 8, "9");
        sb.replace(17, 23, padLeft(String.valueOf(totalLotes), 6));
        sb.replace(23, 29, padLeft(String.valueOf(totalRegistos), 6));

        return sb.toString();
    }

    // --- FUNÇÕES DE AJUDA PARA PADDING ---
}
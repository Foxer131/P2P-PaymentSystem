package com.p2ppayment.transactiontypes;

public record TransacaoPagamento(
        String nomeFavorecido,
        String bancoFavorecido,
        String agenciaFavorecida,
        String contaFavorecida,
        String cpfFavorecido,
        double valor,
        String dataPagamento,
        String seuNumero
) {
}

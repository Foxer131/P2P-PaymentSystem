package com.p2ppayment.fileparsers;

public record TransacaoCobranca(
        String nossoNumero,
        String nomeSacado,
        double valor,
        String dataVencimento,
        String cpf,
        String status
) {}

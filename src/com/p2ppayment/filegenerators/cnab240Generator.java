package com.p2ppayment.filegenerators;

import  com.p2ppayment.transactiontypes.TransacaoPagamento;

import java.util.List;

public class cnab240Generator implements FileGenerator<TransacaoPagamento> {

    @Override
    public void generate(List<TransacaoPagamento> transacaoPagamento, String ficheiroSaida) {

    }

    private String construirHeaderArquivo() {
        System.out.println("Construir Header");
        return "";
    }

    private String construirHeaderLote() {
        System.out.println("Construir Header");
        return "";
    }

    private String construirSegmentoA() {
        System.out.println("Construir SegmentoA");
        return "";
    }

    private String construirSegmentoB() {
        System.out.println("Construir SegmentoB");
        return "";
    }

    private String construirTrailerLote() {
        return "";
    }

    private String construirTrailerArquivo() {
        return "";
    }
}

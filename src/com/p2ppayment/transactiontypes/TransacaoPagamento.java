package com.p2ppayment.transactiontypes;

import java.util.Objects;

public class TransacaoPagamento{
    String nomeFavorecido;
    String bancoFavorecido;
    String agenciaFavorecida;
    String contaFavorecida;
    String cpfFavorecido;
    double valor;
    String dataPagamento;
    String seuNumero;

    private TransacaoPagamento(Builder builder) {
        this.nomeFavorecido = builder.nomeFavorecido;
        this.bancoFavorecido = builder.bancoFavorecido;
        this.agenciaFavorecida = builder.agenciaFavorecida;
        this.contaFavorecida = builder.contaFavorecida;
        this.cpfFavorecido = builder.cpfFavorecido;
        this.valor = builder.valor;
        this.dataPagamento = builder.dataPagamento;
        this.seuNumero = builder.seuNumero;
    }

    public String nomeFavorecido() { return nomeFavorecido; }
    public String bancoFavorecido() { return bancoFavorecido; }
    public String agenciaFavorecida() { return agenciaFavorecida; }
    public String contaFavorecida() { return contaFavorecida; }
    public String cpfFavorecido() { return cpfFavorecido; }
    public double valor() { return valor; }
    public String dataPagamento() { return dataPagamento; }
    public String seuNumero() { return seuNumero; }

    public static class Builder {
        private String nomeFavorecido;
        private String bancoFavorecido;
        private String agenciaFavorecida;
        private String contaFavorecida;
        private String cpfFavorecido;
        private double valor;
        private String dataPagamento;
        private String seuNumero;

        public Builder nomeFavorecido(String val) { this.nomeFavorecido = val; return this; }
        public Builder bancoFavorecido(String val) { this.bancoFavorecido = val; return this; }
        public Builder agenciaFavorecida(String val) { this.agenciaFavorecida = val; return this; }
        public Builder contaFavorecida(String val) { this.contaFavorecida = val; return this; }
        public Builder cpfFavorecido(String val) { this.cpfFavorecido = val; return this; }
        public Builder valor(double val) { this.valor = val; return this; }
        public Builder dataPagamento(String val) { this.dataPagamento = val; return this; }
        public Builder seuNumero(String val) { this.seuNumero = val; return this; }

        public TransacaoPagamento build() {
            Objects.requireNonNull(nomeFavorecido, "Nome do favorecido não pode ser nulo.");
            return new TransacaoPagamento(this);
        }
    }

}
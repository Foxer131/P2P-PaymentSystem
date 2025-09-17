package com.p2ppayment.domain;

import com.p2ppayment.security.RSA;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pessoa {

    private final String nome;
    private final String password;
    private final Carteira carteira;

    private Pessoa(String nome, String password, Carteira carteira) {
        this.nome = nome;
        this.password = password;
        this.carteira = carteira;
    }

    public boolean autenticarComSenha(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public void gerarEsalvarChaves() {
        try {
            RSA.KeyPair novoParDeChaves = RSA.generateKeyPair();
            RSA.saveKeyPair(novoParDeChaves, "keys/" + this.nome);
            System.out.println("Novas chaves RSA salvas para: " + this.nome);
        } catch (Exception e) {
            System.err.println("Erro ao gerar as chaves RSA: " + e.getMessage());
        }
    }

    public String getNome() {
        return this.nome;
    }

    public Carteira getCarteira() {
        return this.carteira;
    }

    public static class Builder {
        private String nome;
        private String password;
        private double saldoInicial = 0;
        private List<String> bensIniciais = new ArrayList<>();

        public Builder nome(String nome) { this.nome = nome; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder saldoInicial(double saldo) { this.saldoInicial = saldo; return this; }

        public Builder bensIniciais(List<String> bens) {
            if (bens != null) {
                this.bensIniciais = bens;
            }
            return this;
        }

        public Pessoa build() {
            Objects.requireNonNull(nome, "O nome não pode ser nulo.");
            Objects.requireNonNull(password, "A password não pode ser nula.");
            
            // A Carteira é agora criada com o saldo E os bens.
            Carteira carteira = new Carteira(this.saldoInicial, this.bensIniciais);
            return new Pessoa(nome, password, carteira);
        }
    }
}
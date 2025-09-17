package com.p2ppayment.config;

import java.util.List;

public class UserConfig {
    private String nome;
    private String password;
    private double saldoInicial;
    private List<String> bens;

    public UserConfig() {

    }

    public String getNome() {
        return this.nome;
    }

    public String getPassword() {
        return this.password;
    }

    public double getSaldoInicial() {
        return this.saldoInicial;
    }

    public List<String> getBens() {
        return this.bens;
    }
}
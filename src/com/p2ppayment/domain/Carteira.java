package com.p2ppayment.domain;

import java.util.ArrayList;
import java.util.List;

public class Carteira {
    private double saldo;
    private ArrayList<String> bens;

    public Carteira(double valor, List<String> bensIniciais) {
        this.saldo = valor;
        if (bensIniciais != null) {
            this.bens = new ArrayList<>(bensIniciais);
        } else {
            this.bens = new ArrayList<>();
        }
    }

    public double getSaldo() {
        return this.saldo;
    }

    public synchronized boolean sacar(double valor) {
        if (valor > 0 && this.saldo >= valor) {
            this.saldo -= valor;
            return true;
        }
        return false;
    }

    public synchronized void depositar(double valor) {
        if (valor > 0) {
            this.saldo += valor;
        }
    }

    public synchronized void adicionarBem(String bem) {
        this.bens.add(bem);
    }

    public synchronized boolean removerBem(String bem) { return this.bens.remove(bem); }

    // Método de verificação útil para a lógica de troca
    public synchronized boolean temBem(String bem) {
        return this.bens.contains(bem);
    }

    public synchronized boolean temSaldoSuficiente(double valor) {
        return this.saldo > valor;
    }

    public synchronized void showCarteira() {
        System.out.println("Mostrando carteira");
        System.out.println("Saldo atual: " + this.saldo);
        System.out.println("Bens: ");
        for (String bem : bens) {
            System.out.println(bem);
        }
    }
}
package com.p2ppayment.filegenerators;

import java.io.IOException;
import java.util.List;

public interface FileGenerator<T> {
    /**
     * Gera um ficheiro a partir de uma lista de transações.
     * @param transacoes A lista de objetos de dados.
     * @param ficheiroSaida O caminho onde o ficheiro será salvo.
     * @throws IOException Se ocorrer um erro durante a geração.
     */
    void generate(List<T> transacoes, String ficheiroSaida) throws IOException;
}

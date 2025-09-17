# Sistema de Pagamento e Troca P2P em Java

Este é um sistema P2P (Peer-to-Peer) completo, construído em Java, que permite aos utilizadores realizar transações financeiras e trocas atómicas de bens e valores de forma segura. O projeto foi desenvolvido a partir do zero, focando numa arquitetura robusta, separação de responsabilidades e protocolos de rede seguros.

\[Imagem de um diagrama de rede P2P]

## ✨ Funcionalidades Principais

  * **Múltiplos Utilizadores:** A configuração dos utilizadores é carregada dinamicamente a partir de um ficheiro `users.json`, tornando o sistema flexível e fácil de gerir.
  * **Dois Modos de Transação:**
    1.  **Pagamento Simples (`enviar`):** Transferência monetária direta entre utilizadores.
    2.  **Troca Atómica (`troca`):** Uma funcionalidade avançada que permite a troca de bens por valores (ex: "Carro" por 1000) de forma segura, usando um protocolo de **Confirmação de Duas Fases (Two-Phase Commit)** para garantir que a transação ou é concluída por ambos os lados, ou é abortada sem perdas.
  * **Sistema de Autenticação Duplo:**
      * **Autenticação por Chave RSA:** O método preferencial e mais seguro. Usa um protocolo de "Desafio-Resposta" para verificar a identidade sem nunca transmitir segredos pela rede.
      * **Autenticação por Senha:** Um método de fallback para transações onde a segurança RSA não está disponível, com um sistema de aceitação manual pelo recetor.
  * **Geração de Chaves RSA:** Os utilizadores podem gerar os seus próprios pares de chaves pública/privada diretamente através da aplicação.

## 🛠️ Arquitetura e Tecnologias

O projeto está estruturado numa arquitetura de 3 camadas para garantir a separação de responsabilidades:

1.  **Camada de Apresentação/Entrada (`cli`):** `ArgumentParser` para uma análise robusta dos comandos da linha de comando.
2.  **Camada de Domínio (`domain`):** As classes `Pessoa` e `Carteira` contêm a lógica de negócio pura.
3.  **Camada de Rede (`network`):** Inclui `PaymentSender`, `TransactionHandler`, `ExchangeListener`, etc., que gerem toda a comunicação de baixo nível com Sockets Java, threads e protocolos de comunicação personalizados.
4.  **Camada de Segurança (`security`):** Contém a implementação do algoritmo RSA e do protocolo de autenticação segura.

<!-- end list -->

  * **Linguagem:** Java
  * **Comunicação de Rede:** Sockets Java (`java.net.Socket`, `java.net.ServerSocket`)
  * **Concorrência:** Java Threads para lidar com múltiplos clientes simultaneamente.
  * **Configuração:** Leitura de dados de utilizadores a partir de um ficheiro `users.json` com a biblioteca **Gson** da Google.
  * **Criptografia:** Implementação do RSA com `BigInteger` e um protocolo de Desafio-Resposta com hash MD5.

## 🚀 Como Executar

### Pré-requisitos

  * JDK (Java Development Kit) 11 ou superior.
  * Git.

### Compilação

1.  Clone o repositório: `git clone https://github.com/Foxer131/P2P-PaymentSystem.git`
2.  Navegue para a pasta do projeto.
3.  Descarregue o ficheiro `gson.jar` e coloque-o numa pasta `lib/`.
4.  Compile o projeto (a partir da raiz do projeto):
    ```
    javac -cp "bin;lib/*" -d bin src/com/p2ppayment/**/*.java
    ```

### Execução

Todos os comandos são executados a partir da pasta raiz do projeto.

**1. Gerar Chaves (Primeira vez):**

```
java -cp "bin;lib/*" com.p2ppayment.Main <nome_utilizador> gerar-chaves
```

**2. Iniciar um Recetor de Pagamentos:**

```
java -cp "bin;lib/*" com.p2ppayment.Main <nome_utilizador> receber --port 9090
```

**3. Iniciar um Anfitrião de Troca:**

```
java -cp "bin;lib/*" com.p2ppayment.Main <nome_utilizador> troca --oferecer-bem "Item" --pedir-valor 100
```

**4. Enviar um Pagamento (com chave RSA):**

```
java -cp "bin;lib/*" com.p2ppayment.Main <seu_utilizador> enviar --destino <outro_utilizador>@localhost:9090 --valor 50 --chave <seu_utilizador>.key
```

**5. Juntar-se a uma Troca (com chave RSA):**

```
java -cp "bin;lib/*" com.p2ppayment.Main <seu_utilizador> troca --destino <outro_utilizador>@localhost:6050 --oferecer-valor 100 --pedir-bem "Item" --chave <seu_utilizador>.key
```
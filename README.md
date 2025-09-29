# Fintech Toolkit: Sistema P2P e Automação Financeira em Java

Este projeto é uma robusta caixa de ferramentas de automação financeira, desenvolvida em Java a partir do zero. O que começou como um sistema de pagamento P2P evoluiu para incluir funcionalidades de nível empresarial, como trocas atómicas seguras e a geração/análise de ficheiros no padrão bancário brasileiro CNAB 400.

O foco principal do projeto é a implementação de uma arquitetura limpa, com forte separação de responsabilidades, protocolos de rede seguros e um design modular que permite a fácil expansão para novos formatos e funcionalidades.

## ✨ Funcionalidades

O sistema é operado via linha de comando e suporta um vasto leque de operações:

* **Gestão de Utilizadores Dinâmica:** Carrega os perfis dos utilizadores (saldo, bens, senhas) a partir de um ficheiro de configuração `users.json` no arranque, usando a biblioteca Gson.

* **Transações P2P:**
    * **Pagamento Simples (`enviar`):** Transferência monetária direta entre dois peers.
    * **Troca Atómica (`troca`):** Uma funcionalidade avançada para a troca segura de bens por valores (ex: "Carro" por 1000). Utiliza um protocolo de **Confirmação de Duas Fases (Two-Phase Commit)** para garantir que a transação ou é concluída por ambos os lados, ou é abortada sem perdas para nenhuma das partes.

* **Automação de Cobrança (Padrão CNAB 400):**
    * **Geração de Remessa (`gerar-cobranca`):** Lê um ficheiro `.csv` com os detalhes das faturas e gera um ficheiro `remessa_cnab400.txt` no formato de largura fixa, pronto para ser enviado a um banco para o registo de boletos.
    * **Análise de Retorno (`processar-cobranca`):** Lê um ficheiro de retorno CNAB 400 do banco e gera um relatório simples sobre o estado de cada boleto (liquidado, pendente, rejeitado).

* **Sistema de Segurança e Autenticação:**
    * **Geração de Chaves (`gerar-chaves`):** Permite que os utilizadores criem os seus próprios pares de chaves RSA de 2048 bits.
    * **Autenticação Dupla:**
        * **Chave RSA (Preferencial):** Utiliza um protocolo de "Desafio-Resposta" para uma autenticação criptograficamente segura.
        * **Senha (Fallback):** Para conexões não seguras, o sistema recorre a uma autenticação por senha para o remetente e a uma confirmação manual por parte do recetor.

## 📡 Protocolos de Comunicação

O sistema utiliza um conjunto de protocolos de comunicação baseados em texto simples (delimitados por `|`) para coordenar as ações entre os peers.

### 1. Protocolo de Handshake de Autenticação

Toda a comunicação P2P (pagamento ou troca) começa com um handshake para estabelecer o método de autenticação. O cliente envia uma das seguintes mensagens como a primeira linha da comunicação:

* `RSA_AUTH_REQUEST|<nome_remetente>`:
    * Enviada por um cliente que deseja autenticar-se usando a sua chave privada RSA.
    * Inicia o fluxo de "Desafio-Resposta", onde o servidor responde com um desafio criptografado.

* `PASS_AUTH_REQUEST`:
    * Enviada por um cliente que não está a usar uma chave RSA.
    * Informa o servidor que a autenticação será feita através da aceitação manual da conexão pelo utilizador do servidor.

### 2. Protocolo de Pagamento Simples

Utilizado pelo comando `enviar` após uma autenticação bem-sucedida.

1.  **Cliente -> Servidor:** `TRANSFER|valor:XX.XX|remetente:<nome_remetente>`
2.  **Servidor -> Cliente:**
    * `SUCCESS`: Se o pagamento foi processado com sucesso.
    * `ERROR`: Se ocorreu um problema.

### 3. Protocolo de Troca Atómica (Two-Phase Commit Simplificado)

Utilizado pelo comando `troca` para garantir que a transação é justa e segura.

* **Fase 1: Proposta e Acordo**
    1.  **Cliente -> Servidor:** Envia a sua proposta de troca completa.
        `EXCHANGE|enviar|bem:ItemA|esperar|valor:100.0|remetente:cliente`
    2.  **Servidor:** Após receber a proposta, o servidor verifica internamente:
        a. Se a proposta do cliente corresponde à sua própria proposta (matchmaking).
        b. Se ele tem os recursos (bem ou valor) que prometeu na sua proposta.
    3.  **Servidor -> Cliente:** Se ambas as verificações passarem, ele envia: `PREPARE_COMMIT`
    4.  **Cliente:** Ao receber `PREPARE_COMMIT`, o cliente verifica se ele próprio tem os recursos que prometeu.
    5.  **Cliente -> Servidor:** Se tiver os recursos, ele concorda com a troca: `AGREED`

* **Fase 2: Execução (Commit)**
    1.  **Servidor:** Ao receber `AGREED`, a troca é considerada final. O servidor executa a transação na sua carteira.
    2.  **Servidor -> Cliente:** Envia a confirmação final: `COMMIT_SUCCESS`
    3.  **Cliente:** Ao receber `COMMIT_SUCCESS`, o cliente executa a transação na sua carteira.

*Nota: Se ocorrer uma falha em qualquer ponto antes do `COMMIT_SUCCESS`, uma mensagem de `ABORT` é enviada e ambos os lados cancelam a transação.*

## 🛠️ Arquitetura e Design

* **Separação de Responsabilidades:** O código está organizado em pacotes distintos (`domain`, `network`, `security`, `filegenerators`, etc.).
* **Injeção de Dependência (DI):** Componentes como os geradores de ficheiros recebem os seus dados "injetados", tornando-os flexíveis.
* **Padrão Strategy:** A lógica de autenticação usa a interface `IauthProcess` e implementações concretas (`RSAClientSide`, `RSAServerSide`).
* **Linguagem:** Java (JDK 11+)
* **Comunicação:** Sockets Java e Threads.
* **Análise de JSON:** Biblioteca **Gson** da Google.
* **Criptografia:** Implementação do RSA com `BigInteger` para chaves de 2048 bits.

## 🚀 Como Executar

### Pré-requisitos
* JDK (Java Development Kit) 11 ou superior.
* Git (para clonar o repositório).

### Compilação
1.  Clone o repositório e navegue para a pasta do projeto.
2.  Crie uma pasta `lib/` e coloque o ficheiro `gson-2.13.1.jar` dentro dela.
3.  Compile o projeto a partir da pasta raiz:

    **No Windows (PowerShell):**
    ```powershell
    javac -cp "lib/*" -d bin (Get-ChildItem -Recurse -Filter "*.java" src | ForEach-Object { $_.FullName })
    ```
    **No macOS/Linux:**
    ```bash
    javac -cp "bin:lib/*" -d bin $(find src -name "*.java")
    ```

### Execução
Todos os comandos são executados a partir da pasta raiz do projeto.

**1. Gerar Chaves (Primeira vez):**
```
java -cp "bin;lib/*" com.p2ppayment.Main <nome_utilizador> gerar-chaves
```
### Transação
**1. Iniciar um Recetor de Pagamentos (requer um terminal):**
```
java -cp "bin;lib/*" com.p2ppayment.Main <nome_utilizador> receber --port 9090
```

**2. Enviar um Pagamento (requer outro terminal):**
```
java -cp "bin;lib/*" com.p2ppayment.Main <seu_utilizador> enviar --destino <outro_utilizador>@localhost:9090 --valor 50 --chave <seu_utilizador>.key
```

### Troca
**1. Iniciar um Anfitrião de Troca (requer um terminal):**
```
java -cp "bin;lib/*" com.p2ppayment.Main <nome_utilizador> troca --oferecer-bem "Quadro Raro" --pedir-valor 1000
```
**2. Juntar-se a uma Troca (requer outro terminal):**
```
java -cp "bin;lib/*" com.p2ppayment.Main <seu_utilizador> troca --destino <outro_utilizador>@localhost:6050 --oferecer-valor 1000 --pedir-bem "Quadro Raro" --chave <seu_utilizador>.key
```
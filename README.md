Resumo dos Protocolos de Comunicação P2P
Este documento descreve os três principais protocolos de comunicação utilizados no projeto P2P, cada um desenhado para uma funcionalidade específica: Autenticação, Pagamento Simples e Troca Atómica.

1. Protocolo de Handshake e Autenticação
Este é o primeiro protocolo executado em qualquer nova conexão para estabelecer uma identidade verificada ou um nível de confiança.

Objetivo: Permitir que o servidor (recetor) saiba como o cliente (remetente) deseja autenticar-se e executar o processo de verificação apropriado.

Fluxo:

Handshake Inicial (Cliente -> Servidor): O cliente envia a sua intenção como a primeira mensagem.

RSA_AUTH_REQUEST|nome_do_remetente: Indica que o cliente deseja usar a sua chave privada para uma autenticação forte.

PASS_AUTH_REQUEST: Indica que o cliente não vai usar uma chave e que o servidor deve recorrer ao método de verificação manual (pedir ao utilizador para aceitar/recusar).

Processo de Autenticação (Servidor <-> Cliente):

Se for RSA: O servidor inicia o protocolo de Desafio-Resposta gerido pelo SecureAuthenticator.

O Servidor envia um desafio aleatório, criptografado com a chave pública do cliente.

O Cliente usa a sua chave privada para descriptografar o desafio.

O Cliente calcula um hash (MD5) do desafio e envia-o de volta.

O Servidor compara o hash recebido com o esperado e envia AUTH_SUCCESS ou AUTH_FAILURE.

Se for Senha/Manual: O servidor simplesmente pergunta ao seu utilizador no terminal se deseja aceitar a conexão não segura.

2. Protocolo de Pagamento Simples
Um protocolo simples, de uma só fase, para transferências monetárias diretas.

Objetivo: Enviar um valor monetário de um utilizador para outro.

Fluxo:

Pedido de Transferência (Cliente -> Servidor): Após uma autenticação bem-sucedida, o cliente envia a sua intenção de pagamento.

Mensagem: TRANSFER|valor:[quantia]|remetente:[nome]

Exemplo: TRANSFER|valor:50.00|remetente:foxer

Confirmação (Servidor -> Cliente): O servidor processa o depósito e envia uma única mensagem de confirmação.

SUCCESS: Se o depósito foi bem-sucedido.

ERROR|MOTIVO: Se ocorreu um problema.

3. Protocolo de Troca Atómica
Este é o protocolo mais complexo, desenhado para garantir que uma troca de bens e/ou valores seja justa e "atómica" (ou acontece por completo, ou não acontece de todo), usando um modelo de Confirmação de Duas Fases (Two-Phase Commit).

Objetivo: Permitir a troca segura de um bem por valor, ou de um bem por outro bem, entre dois utilizadores, sem o risco de uma das partes perder o seu item se a conexão falhar.

Fluxo:

Fase 0: Autenticação: A conexão é primeiro estabelecida e autenticada usando o Protocolo de Handshake.

Fase 1 (Proposta e Acordo):

Proposta (Cliente -> Servidor): O cliente envia a sua proposta de troca completa.

Mensagem: EXCHANGE|enviar|bem:Carro|esperar|valor:1000|remetente:foxer

Matchmaking (Lógica do Servidor): O servidor (ExchangeHandler) compara a proposta recebida com a proposta do seu próprio utilizador (o anfitrião). Se não corresponderem, ele envia ABORT|PROPOSTA_INCOMPATIVEL.

Validação de Recursos (Lógica do Servidor): Se as propostas corresponderem, o servidor verifica se ele próprio tem os recursos que prometeu (o bem ou o valor). Se não tiver, envia ABORT|RECURSOS_INSUFICIENTES_DO_ANFITRIAO.

Sinal de Preparação (Servidor -> Cliente): Se tudo estiver correto, o servidor sinaliza que está pronto para se comprometer.

Mensagem: PREPARE_COMMIT

Acordo (Cliente -> Servidor): O cliente recebe o PREPARE_COMMIT, verifica os seus próprios recursos e, se estiver tudo em ordem, envia o seu acordo final.

Mensagem: AGREED (ou ABORT se ele próprio não tiver os recursos).

Fase 2 (Execução Irreversível):

Confirmação Final (Servidor -> Cliente): Quando o servidor recebe o AGREED, a transação é considerada bloqueada. Ele executa a troca na sua Carteira (remove o seu bem, adiciona o valor, etc.) e envia a confirmação final.

Mensagem: COMMIT_SUCCESS

Execução Final (Lógica do Cliente): Quando o cliente recebe o COMMIT_SUCCESS, ele também executa a troca na sua Carteira. Neste ponto, a transação está concluída para ambos os lados.
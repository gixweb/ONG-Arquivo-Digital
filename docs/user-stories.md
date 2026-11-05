# Histórias de Usuário (User Stories)
## Desafio 14: Arquivo Digital da ONG

Todas as histórias de usuário seguem o formato canônico:
> **Como** [ator], **quero** [ação], **para que** [valor de negócio / benefício].

Os Critérios de Aceite são descritos no padrão BDD/Gherkin (**Dado... Quando... Então...**).

---

### US01 — Cadastro de Documento com Início na Versão 1
* **ID:** US01
* **Requisito Relacionado:** RF01
* **Critério do Desafio:** CA1

**Como** membro da secretaria administrativa da ONG,  
**quero** cadastrar um novo documento informando título, tipo, data e responsável fictício,  
**para que** o arquivo fique centralizado e comece controlado formalmente desde a sua primeira versão.

#### Critérios de Aceite (BDD)
```gherkin
Cenário: Cadastro com sucesso de um documento válido
  Dado que preencho o formulário com:
    | Campo          | Valor                                   |
    | titulo         | Ata da Assembleia Geral Ordinária 2026 |
    | tipo           | ata                                     |
    | data_documento | 2026-03-15                              |
    | responsavel    | Maria Silva                             |
  Quando solicito o cadastro do documento
  Então o sistema retorna o status HTTP 201 Created
  E o documento criado possui "versao_atual" igual a 1
  E o documento criado possui "status" igual a "vigente"
  E existe um registro no histórico com a versão 1 associada

Cenário: Rejeição de tipo de documento não permitido (CA1)
  Dado que tento cadastrar um documento com o tipo "memorando"
  Quando envio a requisição de cadastro
  Então o sistema retorna o status HTTP 400 Bad Request
  E a resposta de erro detalha que o tipo "memorando" é inválido e lista os tipos permitidos (ata, contrato, relatorio, oficio)

Cenário: Rejeição por campos obrigatórios vazios
  Dado que envio uma requisição com o campo "titulo" em branco
  Quando envio a requisição
  Então o sistema retorna HTTP 400 Bad Request com validação no campo "titulo"
```

---

### US02 — Busca Filtrada por Tipo, Período e Título
* **ID:** US02
* **Requisito Relacionado:** RF02
* **Critério do Desafio:** CA4

**Como** auditor fiscal ou responsável administrativo,  
**quero** filtrar os documentos por tipo, intervalo de datas e trecho do título,  
**para que** eu encontre rapidamente as atas e relatórios solicitados em auditorias sem perda de tempo.

#### Critérios de Aceite (BDD)
```gherkin
Cenário: Busca por tipo e período específico (CA4)
  Dado que existem os seguintes documentos cadastrados:
    | Título               | Tipo      | Data       |
    | Ata Reunião Q1       | ata       | 2026-02-10 |
    | Ata Reunião Q2       | ata       | 2026-05-20 |
    | Ata Reunião Q3       | ata       | 2026-09-15 |
    | Contrato Fornecedor  | contrato  | 2026-03-01 |
  Quando realizo uma busca com tipo="ata", data_inicio="2026-01-01" e data_fim="2026-06-30"
  Então o sistema retorna HTTP 200 OK
  E a listagem contém exatamente 2 documentos ("Ata Reunião Q1" e "Ata Reunião Q2")
  E nenhum contrato ou ata fora desse período é retornado

Cenário: Busca por parte do título (busca textual parcial)
  Dado que existem documentos com títulos "Contrato de Aluguel 2026" e "Relatório Financeiro Anual"
  Quando busco pelo termo "aluguel" (em minúsculas)
  Então o documento "Contrato de Aluguel 2026" é retornado
```

---

### US03 — Registro de Nova Versão e Manutenção de Histórico
* **ID:** US03
* **Requisito Relacionado:** RF03
* **Critério do Desafio:** CA2

**Como** secretária da ONG,  
**quero** registrar uma nova versão de um documento vigente informando o motivo da alteração,  
**para que** o número da versão seja incrementado e o histórico de todas as alterações anteriores seja preservado.

#### Critérios de Aceite (BDD)
```gherkin
Cenário: Incremento de versão bem-sucedido (CA2)
  Dado que existe um documento cadastrado na versão 1 com status "vigente"
  Quando registro uma nova versão informando a observação "Adicionada cláusula de rescisão no item 5"
  Então o sistema retorna HTTP 200 OK
  E o campo "versao_atual" do documento passa a ser 2
  E a consulta ao histórico do documento apresenta exatamente 2 linhas (versão 1 e versão 2)
  E a versão 2 exibe a observação informada
```

---

### US04 — Bloqueio de Versionamento em Documento Arquivado
* **ID:** US04
* **Requisito Relacionado:** RF04
* **Critério do Desafio:** CA3

**Como** diretora administrativa da ONG,  
**quero** arquivar um documento concluído ou obsoleto,  
**para que** nenhuma nova versão possa ser acidentalmente ou indevidamente adicionada a ele.

#### Critérios de Aceite (BDD)
```gherkin
Cenário: Arquivamento com sucesso
  Dado um documento com status "vigente"
  Quando envio a requisição de arquivamento
  Então o sistema atualiza o status do documento para "arquivado"
  E retorna HTTP 200 OK com o novo status

Cenário: Tentativa de registrar nova versão em documento arquivado (CA3)
  Dado que um documento possui status "arquivado"
  Quando tento registrar uma nova versão para esse documento
  Então o sistema rejeita a operação com status HTTP 409 Conflict
  E o detalhe do erro informa "Documentos arquivados não podem receber novas versões"
  E a versão atual do documento permanece inalterada
```

---

### US05 — Consulta do Histórico de Revisões
* **ID:** US05
* **Requisito Relacionado:** RF05

**Como** auditor,  
**quero** visualizar a linha do tempo completa de versões de um documento,  
**para que** eu possa auditar quem alterou, quando alterou e qual foi a justificativa formal em cada revisão.

#### Critérios de Aceite (BDD)
```gherkin
Cenário: Consulta de histórico completo
  Dado um documento que já possui 3 versões registradas
  Quando solicito o histórico de versões pelo identificador do documento
  Então o sistema retorna HTTP 200 OK
  E a resposta contém a lista com as 3 versões ordenadas sequencialmente pelo número da versão
  E cada item exibe: número da versão, observação de alteração e data/hora de criação
```

---

### US06 — Painel de Métricas e Contagem Documental
* **ID:** US06
* **Requisito Relacionado:** RF06

**Como** gestor da ONG,  
**quero** visualizar no dashboard o total de documentos cadastrados, vigentes, arquivados e o quantitativo por tipo,  
**para que** eu tenha um panorama imediato do acervo documental da instituição.

#### Critérios de Aceite (BDD)
```gherkin
Cenário: Visualização de métricas consolidadas
  Dado que existem documentos cadastrados de diferentes tipos e status
  Quando acesso o endpoint de métricas
  Então o sistema retorna HTTP 200 OK com os totais exatos:
    | Métrica        | Valor Esperado                     |
    | total          | Soma total de documentos           |
    | vigentes       | Contagem de documentos vigentes    |
    | arquivados     | Contagem de documentos arquivados  |
    | por_tipo       | Quantidade detalhada por tipo      |
```

---

## Rastreabilidade

| História de Usuário | Requisito Funcional | Critério de Aceite | Caso de Uso | Issue |
| :--- | :--- | :--- | :--- | :--- |
| **US01** | RF01 | CA1 | UC01 | ISSUE-09, ISSUE-10 |
| **US02** | RF02 | CA4 | UC04 | ISSUE-12 |
| **US03** | RF03 | CA2 | UC02 | ISSUE-11 |
| **US04** | RF04 | CA3 | UC03 | ISSUE-11 |
| **US05** | RF05 | - | UC05 | ISSUE-10, ISSUE-14 |
| **US06** | RF06 | - | UC06 | ISSUE-13 |

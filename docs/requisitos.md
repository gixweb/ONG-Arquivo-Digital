# Especificação de Requisitos de Software
## Desafio 14: Arquivo Digital da ONG

Este documento especifica os Requisitos Funcionais (RF) e Não Funcionais (RNF) do **Arquivo Digital da ONG**, provendo rastreabilidade bidirecional com as Histórias de Usuário, Casos de Uso e o Backlog de Engenharia.

---

## 1. Requisitos Funcionais (RF)

| Identificador | Título | Descrição | Prioridade | Critério / Regra Associada | História de Usuário |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **RF01** | **Registrar Documento** | O sistema deve permitir o cadastro de um novo documento informando: título, tipo, data do documento e responsável fictício. Todo novo documento deve iniciar obrigatoriamente na versão `1` com status `vigente`. | **Must** | Os tipos válidos são exclusivamente: `ata`, `contrato`, `relatorio` ou `oficio`. Qualquer outro tipo (ex: `memorando`) deve ser rejeitado (**CA1**). | US01 |
| **RF02** | **Buscar Documentos** | O sistema deve permitir a busca e filtragem combinada de documentos com base no tipo (`ata`, `contrato`, etc.), intervalo de datas (`data_inicio` a `data_fim`) e correspondência textual parcial (case-insensitive) no título. | **Must** | Busca filtrada por tipo e período deve retornar exclusivamente os registros correspondentes (**CA4**). Suporte a paginação e ordenação por data descendente. | US02 |
| **RF03** | **Registrar Nova Versão** | O sistema deve permitir o registro de uma nova versão para um documento existente com status `vigente`. A operação deve incrementar atomicamente `versao_atual` e criar uma nova linha no histórico com número sequencial e campo `observacao` obrigatório. | **Must** | Regra central: incremento de versão atômico (`versao_atual = 2`), mantendo integridade com histórico de versões (**CA2**). | US03 |
| **RF04** | **Arquivar Documento** | O sistema deve permitir a alteração do status de um documento de `vigente` para `arquivado`. Um documento arquivado é imutável: qualquer tentativa de registrar nova versão deve ser bloqueada. | **Must** | Regra central: tentativa de versionamento em documento arquivado deve retornar HTTP `409 Conflict` (**CA3**). | US04 |
| **RF05** | **Consultar Histórico de Versões** | O sistema deve disponibilizar a consulta completa do histórico de versões de um documento específico, listando número da versão, observação de alteração e data/hora de criação de cada versão. | **Must** | Deve retornar a lista ordenada por número da versão de forma determinística. | US05 |
| **RF06** | **Métricas e Resumo do Arquivo** | O sistema deve fornecer um endpoint e painel consolidado com o total de documentos registrados, contagem por tipo (`ata`, `contrato`, etc.) e total por status (`vigente` vs `arquivado`). | **Should** | Agregação via consulta única no banco para alimentar o painel gerencial da secretaria. | US06 |

---

## 2. Requisitos Não Funcionais (RNF)

| Identificador | Categoria | Descrição | Métrica / Alvo Mensurável |
| :--- | :--- | :--- | :--- |
| **RNF01** | **Integridade Transacional (ACID)** | A criação de uma nova versão deve persistir o registro em `versoes` e atualizar `documentos` dentro da mesma transação de banco. | 100% atômico: rollback garantido se qualquer etapa falhar; restrição `UNIQUE(documento_id, numero)`. |
| **RNF02** | **Desempenho (Latência)** | A API deve responder consultas de busca (RF02) e histórico (RF05) de maneira ágil sob carga normal. | Latência $p95 \le 200\text{ ms}$ para listagem com até 10.000 registros indexados. |
| **RNF03** | **Padronização de Erros REST** | Todas as exceções e violações de regras de negócio devem retornar o padrão RFC 7807 (`ProblemDetail`). | 100% das respostas de erro com campos padronizados: `type`, `title`, `status`, `detail`, `instance` e `timestamp`. |
| **RNF04** | **Persistência e Migrações** | O schema relacional deve ser gerenciado de forma declarativa e versionada através de migrações Flyway. | Nenhuma tabela criada via `ddl-auto=update`; migrações com idempotência e execução no boot. |
| **RNF05** | **Qualidade e Cobertura de Código** | O código-fonte deve ser coberto por testes automatizados em pirâmide (unitários, slices e integração). | Cobertura JaCoCo $\ge 80\%$ de linhas e $\ge 65\%$ de branches no build Maven (`mvn verify`). |
| **RNF06** | **Usabilidade da Interface (SPA)** | O console web deve oferecer estados visuais explícitos (carregamento, vazio, erro com retry e sucesso) e confirmação para ações destrutivas. | 100% das ações de arquivamento requerem confirmação explícita do usuário. |
| **RNF07** | **Segurança e Validação de Entrada** | A API deve validar estritamente todos os dados de entrada na fronteira HTTP através de Bean Validation. | Zero injeção de SQL ou dados nulos inválidos; CORS configurado restritamente para a origem do frontend. |

---

## 3. Matriz de Rastreabilidade

| Requisito Funcional | Critério de Aceite | História de Usuário | Caso de Uso | Issue | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **RF01** | CA1 (Tipo memorando $\to$ 400) | US01 | UC01 | ISSUE-09, ISSUE-10 | Planejado |
| **RF02** | CA4 (Busca tipo=ata no período) | US02 | UC04 | ISSUE-12 | Planejado |
| **RF03** | CA2 (Nova versão $\to$ v2 + histórico) | US03 | UC02 | ISSUE-11 | Planejado |
| **RF04** | CA3 (Nova versão em arquivado $\to$ 409) | US04 | UC03 | ISSUE-11 | Planejado |
| **RF05** | - (Exibição de histórico completo) | US05 | UC05 | ISSUE-10, ISSUE-14 | Planejado |
| **RF06** | - (Métricas consolidadas) | US06 | UC06 | ISSUE-13 | Planejado |

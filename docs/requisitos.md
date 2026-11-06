# Especificação de Requisitos — Arquivo Digital da ONG

Este documento define os Requisitos Funcionais (**RF**) e Requisitos Não Funcionais (**RNF**) do sistema **Arquivo Digital**, estabelecendo rastreabilidade bidirecional com os critérios do desafio, histórias de usuário, casos de uso e issues no GitHub.

---

## 1. Requisitos Funcionais (RF)

| Identificador | Nome | Descrição | Prioridade (MoSCoW) | Critério de Aceite Relacionado | História Relacionada |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **RF01** | **Cadastrar Documento Inicial** | O sistema deve permitir o cadastro de um novo documento informando título, tipo (`ata`, `contrato`, `relatorio`, `oficio`), data do documento, responsável fictício e observação inicial opcional. O documento é criado automaticamente com versão 1 e status `vigente`. | **Must** | Não permitir tipos fora do domínio (ex: `memorando` deve retornar HTTP `400 Bad Request` — **CA1**). | US01 |
| **RF02** | **Buscar e Filtrar Documentos** | O sistema deve permitir a busca paginada de documentos por tipo, intervalo de datas (`data_inicio` e `data_fim`) e busca textual por parte do título (case-insensitive). | **Must** | Filtro de `tipo=ata` no período de 01/01 a 30/06 deve retornar exclusivamente atas cadastradas dentro desse intervalo (**CA4**). | US02 |
| **RF03** | **Registrar Nova Versão** | O sistema deve permitir a criação de uma nova versão para um documento existente, exigindo uma observação descritiva do motivo da alteração. O número da versão deve ser incrementado automaticamente e o histórico de todas as versões anteriores mantido intacto. | **Must** | Ao cadastrar nova versão para documento na versão 1, a `versao_atual` passa a 2 e o histórico passa a conter exatamente 2 linhas (**CA2**). | US03 |
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
| **RF01** | CA1 (Tipo memorando $\to$ 400) | US01 | UC01 | ISSUE-09, ISSUE-10 | Implementado e Validado |
| **RF02** | CA4 (Busca tipo=ata no período) | US02 | UC04 | ISSUE-12 | Implementado e Validado |
| **RF03** | CA2 (Nova versão $\to$ v2 + histórico) | US03 | UC02 | ISSUE-11 | Implementado e Validado |
| **RF04** | CA3 (Nova versão em arquivado $\to$ 409) | US04 | UC03 | ISSUE-11 | Implementado e Validado |
| **RF05** | - (Exibição de histórico completo) | US05 | UC05 | ISSUE-10, ISSUE-14 | Implementado e Validado |
| **RF06** | - (Métricas consolidadas) | US06 | UC06 | ISSUE-13 | Implementado e Validado |

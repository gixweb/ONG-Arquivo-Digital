# Especificação de Casos de Uso
## Desafio 14: Arquivo Digital da ONG

---

## 1. Atores

* **Secretaria Administrativa (Ator Primário):** Responsável por cadastrar documentos, lançar revisões/novas versões, arquivar documentos obsoletos e pesquisar o acervo durante auditorias.
* **Auditor / Órgão Regulador (Ator Secundário):** Consulta o acervo documental e inspeciona o histórico de versões vigentes para comprovação de conformidade fiscal e estatutária.

---

## 2. Índice de Casos de Uso

| ID | Nome do Caso de Uso | Requisito Relacionado |
| :--- | :--- | :--- |
| **UC01** | Registrar Novo Documento | RF01 |
| **UC02** | Registrar Nova Versão de Documento | RF03 |
| **UC03** | Arquivar Documento | RF04 |
| **UC04** | Buscar Documentos por Filtros | RF02 |
| **UC05** | Consultar Histórico de Versões | RF05 |
| **UC06** | Visualizar Métricas do Acervo | RF06 |

---

## 3. Especificação Detalhada dos Casos de Uso

### UC01 — Registrar Novo Documento
* **Ator Principal:** Secretaria Administrativa
* **Pré-condições:** O sistema está operacional e com a base de dados conectada.
* **Pós-condições:** O documento é persistido na tabela `documentos` com `versao_atual = 1` e `status = vigente`. O primeiro registro histórico da versão 1 é gerado automaticamente na tabela `versoes`.

#### Fluxo Principal
1. O usuário acessa a funcionalidade de cadastro e envia: `titulo`, `tipo` (`ata`, `contrato`, `relatorio` ou `oficio`), `data_documento` e `responsavel`.
2. O sistema valida os campos obrigatórios e confirma se o `tipo` pertence à lista permitida.
3. O sistema cria o documento com status `vigente` e `versao_atual = 1`.
4. O sistema insere a versão inicial (número 1) com observação padrão "Criação inicial do documento".
5. O sistema retorna o documento cadastrado com HTTP `201 Created` e o cabeçalho `Location` apontando para a URI do recurso.

#### Fluxos Alternativos
* **FA01 (Dados opcionais):** O usuário informa uma observação personalizada para a versão inicial; o sistema armazena a observação informada em vez da padrão.

#### Fluxos de Exceção
* **FE01 (Tipo de documento inválido - CA1):** O usuário envia um tipo não suportado (ex.: `memorando`). O sistema interrompe o processamento e retorna HTTP `400 Bad Request` com o detalhe: `"Tipo de documento inválido. Permitidos: ata, contrato, relatorio, oficio"`.
* **FE02 (Campos obrigatórios ausentes):** Título vazio ou data futura inválida. O sistema retorna HTTP `400 Bad Request` com a lista de violações de validação de campo.

---

### UC02 — Registrar Nova Versão de Documento
* **Ator Principal:** Secretaria Administrativa
* **Pré-condições:** O documento existe e seu status atual é `vigente`.
* **Pós-condições:** O número da versão do documento é incrementado em 1 (`versao_atual = versao_atual + 1`). Uma nova linha é inserida na tabela `versoes` vinculada ao documento com a observação da alteração.

#### Fluxo Principal
1. O usuário localiza o documento e solicita o registro de nova versão, informando a `observacao` detalhando as mudanças realizadas.
2. O sistema recupera o documento e verifica que o status é `vigente`.
3. O sistema calcula a nova versão: `novo_numero = documento.versao_atual + 1`.
4. Em uma única transação atômica:
   a. Insere novo registro em `versoes` com `(documento_id, numero = novo_numero, observacao)`.
   b. Atualiza o registro em `documentos` definindo `versao_atual = novo_numero`.
5. O sistema confirma a operação e retorna HTTP `200 OK` (ou `201 Created`) com a representação atualizada do documento e do histórico (**CA2**).

#### Fluxos Alternativos
* **FA01 (Atualização concomitante de metadados):** O usuário também altera o responsável ou data do documento na nova versão; o sistema persiste as alterações no documento vigente.

#### Fluxos de Exceção
* **FE01 (Documento inexistente):** O identificador informado não existe no sistema. O sistema retorna HTTP `404 Not Found`.
* **FE02 (Documento arquivado - CA3):** O documento possui status `arquivado`. O sistema bloqueia a operação e retorna HTTP `409 Conflict` informando: `"Documentos arquivados não podem receber novas versões"`.
* **FE03 (Observação não informada):** O usuário tenta criar uma versão sem justificativa. O sistema rejeita com HTTP `400 Bad Request`.

---

### UC03 — Arquivar Documento
* **Ator Principal:** Secretaria Administrativa
* **Pré-condições:** O documento existe e seu status atual é `vigente`.
* **Pós-condições:** O status do documento na tabela `documentos` passa a ser `arquivado`.

#### Fluxo Principal
1. O usuário seleciona a opção "Arquivar" para um documento específico.
2. O sistema solicita confirmação explícita da ação ("Deseja realmente arquivar este documento? Ele não poderá mais ser alterado").
3. O usuário confirma.
4. O sistema atualiza o status do documento para `arquivado`.
5. O sistema retorna HTTP `200 OK` com os dados do documento atualizados.

#### Fluxos Alternativos
* **FA01 (Cancelamento de confirmação):** O usuário desiste na tela de confirmação; nenhuma alteração é feita.

#### Fluxos de Exceção
* **FE01 (Documento já arquivado):** O documento já está com status `arquivado`. O sistema opera de forma idempotente retornando HTTP `200 OK` com o estado atual.
* **FE02 (Documento inexistente):** Retorna HTTP `404 Not Found`.

---

### UC04 — Buscar Documentos por Filtros
* **Ator Principal:** Secretaria Administrativa / Auditor
* **Pré-condições:** Nenhuma.
* **Pós-condições:** O sistema retorna uma lista paginada de documentos que satisfazem simultaneamente os critérios informados.

#### Fluxo Principal
1. O usuário define critérios de busca: `tipo` (ex: `ata`), `data_inicio`, `data_fim` e/ou `titulo`.
2. O sistema executa a consulta aplicando a interseção (AND) de todos os filtros preenchidos.
3. O sistema retorna HTTP `200 OK` com os documentos encontrados, metadados de paginação e contagem total (**CA4**).

#### Fluxos Alternativos
* **FA01 (Busca sem filtros):** O usuário pesquisa sem critérios; o sistema retorna a listagem geral ordenada por data mais recente.
* **FA02 (Nenhum documento encontrado):** O sistema retorna HTTP `200 OK` com uma lista vazia (`[]`) e total de elementos igual a zero.

#### Fluxos de Exceção
* **FE01 (Período inválido):** Se `data_inicio` for posterior a `data_fim`, o sistema retorna HTTP `400 Bad Request`.

---

### UC05 — Consultar Histórico de Versões
* **Ator Principal:** Secretaria Administrativa / Auditor
* **Pré-condições:** O documento existe.
* **Pós-condições:** O sistema apresenta a lista cronológica de todas as versões do documento.

#### Fluxo Principal
1. O usuário requisita o histórico de um documento específico pelo seu ID.
2. O sistema consulta os registros da tabela `versoes` associados àquele `documento_id`, ordenados por `numero ASC`.
3. O sistema retorna HTTP `200 OK` contendo o cabeçalho do documento e a coleção de versões com `numero`, `observacao` e `criada_em`.

#### Fluxos de Exceção
* **FE01 (Documento inexistente):** O sistema retorna HTTP `404 Not Found`.

---

### UC06 — Visualizar Métricas do Acervo
* **Ator Principal:** Secretaria Administrativa
* **Pré-condições:** Nenhuma.
* **Pós-condições:** O sistema apresenta totais consolidados do arquivo.

#### Fluxo Principal
1. O usuário acessa a tela de dashboard/início.
2. O sistema executa uma agregação no banco de dados e calcula:
   * Total geral de documentos.
   * Total de documentos vigentes vs arquivados.
   * Quantidade de documentos por tipo (`ata`, `contrato`, `relatorio`, `oficio`).
3. O sistema retorna HTTP `200 OK` com a estrutura consolidada.

---

## 4. Rastreabilidade com Backlog e Requisitos

| Caso de Uso | Requisito | Critério de Aceite | Issue do Backlog |
| :--- | :--- | :--- | :--- |
| **UC01** | RF01 | CA1 | ISSUE-09, ISSUE-10 |
| **UC02** | RF03 | CA2, CA3 | ISSUE-11 |
| **UC03** | RF04 | CA3 | ISSUE-11 |
| **UC04** | RF02 | CA4 | ISSUE-12 |
| **UC05** | RF05 | - | ISSUE-10, ISSUE-14 |
| **UC06** | RF06 | - | ISSUE-13 |

# Termo de Abertura do Projeto (Project Charter)
## Desafio 14: Arquivo Digital da ONG

| Informação | Detalhe |
| :--- | :--- |
| **Projeto** | Arquivo Digital da ONG (ONG-AD) |
| **Versão** | 1.0.0 (Planejamento) |
| **Status** | Aprovado |
| **Data** | 05 de Novembro de 2026 |
| **Responsável Técnico** | Engenheiro de Software / Tech Lead |
| **Usuário Principal** | Secretaria Administrativa da ONG |

---

## 1. Problema e Justificativa

### 1.1 O Problema
Atualmente, a organização não governamental (ONG) enfrenta severas dificuldades na rastreabilidade e governança de seus documentos institucionais (atas de assembleia, contratos com parceiros e fornecedores, relatórios financeiros e ofícios governamentais). Os arquivos encontram-se dispersos em caixas de e-mail de colaboradores, diretórios locais e *pen drives*. 

Em momentos críticos — como auditorias fiscais, prestações de contas a financiadores e renovação de certidões públicas —, a equipe gasta dias tentando identificar qual versão de um contrato ou ata é a versão vigente e oficial. Essa desorganização gera riscos regulatórios, perdas financeiras e passivos de conformidade.

### 1.2 Justificativa
A criação de um sistema web centralizado de **Arquivo Digital** resolve a raiz do problema:
* Fornece um repositório central com metadados estruturados.
* Garante versionamento estrito com rastreabilidade histórica e motivos de alteração.
* Implementa bloqueio definitivo contra alterações indevidas (arquivamento de documentos).
* Possibilita buscas instantâneas por tipo de documento, período e parte do título para pronto atendimento às auditorias.

---

## 2. Visão do Produto
> *"Para a Secretaria Administrativa da ONG que necessita de governança, conformidade e agilidade em auditorias, o **Arquivo Digital da ONG** é uma solução web e API RESTful que centraliza o registro documental, versiona alterações com histórico imutável e bloqueia documentos arquivados, eliminando a perda de documentos vigentes e a incerteza sobre versões oficiais."*

---

## 3. Objetivos SMART

1. **S**pecific (Específico): Implementar uma API RESTful e console web que permita registrar documentos (`ata`, `contrato`, `relatorio`, `oficio`), criar novas versões incrementais, arquivar documentos e consultar o histórico completo.
2. **M**easurable (Mensurável): Atingir **100% de conformidade com os Critérios de Aceite (CA1 a CA4)** do Desafio 14 e cobertura de testes automatizados **≥ 80% de linhas** e **≥ 65% de branches** verificada via JaCoCo.
3. **A**chievable (Alcançável): Entregar o escopo completo utilizando uma arquitetura em camadas (Spring Boot 3 + PostgreSQL 16 + Flyway + Frontend Vanilla/SPA) sem dependências externas complexas nesta semana.
4. **R**elevant (Relevante): Reduzir o tempo de localização de um documento oficial e seu histórico de versões de várias horas/dias para **menos de 3 segundos** no console web.
5. **T**ime-bound (Temporal): Concluir o ciclo documental, as 15 issues no GitHub, os quality gates e a tag `v1.0.0` até 07 de novembro de 2026 às 23:59.

---

## 4. Escopo Preliminar

### 4.1 O que está DENTRO do escopo (In-Scope)
* **RF01 - Registro de Documento**: Cadastro com título, tipo (`ata`, `contrato`, `relatorio`, `oficio`), data do documento e responsável. Inicialização automática na versão 1 e status `vigente`.
* **RF02 - Busca Multicritério**: Filtro combinado por tipo, período (`data_inicio` a `data_fim`) e busca textual por parte do título.
* **RF03 - Registro de Nova Versão**: Incremento atômico de versão (`versao_atual = versao_atual + 1`), mantendo o histórico de versões com observação obrigatória do motivo da alteração.
* **RF04 - Arquivamento de Documento**: Transição de status para `arquivado`, bloqueando permanentemente a criação de novas versões (rejeição com HTTP 409 Conflict).
* **RF05 - Consulta de Histórico de Versões**: Endpoint e visualização das linhas históricas de cada versão de um documento com data e observação.
* **Console Web (Frontend Split)**: Interface SPA leve para consulta, cadastro, versionamento e arquivamento consumindo a API REST.
* **Engenharia e Qualidade**: Pipeline CI no GitHub Actions, testes unitários, testes de integração de slice, migrações de schema com Flyway e documentação técnica completa.

### 4.2 O que está EXPRESSAMENTE FORA do escopo (Out-of-Scope)
* Armazenamento binário em Azure Blob Storage (foco estrito no modelo relacional ACID e metadados no PostgreSQL).
* Múltiplos tenants ou isolamento por federação de ONGs.
* Sistema de autenticação/autorização multiusuário (OAuth2/SSO/JWT) nesta fase; a segurança apoia-se em validação estrita de entradas e CORS restrito.
* Assinatura digital com certificados ICP-Brasil.
* Notificações push ou WebSockets em tempo real.
* Aplicativos móveis nativos (Android/iOS).

---

## 5. Stakeholders e Público-Alvo

| Stakeholder | Papel no Projeto | Necessidade Principal |
| :--- | :--- | :--- |
| **Secretaria Administrativa** | Usuário Primário | Interface simples para cadastrar, versionar e localizar atas e contratos rapidamente. |
| **Auditores Fiscais / Financiadores** | Usuário Consultor | Garantia de integridade do histórico e fácil identificação da versão vigente. |
| **Equipe de Governança / Diretoria** | Patrocinadores | Segurança regulatória e fim da dispersão de arquivos em mídias físicas. |
| **Avaliador do Curso (Senac)** | Avaliador / Revisor | Repositório claro, rastreável, com 15 issues, Git Flow, testes e documentação exemplar. |

---

## 6. Métricas de Sucesso

1. **Aderência aos Critérios do Desafio 14:**
   * CA1: Rejeição de tipos não permitidos (`memorando`) com HTTP 400 Bad Request.
   * CA2: Criação de nova versão resulta em `versao_atual = 2` e histórico contendo exatamente 2 registros.
   * CA3: Tentativa de versionar documento com status `arquivado` retorna HTTP 409 Conflict.
   * CA4: Busca por tipo `ata` em determinado intervalo de datas retorna apenas as atas correspondentes.
2. **Qualidade de Software:**
   * Build e testes 100% automatizados no GitHub Actions.
   * Zero falhas nos testes e cobertura de código JaCoCo ≥ 80%.
   * Código em conformidade com princípios SOLID, DRY e KISS sem acoplamento entre camada de domínio e frameworks.

---

## 7. Riscos e Mitigações

| Risco | Probabilidade | Impacto | Estratégia de Mitigação |
| :--- | :--- | :--- | :--- |
| **Concorrência ao criar novas versões** | Média | Alto | Utilizar transação única ACID (`@Transactional`) combinando `INSERT` em `versoes`, `UPDATE` em `documentos` e `UNIQUE (documento_id, numero)`. |
| **Regressão na integridade de dados ao arquivar** | Baixa | Alto | Regra de negócio encapsulada na entidade de domínio lançando exceção específica tratada por `@RestControllerAdvice` (409 Conflict). |
| **Complexidade de ambiente local** | Média | Médio | Configuração de profile de teste com H2 e profile padrão com PostgreSQL documentado via `.env.example` e Docker Compose. |

---

## 8. Rastreabilidade

| Requisito | User Story | Caso de Uso | Issue do Backlog |
| :--- | :--- | :--- | :--- |
| RF01 | US01 | UC01 | ISSUE-09, ISSUE-10 |
| RF02 | US02 | UC04 | ISSUE-12 |
| RF03 | US03 | UC02 | ISSUE-11 |
| RF04 | US04 | UC03 | ISSUE-11 |
| RF05 | US05 | UC05 | ISSUE-10, ISSUE-14 |

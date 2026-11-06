# Changelog

Todas as alterações notáveis deste projeto serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [1.0.0] - 2026-11-05

### Adicionado
- **ISSUE-13 (RF06 / US06):** Endpoint analítico e de métricas consolidadas `GET /api/v1/metricas`, retornando total de documentos, vigentes, arquivados e quantitativo agrupado por tipo (`ata`, `contrato`, `relatorio`, `oficio`).
- **ISSUE-14 (RNF06 / US05 / US06):** Console Web Single-Page Application (SPA) em Vanilla JS, HTML5 e CSS3 moderno na pasta `frontend/`:
  - Cards de KPI em tempo real atualizados via API REST;
  - Barra de filtros com busca por tipo, período (`data_inicio` a `data_fim`) e título parcial;
  - Tabela responsiva com paginação, badges estilizados e ações contextuais por documento;
  - Modal de cadastro de novo documento com validação amigável e captura de erros RFC 7807;
  - Modal de registro de nova versão incremental com motivo obrigatório;
  - Modal de linha do tempo histórica com visualização de auditoria de cada revisão;
  - Modal de confirmação defensiva para arquivamento definitivo (ação irreversível);
  - Estados visuais explícitos de carregamento (spinner), vazio (empty state) e erro com botão de repetição;
  - Sistema de notificações toast automáticas.
- **Configuração CORS Global:** Adicionado `CorsConfig` permitindo métodos `GET`, `POST`, `PATCH`, `PUT`, `DELETE` e `OPTIONS` para integração com o cliente web.
- **ISSUE-15:** Finalização do ciclo de release v1.0.0 com checklist de conformidade e rastreabilidade total de requisitos.

### Testes
- Bateria de testes expandida para **80 testes automatizados** (0 falhas, 0 erros), cobrindo regras de negócio, testes de slice Web MVC com MockMvc e testes de integração com banco de dados H2/PostgreSQL.

---

## [0.3.0] - 2026-11-05

### Adicionado
- **ISSUE-10 (RF01 / CA1 / US01 / UC01):**
  - Endpoint `POST /api/v1/documentos` para criação de novos documentos com inicialização atômica na versão 1 e status `vigente`. Retorno HTTP 201 Created com cabeçalho `Location`.
  - Endpoint `GET /api/v1/documentos/{id}` com `DocumentoDetalhadoResponse` exibindo histórico completo de versões.
  - Validação estrita de tipos com rejeição imediata de tipos não homologados (ex: `memorando` $\to$ HTTP 400 Bad Request — **CA1**).
- **ISSUE-11 (RF03 / RF04 / CA2 / CA3 / US03 / US04 / UC02 / UC03):**
  - Endpoint `POST /api/v1/documentos/{id}/versoes` para registro de nova versão incremental. Comprovado incremento da `versao_atual` para 2 e 2 linhas no histórico (**CA2**).
  - Endpoint `PATCH /api/v1/documentos/{id}/arquivar` para transição irreversível para status `arquivado`.
  - Bloqueio estrito de versionamento em documentos arquivados com HTTP 409 Conflict (**CA3**).
  - Endpoint `GET /api/v1/documentos/{id}/versoes` listando histórico cronológico ordenado.
- **ISSUE-12 (RF02 / CA4 / US02 / UC04):**
  - Endpoint `GET /api/v1/documentos` com suporte a filtros combinados (`tipo`, `data_inicio`, `data_fim`, `titulo`) e paginação `PaginatedResponse`.
  - Validação do Critério de Aceite 4: busca por `tipo=ata` no intervalo 01/01 a 30/06 retorna com precisão exclusivamente as atas do período (**CA4**).

---

## [0.2.0] - 2026-11-05

### Adicionado
- **ISSUE-08:** Esqueleto de arquitetura hexagonal desacoplada (`domain`, `application`, `infrastructure`, `web`).
  - Suíte de testes arquiteturais com ArchUnit (`ArchitectureTest`) garantindo isolamento total do domínio sem referências a Spring ou JPA.
  - Tratamento global de exceções `@RestControllerAdvice` (`GlobalExceptionHandler`) aderente à RFC 7807 (`ProblemDetail`) com `correlationId` e `timestamp`.
- **ISSUE-09:** Camada de persistência relacional com Spring Data JPA e Flyway.
  - Migração `V1__criar_tabelas_documentos_e_versoes.sql` com constraints relacionais, `CHECK` e unicidade `UNIQUE(documento_id, numero)`.
  - Entidades `DocumentoJpaEntity` e `VersaoJpaEntity` com relacionamento cascata e mapeamento bidirecional.
  - Adaptador de persistência `DocumentoPersistenceAdapter` implementando a porta de saída `DocumentoRepositoryPort`.

---

## [0.1.0] - 2026-11-05

### Adicionado
- **ISSUE-01:** Bootstrap do repositório, estrutura em pastas segregadas (`backend/`, `frontend/`, `docs/`, `.github/`), arquivo `.gitignore`, `.env.example` e licença MIT.
- **ISSUE-02:** Termo de Abertura do Projeto (`docs/project-charter.md`) com metas SMART, escopo delimitado, público-alvo e análise de riscos.
- **ISSUE-03:** Especificação formal de requisitos funcionais (RF01 a RF06) e não-funcionais (RNF01 a RNF07) com matriz de rastreabilidade (`docs/requisitos.md`).
- **ISSUE-04:** Especificação de casos de uso (UC01 a UC06) com fluxos principais, alternativos e de exceção (`docs/casos-de-uso.md`).
- **ISSUE-05:** Histórias de usuário (US01 a US06) no padrão Gherkin Given/When/Then (`docs/user-stories.md`).
- **ISSUE-06:** README técnico inicial completo com badges, guia de inicialização e fluxo Git (`README.md`).
- **ISSUE-07:** Pipeline de Integração Contínua com GitHub Actions (`.github/workflows/ci.yml`) e quality gate de cobertura JaCoCo.
- **ADRs:** Decisões Arquiteturais documentadas para stack PostgreSQL (`ADR-001`) e versionamento imutável de documentos (`ADR-002`).

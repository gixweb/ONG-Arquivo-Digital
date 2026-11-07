# Documento de Arquitetura de Software (DAS)
## Arquivo Digital da ONG — Desafio 14

---

## 1. Visão Geral e Princípios Arquiteturais

O **Arquivo Digital da ONG** é projetado sob os fundamentos da **Arquitetura Limpa (Clean Architecture)** e **Arquitetura Hexagonal (Ports & Adapters)**, combinada com uma estratégia de divisão estrita de pastas (**folder-split**) entre cliente web e servidor de aplicação.

### 1.1 Princípios Norteadores
* **Independência de Frameworks (Inversão de Dependência):** A camada de domínio é escrita em **Java 21 puro**, sem nenhuma anotação ou dependência de Spring Boot ou Jakarta Persistence (JPA).
* **Alta Coesão e Baixo Acoplamento:** As regras de negócio centrais (validação de tipos homologados, incremento atômico de versão e bloqueio estrito contra alterações em documentos arquivados) residem exclusivamente no agregado de domínio.
* **Integridade Transacional (ACID):** O versionamento documental sincroniza atomicamente as tabelas `documentos` e `versoes` sob o isolamento `READ_COMMITTED` e restrição relacional de unicidade `UNIQUE(documento_id, numero)`.
* **Segurança e Papéis de Acesso (RBAC):** Controle estrito por papéis: `SECRETARIA` (permissão de escrita e consulta) e `AUDITOR` (permissão estritamente de consulta/somente leitura).
* **Padronização RESTful e RFC 7807:** Todas as respostas de erro HTTP seguem o contrato padronizado *Problem Details for HTTP APIs* (`ProblemDetail`), com `correlationId` e `timestamp`.
* **Quality Gates Arquiteturais Automatizados:** A pureza das fronteiras arquiteturais é verificada em cada build pelo **ArchUnit**.

---

## 2. Diagramas Arquiteturais (C4 Model)

### 2.1 C4 Nível 1 — Diagrama de Contexto do Sistema

```mermaid
flowchart TD
    UserSec["Secretaria Administrativa\n(Cadastra, versiona, arquiva)"]
    UserAud["Auditoria / Órgão Regulador\n(Consulta acervo e histórico de auditoria)"]
    System["Sistema Arquivo Digital da ONG\n(Gestão de Documentos Oficiais, Auditoria e Versionamento)"]

    UserSec -->|Cadastra, versiona e arquiva documentos| System
    UserAud -->|Consulta acervo e histórico imutável| System
```

---

### 2.2 C4 Nível 2 — Diagrama de Contêineres

```mermaid
flowchart TD
    User["Secretaria Administrativa / Auditor"]

    subgraph ClientContainer ["Contêiner Frontend (Pasta frontend/)"]
        SPA["Console Web SPA\n(HTML5 / CSS3 / Vanilla JavaScript)\nInterface responsiva, painel de métricas e modais"]
    end

    subgraph ServerContainer ["Contêiner Backend (Pasta backend/)"]
        API["API REST Spring Boot 3.4.x (Java 21 LTS)\nControladores REST, Segurança RBAC, Casos de Uso e Domínio Limpo"]
    end

    subgraph DatabaseContainer ["Contêiner de Banco de Dados"]
        Postgres[("PostgreSQL 16\n(Tabelas: documentos, versoes e flyway_schema_history)\nPerfil prod / Docker")]
        H2Mem[("H2 Database em Memória\n(Modo de Compatibilidade PostgreSQL)\nPerfil dev / test")]
    end

    User -->|HTTPS / Navegador Web| SPA
    SPA -->|JSON sobre HTTP / Endpoints /api/v1| API
    API -->|JDBC / Driver PostgreSQL| Postgres
    API -.->|Execução imediata sem dependências| H2Mem
```

---

### 2.3 C4 Nível 3 — Diagrama de Componentes do Backend

```mermaid
flowchart TD
    subgraph WebLayer ["Camada Web (com.ong.backend.web)"]
        DocController["DocumentoController\nEndpoints /api/v1/documentos"]
        MetController["MetricasController\nEndpoints /api/v1/metricas"]
        GlobalHandler["GlobalExceptionHandler\nRFC 7807 (ProblemDetail)"]
    end

    subgraph SecurityLayer ["Camada de Segurança (com.ong.backend.infrastructure.config)"]
        SecConfig["SecurityConfig\nFiltros RBAC (SECRETARIA vs AUDITOR)"]
    end

    subgraph ApplicationLayer ["Camada de Aplicação (com.ong.backend.application)"]
        InPorts["Portas de Entrada (Use Cases)\nCriarDocumentoUseCase\nRegistrarNovaVersaoUseCase\nArquivarDocumentoUseCase\nBuscarDocumentosComFiltroUseCase\nConsultarHistoricoVersoesUseCase\nObterMetricasArquivoUseCase"]
        DocService["DocumentoService\nOrquestração de Casos de Uso e Transações"]
        OutPort["Porta de Saída (Output Port)\nDocumentoRepositoryPort"]
    end

    subgraph DomainLayer ["Camada de Domínio Puro (com.ong.backend.domain)"]
        DocAggregate["Documento (Aggregate Root)\nRegras de transição de status e versão"]
        VersaoVO["VersaoDocumento\nHistórico imutável de revisões"]
        Enums["Enums de Domínio\nTipoDocumento (ata, contrato, relatorio, oficio)\nStatusDocumento (vigente, arquivado)"]
        Exceptions["Exceções de Domínio\nDocumentoArquivadoException (409)\nTipoDocumentoInvalidoException (400)\nDocumentoNaoEncontradoException (404)"]
    end

    subgraph InfrastructureLayer ["Camada de Infraestrutura (com.ong.backend.infrastructure)"]
        Adapter["DocumentoPersistenceAdapter\nImplementa DocumentoRepositoryPort"]
        SpringDataDoc["SpringDataDocumentoRepository\nJPA Specifications & Agregações Group By"]
        SpringDataVer["SpringDataVersaoRepository\nConsultas ordenadas por número"]
        Entities["Entidades JPA\nDocumentoJpaEntity & VersaoJpaEntity"]
        FlywayMig["Flyway Migration (V1)\nDDL declarativa com CHECK e UNIQUE"]
        CorsCfg["CorsConfig\nConfiguração de origens cruzadas"]
    end

    SecConfig --> DocController
    SecConfig --> MetController
    DocController --> InPorts
    MetController --> InPorts
    DocController -.-> GlobalHandler
    InPorts --> DocService
    DocService --> DocAggregate
    DocService --> OutPort
    DocAggregate --> VersaoVO
    DocAggregate --> Enums
    DocAggregate -.-> Exceptions
    Adapter --> OutPort
    Adapter --> SpringDataDoc
    Adapter --> SpringDataVer
    Adapter --> Entities
```

---

## 3. Fluxo de Versionamento e Arquivamento Documental

O ciclo de vida de um documento é regido por regras estritas de integridade transacional e imutabilidade pós-arquivamento (**CA2** e **CA3**).

### 3.1 Diagrama de Estados do Documento

```mermaid
stateDiagram-v2
    [*] --> Vigente : POST /api/v1/documentos (versao = 1)
    
    state Vigente {
        [*] --> Versao1
        Versao1 --> VersaoN : POST /api/v1/documentos/{id}/versoes (incrementa versao_atual)
        VersaoN --> VersaoN : Novas versões sequenciais com observação
    }

    Vigente --> Arquivado : PATCH /api/v1/documentos/{id}/arquivar
    
    state Arquivado {
        [*] --> Imutavel : Status 'arquivado' permanente
        Imutavel --> Bloqueio : Tentativa de registrar nova versão
        Bloqueio --> [*] : Retorna HTTP 409 Conflict (CA3)
    }
```

---

### 3.2 Diagrama de Sequência — Registro de Nova Versão (CA2)

```mermaid
sequenceDiagram
    autonumber
    actor User as Secretaria Administrativa
    participant Controller as DocumentoController
    participant Service as DocumentoService
    participant Domain as Documento (Aggregate)
    participant Repo as DocumentoPersistenceAdapter
    participant DB as PostgreSQL 16

    User->>Controller: POST /api/v1/documentos/1/versoes {"observacao": "Revisão cláusula 3"}
    Controller->>Service: registrarNovaVersao(1, "Revisão...")
    Service->>Repo: buscarPorIdComVersoes(1)
    Repo->>DB: SELECT com JOIN FETCH versoes
    DB-->>Repo: DocumentoJpaEntity
    Repo-->>Service: Documento (Domain Aggregate)
    Service->>Domain: registrarNovaVersao("Revisão...")
    Note over Domain: Valida se status != ARQUIVADO<br/>Calcula proximoNumero = versaoAtual + 1<br/>Cria VersaoDocumento(numero, observacao)<br/>Atualiza versaoAtual = 2
    Domain-->>Service: VersaoDocumento gerada
    Service->>Repo: salvar(Documento)
    Repo->>DB: INSERT INTO versoes + UPDATE documentos (Transação Única)
    DB-->>Repo: Confirmado
    Service-->>Controller: VersaoDocumento
    Controller-->>User: HTTP 200 OK {"numero": 2, "observacao": "..."}
```

---

### 3.3 Diagrama de Sequência — Tentativa de Versionamento em Documento Arquivado (CA3)

```mermaid
sequenceDiagram
    autonumber
    actor User as Secretaria Administrativa
    participant Controller as DocumentoController
    participant Service as DocumentoService
    participant Domain as Documento (Aggregate)
    participant Handler as GlobalExceptionHandler

    User->>Controller: POST /api/v1/documentos/1/versoes {"observacao": "Tentativa indevida"}
    Controller->>Service: registrarNovaVersao(1, "...")
    Service->>Domain: registrarNovaVersao("...")
    Note over Domain: Documento com status == ARQUIVADO!<br/>Lança DocumentoArquivadoException
    Domain-->>Service: throw DocumentoArquivadoException(1)
    Service-->>Controller: throw DocumentoArquivadoException(1)
    Controller-->>Handler: intercepta DocumentoArquivadoException
    Note over Handler: Monta RFC 7807 ProblemDetail<br/>status = 409 Conflict
    Handler-->>User: HTTP 409 Conflict {"title": "Documento Arquivado", "detail": "..."}
```

---

## 4. Segurança e Controle de Acesso Baseado em Papéis (RBAC)

A API adota **Spring Security** com autenticação HTTP Basic e divisão de papéis (**Role-Based Access Control**):

| Papel | Descrição | Endpoints Permitidos | Endpoints Bloqueados |
| :--- | :--- | :--- | :--- |
| **`ROLE_SECRETARIA`** | Secretaria Administrativa | `POST /api/v1/documentos`<br>`POST /api/v1/documentos/{id}/versoes`<br>`PATCH /api/v1/documentos/{id}/arquivar`<br>`GET /api/v1/**` | Nenhum (Acesso completo) |
| **`ROLE_AUDITOR`** | Auditor / Conselho Fiscal | `GET /api/v1/documentos`<br>`GET /api/v1/documentos/{id}`<br>`GET /api/v1/documentos/{id}/versoes`<br>`GET /api/v1/metricas` | `POST /api/v1/documentos` (403 Forbidden)<br>`POST /api/v1/documentos/{id}/versoes` (403)<br>`PATCH /api/v1/documentos/{id}/arquivar` (403) |
| **Anônimo** | Usuário não autenticado | Arquivos estáticos do frontend (`/`, `/index.html`, `/css/**`, `/js/**`) | Todos os endpoints `/api/v1/**` (401 Unauthorized) |

---

## 5. Garantia Arquitetural Automatizada (ArchUnit)

Para garantir que violações de arquitetura não passem despercebidas em revisões manuais, o projeto conta com testes de conformidade arquitetural em tempo de compilação ([`ArchitectureTest.java`](file:///C:/Users/gisel/OneDrive/Documentos/Estudos/Senac/ONG%20AD/backend/src/test/java/com/ong/backend/architecture/ArchitectureTest.java)):

1. **Regra 1 (Desacoplamento do Spring):** Nenhuma classe do pacote `..domain..` pode depender de pacotes `org.springframework..`.
2. **Regra 2 (Desacoplamento de Persistência):** Nenhuma classe do pacote `..domain..` pode depender de `jakarta.persistence..`.
3. **Regra 3 (Inversão de Dependência):** Classes do domínio não podem depender das camadas externas `..web..` ou `..infrastructure..`.

---

## 6. Modelo de Dados Relacional e Persistência

### 6.1 Diagrama Entidade-Relacionamento (ERD)

```mermaid
erDiagram
    DOCUMENTOS ||--o{ VERSOES : "possui historico de"
    
    DOCUMENTOS {
        bigserial id PK
        varchar_255 titulo "NOT NULL"
        varchar_20 tipo "NOT NULL (CHECK: ata, contrato, relatorio, oficio)"
        date data_documento "NOT NULL"
        varchar_150 responsavel "NOT NULL"
        varchar_20 status "NOT NULL DEFAULT vigente (CHECK: vigente, arquivado)"
        integer versao_atual "NOT NULL DEFAULT 1"
        timestamp criado_em "NOT NULL DEFAULT NOW()"
        timestamp atualizado_em "NOT NULL DEFAULT NOW()"
    }

    VERSOES {
        bigserial id PK
        bigint documento_id FK "NOT NULL REFERENCES documentos(id) ON DELETE CASCADE"
        integer numero "NOT NULL (CHECK > 0)"
        text observacao "NULL"
        timestamp criado_em "NOT NULL DEFAULT NOW()"
    }
```

### 6.2 Integridade e Invariantes no Banco
* **Restrição de Unicidade Composta:** `CONSTRAINT uk_documento_versao UNIQUE (documento_id, numero)`. Garante que sob nenhuma circunstância de concorrência duas versões com o mesmo número sejam geradas para o mesmo documento.
* **Integridade Referencial:** Cascata na exclusão `ON DELETE CASCADE` da chave estrangeira.
* **Migrações Declarativas:** Executadas na inicialização via **Flyway** (`V1__criar_tabelas_documentos_e_versoes.sql`). Nenhuma alteração de schema é feita via `ddl-auto=update`.

---

## 7. Padronização de Erros REST (RFC 7807)

Todas as falhas de validação, regras de negócio violadas e recursos inexistentes retornam uma estrutura padronizada da RFC 7807 através do [`GlobalExceptionHandler`](file:///C:/Users/gisel/OneDrive/Documentos/Estudos/Senac/ONG%20AD/backend/src/main/java/com/ong/backend/web/exception/GlobalExceptionHandler.java):

```json
{
  "type": "about:blank",
  "title": "Documento Arquivado",
  "status": 409,
  "detail": "O documento ID 1 está arquivado e não pode receber novas versões.",
  "instance": "/api/v1/documentos/1/versoes",
  "timestamp": "2026-11-07T19:30:00Z",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Mapeamento de Status HTTP

| Exceção de Domínio / Web | Código HTTP | Justificativa / Critério |
| :--- | :--- | :--- |
| `TipoDocumentoInvalidoException` | `400 Bad Request` | Tipo fora do vocabulário homologado (**CA1**) |
| `MethodArgumentNotValidException` | `400 Bad Request` | Violação de anotações Bean Validation |
| `AuthenticationException` | `401 Unauthorized` | Credenciais ausentes ou inválidas (**SEC**) |
| `AccessDeniedException` | `403 Forbidden` | Auditor tentando ação de escrita (**SEC**) |
| `DocumentoNaoEncontradoException` | `404 Not Found` | Identificador do documento inexistente |
| `DocumentoArquivadoException` | `409 Conflict` | Documento imutável bloqueia nova versão (**CA3**) |
| `Exception` (inesperada) | `500 Internal Server Error` | Erros não mapeados com ocultação de stacktrace |

---

## 8. Rastreabilidade com ADRs (Architectural Decision Records)

Para o histórico completo de justificativas e alternativas consideradas nas decisões-chave, consulte:

1. [ADR 001: Persistência Relacional em PostgreSQL](adr/001-stack-e-banco-postgresql.md) — Justificativa da adoção do PostgreSQL 16 e integridade ACID frente a opções NoSQL/Blob Storage.
2. [ADR 002: Estratégia de Versionamento e Arquivamento Documental](adr/002-estrategia-versionamento-documental.md) — Decisão de modelo relacional normalizado com agregado de domínio, versionamento atômico e bloqueio via HTTP 409.

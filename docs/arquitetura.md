# Documento de Arquitetura de Software (DAS)
## Arquivo Digital da ONG — Desafio 14

---

## 1. Visão Geral e Princípios Arquiteturais

O **Arquivo Digital da ONG** é projetado sob os fundamentos da **Arquitetura Limpa (Clean Architecture)** e **Arquitetura Hexagonal (Ports & Adapters)**, combinada com uma estratégia de divisão estrita de pastas (**folder-split**) entre cliente web e servidor de aplicação.

### 1.1 Princípios Norteadores
* **Independência de Frameworks (Inversão de Dependência):** A camada de domínio é escrita em **Java 21 puro**, sem nenhuma anotação ou dependência de Spring Boot ou Jakarta Persistence (JPA).
* **Alta Coesão e Baixo Acoplamento:** As regras de negócio centrais (validação de tipos homologados, incremento atômico de versão e bloqueio estrito contra alterações em documentos arquivados) residem exclusivamente no agregado de domínio.
* **Integridade Transacional (ACID):** O versionamento documental sincroniza atomicamente as tabelas `documentos` e `versoes` sob o isolamento `READ_COMMITTED` e restrição relacional de unicidade `UNIQUE(documento_id, numero)`.
* **Padronização RESTful e RFC 7807:** Todas as respostas de erro HTTP seguem o contrato padronizado *Problem Details for HTTP APIs* (`ProblemDetail`), com `correlationId` e `timestamp`.
* **Quality Gates Arquiteturais Automatizados:** A pureza das fronteiras arquiteturais é verificada em cada build pelo **ArchUnit**.

---

## 2. Diagramas Arquiteturais (C4 Model)

### 2.1 C4 Nível 1 — Diagrama de Contexto do Sistema

```mermaid
flowchart TD
    User["Secretaria Administrativa / Auditoria\n(Usuário do Sistema)"]
    System["Sistema Arquivo Digital da ONG\n(Gestão de Documentos Oficiais, Auditoria e Versionamento)"]
    External["Conselho / Financiadores / Órgãos Fiscais\n(Inspeção de Contratos e Atas Vigentes)"]

    User -->|Cadastra, versiona, arquiva e pesquisa documentos| System
    External -->|Recebe relatórios e auditorias das versões oficiais| System
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
        API["API REST Spring Boot 3.4.x (Java 21 LTS)\nControladores REST, Casos de Uso e Domínio Limpo"]
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

## 3. Divisão de Camadas e Regras de Dependência

A arquitetura garante que as dependências fluam **exclusivamente de fora para dentro**:

```text
com.ong.backend
├── domain/                    # NÚCLEO: Pureza absoluta (Zero dependências externas)
│   ├── exception/             # Exceções de negócio (DomainException, etc.)
│   └── model/                 # Entidades, Value Objects e Enums ricos
├── application/               # CASOS DE USO: Orquestração e contratos
│   ├── port/
│   │   ├── in/                # Interfaces de Use Cases, Commands e Queries
│   │   └── out/               # Interface de persistência (DocumentoRepositoryPort)
│   └── service/               # Implementação dos Use Cases com @Transactional
├── infrastructure/            # ADAPTADORES DE SAÍDA: Tecnologias e Frameworks
│   ├── config/                # WebMvcConfigurer (CORS)
│   └── persistence/
│       ├── adapter/           # DocumentoPersistenceAdapter (implementa a porta)
│       ├── entity/            # Entidades relacionais JPA (DocumentoJpaEntity, etc.)
│       ├── mapper/            # Mapeamento bidirecional Domínio <-> Entidade JPA
│       └── repository/        # Repositórios Spring Data com JPA Specifications
└── web/                       # ADAPTADORES DE ENTRADA: Protocolo HTTP REST
    ├── controller/            # Controllers RESTful (@RestController)
    ├── dto/                   # Records de request e response imutáveis
    └── exception/             # GlobalExceptionHandler (@RestControllerAdvice com RFC 7807)
```

---

## 4. Garantia Arquitetural Automatizada (ArchUnit)

Para garantir que violações de arquitetura não passem despercebidas em revisões manuais, o projeto conta com testes de conformidade arquitetural em tempo de compilação ([`ArchitectureTest.java`](file:///C:/Users/gisel/OneDrive/Documentos/Estudos/Senac/ONG%20AD/backend/src/test/java/com/ong/backend/architecture/ArchitectureTest.java)):

1. **Regra 1 (Desacoplamento do Spring):** Nenhuma classe do pacote `..domain..` pode depender de pacotes `org.springframework..`.
2. **Regra 2 (Desacoplamento de Persistência):** Nenhuma classe do pacote `..domain..` pode depender de `jakarta.persistence..`.
3. **Regra 3 (Inversão de Dependência):** Classes do domínio não podem depender das camadas externas `..web..` ou `..infrastructure..`.

---

## 5. Modelo de Dados Relacional e Persistência

### 5.1 Diagrama Entidade-Relacionamento (ERD)

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

### 5.2 Integridade e Invariantes no Banco
* **Restrição de Unicidade Composta:** `CONSTRAINT uk_documento_versao UNIQUE (documento_id, numero)`. Garante que sob nenhuma circunstância de concorrência duas versões com o mesmo número sejam geradas para o mesmo documento.
* **Integridade Referencial:** Cascata na exclusão `ON DELETE CASCADE` da chave estrangeira.
* **Migrações Declarativas:** Executadas na inicialização via **Flyway** (`V1__criar_tabelas_documentos_e_versoes.sql`). Nenhuma alteração de schema é feita via `ddl-auto=update`.

---

## 6. Padronização de Erros REST (RFC 7807)

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
| `DocumentoNaoEncontradoException` | `404 Not Found` | Identificador do documento inexistente |
| `DocumentoArquivadoException` | `409 Conflict` | Documento imutável bloqueia nova versão (**CA3**) |
| `Exception` (inesperada) | `500 Internal Server Error` | Erros não mapeados com ocultação de stacktrace |

---

## 7. Rastreabilidade com ADRs (Architectural Decision Records)

Para o histórico completo de justificativas e alternativas consideradas nas decisões-chave, consulte:

1. [ADR 001: Persistência Relacional em PostgreSQL](adr/001-stack-e-banco-postgresql.md) — Justificativa da adoção do PostgreSQL 16 e integridade ACID frente a opções NoSQL/Blob Storage.
2. [ADR 002: Estratégia de Versionamento e Arquivamento Documental](adr/002-estrategia-versionamento-documental.md) — Decisão de modelo relacional normalizado com agregado de domínio, versionamento atômico e bloqueio via HTTP 409.

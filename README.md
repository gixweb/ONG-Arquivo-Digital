# Arquivo Digital da ONG (ONG-AD)

[![Versão](https://img.shields.io/badge/versão-v1.0.0-blue.svg)](https://github.com/gixweb/ONG-AD/releases)
[![Build & Test](https://img.shields.io/badge/testes-80%20passando%20%7C%200%20falhas-brightgreen.svg)](https://github.com/gixweb/ONG-AD/actions)
[![Cobertura JaCoCo](https://img.shields.io/badge/cobertura-%E2%89%A580%25%20linhas%20%7C%20%E2%89%A565%25%20branches-brightgreen.svg)](https://github.com/gixweb/ONG-AD)
[![Licença](https://img.shields.io/badge/licen%C3%A7a-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%20LTS-red.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

> **Desafio 14: Arquivo Digital da ONG** — Atividade Prática de Engenharia de Software, Gestão de Backlog e SDLC (Senac 2026.2, Prof. Roni Maciel).

---

## 1. Problema e Solução

### O Problema
Em organizações não governamentais, documentos regulatórios essenciais (como atas de assembleia, contratos de convênios, relatórios financeiros e ofícios institucionais) costumam ficar dispersos entre e-mails, computadores locais e mídias físicas (*pen drives*). Quando a organização é submetida a **auditorias fiscais e prestações de contas**, a equipe enfrenta enorme morosidade e incerteza para determinar qual é a versão vigente e oficial de cada documento.

### A Solução
O **Arquivo Digital da ONG** é uma aplicação web e API RESTful que centraliza o acervo documental:
* Registra metadados e garante início padronizado na versão 1 com status `vigente`.
* Controla alterações por **versionamento atômico incremental**, preservando o histórico de todas as revisões anteriores e a justificativa de alteração.
* Permite **arquivar documentos obsoletos ou finalizados**, bloqueando permanentemente qualquer tentativa de alteração ou nova versão (HTTP 409 Conflict).
* Oferece **busca multicritério** veloz por tipo de documento, período de datas e parte do título.
* Disponibiliza um **console web SPA integrado** e painel de métricas em tempo real para a secretaria da ONG.

---

## 2. Status das Funcionalidades

| Identificador | Funcionalidade | Status | Milestone |
| :--- | :--- | :--- | :--- |
| **RF01** | Registrar documento (título, tipo, data, responsável) com versão 1 e status vigente | 🟢 **Implementado** | `v0.3.0` |
| **CA1** | Rejeição de tipos não permitidos (ex: memorando) com HTTP 400 Bad Request | 🟢 **Implementado** | `v0.3.0` |
| **RF02 / CA4** | Buscar por tipo, período (`data_inicio` a `data_fim`) e parte do título | 🟢 **Implementado** | `v0.3.0` |
| **RF03 / CA2** | Registrar nova versão incrementando `versao_atual` para 2 e mantendo 2 linhas no histórico | 🟢 **Implementado** | `v0.3.0` |
| **RF04 / CA3** | Arquivar documento com bloqueio estrito contra novas versões (HTTP 409 Conflict) | 🟢 **Implementado** | `v0.3.0` |
| **RF05** | Consultar histórico completo de versões e auditoria de um documento | 🟢 **Implementado** | `v0.3.0` |
| **RF06** | Painel e endpoint de métricas consolidadas do arquivo (`GET /api/v1/metricas`) | 🟢 **Implementado** | `v1.0.0` |
| **UI** | Console Web SPA (folder-split em `frontend/`) consumindo a API REST | 🟢 **Implementado** | `v1.0.0` |
| **SDLC** | Especificação completa, 15 Issues no GitHub, Git Flow e Quality Gates | 🟢 **Implementado** | `v0.1.0` |

---

## 3. Arquitetura e Contrato REST

### 3.1 Diagrama de Contêineres (C4-Lite)

```mermaid
flowchart TD
    User["Secretaria Administrativa / Auditor"]
    
    subgraph FrontendApp ["Frontend (Client - pasta frontend/)"]
        SPA["Console Web SPA\n(HTML5 / CSS3 / Vanilla JS)"]
    end
    
    subgraph BackendApp ["Backend (Spring Boot - pasta backend/)"]
        WebLayer["Camada Web\nControllers REST & ProblemDetail (RFC 7807)"]
        AppLayer["Camada de Aplicação\nUse Cases & DTOs"]
        DomainLayer["Camada de Domínio\nEntidades e Regras de Negócio (Sem dependências)"]
        InfraLayer["Camada de Infraestrutura\nSpring Data JPA & Migrações Flyway"]
    end
    
    subgraph Database ["Persistência Relacional"]
        Postgres[("PostgreSQL 16\nTabelas: documentos e versoes")]
    end
    
    User -->|HTTP / Navegador| SPA
    SPA -->|JSON REST API| WebLayer
    WebLayer --> AppLayer
    AppLayer --> DomainLayer
    AppLayer --> InfraLayer
    InfraLayer --> Postgres
```

### 3.2 Tabela de Endpoints REST (`/api/v1`)

| Método | Endpoint | Descrição | Códigos de Retorno |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/documentos` | Cadastra novo documento na versão 1 | `201 Created`, `400 Bad Request` |
| `GET` | `/api/v1/documentos` | Lista documentos com filtros (tipo, período, título) | `200 OK` |
| `GET` | `/api/v1/documentos/{id}` | Recupera documento e versão vigente por ID | `200 OK`, `404 Not Found` |
| `POST` | `/api/v1/documentos/{id}/versoes` | Cria nova versão incremental com histórico | `200 OK`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `PATCH`| `/api/v1/documentos/{id}/arquivar`| Arquiva documento bloqueando alterações | `200 OK`, `404 Not Found` |
| `GET` | `/api/v1/documentos/{id}/versoes` | Consulta histórico completo de versões | `200 OK`, `404 Not Found` |
| `GET` | `/api/v1/metricas` | Resumo consolidado de contagens | `200 OK` |

---

## 4. Estrutura do Repositório (Folder-Split)

```text
ONG-AD/
├── .github/
│   ├── issue-templates/           # Templates de Issues para o GitHub
│   └── workflows/
│       └── ci.yml                 # Pipeline GitHub Actions (Build, Test, JaCoCo)
├── docs/                          # Documentação técnica obrigatória (pt-BR)
│   ├── project-charter.md         # Termo de Abertura e Metas SMART
│   ├── requisitos.md              # RF01-RF06, RNF01-RNF07 e Matriz de Rastreabilidade
│   ├── casos-de-uso.md            # Atores, fluxos principais, alternativos e exceções
│   ├── user-stories.md            # Histórias de Usuário BDD (Dado/Quando/Então)
│   ├── backlog-issues.ps1         # Script de automação do backlog (PowerShell)
│   ├── backlog-issues.sh          # Script de automação do backlog (Bash)
│   └── adr/                       # Registro de Decisões Arquiteturais (ADRs)
│       ├── 001-stack-e-banco-postgresql.md
│       └── 002-estrategia-versionamento-documental.md
├── backend/                       # Aplicação Spring Boot 3.4.x (Java 21)
│   ├── pom.xml                    # Configuração Maven e Quality Gates (JaCoCo, ArchUnit)
│   └── src/
│       ├── main/java/com/ong/backend/
│       │   ├── domain/            # Domínio puro (sem Spring / JPA)
│       │   ├── application/       # Portas de entrada/saída e Casos de Uso
│       │   ├── infrastructure/    # Adaptadores JPA, Flyway e Configurações CORS
│       │   └── web/               # Controllers REST e GlobalExceptionHandler (RFC 7807)
│       └── main/resources/
│           ├── db/migration/      # Migrações relacionais Flyway (V1)
│           └── application.properties
├── frontend/                      # Console Web SPA (Folder-Split)
│   ├── index.html                 # Interface administrativa da secretaria
│   ├── css/style.css              # Design responsivo e estados visuais
│   └── js/app.js                  # Integração assíncrona REST
├── CHANGELOG.md                   # Histórico de releases no padrão Keep a Changelog
├── LICENSE                        # Licença MIT
└── README.md                      # Documentação central do projeto
```

---

## 5. Documentação do Projeto (`docs/`)

Acesse a documentação formal e rastreável nos links diretos abaixo:

1. [Termo de Abertura (Project Charter)](docs/project-charter.md) — Visão, objetivos SMART, escopo e partes interessadas.
2. [Especificação de Requisitos](docs/requisitos.md) — Requisitos funcionais (RF01-RF06), não-funcionais (RNF01-RNF07) e critérios CA1 a CA4.
3. [Especificação de Casos de Uso](docs/casos-de-uso.md) — Fluxos detalhados e cenários de exceção para cada caso de uso.
4. [Histórias de Usuário (User Stories)](docs/user-stories.md) — Critérios de aceite em formato BDD/Gherkin (Dado/Quando/Então).
5. [Registro de Alterações (Changelog)](CHANGELOG.md) — Histórico detalhado de versões `v0.1.0`, `v0.2.0`, `v0.3.0` e `v1.0.0`.
6. [ADR 001: Persistência em PostgreSQL](docs/adr/001-stack-e-banco-postgresql.md) — Justificativa da stack e integridade ACID.
7. [ADR 002: Versionamento e Arquivamento](docs/adr/002-estrategia-versionamento-documental.md) — Invariantes de domínio e bloqueio 409.

---

## 6. Instruções de Configuração e Execução

### 6.1 Pré-requisitos
* **Java Development Kit (JDK):** Versão 21 ou superior instalada.
* **Maven:** Incluso via wrapper (`./mvnw` ou `mvnw.cmd`).
* **PostgreSQL:** Versão 16 (ou container Docker equivalente). Para execução rápida de testes, o perfil de teste utiliza banco H2 em memória.

### 6.2 Executando o Backend
```bash
# 1. Navegue até o diretório do backend
cd backend

# 2. Compile e execute a suíte completa de testes automatizados (80 testes)
./mvnw clean test

# 3. Inicie o servidor da aplicação
./mvnw spring-boot:run
```
A API REST estará operacional em: `http://localhost:8080`.

### 6.3 Executando o Frontend Console
Como o front-end foi construído em arquitetura estática leve (Vanilla JS/CSS3/HTML5), basta abrir o arquivo diretamente no navegador ou servi-lo localmente:
```bash
# Opção 1: Abra diretamente o index.html no navegador
# Opção 2: Ou sirva via utilitário estático simples
npx serve frontend
# ou
python -m http.server 3000 --directory frontend
```
Acesse a aplicação no navegador em `http://localhost:3000` (ou abrindo `frontend/index.html`).

---

## 7. Fluxo de Trabalho de Engenharia (Git Flow)

Adotamos o modelo de **Git Flow** com branches temáticas associadas às 15 issues do GitHub:

| Branch | Finalidade | Origem | Merge para |
| :--- | :--- | :--- | :--- |
| `main` | Código de produção, estável e com tags de versão anotadas | — | — |
| `develop` | Branch de integração de novas funcionalidades | `main` | `main` (via PR) |
| `feature/issue-XX-slug` | Implementação de funcionalidade específica | `develop` | `develop` (via PR) |
| `docs/issue-XX-slug` | Criação e manutenção de documentação | `develop` | `develop` (via PR) |
| `chore/issue-XX-slug` | Configurações, dependências e tarefas de tooling | `develop` | `develop` (via PR) |
| `release/issue-XX-slug` | Consolidação de changelog e entrega de release | `develop` | `develop` e `main` |

---

## 8. Licença
Este projeto está sob a licença [MIT](LICENSE).

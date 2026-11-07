# Índice e Governança da Documentação Técnica (`docs/`)

Este diretório centraliza os artefatos de engenharia de software do projeto **Arquivo Digital da ONG**, estruturados em conformidade com o protocolo acadêmico e as melhores práticas de SDLC.

---

## 1. Índice dos Artefatos de Engenharia

| Artefato | Arquivo | Finalidade |
| :--- | :--- | :--- |
| **Documento de Arquitetura (DAS)** | [`arquitetura.md`](arquitetura.md) | C4 Model (Contexto, Contêineres, Componentes), DER, regras ArchUnit e RFC 7807. |
| **Termo de Abertura (Project Charter)** | [`project-charter.md`](project-charter.md) | Visão, metas SMART, escopo, stakeholders e riscos preliminares. |
| **Especificação de Requisitos** | [`requisitos.md`](requisitos.md) | RF01-RF06, RNF01-RNF07, critérios CA1-CA4 e matriz de rastreabilidade até o teste. |
| **Especificação de Casos de Uso** | [`casos-de-uso.md`](casos-de-uso.md) | Atores, pré/pós-condições, fluxos principais, alternativos e de exceção. |
| **Histórias de Usuário (User Stories)** | [`user-stories.md`](user-stories.md) | US01-US06 no formato canônico com critérios de aceite BDD (Dado/Quando/Então). |
| **ADR 001** | [`adr/001-stack-e-banco-postgresql.md`](adr/001-stack-e-banco-postgresql.md) | Decisão arquitetural de adoção de Java 21, Spring Boot e PostgreSQL. |
| **ADR 002** | [`adr/002-estrategia-versionamento-documental.md`](adr/002-estrategia-versionamento-documental.md) | Decisão de versionamento incremental e imutabilidade de arquivos arquivados (409). |

---

## 2. Regra de Sincronização Obrigatória (Documentação ↔ Código)

Para evitar a degradação documental durante o ciclo de desenvolvimento:

1. **Alteração de Escopo ou Regra de Negócio:**
   * Qualquer Pull Request que adicione, modifique ou deprecie um Requisito Funcional (**RF**) ou Critério de Aceite (**CA**) deve obrigatoriamente incluir a atualização correspondente em:
     * `docs/requisitos.md` (especificação e matriz de rastreabilidade).
     * `docs/casos-de-uso.md` (fluxos operacionais).
     * `docs/user-stories.md` (critérios BDD).
2. **Alteração Arquitetural ou de Persistência:**
   * Modificações em portas, adapters ou no esquema de banco de dados devem ser refletidas em `docs/arquitetura.md` (C4 / DER) e registradas em uma nova ADR em `docs/adr/`.
3. **Revisão de PR:**
   * Nenhum Pull Request será aprovado se houver divergência entre o comportamento implementado e o declarado neste diretório.

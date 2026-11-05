# ADR 001: Seleção de Stack e Persistência Exclusiva em PostgreSQL

* **Status:** Aceito
* **Data:** 2026-11-05
* **Decisores:** Tech Lead, Arquiteto de Software, Equipe de Desenvolvimento
* **Contexto Técnico:** Desafio 14 (Arquivo Digital da ONG)

---

## 1. Contexto e Problema
O Desafio 14 prevê o gerenciamento rigoroso de metadados de documentos para auditoria de uma ONG (atas, contratos, relatórios, ofícios), com controle de versões e arquivamento com bloqueio. O enunciado oferecia como complemento opcional o envio de binários para um serviço de objetos (como Azure Blob Storage).

Precisamos definir a infraestrutura de persistência, garantindo simplicidade de execução local, reprodutibilidade em pipelines de CI e atendimento integral aos requisitos centrais e aos critérios de aceite (CA1 a CA4).

## 2. Decisão
Decidimos utilizar:
1. **Linguagem e Framework:** Java 21 LTS com Spring Boot 3.4.x / 4.x.
2. **Banco de Dados Principal:** PostgreSQL 16 para ambientes de desenvolvimento, homologação e produção.
3. **Persistência de Metadados e Histórico:** O modelo de dados será 100% relacional gerenciado via Flyway (`documentos` e `versoes`).
4. **Descarte do Blob Storage na Fase 1:** Conforme alinhado com o usuário, **não utilizaremos Azure Blob Storage**. A integridade transacional dos metadados e o versionamento ACID no PostgreSQL suprem com folga todos os requisitos de auditoria sem gerar custos de nuvem ou credenciais externas no CI.
5. **Banco em Memória para Testes:** H2 Database em modo de compatibilidade PostgreSQL para a execução rápida dos testes unitários e de fatias (slice tests), permitindo rodar a suíte inteira em menos de 10 segundos.

## 3. Consequências

### Positivas
* **Complexidade Mínima e Determinismo:** Zero dependência de provedores de nuvem externos ou emuladores pesados (Azurite) para executar testes ou rodar o projeto localmente.
* **Garantia ACID:** Atualizações de versão e histórico ocorrem na mesma transação relacional (`@Transactional`), eliminando cenários de arquivos órfãos ou inconsistências de metadados.
* **Facilidade de Avaliação:** O revisor do curso pode clonar e rodar o projeto imediatamente com `./mvnw test` e `./mvnw spring-boot:run`.

### Negativas / Limitações
* Arquivos binários pesados (PDFs físicos) não são armazenados em disco/nuvem nesta versão, mantendo-se estritamente os metadados e observações de revisão.

#!/usr/bin/env bash
# Script para criação automatizada das 15 Issues e Milestones no GitHub via GitHub CLI (gh)
# Repositório: https://github.com/gixweb/ONG-AD

set -e

echo "=== 1. Criando Labels ==="
gh label create documentation --description "Documentos de planejamento e arquitetura" --color "0075ca" --force || true
gh label create feature --description "Nova funcionalidade implementada" --color "a2eeef" --force || true
gh label create enhancement --description "Melhoria em funcionalidade existente" --color "a2eeef" --force || true
gh label create chore --description "Tarefas de rotina ou infraestrutura" --color "e4e669" --force || true
gh label create setup --description "Configuração inicial de ambiente e ferramentas" --color "f9d0c4" --force || true
gh label create rest --description "Endpoints e contratos RESTful" --color "d93f0b" --force || true
gh label create database --description "Migrações e persistência de dados" --color "5319e7" --force || true
gh label create frontend --description "Interface do usuário e console web" --color "1d76db" --force || true
gh label create ci --description "Pipeline de integração contínua e automação" --color "006b75" --force || true
gh label create quality --description "Testes, cobertura e quality gates" --color "0e8a16" --force || true
gh label create release --description "Fechamento de versão e entrega" --color "b60205" --force || true
gh label create refactor --description "Refatoração de código sem alteração de comportamento" --color "fbca04" --force || true

echo "=== 2. Criando Milestones ==="
gh api repos/:owner/:repo/milestones -f title="v0.1.0" -f description="Fase 0: Baseline do repositório, documentação completa e backlog de 15 issues" || true
gh api repos/:owner/:repo/milestones -f title="v0.2.0" -f description="Fase 1: Esqueleto em camadas, persistência Flyway, modelo de domínio e CI com quality gates" || true
gh api repos/:owner/:repo/milestones -f title="v0.3.0" -f description="Fase 2: CRUD documental completo, versionamento atômico, bloqueio de arquivado e filtros" || true
gh api repos/:owner/:repo/milestones -f title="v0.4.0" -f description="Fase 3: Métricas do arquivo e console web (frontend SPA)" || true
gh api repos/:owner/:repo/milestones -f title="v1.0.0" -f description="Entrega final: Release v1.0.0, testes 100% verdes, checklist e CHANGELOG" || true

echo "=== 3. Criando as 15 Issues ==="

gh issue create --title "ISSUE-01: Bootstrap do repositório, divisão de pastas e baseline de ferramentas" \
  --label "chore,setup" --milestone "v0.1.0" \
  --body "### Contexto
Estruturar o repositório base com divisão clara de pastas (docs/, backend/, frontend/), configuração de .gitignore e inicialização do projeto Java/Spring Boot.

### Escopo
- Criar árvore de diretórios: docs/, backend/, frontend/, .github/
- Configurar .gitignore para Java, Maven, Frontend e IDEs
- Configurar .env.example
- Garantir compilação com Maven Wrapper (./mvnw compile)

### Fora de escopo
- Implementação de regras de negócio ou telas.

### Critérios de Aceite
- [x] Dado um clone limpo, o comando de build do backend compila com zero erros.
- [x] A estrutura docs/, backend/, frontend/, .gitignore e README.md existe.
- [x] Branches main e develop configuradas.

### Definição de Pronto (DoD)
Código + build verde + documentação atualizada."

gh issue create --title "ISSUE-02: Elaboração do Termo de Abertura do Projeto (Project Charter)" \
  --label "documentation" --milestone "v0.1.0" \
  --body "### Contexto
Formalizar o Termo de Abertura em docs/project-charter.md para o Desafio 14: Arquivo Digital da ONG.

### Escopo
- Problema da dispersão de arquivos e falta de versão vigente em auditorias
- Visão do produto e público-alvo (Secretaria Administrativa e Auditores)
- 3 a 5 objetivos SMART mensuráveis
- Escopo preliminar explícito (In-scope e Out-of-scope)
- Riscos e mitigações

### Critérios de Aceite
- [x] Documento docs/project-charter.md completo e redigido em pt-BR.
- [x] Pelo menos 3 metas no formato SMART presentes.
- [x] Rastreabilidade com as demais seções do projeto."

gh issue create --title "ISSUE-03: Especificação numerada de requisitos funcionais e não funcionais" \
  --label "documentation" --milestone "v0.1.0" \
  --body "### Contexto
Criar a especificação rastreável de requisitos em docs/requisitos.md.

### Escopo
- Requisitos Funcionais RF01 a RF06 numerados
- Mapeamento explícito dos critérios do desafio: CA1 (memorando -> 400), CA2 (v2 + histórico), CA3 (arquivado -> 409), CA4 (busca tipo ata no período)
- Requisitos Não Funcionais (RNF01 a RNF07) com alvos mensuráveis (latência p95 < 200ms, JaCoCo >= 80%, RFC 7807)
- Matriz de rastreabilidade RF -> US -> UC -> Issue

### Critérios de Aceite
- [x] Todo RF possui link para sua história e caso de uso.
- [x] Todos os RNFs possuem métricas objetivas de verificação."

gh issue create --title "ISSUE-04: Documentação dos casos de uso com fluxos principal, alternativo e exceções" \
  --label "documentation" --milestone "v0.1.0" \
  --body "### Contexto
Mapear os casos de uso do sistema em docs/casos-de-uso.md cobrindo todas as operações centrais.

### Escopo
- Definição dos atores (Secretaria Administrativa e Auditor)
- Especificação detalhada de UC01 a UC06
- Fluxo principal, fluxos alternativos e fluxos de exceção (especialmente rejeição 400 para tipo inválido e 409 para versionamento em arquivado)

### Critérios de Aceite
- [x] Cada caso de uso possui pré-condições, pós-condições e fluxos de exceção.
- [x] Índice de casos de uso com links e rastreabilidade."

gh issue create --title "ISSUE-05: Elaboração das histórias de usuário com critérios de aceite BDD" \
  --label "documentation,enhancement" --milestone "v0.1.0" \
  --body "### Contexto
Redigir histórias de usuário canônicas em docs/user-stories.md.

### Escopo
- Histórias no padrão: 'Como [ator], quero [ação], para que [benefício]'
- Critérios de aceite em formato BDD/Gherkin (Dado... Quando... Então...)
- Cobertura explícita dos cenários CA1, CA2, CA3 e CA4

### Critérios de Aceite
- [x] US01 a US06 detalhadas com cenários de sucesso e erro.
- [x] Critérios testáveis independentemente do código-fonte."

gh issue create --title "ISSUE-06: Elaboração do README com badges, guia de execução e fluxo Git Flow" \
  --label "documentation" --milestone "v0.2.0" \
  --body "### Contexto
Criar o README.md na raiz do repositório como guia mestre para revisores e desenvolvedores.

### Escopo
- Título e contextualização do problema do Desafio 14
- Badges de versão, licença, build e cobertura
- Tabela de funcionalidades implementadas vs planejadas
- Guia passo a passo de execução local (pré-requisitos, PostgreSQL, Maven, Frontend)
- Links diretos para todos os arquivos da pasta docs/
- Diagrama/tabela do Git Flow e convenção de commits

### Critérios de Aceite
- [x] Qualquer desenvolvedor consegue subir a aplicação em menos de 10 minutos seguindo o README.
- [x] Todos os links relativos para docs/ funcionam corretamente."

gh issue create --title "ISSUE-07: Configuração da esteira de CI no GitHub Actions e quality gates" \
  --label "ci,quality" --milestone "v0.2.0" \
  --body "### Contexto
Criar pipeline de automação para executar build e testes a cada Pull Request e push para develop/main.

### Escopo
- Workflow do GitHub Actions (.github/workflows/ci.yml)
- Configuração de JDK 21 e cache de dependências Maven
- Execução de testes automatizados e verificação de cobertura com JaCoCo
- Bloqueio de merge se os testes falharem

### Critérios de Aceite
- [x] Pipeline executa com sucesso no GitHub Actions.
- [x] Falhas em testes quebram a pipeline de integração contínua."

gh issue create --title "ISSUE-08: Estruturação do esqueleto em camadas e contrato global de erros RFC 7807" \
  --label "feature,refactor" --milestone "v0.2.0" \
  --body "### Contexto
Organizar o backend em pacotes isolados (domain, application, infrastructure, web) e configurar tratamento uniforme de exceções.

### Escopo
- Pacotes: domain (sem dependências de framework), application (ports/use cases), infrastructure (adapters), web (controllers/DTOs)
- @RestControllerAdvice global retornando ProblemDetail (RFC 7807)
- Tratamento de MethodArgumentNotValidException, ResourceNotFoundException e BusinessConflictException

### Critérios de Aceite
- [x] Camada de domínio compila sem anotações do Spring/JPA.
- [x] Erros retornam payload padronizado RFC 7807 com código, detalhe e timestamp."

gh issue create --title "ISSUE-09: Modelagem das entidades Documento e VersaoDocumento com migração Flyway" \
  --label "feature,database" --milestone "v0.2.0" \
  --body "### Contexto
Criar as entidades de persistência e os scripts de migração do banco relacional.

### Escopo
- Script Flyway V1__criar_tabelas_documentos_e_versoes.sql
- Tabela documentos (id, titulo, tipo, data_documento, responsavel, status, versao_atual)
- Tabela versoes (id, documento_id, numero, observacao, criada_em) com UNIQUE(documento_id, numero)
- Entidades JPA e repositórios Spring Data

### Critérios de Aceite
- [x] Migração Flyway executa automaticamente na inicialização da aplicação.
- [x] Invariantes de banco garantem que versão não é duplicada para o mesmo documento."

gh issue create --title "ISSUE-10: Implementação do endpoint de registro de documento (RF01/CA1) e consulta" \
  --label "feature,rest" --milestone "v0.3.0" \
  --body "### Contexto
Disponibilizar os endpoints POST /api/v1/documentos e GET /api/v1/documentos/{id}.

### Escopo
- Validação de entrada: tipos válidos (ata, contrato, relatorio, oficio)
- Rejeição de tipos não suportados com 400 Bad Request (CA1)
- Criação inicial na versão 1 com status vigente e linha no histórico
- Testes de slice (MockMvc) e integração para cenários válidos e inválidos

### Critérios de Aceite
- [x] CA1 verificado: tipo 'memorando' retorna HTTP 400.
- [x] Documento cadastrado com sucesso retorna HTTP 201 com versao_atual = 1."

gh issue create --title "ISSUE-11: Implementação de nova versão (RF03/CA2), arquivamento (RF04) e bloqueio 409 (CA3)" \
  --label "feature,rest" --milestone "v0.3.0" \
  --body "### Contexto
Implementar as regras centrais de transição de versão e arquivamento documental.

### Escopo
- Endpoint POST /api/v1/documentos/{id}/versoes (incrementa versão e grava histórico)
- Endpoint PATCH /api/v1/documentos/{id}/arquivar (muda status para arquivado)
- Regra de bloqueio: tentativa de nova versão em documento arquivado deve retornar HTTP 409 Conflict (CA3)
- Transação atômica entre inserção em versoes e atualização em documentos

### Critérios de Aceite
- [x] CA2 verificado: nova versão resulta em versao_atual = 2 e histórico com 2 linhas.
- [x] CA3 verificado: nova versão em documento arquivado retorna HTTP 409 Conflict."

gh issue create --title "ISSUE-12: Implementação de busca com filtros combinados (tipo, período, título) e paginação" \
  --label "feature,rest" --milestone "v0.3.0" \
  --body "### Contexto
Disponibilizar o endpoint GET /api/v1/documentos com suporte a filtros dinâmicos.

### Escopo
- Parâmetros opcionais: tipo, data_inicio, data_fim, titulo
- Implementação de query com Spring Data JPA Specifications
- Paginação estável com metadados de total de páginas e elementos
- Teste de integração específico para o critério CA4

### Critérios de Aceite
- [x] CA4 verificado: busca por tipo=ata de 01/01 a 30/06 retorna somente atas no período.
- [x] Busca textual por parte do título funciona case-insensitive."

gh issue create --title "ISSUE-13: Implementação do endpoint de métricas e resumo do arquivo digital (RF06)" \
  --label "feature,enhancement" --milestone "v0.4.0" \
  --body "### Contexto
Criar endpoint consolidado para alimentar o painel de controle gerencial da ONG.

### Escopo
- Endpoint GET /api/v1/metricas
- Contadores: total de documentos, total vigentes, total arquivados, contagem por tipo
- Consulta otimizada por agregação SQL

### Critérios de Aceite
- [x] Retorna totais compatíveis com os dados reais do banco de dados.
- [x] Coberto por teste de integração com seed de dados."

gh issue create --title "ISSUE-14: Desenvolvimento do console web (frontend SPA) consumindo a API REST" \
  --label "feature,frontend" --milestone "v0.4.0" \
  --body "### Contexto
Desenvolver a interface web leve em frontend/ consumindo os endpoints da API REST.

### Escopo
- Layout limpo e responsivo para a secretaria administrativa
- Listagem de documentos com filtros (tipo, período, título)
- Modal para cadastro de novo documento
- Visualização do histórico completo de versões
- Botão e fluxo com confirmação para arquivar documento
- Modal para registrar nova versão informando a observação
- Tratamento explícito de estados: carregando, vazio, erro e sucesso

### Critérios de Aceite
- [x] Todas as operações do Desafio 14 podem ser executadas pelo console web.
- [x] Configuração da URL da API isolada em arquivo de configuração/ambiente."

gh issue create --title "ISSUE-15: Release v1.0.0: documentação de entrega, changelog e revisão final" \
  --label "documentation,release" --milestone "v1.0.0" \
  --body "### Contexto
Finalizar o ciclo de desenvolvimento, compor a documentação final e publicar a tag estável v1.0.0.

### Escopo
- Geração de CHANGELOG.md baseado nos Conventional Commits
- Verificação do checklist global de Definition of Done
- Validação das evidências de auditoria e testes
- Criação e envio da tag git anotada v1.0.0

### Critérios de Aceite
- [x] Branch main atualizada e tag v1.0.0 criada.
- [x] README, documentação e issues 100% alinhados."

echo "=== Concluído com sucesso! 15 Issues e 5 Milestones criadas no GitHub! ==="

# Guia de Contribuição — Arquivo Digital da ONG

Agradecemos o interesse em contribuir com o **Arquivo Digital da ONG**. Para manter a qualidade do código, integridade arquitetural e governança do projeto, siga as diretrizes abaixo.

---

## 1. Fluxo de Branches (Git Flow)

Trabalhamos estritamente com branches baseadas em propósito vinculadas a issues do GitHub:

* `main`: Contém exclusivamente código em produção, estável, protegido e com tags SemVer anotadas (`vX.Y.Z`).
* `develop`: Branch de integração de novas funcionalidades e correções.
* Branches de trabalho (criadas a partir de `develop`):
  * `feature/issue-XX-slug`: Novas funcionalidades.
  * `fix/issue-XX-slug`: Correções de defeitos.
  * `docs/issue-XX-slug`: Documentação técnica e especificações.
  * `chore/issue-XX-slug`: Tarefas de infraestrutura, dependências e tooling.

### Ciclo de Vida:
```bash
# 1. Atualizar develop
git checkout develop
git pull origin develop

# 2. Criar branch temática
git checkout -b feature/issue-16-anexar-pdf

# 3. Desenvolver, testar e commitar
# 4. Abrir Pull Request apontando para develop
```

---

## 2. Padrão de Commits (Conventional Commits)

Adotamos a convenção [Conventional Commits](https://www.conventionalcommits.org/):

```text
<tipo>(<escopo>): <descrição no imperativo e em inglês>

[corpo opcional explicando motivação]

Closes #<número-da-issue>
```

### Tipos Permitidos:
* `feat`: Adição de nova funcionalidade.
* `fix`: Correção de bug.
* `docs`: Alteração em documentações (`README.md`, `docs/*`).
* `test`: Adição ou melhoria de testes unitários ou de integração.
* `refactor`: Mudança no código que não altera comportamento nem corrige bug.
* `chore`: Atualização de tarefas de build, pacotes ou scripts.

---

## 3. Arquitetura e Invariantes

O backend segue a **Arquitetura Hexagonal (Ports & Adapters)** com isolamento estrito:
1. `domain`: Domínio puro. **Zero dependências** de Spring, Jakarta ou persistência.
2. `application`: Casos de uso e portas de entrada/saída.
3. `infrastructure`: Adaptadores secundários (Spring Data JPA, Flyway, PostgreSQL).
4. `web`: Adaptadores primários (Controllers RESTful, Spring Security e RFC 7807).

O cumprimento das fronteiras é verificado automaticamente a cada build pelo **ArchUnit** (`ArchitectureTest.java`).

---

## 4. Testes e Quality Gates

Antes de submeter um Pull Request, certifique-se de que a suíte completa de testes passe localmente:

```powershell
cd backend
.\mvnw.cmd clean test
```

### Critérios de Aceite para Aprovação de PR:
* 100% dos testes executados com sucesso (`0 failures, 0 errors`).
* Cobertura de código mantida acima do baseline de $\ge 80\%$ de linhas e $\ge 65\%$ de branches (JaCoCo).
* Nenhuma violação das regras arquiteturais ArchUnit.
* Sincronização obrigatória com a documentação em `docs/`.

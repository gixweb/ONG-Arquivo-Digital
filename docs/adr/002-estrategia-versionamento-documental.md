# ADR 002: Estratégia de Versionamento Atômico e Bloqueio de Arquivamento

* **Status:** Aceito
* **Data:** 2026-11-05
* **Decisores:** Tech Lead, Engenheiro de Software
* **Contexto Técnico:** Regras Centrais RF3 (Nova Versão) e RF4 (Arquivar Documento) do Desafio 14

---

## 1. Contexto e Problema
O Desafio 14 estabelece duas regras centrais de negócio:
1. Uma nova versão deve incrementar o número da versão (`versao_atual`), mantendo o histórico de todas as anteriores com observação explicativa (**CA2**).
2. Um documento com status `arquivado` não pode receber novas versões sob nenhuma hipótese, devendo resultar em HTTP `409 Conflict` (**CA3**).

Precisávamos decidir como modelar as tabelas e o fluxo de dados para garantir que nunca ocorra sobrescrita acidental de versões ou criação de versões em documentos arquivados.

## 2. Decisão

1. **Separação em Duas Tabelas Relacionadas:**
   * Tabela `documentos`: guarda os dados do documento e o ponteiro da versão vigente (`id`, `titulo`, `tipo`, `data_documento`, `responsavel`, `status`, `versao_atual`).
   * Tabela `versoes`: armazena cada registro imutável do histórico (`id`, `documento_id`, `numero`, `observacao`, `criada_em`).
2. **Restrição de Unicidade Composta:**
   * `UNIQUE (documento_id, numero)` na tabela `versoes`, impedindo duplicidade no nível de banco de dados.
3. **Invariante no Domínio (DDD-lite):**
   * A entidade `Documento` possui o método de negócio `registrarNovaVersao(String observacao)`.
   * Se `this.status == DocumentoStatus.ARQUIVADO`, lança imediatamente `DocumentoArquivadoException` (mapeada para HTTP 409).
   * Incrementa `this.versaoAtual++` e cria um objeto `VersaoDocumento` vinculado.
4. **Transacionalidade na Camada de Aplicação:**
   * O caso de uso executa com `@Transactional(isolation = Isolation.READ_COMMITTED)`, garantindo atomicidade: ou a nova linha é persistida em `versoes` e o documento atualizado, ou tudo sofre rollback.

## 3. Consequências

### Positivas
* **Imutabilidade e Auditoria:** O histórico nunca é sobrescrito; cada revisão é um registro permanente com timestamp e justificativa.
* **Resiliência a Concorrência:** A constraint de unicidade no banco atua como última barreira defensiva caso duas requisições simultâneas tentem versionar o mesmo documento.
* **Aderência aos Critérios:** Atende diretamente aos critérios CA2 e CA3 com validações de domínio claras e mensagens de erro RFC 7807 explicativas.

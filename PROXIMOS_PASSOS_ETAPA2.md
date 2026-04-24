# 🎯 Próximos Passos — Etapa 2 (Núcleo Pro Refinado)

## ✅ Etapa 1 Concluída

**Data:** 23 de Abril, 2026  
**Versão:** 2.4.0-BETA  
**Status:** ✅ PRONTO PARA ETAPA 2

---

## 📋 Resumo Rápido do Que Você Tem

### Arquivos Criados
- ✅ **11 classes Java** em `br.com.expertdev.pro.*`
- ✅ **4 documentos markdown** (entrega, usuário, checklist, resumo)
- ✅ **1 pom.xml atualizado** para versão 2.4.0-BETA
- ✅ **1 ExpertDevGUI.java modificado** com aba Pro integrada

### Funcionalidades Ativas
- ✅ **Botão "Abrir na IDE"** — abre arquivo na linha exata
- ✅ **Botão "Copiar Prompt"** — gera e copia prompt estruturado
- ✅ **Botão "Aplicar Checklist"** — cria checklist por categoria

---

## 🚀 Etapa 2 — Núcleo Pro Refinado

### Objetivos
1. **Persistência** — Salvar histórico de prompts em BD SQLite
2. **Adapters** — Criar wrappers para serviços antigos
3. **Prompts Especializados** — Templates mais detalhados por tipo
4. **Mais Checklists** — Expandir para mais categorias

### Entregável Esperado
- **Versão:** `2.4.1-BETA`
- **Novos recursos:** Histórico persistente, prompts avançados
- **Quebra esperada:** NENHUMA (backwards compatible)

---

## 📝 Tarefas da Etapa 2 (Ordem Recomendada)

### 1️⃣ Persistência em BD (Prioridade ALTA)

**O que fazer:**
- Criar tabelas em `expertdev.db`:
  - `pro_prompt_history` (id, arquivo, linha, prompt, data_criacao, tipo)
  - `pro_checklist_history` (id, categoria, score, items, data_avaliacao)
- Criar classe `ProHistoryService` para gerenciar BD
- Integrar em `ProAssistantPanel` (salvar automaticamente)

**Arquivo a criar:**
```
src/main/java/br/com/expertdev/pro/service/ProHistoryService.java
```

**Recursos SQLite:**
```sql
CREATE TABLE pro_prompt_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    arquivo_alvo TEXT NOT NULL,
    linha_alvo INTEGER,
    prompt_gerado TEXT,
    tipo_prompt TEXT,
    timestamp_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pro_checklist_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    categoria TEXT NOT NULL,
    score_percentual INTEGER,
    items_json TEXT,
    timestamp_avaliacao DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

### 2️⃣ Adapters para Serviços Antigos (Prioridade ALTA)

**O que fazer:**
- Criar `PromptGenerationServiceAdapter` (wrapper para `PromptGenerationService` legado)
- Criar `AuthServiceAdapter` (wrapper para `AuthService`)
- Criar `AuditoriaServiceAdapter` (wrapper para `AuditoriaService`)
- Permitir que Pro reutilize serviços existentes

**Padrão:**
```java
public class PromptGenerationServiceAdapter implements IPromptServiceV2 {
    private PromptGenerationService legado;
    
    public PromptGenerationServiceAdapter(PromptGenerationService legado) {
        this.legado = legado;
    }
    
    @Override
    public PromptBundle gerarComContextoEstendido(...) {
        // wrapper que chama o serviço legado
    }
}
```

**Arquivos a criar:**
```
src/main/java/br/com/expertdev/pro/adapter/PromptGenerationServiceAdapter.java
src/main/java/br/com/expertdev/pro/adapter/AuthServiceAdapter.java
src/main/java/br/com/expertdev/pro/adapter/AuditoriaServiceAdapter.java
```

---

### 3️⃣ Templates de Prompts Especializados (Prioridade MÉDIA)

**O que fazer:**
- Expandir `PromptContextService` com templates mais detalhados
- Adicionar `gerarPromptDatabaseError()`, `gerarPromptPerformance()`, etc.
- Cada template inclui: contexto, histórico similar, best practices

**Exemplo:**
```java
public PromptBundle gerarPromptDatabaseError(IssueContext contexto) {
    StringBuilder sb = new StringBuilder();
    sb.append("# Bug de Database\n\n");
    sb.append("## Contexto\n");
    sb.append(contexto.getDescricaoProblema()).append("\n\n");
    sb.append("## Stack Trace\n");
    // ... mais detalhes
    return new PromptBundle(sb.toString(), contexto);
}
```

**Métodos a adicionar:**
```
gerarPromptDatabaseError()
gerarPromptPerformance()
gerarPromptSecurity()
gerarPromptTestCoverage()
gerarPromptRefactoring()
```

---

### 4️⃣ Expandir Checklists (Prioridade MÉDIA)

**O que fazer:**
- Adicionar mais templates em `ChecklistService`
- Templates: Database, Performance, Security, Tests, Refactoring
- Cada template com 5-8 items

**Exemplo:**
```java
public ChecklistResult criarChecklistDatabase() {
    ChecklistResult result = new ChecklistResult("Database");
    result.adicionarItem(new ChecklistItem(
        "Query otimizada",
        "SQL não faz N+1 queries",
        true
    ));
    // ... mais items
    return result;
}
```

---

## 🔧 Como Começar Etapa 2

### Passo 1: Criar Estrutura
```bash
# Criar pastas de pacotes (se não existir)
mkdir -p src/main/java/br/com/expertdev/pro/service
mkdir -p src/main/java/br/com/expertdev/pro/adapter
```

### Passo 2: Implementar ProHistoryService
```bash
# Editar arquivo
vim src/main/java/br/com/expertdev/pro/service/ProHistoryService.java
```

### Passo 3: Testar Compilação
```bash
mvn clean compile -DskipTests
```

### Passo 4: Integrar em ProAssistantPanel
```bash
# Modificar ProAssistantPanel para usar ProHistoryService
vim src/main/java/br/com/expertdev/pro/ui/ProAssistantPanel.java
```

### Passo 5: Validar
```bash
mvn package -DskipTests
```

---

## 📊 Estimativa de Esforço (Etapa 2)

| Tarefa | Estimativa | Prioridade |
|--------|-----------|-----------|
| 1. Persistência BD | 2-3 horas | 🔴 ALTA |
| 2. Adapters | 2-3 horas | 🔴 ALTA |
| 3. Templates | 1-2 horas | 🟡 MÉDIA |
| 4. Checklists | 1-2 horas | 🟡 MÉDIA |
| **Total** | **~6-10 horas** | |

**Timeline recomendado:** 1-2 dias de trabalho focado

---

## 🎯 Critério de Aceite (Etapa 2)

- [ ] Persistência de prompts em BD SQLite funcionando
- [ ] Adapters para serviços antigos criados
- [ ] Templates especializados implementados
- [ ] Novos checklists funcionando
- [ ] Compilação sem erros
- [ ] Backwards compatible com Etapa 1
- [ ] Documentação atualizada
- [ ] Testes manuais passam

---

## 📚 Referências Úteis

### Serviu que Precisa Chamar
- `AuthService` — `src/main/java/br/com/expertdev/service/AuthService.java`
- `AuditoriaService` — `src/main/java/br/com/expertdev/service/AuditoriaService.java`
- `PromptGenerationService` — `src/main/java/br/com/expertdev/service/PromptGenerationService.java`

### Banco de Dados
- Usando: **SQLite** via `sqlite-jdbc`
- BD: `expertdev.db` (raiz do projeto)
- Pool de conexão: Não necessário para Pro (uso light)

### Padrões Usados Etapa 1
- **Factory Pattern:** `IdeBridgeFactory`
- **Strategy Pattern:** `ChecklistService`
- **Adapter Pattern:** (para criar em Etapa 2)
- **Observer Pattern:** `SwingWorker` para threading

---

## 💡 Dicas

1. **BD**: Use `try-with-resources` para statements SQL
2. **Histórico**: Salve automaticamente após gerar prompt
3. **Adapters**: Reutilize padrões do código existente
4. **Testes**: Use `mvn test` após implementação
5. **Docs**: Atualize `MODULO_PRO_USUARIO.md` com novos recursos

---

## ❓ Dúvidas Frequentes (Antecipadas)

**P: Devo quebrar a compatibilidade?**  
R: Não! Etapa 2 deve ser 100% backwards compatible com Etapa 1.

**P: Usar Spring ou JPA?**  
R: Não! Manter simplicidade com JDBC puro (projeto não usa frameworks pesados).

**P: Fazer testes unitários?**  
R: Recomendado para Etapa 2 (MockDatabase, etc). Etapa 7 tem teste automatizado completo.

**P: Qual ordem devo implementar?**  
R: 1 → 2 → 3 → 4 (prioridade + dependências).

---

## 🎊 Pronto?

Se respondeu SIM a tudo abaixo, **inicie Etapa 2**:

- ✅ Leu `RESUMO_ETAPA1_EXECUTIVO.md`?
- ✅ Entende a arquitetura Pro criada?
- ✅ Tem SQLite pronto?
- ✅ Entende padrões (Factory, Adapter)?

**Se SIM → Vamos para Etapa 2!** 🚀

---

**Data:** 23 de Abril, 2026  
**Versão:** 2.4.0-BETA (Etapa 1)  
**Próxima:** 2.4.1-BETA (Etapa 2)



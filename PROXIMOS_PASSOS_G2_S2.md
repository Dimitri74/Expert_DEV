# 🎯 Próximos Passos — Sprint G2-S2 (Deduplicação + Grafo de Dependências)

## ✅ G2-S1 Concluída

**Data:** 23 de Abril, 2026  
**Versão:** 2.4.0-BETA  
**Status:** ✅ CONSOLIDAÇÃO BÁSICA PRONTA

---

## 📋 Resumo Rápido do Que Você Tem (Após G2-S1)

### Arquivos Criados
- ✅ **3 modelos** em `br.com.expertdev.gid.model.*`
  - `IBMRelacaoDependencia`
  - `IBMConsolidacaoDependencias`
  - `IBMDeduplicacaoArtefato`

- ✅ **3 serviços** em `br.com.expertdev.gid.service.*`
  - `IBMG2ConsolidationService` (consolidação por RTC)
  - `IBMG2OrchestrationService` (orquestração B3→B4→G2)
  - `IBMG2Probe` (validação)

### Funcionalidades Ativas
- ✅ **Agrupamento por RTC** — Consolida múltiplos artefatos por RTC
- ✅ **Mapa de Dependências Básico** — Relações sequenciais entre tipos
- ✅ **Avisos e Estatísticas** — Acompanha execução e erros
- ✅ **Pipeline Completo** — B3→B4→G2 em uma chamada

---

## 🚀 Sprint G2-S2 — Deduplicação + Mapa Visual de Dependências

### Objetivos

1. **Deduplicação** — Remover regras/URLs/mensagens duplicadas
2. **Grafo de Dependências** — Construir DAG visual (UC→DI→API)
3. **Detecção de Ciclos** — Alertar referências circulares
4. **Relatório de Consolidação** — Sumário de deduplicações

### Entregável Esperado
- **Versão:** `2.4.1-BETA`
- **Novos recursos:** Deduplicação automática, grafo visual, detecção de ciclos
- **Quebra esperada:** NENHUMA (adiciona nova funcionalidade, não altera B4)

---

## 📝 Tarefas da Sprint G2-S2 (Ordem Recomendada)

### 1️⃣ Criar Estratégia de Deduplicação (Prioridade ALTA)

**O que fazer:**
- Criar interface `IBMDeduplicatorStrategy`
- Implementar `IBMRegraNegocioDeduplicator` (hash MD5 + Levenshtein)
- Implementar `IBMURLDeduplicador` (normalização + comparação)
- Implementar `IBMMensagemDeduplicador` (comparação semântica)
- Criar `IBMDeduplicatorFactory`

**Padrão:**
```java
public interface IBMDeduplicatorStrategy {
    List<IBMDeduplicacaoArtefato> deduplicar(IBMContextoRTC contexto);
}

public class IBMRegraNegocioDeduplicator implements IBMDeduplicatorStrategy {
    @Override
    public List<IBMDeduplicacaoArtefato> deduplicar(IBMContextoRTC contexto) {
        // Lógica de deduplicação
        List<IBMDeduplicacaoArtefato> resultados = new ArrayList<>();
        // ... implementar
        return resultados;
    }
}
```

**Arquivos a criar:**
```
src/main/java/br/com/expertdev/gid/service/deduplicator/
├─ IBMDeduplicatorStrategy.java (novo)
├─ IBMRegraNegocioDeduplicator.java (novo)
├─ IBMURLDeduplicador.java (novo)
├─ IBMMensagemDeduplicador.java (novo)
└─ IBMDeduplicatorFactory.java (novo)
```

---

### 2️⃣ Integrar Deduplicação em G2 (Prioridade ALTA)

**O que fazer:**
- Adicionar método em `IBMG2ConsolidationService`
- Chamar deduplicadores após consolidação
- Rastrear estatísticas de deduplicação
- Gerar avisos de duplicatas removidas

**Exemplo:**
```java
public class IBMG2ConsolidationService {
    private final IBMDeduplicatorFactory deduplicatorFactory = new IBMDeduplicatorFactory();
    
    public Map<String, IBMDeduplicacaoArtefato> executarDeduplicacao(String rtc) {
        IBMContextoRTC contexto = contextosPorRTC.get(rtc);
        Map<String, IBMDeduplicacaoArtefato> resultado = new HashMap<>();
        
        // Deduplicar por tipo
        IBMDeduplicatorStrategy dedup = deduplicatorFactory.criar("REGRA");
        resultado.put("REGRA", dedup.deduplicar(contexto).get(0));
        // ... para cada tipo
        
        return resultado;
    }
}
```

**Método a adicionar:**
```
executarDeduplicacao(String rtc)
```

---

### 3️⃣ Construir Mapa de Dependências (Grafo) (Prioridade MÉDIA)

**O que fazer:**
- Criar `IBMDependencyGraphBuilder`
- Construir DAG (Directed Acyclic Graph)
- Detectar ciclos
- Gerar representação visual (JSON com nós e arestas)

**Exemplo estrutura de saída:**
```json
{
  "nodes": [
    {"id": "UC_001", "label": "Autenticar", "type": "INTEGRACAO_UC"},
    {"id": "DI_001", "label": "Login", "type": "INTEGRACAO_DI"},
    {"id": "API_001", "label": "POST /auth", "type": "API_QUARKUS"}
  ],
  "edges": [
    {"source": "UC_001", "target": "DI_001", "label": "implementa"},
    {"source": "DI_001", "target": "API_001", "label": "consome"}
  ],
  "ciclos": []
}
```

**Arquivos a criar:**
```
src/main/java/br/com/expertdev/gid/service/graph/
├─ IBMDependencyGraphBuilder.java (novo)
├─ IBMDependencyNode.java (novo)
└─ IBMDependencyEdge.java (novo)
```

---

### 4️⃣ Detectar e Alertar Ciclos (Prioridade MÉDIA)

**O que fazer:**
- Implementar algoritmo de detecção de ciclos (DFS)
- Adicionar avisos para ciclos detectados
- Incluir informação em relatório de consolidação

**Exemplo:**
```java
private void detectarCiclos(IBMConsolidacaoDependencias deps) {
    // DFS para encontrar ciclos
    for (IBMRelacaoDependencia aresta : deps.getRelacoes()) {
        if (temCicloPartindoDe(aresta.getCodigoDestino(), aresta)) {
            deps.adicionarAvisoCiclo("CICLO DETECTADO: " + aresta);
        }
    }
}
```

---

## 🔧 Como Começar Sprint G2-S2

### Passo 1: Criar Estrutura de Deduplicadores
```bash
mkdir -p src/main/java/br/com/expertdev/gid/service/deduplicator
mkdir -p src/main/java/br/com/expertdev/gid/service/graph
```

### Passo 2: Implementar Estratégia Base
```bash
# Editar arquivos
vim src/main/java/br/com/expertdev/gid/service/deduplicator/IBMDeduplicatorStrategy.java
```

### Passo 3: Implementar Deduplicadores Concretos
```bash
# 1. Regras
vim src/main/java/br/com/expertdev/gid/service/deduplicator/IBMRegraNegocioDeduplicator.java
# 2. URLs
vim src/main/java/br/com/expertdev/gid/service/deduplicator/IBMURLDeduplicador.java
# 3. Mensagens
vim src/main/java/br/com/expertdev/gid/service/deduplicator/IBMMensagemDeduplicador.java
```

### Passo 4: Testar Compilação
```bash
mvn clean compile -DskipTests
```

### Passo 5: Integrar e Validar
```bash
# Modificar IBMG2ConsolidationService
vim src/main/java/br/com/expertdev/gid/service/IBMG2ConsolidationService.java
```

### Passo 6: Criar Probe Expandido
```bash
vim src/main/java/br/com/expertdev/gid/service/IBMG2DeduplicationProbe.java
```

---

## 📊 Estimativa de Esforço (Sprint G2-S2)

| Tarefa | Estimativa | Prioridade |
|--------|-----------|-----------|
| 1. Estratégia de Deduplicação | 4-5 horas | 🔴 ALTA |
| 2. Integração em G2 | 2-3 horas | 🔴 ALTA |
| 3. Grafo de Dependências | 4-5 horas | 🟡 MÉDIA |
| 4. Detecção de Ciclos | 2-3 horas | 🟡 MÉDIA |
| 5. Testes e Probe | 2-3 horas | 🟡 MÉDIA |
| **Total** | **~14-19 horas** | |

**Timeline recomendado:** 2-3 dias de desenvolvimento

---

## 🎯 Critério de Aceite (Sprint G2-S2)

- [ ] `IBMDeduplicatorStrategy` e implementações criadas
- [ ] Deduplicação funcionando para regras, URLs, mensagens
- [ ] Integrada em `IBMG2ConsolidationService`
- [ ] Grafo DAG construído corretamente
- [ ] Ciclos detectados e alertados
- [ ] Probe `IBMG2DeduplicationProbe` funcional
- [ ] Compilação sem erros
- [ ] Teste manual com arquivos reais
- [ ] Documentação atualizada

---

## 📚 Referências Úteis

### Padrões Usados
- **Strategy Pattern:** `IBMDeduplicatorStrategy`
- **Factory Pattern:** `IBMDeduplicatorFactory`
- **Observer Pattern:** Avisos de consolidação

### Algoritmos
- **Detecção de Duplicatas:** Hash MD5 + Levenshtein (dist. ≤ 0.2)
- **Detecção de Ciclos:** DFS (Depth First Search)
- **Normalização:** Trim, lowercase, remover pontuação

### Versões
- **Jackson:** 2.17.2 (para gerar JSON)
- **Apache Commons:** Lang 3.x (para StringUtils)

---

## 💡 Dicas

1. **Deduplicação**: Use hash para comparação rápida, Levenshtein para fuzzy match
2. **Grafo**: Mantenha Node IDs simples (RTC:TIPO) para rastreabilidade
3. **Ciclos**: Alertar sem falhar (ciclos podem existir em fluxos reais)
4. **Teste**: Use arquivo com múltiplos RTCs e tipos variados
5. **Docs**: Mantenha arquivo `g2-consolidacao-hierarquica.md` atualizado

---

## ❓ Dúvidas Antecipadas

**P: Qual nível de deduplicação usar?**  
R: Recomendado: Hash exato (100% confiança) + fuzzy (70-90% confiança com revisão).

**P: Como representar ciclos no grafo?**  
R: Marcar arestas com ciclo identificado, não remover (fluxos reais podem ter ciclos).

**P: Integrar no GUI logo?**  
R: Não em G2-S2. G2-S3+ adicionam exportadores que poderão ser integrados à UI.

---

## 🎊 Pronto para Sprint G2-S2?

Se respondeu SIM a tudo abaixo, **inicie G2-S2**:

- ✅ Entende estrutura de deduplicadores (Strategy)?
- ✅ Conhece DFS para ciclos?
- ✅ Tem arquivo `g2-consolidacao-hierarquica.md` como referência?
- ✅ Compilação G2-S1 funcionando (`mvn clean compile`)?

**Se SIM → Vamos para Sprint G2-S2!** 🚀

---

**Data:** 23 de Abril, 2026  
**Versão:** 2.4.0-BETA (Etapa G2-S1)  
**Próxima:** 2.4.1-BETA (Etapa G2-S2)


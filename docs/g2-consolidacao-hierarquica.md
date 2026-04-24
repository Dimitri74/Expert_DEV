# G2 - Consolidação Hierárquica de Artefatos IBM

## Escopo Implementado (Sprint G2-S1)

A Etapa G2 consolida artefatos extraídos pela Etapa B4, agrupando-os por RTC e construindo um mapa de dependências entre tipos de documentos.

### Componentes Criados

- **Modelos** (`gid/model/`):
  - `IBMRelacaoDependencia` — Representa uma relação entre dois artefatos
  - `IBMConsolidacaoDependencias` — Consolida todas as relações para um RTC
  - `IBMDeduplicacaoArtefato` — Rastreia deduplicações realizadas

- **Serviços** (`gid/service/`):
  - `IBMG2ConsolidationService` — Consolida artefatos por RTC
  - `IBMG2OrchestrationService` — Orquestra pipeline B3→B4→G2
  - `IBMG2Probe` — Probe para validação

### Pipeline Completo B2→B3→B4→G2

```
Entrada (Word .doc/.docx)
          ↓
      B2: Leitura
          ↓
    B3: Classificação (tipo IBM)
          ↓
    B4: Extração Semântica (RTC, UC, rules, APIs...)
          ↓
    G2: Consolidação por RTC + Mapa de Dependências
          ↓
   Saída: Contextos consolidados por RTC + Relações
```

## Como Validar com Probe

```powershell
mvn -f "C:\Users\marcu\workspace\Pessoal\expertDev\pom.xml" -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMG2Probe" "-Dexec.args=C:\caminho\doc1.doc C:\caminho\doc2.doc C:\caminho\doc3.doc"
```

### Saída Esperada

Para cada arquivo:
- RTC detectado
- UC associada (se houver)
- Quantidade de artefatos consolidados
- Relações de dependência entre tipos
- Confiança da extração
- Avisos de processamento

### Exemplo de Resultado

```
================================================================================
RESULTADO DA ETAPA G2 - CONSOLIDAÇÃO
================================================================================

📊 ESTATÍSTICAS GERAIS:
  RTCs Consolidados: 2
  Artefatos Extraídos: 2
  Total Relações: 0
  Avisos: 1

📋 RTCS CONSOLIDADOS:

  ▶ RTC: 24824194
    └─ Artefatos: 1
    └─ Relações: 0

  ▶ RTC: 21275172
    └─ Artefatos: 1
    └─ Relações: 0

⚠️  AVISOS:
  • INFO: Consolidação concluída. 2 RTC(s) agrupados de 2 artefatos.
  • INFO: G2 concluído - 2 RTC(s) consolidado(s).
  • INFO: Pipeline completo finalizado em 1.19 segundos.

📄 ARTEFATOS EXTRAÍDOS (2):
  1. Tipo: API_QUARKUS | RTC: 21275172 | Confiança: 76%
  2. Tipo: CANAIS_UC | RTC: 24824194 | Confiança: 70%
```

## Arquitetura

### IBMG2ConsolidationService

Responsável por agrupar artefatos por RTC e construir o mapa de dependências.

**Métodos principais:**
- `consolidarPorRTC(List<IBMArtefatoExtracao>)` — Agrupa por RTC e retorna contextos
- `obterContextoRTC(String rtc)` — Obtém contexto de um RTC
- `obterDependenciasRTC(String rtc)` — Obtém relações de um RTC
- `obterEstatisticas()` — Retorna estatísticas da consolidação
- `obterAvisos()` — Lista avisos gerados

### IBMG2OrchestrationService

Orquestra o pipeline completo (B3→B4→G2) com interface unificada.

**Métodos principais:**
- `processarCompleto(List<File> arquivos)` — Executa pipeline completo B3+B4+G2
- `processarExtracacaoConsolidacao(List<File> arquivos)` — Executa apenas B4+G2
- `consolidarArtefatos(List<IBMArtefatoExtracao>)` — Executa apenas G2
- `obterUltimaConsolidacao()` — Acessa resultado
- `listarRTCsConsolidados()` — Lista RTCs processados

## Próximos Passos (G2-S2 e além)

### Sprint G2-S2: Deduplicação + Mapa de Dependências
- Implementar `IBMDeduplicatorStrategy` com detecção de duplicatas
- Construir grafo visual de dependências (UC→DI→API)
- Detecção de ciclos em referências cruzadas

### Sprint G2-S3: Exportadores JSON/XLSX
- `IBMJsonReportExporter` — JSON estruturado com dados consolidados
- `IBMXlsxReportExporter` — Planilha Excel com abas por RTC

### Sprint G2-S4: Exportadores HTML/PDF
- `IBMHtmlReportExporter` — Relatório interativo com D3.js/Mermaid
- `IBMPdfReportExporter` — Relatório profissional com gráficos

### Sprint G2-S5: Testes e Documentação
- Testes unitários para consolidação
- Testes de integração B4→G2
- Documentação completa e guias de uso

## Critério de Aceite (G2-S1) ✅

- [x] Modelos criados e compilam sem erro
- [x] Serviço de consolidação funcionando
- [x] Agrupamento por RTC funcional
- [x] Mapa básico de dependências criado
- [x] Probe validando pipeline B3→B4→G2
- [x] Avisos e estatísticas funcionando
- [x] Build sem erros: `mvn clean compile`

## Versão

- **Expert Dev:** 2.4.0-BETA
- **Etapa:** G2-S1 (Sprint 1 de 5)
- **Data:** 23 de Abril de 2026
- **Status:** ✅ CONCLUÍDO E VALIDADO


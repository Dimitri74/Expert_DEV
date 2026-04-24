# 🚀 Quick Reference — G2 Consolidação

## Execução Rápida

### Rodar Probe G2
```powershell
mvn -DskipTests exec:java \
  -Dexec.mainClass=br.com.expertdev.gid.service.IBMG2Probe \
  -Dexec.args="C:\arquivo1.doc C:\arquivo2.doc C:\arquivo3.doc"
```

### Compilar
```powershell
mvn clean compile -DskipTests     # Apenas compile
mvn clean package -DskipTests     # Gerar JAR
```

---

## Classes Principais

### `IBMG2OrchestrationService` (Interface Principal)
```java
// Criar orquestrador
IBMG2OrchestrationService orq = new IBMG2OrchestrationService();

// Pipeline completo B3+B4+G2
Map<String, IBMContextoRTC> resultado = orq.processarCompleto(arquivos);

// Apenas B4+G2
Map<String, IBMContextoRTC> resultado = orq.processarExtracacaoConsolidacao(arquivos);

// Apenas G2 (com artefatos já extraídos)
Map<String, IBMContextoRTC> resultado = orq.consolidarArtefatos(artefatos);

// Acessar dados
Set<String> rtcs = orq.listarRTCsConsolidados();
IBMContextoRTC ctx = orq.obterUltimaConsolidacao().get(rtc);
IBMConsolidacaoDependencias deps = orq.obterDependenciasRTC(rtc);
Map<String, Object> stats = orq.obterEstatisticas();
List<String> avisos = orq.obterAvisos();
```

### `IBMG2ConsolidationService` (Serviço Base)
```java
// Consolidação direta
IBMG2ConsolidationService svc = new IBMG2ConsolidationService();
Map<String, IBMContextoRTC> contextos = svc.consolidarPorRTC(artefatos);

// Acessar resultados
IBMContextoRTC ctx = svc.obterContextoRTC(rtc);
IBMConsolidacaoDependencias deps = svc.obterDependenciasRTC(rtc);
Map<String, Object> stats = svc.obterEstatisticas();
```

### `IBMContextoRTC` (Resultado da Consolidação)
```java
IBMContextoRTC contexto = consolidacao.get(rtc);

// Informações
String rtc = contexto.getRtcNumero();
String uc = contexto.getUcCodigo();
List<IBMArtefatoExtracao> artefatos = contexto.getArtefatosComplementares();
LocalDateTime data = contexto.getDataConsolidacao();

// Dados específicos por tipo
IBMExtracaoMensagem msgs = contexto.getExtracaoMensagem();
IBMExtracaoAPI apis = contexto.getExtracaoAPI();
IBMExtracaoCanalUC canais = contexto.getExtracaoCanalUC();
// ... outros
```

### `IBMConsolidacaoDependencias` (Relações)
```java
IBMConsolidacaoDependencias deps = orq.obterDependenciasRTC(rtc);

// Informações gerais
int total = deps.getTotalRelacoes();
Map<String, Integer> contadores = deps.getContadorPorTipo();
List<String> avisos = deps.getAvisos();
List<String> avisosCiclos = deps.getAvisosCiclos();

// Relações
List<IBMRelacaoDependencia> relacoes = deps.getRelacoes();
List<IBMRelacaoDependencia> origem = deps.obterRelacoesOrigem(codigo);
List<IBMRelacaoDependencia> destino = deps.obterRelacoesDestino(codigo);

// Estatísticas
int relacoesDoTipo = deps.contarRelacoesDoTipo("implementa");
boolean temCiclos = deps.temCiclos();
```

### `IBMRelacaoDependencia` (Uma Relação)
```java
for (IBMRelacaoDependencia rel : deps.getRelacoes()) {
    String origem = rel.getCodigoOrigem();        // "RTC:21275172:API_QUARKUS"
    String tipoOrigem = rel.getTipoOrigem();      // "API_QUARKUS"
    String destino = rel.getCodigoDestino();      // "RTC:21275172:ESPECIFICACAO"
    String tipoDestino = rel.getTipoDestino();    // "ESPECIFICACAO_SUPLEMENTAR"
    String relacao = rel.getTipoRelacao();        // "documenta", "implementa", "depende_de", etc
    double confianca = rel.getConfianca();        // 80.0 até 100.0
    String descricao = rel.getDescricao();
}
```

---

## Estrutura de Dados

### Entrada: Arquivo Word
```
arquivo.doc / arquivo.docx
   ↓ (B2 - Leitura)
IBMArtefatoExtracao {
  tipoArtefato: API_QUARKUS,
  rtcNumero: "21275172",
  ucCodigo: "UC_AUTO",
  nomeArquivoOrigem: "arquivo.doc",
  confiancaDeteccao: 76
}
```

### Saída: Contexto Consolidado
```
IBMContextoRTC {
  rtcNumero: "21275172",
  ucCodigo: "UC_AUTO",
  artefatosComplementares: [
    IBMArtefatoExtracao {...},
    IBMArtefatoExtracao {...}
  ],
  dataConsolidacao: 2026-04-23T19:34:00
}

+ 

IBMConsolidacaoDependencias {
  rtcNumero: "21275172",
  totalRelacoes: 3,
  relacoes: [
    IBMRelacaoDependencia { origem: "API_...", destino: "ESPECIFICACAO_...", tipo: "documenta" },
    ...
  ],
  avisos: [...]
}
```

---

## Tipo de Relações Possíveis

| Tipo | Significado | Exemplo |
|------|------------|---------|
| `implementa` | A → B implementa A | UC → Integração |
| `refina` | A → B refina A | INTEGRACAO_UC → INTEGRACAO_DI |
| `documenta` | A → B documenta A | API → ESPECIFICACAO |
| `consome` | A → B consome A | INTEGRACAO_DI → API |
| `envia` | A → B envia | CANAIS_UC → MSG_SISTEMA |
| `depende_de` | A depende de B | API → ESPECIFICACAO |
| `notifica` | A notifica B | MSG_SISTEMA → CANAL |
| `relacionada` | Relação genérica | Qualquer tipo → qualquer tipo |

---

## Tipos de Artefatos (Enum)

```java
enum IBMTipoArtefato {
    MSG_SISTEMA,
    INTEGRACAO_UC,
    INTEGRACAO_DI,
    API_QUARKUS,
    CANAIS_UC,
    ESPECIFICACAO_SUPLEMENTAR,
    DESCONHECIDO
}
```

---

## Exemplo Completo

```java
// 1. Processar documentos
IBMG2OrchestrationService orq = new IBMG2OrchestrationService();
List<File> arquivos = Arrays.asList(
    new File("SIPCS_API_Quarkus.doc"),
    new File("SIPCS_Canais_UC.doc")
);
Map<String, IBMContextoRTC> consolidacao = orq.processarCompleto(arquivos);

// 2. Listar RTCs encontrados
System.out.println("RTCs: " + orq.listarRTCsConsolidados());
// RTCs: [21275172, 24824194]

// 3. Acessar contexto de um RTC
IBMContextoRTC ctx = consolidacao.get("21275172");
System.out.println("UC: " + ctx.getUcCodigo());
System.out.println("Artefatos: " + ctx.getArtefatosComplementares().size());

// 4. Acessar dependências
IBMConsolidacaoDependencias deps = orq.obterDependenciasRTC("21275172");
System.out.println("Relações: " + deps.getTotalRelacoes());
for (IBMRelacaoDependencia rel : deps.getRelacoes()) {
    System.out.println("  " + rel.toString());
}

// 5. Estatísticas
Map<String, Object> stats = orq.obterEstatisticas();
System.out.println("RTCs: " + stats.get("rtcsConsolidados"));
System.out.println("Artefatos: " + stats.get("totalArtefatos"));
System.out.println("Relações: " + stats.get("totalRelacoes"));

// 6. Avisos
for (String aviso : orq.obterAvisos()) {
    System.out.println("⚠️ " + aviso);
}
```

---

## Próximas Fases

### Sprint G2-S2: Deduplicação + Grafo
- `IBMDeduplicatorStrategy` (interface)
- `IBMRegraNegocioDeduplicator` (remove regras duplicadas)
- `IBMURLDeduplicador` (normaliza URLs)
- `IBMDependencyGraphBuilder` (grafo visual)

### Sprint G2-S3: Exportadores
- `IBMJsonReportExporter` (→ JSON)
- `IBMXlsxReportExporter` (→ Excel)

### Sprint G2-S4: Relatórios
- `IBMHtmlReportExporter` (→ HTML interativo)
- `IBMPdfReportExporter` (→ PDF profissional)

---

## Debug

### Verificar avisos
```java
List<String> avisos = orq.obterAvisos();
if (!avisos.isEmpty()) {
    System.out.println("Avisos encontrados:");
    avisos.forEach(a -> System.out.println("  • " + a));
}
```

### Verificar ciclos
```java
IBMConsolidacaoDependencias deps = orq.obterDependenciasRTC(rtc);
if (deps.temCiclos()) {
    System.out.println("⚠️ Ciclos detectados:");
    deps.getAvisosCiclos().forEach(System.out::println);
}
```

### Listar todas as relações
```java
IBMConsolidacaoDependencias deps = orq.obterDependenciasRTC(rtc);
System.out.println("Total relações: " + deps.getTotalRelacoes());
for (IBMRelacaoDependencia rel : deps.getRelacoes()) {
    System.out.println(
        String.format("%s --[%s]--> %s (%.0f%%)",
            rel.getTipoOrigem(),
            rel.getTipoRelacao(),
            rel.getTipoDestino(),
            rel.getConfianca())
    );
}
```

---

## Localização dos Arquivos

```
src/main/java/br/com/expertdev/gid/
├── model/
│   ├── IBMRelacaoDependencia.java
│   ├── IBMConsolidacaoDependencias.java
│   ├── IBMDeduplicacaoArtefato.java
│   └── ... (outros modelos)
└── service/
    ├── IBMG2ConsolidationService.java
    ├── IBMG2OrchestrationService.java
    ├── IBMG2Probe.java
    └── ... (outros serviços)

docs/
├── g2-consolidacao-hierarquica.md
└── ...

ENTREGA_G2_S1.md
PROXIMOS_PASSOS_G2_S2.md
QUICK_REFERENCE_G2.md (este arquivo)
```

---

**Sprint:** G2-S1 ✅  
**Versão:** 2.4.0-BETA  
**Última atualização:** 23 de Abril, 2026


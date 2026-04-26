<div style="text-align: center;">
  <img src="src/main/resources/icons/logo_transparente.png" alt="Expert Dev" style="width: 168px;">
</div>

<h1 style="text-align: center;">Expert Dev</h1>

<div style="text-align: center;">
  Aplicação desktop Java 8 para extrair contexto de URLs e documentos Word, consolidar regras e referências visuais e gerar artefatos prontos para uso com assistentes de código.
</div>

<div style="text-align: center;">
  <img alt="Java 8" src="https://img.shields.io/badge/Java-8-007396?style=flat-square&logo=openjdk&logoColor=white">
  <img alt="Desktop" src="https://img.shields.io/badge/App-Desktop-4C1D95?style=flat-square">
  <img alt="Swing" src="https://img.shields.io/badge/UI-Swing-0F766E?style=flat-square">
  <img alt="Maven" src="https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white">
  <img alt="SQLite" src="https://img.shields.io/badge/Cache-SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white">
</div>

> **Visão geral**
> O Expert Dev centraliza extração de contexto, leitura de documentos Word, consolidação de regras e geração de artefatos para acelerar o fluxo com assistentes de código em ambiente local ou com IA.

## Navegação rápida

| Visão geral | Stack | Fluxo | Execução | Configuração | Apoio |
| --- | --- | --- | --- | --- | --- |
| [Documentação oficial](#documentação-oficial) | [Stack principal](#stack-principal) | [Estrutura funcional](#estrutura-funcional) | [Como executar](#como-executar) | [Configuração](#configuração) | [Compatibilidade](#compatibilidade) |
| [O que o projeto entrega](#o-que-o-projeto-entrega) | [Módulos importantes](#módulos-importantes) | [Execuções úteis](#execuções-úteis) | [Ambiente corporativo restrito](#ambiente-corporativo-restrito) | [Situação atual da documentação](#situação-atual-da-documentação) | [Quickstart](quickstart.md) |

## Documentação oficial

| Documento | Finalidade |
| --- | --- |
| `README.md` | Visão geral do produto, arquitetura, build e operação |
| `quickstart.md` | Passo a passo curto para rodar e usar a aplicação |
| `DOCUMENTACAO_CONSOLIDADA.md` | Resumos e histórico dos demais documentos do repositório |

## O que o projeto entrega

### Capacidades principais

- extração de contexto a partir de URLs
- upload de arquivos Word `.doc` e `.docx`
- consolidação de texto, imagens e regras
- geração de `prompt_para_junie_copilot.txt`
- geração de `regras_extraidas.txt` e `imagens_encontradas.txt`
- exportação para `contexto_com_imagens.docx` e `contexto_com_imagens.pdf`
- cache local em SQLite
- modo local ou IA com fallback automático
- módulo `Performance & ROI`
- aba `Assistente Pro`
- pipeline IBM/GID com leitura Word, classificação, extração semântica e consolidação por RTC

## Stack principal

| Tecnologia | Versão | Uso no projeto |
| --- | --- | --- |
| ![Java](https://img.shields.io/badge/Java-8-007396?style=flat-square&logo=openjdk&logoColor=white) | 8 | Base da aplicação desktop e dos serviços |
| ![Swing](https://img.shields.io/badge/Java-Swing-007396?style=flat-square&logo=openjdk&logoColor=white) | JDK 8 | Interface gráfica principal |
| ![Maven](https://img.shields.io/badge/Build-Apache%20Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white) | 3.x | Build, empacotamento e plugins |
| ![Jsoup](https://img.shields.io/badge/Parser-jsoup-2E7D32?style=flat-square) | 1.15.3 | Parsing HTML |
| ![Apache POI](https://img.shields.io/badge/Docs-Apache%20POI-D22128?style=flat-square&logo=apache&logoColor=white) | 4.1.2 | Leitura e geração de arquivos Word |
| ![PDFBox](https://img.shields.io/badge/PDF-Apache%20PDFBox-B71C1C?style=flat-square&logo=apache&logoColor=white) | 2.0.27 | Geração e leitura de PDF |
| ![SQLite JDBC](https://img.shields.io/badge/DB-SQLite%20JDBC-003B57?style=flat-square&logo=sqlite&logoColor=white) | 3.45.1.0 | Persistência local e cache |
| ![Jackson](https://img.shields.io/badge/JSON-Jackson-000000?style=flat-square) | 2.17.2 | Integrações JSON |
| ![JFreeChart](https://img.shields.io/badge/Charts-JFreeChart-1565C0?style=flat-square) | 1.0.19 | Dashboard e gráficos de ROI |

## Estrutura funcional

| Entradas | Saídas |
| --- | --- |
| - URLs individuais ou múltiplas<br>- Word `.docx`<br>- Word legado `.doc`, com conversão via LibreOffice quando disponível e fallback de leitura direta quando necessário | 1. `regras_extraidas.txt`<br>2. `imagens_encontradas.txt`<br>3. `prompt_para_junie_copilot.txt`<br>4. `contexto_com_imagens.docx`<br>5. `contexto_com_imagens.pdf`<br>6. `resumo_execucao.txt`<br>7. `erros_processamento.txt` (quando houver falhas)<br>8. relatórios de ROI exportados em PDF, quando aplicável |

## Módulos importantes

| Módulo | Papel no produto |
| --- | --- |
| **Núcleo principal** | Responsável pela extração, limpeza, consolidação de contexto e geração de prompt |
| **Upload Word** | Suporta múltiplos arquivos, faz leitura de texto e imagens, extrai texto de parágrafos e tabelas e usa fallback para `.doc` legado |
| **Assistente Pro** | Fornece apoio ao fluxo de desenvolvimento com abertura de arquivo/linha na IDE, geração de prompt estruturado, checklists por categoria e histórico do fluxo Pro |
| **Performance & ROI** | Permite comparar estimativa tradicional x tempo real com a ferramenta e exportar relatórios executivos |
| **Pipeline IBM / GID** | Fluxo especializado para documentos IBM com `B2` leitura de Word, `B3` classificação e filtro de ruído, `B4` extração semântica por tipo e `G2` consolidação por RTC e dependências |

## Como executar

> Escolha o modo de uso conforme o contexto: IDE para desenvolvimento, terminal para execução direta do pacote e Maven para build.

### Pela IDE
1. Abra o projeto no IntelliJ.
2. Execute a classe `ExpertDev`.
3. Use a GUI para URLs, upload Word ou módulos auxiliares.

### Pelo terminal
```powershell
java -jar target/expert-dev-2.5.0-BETA.jar
```

### Build
```powershell
mvn clean compile
mvn clean package -DskipTests
```

### Gerar versão distribuída e zipada
Para montar a versão portátil de distribuição para Windows, com pasta staged e arquivo `.zip`, use o script de release do projeto:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1
```

Saídas esperadas:
- `target\release\ExpertDev-<versao>-win64`
- `target\release\ExpertDev-<versao>-win64-portable.zip`

Se precisar informar uma runtime específica ou incluir também o JAR no pacote staged:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1 -RuntimePath "C:\caminho\jre8" -IncludeJar
```

## Execuções úteis

Comandos de apoio para validação dos fluxos Word e probes do pipeline IBM/GID.

### Reempacotar release sem novo build
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1 -SkipBuild
```

### Validar ingestão Word
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\validate-word-b23.ps1 -DocxPath "C:\caminho\arquivo.docx" -DocPath "C:\caminho\arquivo.doc"
```

### Rodar probe B3
```powershell
mvn -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMB3Probe" "-Dexec.args=C:\caminho\arquivo1.doc C:\caminho\arquivo2.docx"
```

### Rodar probe B4
```powershell
mvn -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMB4Probe" "-Dexec.args=C:\caminho\doc1.doc C:\caminho\doc2.doc"
```

### Rodar probe G2
```powershell
mvn -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMG2Probe" "-Dexec.args=C:\arquivo1.doc C:\arquivo2.doc C:\arquivo3.doc"
```

## Configuração

Arquivo principal: `expertdev.properties`

| Configurações comuns |
| --- |
| timeout e limites de texto |
| tema da UI |
| modo de geração de prompt |
| parâmetros de IA |
| conversão de `.doc` |
| autenticação local |

### API Key por variável de ambiente
```powershell
$env:EXPERTDEV_AI_API_KEY="sua_chave_aqui"
```

## Ambiente corporativo restrito

Build usando repositório corporativo:
```powershell
mvn -Dcorp.offline=true clean test
```

Build usando fallback local em `lib/`:
```powershell
mvn -Dcorp.offline=true -Dcorp.lib.fallback=true clean test
```

Detalhes operacionais e governança de JARs foram consolidados em `DOCUMENTACAO_CONSOLIDADA.md`.

## Compatibilidade

> **Compatibilidade conhecida**
>
> - desenvolvimento preferencial com Java 8
> - em JDK 25+, o SQLite JDBC pode exigir:

```text
--enable-native-access=ALL-UNNAMED
```

## Situação atual da documentação

A documentação histórica de etapas, sprints, checklists, quick references e notas técnicas foi condensada em `DOCUMENTACAO_CONSOLIDADA.md`.

### Se você está começando agora

1. leia este `README.md`
2. siga `quickstart.md`
3. consulte `DOCUMENTACAO_CONSOLIDADA.md` apenas para contexto histórico, técnico ou gerencial mais detalhado

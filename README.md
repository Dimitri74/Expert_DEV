# Expert Dev

Aplicação desktop Java 8 para extrair contexto de URLs e documentos Word, consolidar regras e referências visuais e gerar artefatos prontos para uso com assistentes de código.

## Documentação oficial

- `README.md` — visão geral do produto, arquitetura, build e operação.
- `quickstart.md` — passo a passo curto para rodar e usar a aplicação.
- `DOCUMENTACAO_CONSOLIDADA.md` — resumos e histórico dos demais documentos do repositório.

## O que o projeto entrega

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
| Java | 8 | Base da aplicação desktop e dos serviços |
| Swing | JDK 8 | Interface gráfica principal |
| Maven | 3.x | Build, empacotamento e plugins |
| Jsoup | 1.15.3 | Parsing HTML |
| Apache POI | 4.1.2 | Leitura e geração de arquivos Word |
| PDFBox | 2.0.27 | Geração e leitura de PDF |
| SQLite JDBC | 3.45.1.0 | Persistência local e cache |
| Jackson | 2.17.2 | Integrações JSON |
| JFreeChart | 1.0.19 | Dashboard e gráficos de ROI |

## Estrutura funcional

### Entradas
- URLs individuais ou múltiplas
- Word `.docx`
- Word legado `.doc`, com conversão via LibreOffice quando disponível e fallback de leitura direta quando necessário

### Saídas
1. `regras_extraidas.txt`
2. `imagens_encontradas.txt`
3. `prompt_para_junie_copilot.txt`
4. `contexto_com_imagens.docx`
5. `contexto_com_imagens.pdf`
6. `resumo_execucao.txt`
7. `erros_processamento.txt` (quando houver falhas)
8. relatórios de ROI exportados em PDF, quando aplicável

## Módulos importantes

### Núcleo principal
Responsável pela extração, limpeza, consolidação de contexto e geração de prompt.

### Upload Word
- suporta múltiplos arquivos
- faz leitura de texto e imagens
- extrai texto de parágrafos e tabelas
- usa fallback para `.doc` legado

### Assistente Pro
Fornece apoio ao fluxo de desenvolvimento com:
- abertura de arquivo/linha na IDE
- geração de prompt estruturado
- checklists por categoria
- histórico e evolução do fluxo Pro

### Performance & ROI
Permite comparar estimativa tradicional x tempo real com a ferramenta e exportar relatórios executivos.

### Pipeline IBM / GID
Fluxo especializado para documentos IBM:
- `B2`: leitura de Word
- `B3`: classificação e filtro de ruído
- `B4`: extração semântica por tipo
- `G2`: consolidação por RTC e dependências

## Como executar

### Pela IDE
1. Abra o projeto no IntelliJ.
2. Execute a classe `ExpertDev`.
3. Use a GUI para URLs, upload Word ou módulos auxiliares.

### Pelo terminal
```powershell
java -jar target/expert-dev-2.4.0-BETA.jar
```

### Build
```powershell
mvn clean compile
mvn clean package -DskipTests
```

## Execuções úteis

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

Configurações comuns:
- timeout e limites de texto
- tema da UI
- modo de geração de prompt
- parâmetros de IA
- conversão de `.doc`
- autenticação local

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

- desenvolvimento preferencial com Java 8
- em JDK 25+, o SQLite JDBC pode exigir:

```text
--enable-native-access=ALL-UNNAMED
```

## Situação atual da documentação

A documentação histórica de etapas, sprints, checklists, quick references e notas técnicas foi condensada em `DOCUMENTACAO_CONSOLIDADA.md`.

Se você está começando agora:
1. leia este `README.md`
2. siga `quickstart.md`
3. consulte `DOCUMENTACAO_CONSOLIDADA.md` apenas para contexto histórico, técnico ou gerencial mais detalhado

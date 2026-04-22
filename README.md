# Expert Dev

Extrai regras de negócio e referências visuais de páginas web, consolida o conteúdo e gera múltiplos formatos de saída (TXT, DOCX, PDF) otimizados para uso com assistentes de código. Possui módulo integrado de medição de ROI e performance.

## Stack tecnológica

| Tecnologia | Versão | O que é | O que faz no projeto |
| --- | --- | --- | --- |
| ![Java](https://img.shields.io/badge/Java-8-007396?style=flat-square&logo=openjdk&logoColor=white) | 8 | Linguagem principal da aplicação. | Implementa a lógica de extração, consolidação de contexto, geração de artefatos e integração da UI. |
| ![Swing](https://img.shields.io/badge/Java-Swing-007396?style=flat-square&logo=openjdk&logoColor=white) | JDK 8 | Toolkit de interface gráfica do Java. | Renderiza a interface desktop, entradas de URL/configuração e painel de execução. |
| ![Maven](https://img.shields.io/badge/Build-Apache%20Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white) | Ambiente (3.x) | Ferramenta de build e gerenciamento de dependências. | Compila, empacota JARs, executa plugins (Shade/Launch4j) e padroniza o ciclo de build. |
| ![Jsoup](https://img.shields.io/badge/Parser-jsoup-2E7D32?style=flat-square) | 1.15.3 | Biblioteca de parsing HTML. | Faz o fetch/parse das páginas web e extrai texto e referências de imagens. |
| ![Apache POI](https://img.shields.io/badge/Docs-Apache%20POI-D22128?style=flat-square&logo=apache&logoColor=white) | 4.1.2 | Biblioteca para documentos Office. | Gera o `contexto_com_imagens.docx` com conteúdo consolidado e imagens inline. |
| ![PDFBox](https://img.shields.io/badge/PDF-Apache%20PDFBox-B71C1C?style=flat-square&logo=apache&logoColor=white) | 2.0.27 | Biblioteca para manipulação de PDF. | Gera o `contexto_com_imagens.pdf` e apoia a exportação de saídas em PDF. |
| ![SQLite JDBC](https://img.shields.io/badge/DB-SQLite%20JDBC-003B57?style=flat-square&logo=sqlite&logoColor=white) | 3.45.1.0 | Driver JDBC para banco SQLite embarcado. | Mantém cache de contexto e dados de execução para acelerar reprocessamentos e reduzir custos. |
| ![JFreeChart](https://img.shields.io/badge/Charts-JFreeChart-1565C0?style=flat-square) | 1.0.19 | Biblioteca de gráficos em Java. | Exibe o dashboard de tendências e comparativos do módulo de Performance & ROI. |
| ![Jackson](https://img.shields.io/badge/JSON-Jackson-000000?style=flat-square) | 2.17.2 | Biblioteca de serialização/desserialização JSON. | Constrói e interpreta payloads de integração com provedores de IA (OpenAI/Claude). |

## Versão atual: Expert Dev 2.2.3-BETA

- **Cache de Contexto**: Armazenamento local (SQLite) de resultados de processamento para evitar re-download e economizar tokens.
- **Automação de Workflow**: Detecção inteligente de links no clipboard (Jira/RTC) e notificações no System Tray.
- **Layout Light por padrão**: A interface agora inicia sempre com o tema claro (light), otimizada para visibilidade em ambientes corporativos.
- **Módulo de Performance & ROI**: compara o tempo de desenvolvimento entre o rito Scrum tradicional e o uso do ExpertDev.
- **Dashboard de Tendências**: visualização gráfica da curva de aprendizado e ganho de velocidade (Productivity Gain).
- **Relatório Executivo**: exportação em PDF consolidando a economia de tempo e ROI da Sprint para apresentações de revisão.
- **Paralelismo**: processa múltiplas URLs simultaneamente (usa cores disponíveis no processador).
- **Múltiplos formatos**: texto, Word com imagens embarcadas, PDF com imagens embarcadas.
- **Geração de prompt por IA (opcional)**: modo local ou modo IA com API Key.
- **Fallback automático**: se a IA falhar, o sistema volta para geração local.
- **Modo econômico de IA**: limita contexto enviado e reduz tokens para baixar custo.
- **Providers flexíveis**: pronto para OpenAI e Claude/Anthropic.
- **Base unificada de prompt**: o refinamento e o contrato do prompt são compartilhados entre entradas locais (URL/Word) e geração via API.
- **Perfis de prompt**: alternância entre `tecnico` e `executivo`.
- **Estimativa visual IA**: exibe estimativa de tokens e custo aproximado na UI.
- **Java 8**: totalmente compatível

## Saída gerada

1. `regras_extraidas.txt` — conteúdo bruto consolidado
2. `imagens_encontradas.txt` — URLs das imagens detectadas
3. `prompt_para_junie_copilot.txt` — prompt pronto para IA
4. `contexto_com_imagens.docx` — **documento Word com imagens inline**
5. `contexto_com_imagens.pdf` — **documento PDF com imagens inline**
6. `RELATORIO_ROI_EXPORTADO.pdf` — **Relatório Executivo de Performance & ROI** (quando exportado)
7. `resumo_execucao.txt` — métricas e dados de execução
8. `erros_processamento.txt` — detalhes de URLs que falharam (quando houver)

## Módulo de Performance & ROI

Este módulo permite mensurar o valor real que a ferramenta agrega ao seu workflow de desenvolvimento, agora com automações inteligentes.

### Automações (Workflow) & Otimização
- **Cache de Contexto**: Se você processar uma URL que já foi analisada anteriormente, o ExpertDev recupera o texto e as imagens instantaneamente do banco de dados local (SQLite). Isso acelera o trabalho e economiza tokens se você estiver usando IA.
- **Captura via Clipboard**: Ao copiar um link ou ID de tarefa (Jira ou RTC), o ExpertDev identifica automaticamente o número e o título, preenchendo os campos de auditoria para você.
- **Notificações de Sistema**: Um ícone no System Tray monitora o tempo de desenvolvimento. Se você exceder a estimativa definida no Scrum Poker, o sistema emitirá um alerta visual para lembrá-lo de revisar ou finalizar a tarefa.

### Fluxo de Trabalho
1. **Planejamento**: No início da sprint, informe o RTC e a estimativa definida no Scrum Poker (em horas ou pontos).
2. **Execução ExpertDev**: Ao processar a tarefa no sistema, o tempo de início do ExpertDev é registrado automaticamente.
3. **Fechamento Sensibilizado**: Só finalize a tarefa no ExpertDev (botão "Finalizar (Dev + Teste)") quando concluir o desenvolvimento E os testes.
4. **Comparação**: O sistema calcula a diferença entre o tempo real gasto no rito tradicional (estimado via Poker) e o tempo com a ferramenta.

### Visualizações
- **Gráfico de Barras**: Comparativo direto entre Estimativa Poker, Tempo Real Scrum e Tempo ExpertDev para cada tarefa.
- **Gráfico de Linha (Tendências)**: Mostra a evolução do ganho de produtividade ao longo do tempo.
- **Botão Exportar ROI**: Gera um PDF profissional com os dados consolidados, ideal para apresentar em Sprint Reviews.

## Como usar

### 1. Usando o JAR Executável (Recomendado)
Para facilitar o uso sem necessidade de uma IDE, agora o Expert Dev pode ser executado diretamente como um arquivo JAR.

1. Baixe ou gere o arquivo `expert-dev-2.1.0.jar` (localizado na pasta `target/`).
2. **Duplo clique**: Em sistemas configurados, basta dar um duplo clique no arquivo `.jar`. O sistema iniciará com a logo oficial como ícone da janela.
3. **Via Terminal/PowerShell**:
   ```powershell
   java -jar expert-dev-2.1.0.jar
   ```

### 2. Execução via IDE (Desenvolvimento)
1. Abra o projeto no IntelliJ Ultimate.
2. Execute a classe `ExpertDev` (ou use `run.bat`).
3. Cole as URLs do site quando solicitado, **separadas por vírgula**.

Exemplo:
```
Cole as URL(s) do site (separadas por vírgula): https://example.com, https://www.site.com
```

### Geração do JAR (Build)
Para gerar o JAR executável com todas as dependências embutidas (Fat JAR):
```powershell
mvn clean package -DskipTests
```
O arquivo será gerado em `target/expert-dev-2.1.0.jar`.

### Pacote oficial de release (Windows)

Para distribuição a terceiros, use o pacote portátil com `ExpertDev.exe` e runtime embarcado.

1. Garanta que existe uma runtime em `./jre8` (ou informe outro caminho no comando).
2. Gere o release oficial:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1
```

Artefatos gerados:
- `target/release/ExpertDev-<versao>-win64/`
- `target/release/ExpertDev-<versao>-win64-portable.zip`

Se sua runtime não estiver em `./jre8`, informe o caminho:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1 -RuntimePath "C:\Runtime\jre8"
```

### Execução com JDK 25+ (SQLite)

Ao usar JDK 25+ pode aparecer aviso de acesso nativo do SQLite JDBC.
Para evitar o warning no IntelliJ, adicione em **Run Configuration > VM options**:

```text
--enable-native-access=ALL-UNNAMED
```


Para desenvolvimento padrão do projeto, prefira JDK 8.
No `run.bat`, se `JAVA8_HOME` estiver definido, ele será usado automaticamente.

## Processamento paralelo

A VR1.5 usa automaticamente:
```
número de threads = cores disponíveis - 1
```

Isso acelera o processamento de múltiplas URLs sem comprometer a estabilidade.

## Configuração opcional

## Operacao em ambiente corporativo restrito

- Guia operacional: `README_CORP_OFFLINE.md`
- Checklist de governanca de JARs: `docs/checklist-atualizacao-jars.md`

Build corporativo restrito (sem fallback local):

```powershell
mvn -Dcorp.offline=true clean test
```

Build corporativo com fallback local (`lib/`) habilitado:

```powershell
mvn -Dcorp.offline=true -Dcorp.lib.fallback=true clean test
```

Crie um arquivo `expertdev.properties` na raiz do projeto para sobrescrever padrões:

```properties
timeout.ms=45000
texto.limite=150000
output.summary.file=meu_resumo.txt

# Tema UI
ui.theme.light.default=true

# Modo de geração de prompt
ui.generation.mode=LOCAL

# IA opcional (Expert Dev 2.0)
ai.enabled=false
ai.provider=openai
ai.endpoint=https://api.openai.com/v1/chat/completions
ai.model=gpt-4o-mini
ai.timeout.ms=30000
ai.max.tokens=700
ai.max.context.chars=12000
ai.temperature=0.1
ai.economy.mode=true
ai.api.key=
prompt.profile=tecnico
```

### API Key por variável de ambiente (recomendado)

```powershell
$env:EXPERTDEV_AI_API_KEY="sua_chave_aqui"
```

Na UI, selecione `IA`, informe a chave (ou use a variável de ambiente), clique em `Testar IA` e depois em `Gerar Prompt`.

### Providers suportados

- `openai` → endpoint padrão `https://api.openai.com/v1/chat/completions`
- `claude` → endpoint padrão `https://api.anthropic.com/v1/messages`

Na interface, você pode escolher o provider no combo `Provider`.

### Perfil do prompt

- `tecnico` → foco em implementação detalhada, contratos e testes
- `executivo` → foco em priorização, riscos e visão estratégica

### Segurança recomendada

- deixe `ai.api.key=` vazio no arquivo
- prefira variável de ambiente
- use o botão `Limpar` na UI para apagar a chave salva localmente

## Histórico de versões

- **Expert Dev 2.2.3-BETA** — Autenticação local + Login/Trial 15 dias + Lockout + Expiração de senha (30 dias) + Aviso de renovação + Ícone de usuário no cabeçalho.
- **Expert Dev 2.2** — Versão Portátil (JRE Embarcada) + Wrapper Executável (.exe) + Melhorias de Distribuição.
    - Suporte a JRE portátil em `./jre8`.
    - Geração de `ExpertDev.exe` com ícone nativo via Launch4j.
    - Script de inicialização inteligente `expert-dev.bat`.
- **Expert Dev 2.1** — Cache de Contexto (SQLite) + Layout Light fixo + Módulo de Performance & ROI + Dashboard de Tendências + Exportação de Relatório PDF + Automação de Clipboard e Notificações Tray
- **Expert Dev 2.0** — modo IA opcional + fallback local + persistência de configuração de geração
- **VR1.5** — paralelismo + PDF
- **VR1.4** — Word + PDF
- **VR1.3** — Word com imagens embarcadas
- **VR1.2** — resumo de execução
- **VR1.1** — tolerância a falhas por URL
- **VR1.0** — MVP básico

## Próximos passos

- 2.2.4 — Lockout incremental + integração SMTP para recuperação de senha
- 2.3.0 — Suporte a Agentes Locais (Ollama)
- 2.4.0 — Refinamento de Prompt Iterativo (Chat)



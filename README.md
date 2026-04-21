# Expert Dev

Extrai regras de negócio e referências visuais de páginas web, consolida o conteúdo e gera múltiplos formatos de saída (TXT, DOCX, PDF) otimizados para uso com assistentes de código.

## Versão atual: Expert Dev 2.0

- **Paralelismo**: processa múltiplas URLs simultaneamente (usa cores disponíveis no processador)
- **Múltiplos formatos**: texto, Word com imagens embarcadas, PDF com imagens embarcadas
- **Geração de prompt por IA (opcional)**: modo local ou modo IA com API Key
- **Fallback automático**: se a IA falhar, o sistema volta para geração local
- **Modo econômico de IA**: limita contexto enviado e reduz tokens para baixar custo
- **Providers flexíveis**: pronto para OpenAI e Claude/Anthropic
- **Base unificada de prompt**: o refinamento e o contrato do prompt são compartilhados entre entradas locais (URL/Word) e geração via API
- **Perfis de prompt**: alternância entre `tecnico` e `executivo`
- **Estimativa visual IA**: exibe estimativa de tokens e custo aproximado na UI
- **Java 8**: totalmente compatível

## Saída gerada

1. `regras_extraidas.txt` — conteúdo bruto consolidado
2. `imagens_encontradas.txt` — URLs das imagens detectadas
3. `prompt_para_junie_copilot.txt` — prompt pronto para IA
4. `contexto_com_imagens.docx` — **documento Word com imagens inline** ← para visualizar com contexto
5. `contexto_com_imagens.pdf` — **documento PDF com imagens inline** ← para compartilhar
6. `resumo_execucao.txt` — métricas e dados de execução
7. `erros_processamento.txt` — detalhes de URLs que falharam (quando houver)

## Como usar

1. Abra o projeto no IntelliJ Ultimate.
2. Execute a classe `ExpertDev` (ou use `run.bat`).
3. Cole as URLs do site quando solicitado, **separadas por vírgula**.

Exemplo:
```
Cole as URL(s) do site (separadas por vírgula): https://example.com, https://www.site.com
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

- **Expert Dev 2.0** — modo IA opcional + fallback local + persistência de configuração de geração
- **VR1.5** — paralelismo + PDF
- **VR1.4** — Word + PDF
- **VR1.3** — Word com imagens embarcadas
- **VR1.2** — resumo de execução
- **VR1.1** — tolerância a falhas por URL
- **VR1.0** — MVP básico

## Próximos passos

- VR1.6 — testes unitários em JUnit
- VR1.7 — autenticação e cookies
- VR1.8 — exportação para Markdown



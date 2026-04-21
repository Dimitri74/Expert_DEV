# Quickstart - Expert Dev

Guia rapido para abrir, navegar na interface Swing e entender cada funcao principal.

## 1) O que o Expert Dev faz

O Expert Dev coleta contexto de URLs (ou de um arquivo Word), consolida o conteudo e gera:

- `prompt_para_junie_copilot.txt`
- `regras_extraidas.txt`
- `imagens_encontradas.txt`
- `resumo_execucao.txt`
- `erros_processamento.txt` (quando houver falhas)
- `contexto_com_imagens.docx`
- `contexto_com_imagens.pdf`

## 2) Como executar

### Opcao A - IntelliJ

1. Abra o projeto.
2. Execute a classe `ExpertDev`.
3. A interface Swing abre no modo grafico.

### Opcao B - Script local

```bat
run.bat
```

O script usa Maven para compilar e executar `ExpertDev`.

### Opcao C - Maven direto

```powershell
mvn clean compile exec:java -Dexec.mainClass="ExpertDev"
```

## 3) Navegacao da interface

A tela esta dividida em 3 partes:

- **Cabecalho**: logo, alternancia de tema (`Fundo branco`) e versao.
- **Coluna esquerda (Entrada)**: abas de captura + configuracoes + botao de processamento.
- **Coluna direita (Saida)**: log de execucao e prompt gerado.

## 4) Abas de entrada

### 4.1 Aba `Via URLs`

Para coletar contexto de paginas web.

Funcoes:
- Campo de texto para colar uma ou mais URLs (uma por linha ou separadas por virgula).
- Botao `Limpar` para apagar as URLs.

Quando usar:
- Documentacao online
- Wikis internas
- Repositorios e paginas tecnicas

### 4.2 Aba `Upload Word`

Para usar contexto vindo de um arquivo `.docx`.

Funcoes:
- Botao `Selecionar` para escolher o arquivo.
- Preview do texto extraido para validacao rapida.

Quando usar:
- Regras de negocio internas
- Documentos sem fonte web publica

## 5) Painel de configuracao (esquerda, abaixo das abas)

### `Modo`

- `LOCAL`: gera prompt sem chamar API externa.
- `IA`: gera prompt via provider de IA.

### `Provider`

- `openai`
- `claude`

### `Perfil`

- `tecnico`: linguagem detalhada de implementacao, contratos, edge cases e testes.
- `executivo`: foco em visao de negocio, prioridades, riscos e comunicacao para decisao.

### `API Key`

- Campo para chave da IA.
- Botao `Limpar`: remove a chave da tela e da configuracao local.
- Checkbox `Salvar chave localmente`: persiste a chave no arquivo de configuracao local.
- Botao `Testar IA`: valida conexao antes de processar.

### `Estimativa IA`

Mostra estimativa de tokens e custo aproximado para o contexto atual.

## 6) Acao principal

### Botao `Gerar Prompt`

Executa o pipeline completo:

1. Leitura da entrada (URLs ou Word)
2. Processamento e consolidacao
3. Geracao de Word e PDF com imagens
4. Geracao do prompt final
5. Gravacao dos arquivos de saida

Se o modo IA falhar, o sistema tenta fallback para modo local automaticamente.

## 7) Painel de saida (direita)

### `Log de Execucao`

Exibe progresso em tempo real:
- quantidade de URLs
- etapa atual
- avisos e erros
- resumo final

### `Prompt Gerado`

Mostra o prompt final.

Funcoes:
- `Copiar`: copia o prompt para area de transferencia.
- `Salvar Arquivos`: informa que os arquivos ja sao salvos automaticamente no processamento.

## 8) Tema claro/escuro

- O checkbox `Fundo branco` troca entre tema claro e escuro.
- O projeto suporta logo por tema em `src/main/resources/icons`.
- Padrao configuravel: `ui.theme.light.default=true` no `expertdev.properties`.

## 9) Modo CLI (legado)

A classe `ExpertDev` aceita `--cli` para usar o fluxo legado em linha de comando:

```powershell
mvn -q exec:java -Dexec.mainClass="ExpertDev" -Dexec.args="--cli"
```

## 10) Troubleshooting rapido

- Sem resposta de IA: confirme `API Key`, provider e endpoint.
- Prompt vazio: verifique se a entrada tem conteudo valido (URL acessivel ou Word legivel).
- Layout estranho: alterne tema e reabra a aplicacao para recarregar recursos visuais.

## 11) Checklist de uso diario

1. Escolher entrada (`Via URLs` ou `Upload Word`)
2. Escolher `Modo` (LOCAL ou IA)
3. Escolher `Perfil` (`tecnico` ou `executivo`)
4. (Opcional IA) informar chave e `Testar IA`
5. Clicar `Gerar Prompt`
6. Copiar prompt e usar os arquivos gerados


# Documentação Consolidada — Expert Dev 2.4.0-BETA

Este arquivo centraliza os resumos da documentação histórica do projeto.

## Estrutura oficial da documentação

- `README.md` — visão geral do produto, stack, arquitetura e formas de execução.
- `quickstart.md` — passo a passo curto para rodar e usar a aplicação.
- `DOCUMENTACAO_CONSOLIDADA.md` — referência consolidada, histórico técnico e resumos dos documentos legados.

## Objetivo da consolidação

O repositório acumulou muitos arquivos `.md` criados por etapa, sprint, prova de conceito e validação pontual. Para reduzir ruído:

- os conteúdos permanentes foram concentrados em `README.md` e `quickstart.md`;
- os demais documentos foram resumidos aqui;
- os arquivos legados passam a servir apenas como ponte para este consolidado.

## Mapa rápido por tema

### 1. Operação do produto
- Execução principal: `README.md` e `quickstart.md`
- Saídas geradas: `regras_extraidas.txt`, `imagens_encontradas.txt`, `prompt_para_junie_copilot.txt`, `contexto_com_imagens.docx`, `contexto_com_imagens.pdf`, `resumo_execucao.txt`
- Modo IA: providers OpenAI e Claude, com fallback local

### 2. Módulo Pro
- Entregue na versão `2.4.0-BETA`
- Inclui aba `Assistente Pro`
- Recursos principais:
  - abrir arquivo/linha na IDE JetBrains;
  - gerar prompt estruturado;
  - aplicar checklist por categoria;
  - histórico local e evolução para persistência SQLite.

### 3. Pipeline IBM / GID
- B2: leitura de Word (`.doc` / `.docx`)
- B3: classificação e filtro de ruído
- B4: extração semântica por tipo
- G2: consolidação por RTC e dependências entre artefatos
- Melhoria recente: catálogos `MSG_SISTEMA` agora podem ser reduzidos por RTC para evitar contexto excessivo no prompt.

### 4. Autenticação local
- Login por usuário ou e-mail
- Trial local de 15 dias
- Hash de senha com PBKDF2 + salt em SQLite
- Recuperação por código temporário local

### 5. Ambiente corporativo restrito
- Profile `corp-offline` usa repositório corporativo
- Profile `corp-lib-fallback` instala JARs aprovados a partir de `lib/`
- Governança de atualização de bibliotecas documentada na checklist consolidada abaixo

## Resumo dos documentos legados

| Arquivo legado | Tema | Resumo consolidado |
|---|---|---|
| `CHECKLIST_ETAPA1.md` | Validação Etapa 1 | Checklist de aceite da primeira entrega do módulo Pro: compilação, build, integração da aba Pro, botões e testes manuais das abas existentes. |
| `ENTREGA_G2_S1.md` | Entrega G2-S1 | Sumário formal da sprint de consolidação hierárquica IBM, com modelos, serviços e escopo da entrega inicial da etapa G2. |
| `ETAPA1_ENTREGA.md` | Arquitetura Pro | Descrição técnica da Etapa 1 do módulo Pro: pacotes criados, integração em `ExpertDevGUI`, métricas e comportamento dos três botões principais. |
| `ETAPA1_README.md` | Visão geral Etapa 1 | Documento “comece aqui” da Etapa 1, explicando entregáveis, build, acesso à aba Pro e visão geral da estrutura criada. |
| `INDICE_DOCUMENTACAO_ETAPA1.md` | Índice da Etapa 1 | Índice de navegação da documentação criada para a Etapa 1, segmentado por usuário, dev, gestão e próximos passos. |
| `MODULO_PRO_USUARIO.md` | Guia do usuário Pro | Explica como preencher arquivo, linha, categoria e descrição no Assistente Pro, além de exemplos de uso dos botões. |
| `PROXIMOS_PASSOS_ETAPA2.md` | Roadmap Pro | Planejamento da Etapa 2 do módulo Pro: persistência SQLite, adapters, prompts especializados e expansão de checklists. |
| `QUICK_REFERENCE.md` | Referência Etapa 1 | Resumo operacional da Etapa 1 com comandos de build e visão curta da estrutura `br.com.expertdev.pro.*`. |
| `RESUMO_ETAPA1_EXECUTIVO.md` | Executivo Pro | Resumo gerencial da Etapa 1, focado em status, métricas, compatibilidade e entregáveis da arquitetura Pro. |
| `RESUMO_EXECUTIVO_G2_S1.md` | Executivo G2 | Resumo gerencial da sprint G2-S1, com destaque para agrupamento por RTC, mapa inicial de dependências e validação do pipeline. |
| `RESUMO_FINAL.md` | Meta-resumo | Consolidação anterior da documentação do projeto, com forte foco nas entregas de login/UI e panorama geral das mudanças. |
| `STATUS_PROJETO.md` | Status do projeto | Painel de status da frente G2: entregas concluídas, métricas, pipeline B2→B3→B4→G2, roadmap e critérios de sucesso. |
| `PROXIMOS_PASSOS_G2_S2.md` | Roadmap G2 | Planejamento da sprint seguinte do pipeline IBM, com foco em deduplicação, grafo de dependências e detecção de ciclos. |
| `QUICK_REFERENCE_G2.md` | Referência G2 | Comandos rápidos para executar probe G2, usar `IBMG2OrchestrationService` e consultar resultados consolidados. |
| `docs/b2.3-validacao-word.md` | Validação Word | Matriz de testes da leitura Word para cenários `.docx`, `.doc` convertido e fallback de `.doc` sem LibreOffice. |
| `docs/b3-classificacao-ruido.md` | B3 | Resumo da classificação automática de artefatos IBM e filtro de ruído de documentos Word/OLE2. |
| `docs/b4-extracao-semantica.md` | B4 | Resumo das estratégias de extração semântica por tipo IBM e do probe para validar RTC, UC e contadores extraídos. |
| `docs/checklist-atualizacao-jars.md` | Governança de libs | Checklist para atualizar dependências em `lib/`, cobrindo ticket, owner, checksum, licença, scan de vulnerabilidade e validação do build. |
| `docs/especificacao-tecnica-auth-2.2.3-BETA.md` | Autenticação | Especificação da autenticação local: login, trial, hash de senha, recuperação, tabelas SQLite e regras funcionais. |
| `docs/g2-consolidacao-hierarquica.md` | Especificação G2 | Documento técnico da etapa G2, explicando pipeline completo B2→B3→B4→G2, saída esperada e arquitetura dos serviços de consolidação. |
| `docs/tooltips-assistente-pro.md` | UX Assistente Pro | Documenta tooltips, formato esperado dos campos e mensagens orientativas adicionadas ao painel do Assistente Pro. |
| `lib/README.md` | Fallback corporativo | Lista de JARs esperados no diretório `lib/` para o profile `corp-lib-fallback` e comando de build em modo restrito. |

## Síntese técnica por domínio

### Módulo Pro
- Base criada em `br.com.expertdev.pro.*`
- Estrutura principal:
  - `model`: `IssueContext`, `PromptBundle`, `ChecklistResult`
  - `ide`: `IdeBridge`, `JetBrainsBridge`, `IdeBridgeFactory`
  - `workflow`: `ProWorkflowService`, `PromptContextService`, `PromptDeliveryService`, `ChecklistService`
  - `ui`: `ProAssistantPanel`
- Evolução posterior adicionou histórico, adapters e categorias avançadas.

### IBM / GID
- B2 trata ingestão Word com fallback para `.doc` legado
- B3 detecta tipo de artefato e remove ruído
- B4 extrai semântica especializada por tipo
- G2 agrupa artefatos por RTC e prepara consolidação de dependências
- O filtro de `MSG_SISTEMA` por RTC evita despejar catálogos inteiros no prompt final.

### Auth / licença
- Persistência em `expertdev.db`
- Tabelas principais: `auth_users`, `auth_password_reset`, `auth_license_state`
- Trial de 15 dias e badge visual no cabeçalho
- Próximos incrementos conhecidos: SMTP e refinamentos operacionais.

### Ambiente corporativo
- `corp-offline=true`: build apontado ao repositório corporativo
- `corp.lib.fallback=true`: instala JARs locais aprovados
- Dependências locais exigem rastreabilidade, licença validada e checksum.

## Comandos importantes preservados

### Build principal
```powershell
mvn clean compile
mvn clean package -DskipTests
```

### Execução da GUI
```powershell
java -jar target/expert-dev-2.4.0-BETA.jar
```

### Geração do pacote distribuível portátil
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1
```

Saídas esperadas:
- `target\release\ExpertDev-<versao>-win64`
- `target\release\ExpertDev-<versao>-win64-portable.zip`

Variações úteis:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1 -RuntimePath "C:\caminho\jre8" -IncludeJar
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1 -SkipBuild
```

### Validação Word B2.3
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\validate-word-b23.ps1 -DocxPath "C:\caminho\arquivo.docx" -DocPath "C:\caminho\arquivo.doc"
```

### Probe B3
```powershell
mvn -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMB3Probe" "-Dexec.args=C:\caminho\arquivo1.doc C:\caminho\arquivo2.docx"
```

### Probe B4
```powershell
mvn -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMB4Probe" "-Dexec.args=C:\caminho\doc1.doc C:\caminho\doc2.doc"
```

### Probe G2
```powershell
mvn -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMG2Probe" "-Dexec.args=C:\arquivo1.doc C:\arquivo2.doc C:\arquivo3.doc"
```

### Build corporativo restrito
```powershell
mvn -Dcorp.offline=true clean test
mvn -Dcorp.offline=true -Dcorp.lib.fallback=true clean test
```

## Decisões de documentação a partir desta consolidação

1. Novas instruções permanentes devem entrar preferencialmente em `README.md` ou `quickstart.md`.
2. Relatórios temporários, notas de sprint e entregas pontuais devem ser resumidos aqui, em vez de criar novos índices paralelos.
3. Se um assunto exigir profundidade própria novamente, ele deve nascer em `docs/` com justificativa clara e link explícito a este consolidado.

## Estado final

Este arquivo passa a ser a referência única para leitura histórica e resumos da documentação auxiliar do projeto.

_Atualizado em 23/04/2026 para reduzir redundância e concentrar o conhecimento documental do repositório._

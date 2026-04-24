<div style="text-align: center;">
  <img src="src/main/resources/icons/logo_transparente.png" alt="Expert Dev" style="width: 156px;">
</div>

<h1 style="text-align: center;">Quickstart - Expert Dev</h1>

<div style="text-align: center;">
  Guia rápido para executar a aplicação, usar os fluxos principais e gerar o pacote distribuível sem navegar pela documentação histórica.
</div>

<div style="text-align: center;">
  <img alt="Java 8" src="https://img.shields.io/badge/Java-8-007396?style=flat-square&logo=openjdk&logoColor=white">
  <img alt="Quickstart" src="https://img.shields.io/badge/Guia-R%C3%A1pido-1D4ED8?style=flat-square">
  <img alt="Release" src="https://img.shields.io/badge/Release-Port%C3%A1til-7C3AED?style=flat-square">
</div>

> **Objetivo**
> Use este guia para iniciar a aplicação rapidamente, validar os fluxos principais e gerar a versão portátil distribuível em `.zip` para Windows.

## Navegação rápida

| Execução | Fluxo | Configuração | Comandos | Apoio |
| --- | --- | --- | --- | --- |
| [1) Como executar](#1-como-executar) | [3) Fluxos principais da interface](#3-fluxos-principais-da-interface) | [4) Configuração rápida](#4-configuração-rápida) | [5) Comandos úteis](#5-comandos-úteis) | [7) Quando usar o consolidado](#7-quando-usar-o-consolidado) |
| [2) O que a aplicação gera](#2-o-que-a-aplicação-gera) | [6) Troubleshooting rápido](#6-troubleshooting-rápido) | [README](README.md) | [DOCUMENTACAO_CONSOLIDADA.md](DOCUMENTACAO_CONSOLIDADA.md) | [scripts/create-release.ps1](scripts/create-release.ps1) |

## Documentação que você realmente precisa

| Documento | Finalidade |
| --- | --- |
| `README.md` | Visão geral, build, stack, módulos e operação |
| `quickstart.md` | Este passo a passo de uso rápido |
| `DOCUMENTACAO_CONSOLIDADA.md` | Histórico e resumos técnicos das demais notas |

## 1) Como executar

> Escolha o modo mais adequado: IDE para desenvolvimento, JAR para execução direta, Maven para rodar pelo ciclo de build ou pacote portátil para distribuição.

### Opção A — IntelliJ
1. Abra o projeto.
2. Execute a classe `ExpertDev`.
3. Aguarde a abertura da interface Swing.

### Opção B — JAR
```powershell
java -jar target/expert-dev-2.4.0-BETA.jar
```

### Opção C — Maven
```powershell
mvn clean compile exec:java -Dexec.mainClass="ExpertDev"
```

### Opção D — Pacote distribuível portátil
1. Garanta que existe uma runtime em `jre8` na raiz do projeto, ou informe um caminho alternativo no comando.
2. Execute o script de release para gerar a pasta staged e o `.zip` portátil.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1
```

Saídas esperadas:
- pasta staged em `target\release\ExpertDev-<versao>-win64`
- pacote zipado em `target\release\ExpertDev-<versao>-win64-portable.zip`

## 2) O que a aplicação gera

### Artefatos principais

- `prompt_para_junie_copilot.txt`
- `regras_extraidas.txt`
- `imagens_encontradas.txt`
- `contexto_com_imagens.docx`
- `contexto_com_imagens.pdf`
- `resumo_execucao.txt`
- `erros_processamento.txt` quando houver falhas

### Artefatos de distribuição

- `target\release\ExpertDev-<versao>-win64`
- `target\release\ExpertDev-<versao>-win64-portable.zip`

## 3) Fluxos principais da interface

### Aba `Via URLs`
Use quando o contexto estiver em páginas web.

Passos:
1. cole uma ou mais URLs;
2. escolha `Modo` (`LOCAL` ou IA);
3. escolha o `Perfil`;
4. clique em `Gerar Prompt`.

### Aba `Upload Word`
Use quando o contexto estiver em `.doc` ou `.docx`.

Passos:
1. adicione um ou mais arquivos;
2. selecione um item da lista para conferir a prévia, quando aplicável;
3. ajuste `Modo`, `Provider` e `Perfil` se necessário;
4. clique em `Gerar Prompt`.

### Aba `Assistente Pro`
Use para apoio ao fluxo de implementação.

Ações principais:
- `Abrir na IDE`
- `Copiar Prompt`
- `Aplicar Checklist`

### Aba `Performance & ROI`
Use para comparar esforço estimado x esforço real e exportar relatórios executivos.

## 4) Configuração rápida

### Modo de geração
- `LOCAL`: sem chamada externa
- `IA`: usa provider configurado

### Providers suportados
- `openai`
- `claude`

### API Key por variável de ambiente
```powershell
$env:EXPERTDEV_AI_API_KEY="sua_chave_aqui"
```

## 5) Comandos úteis

### Build completo
```powershell
mvn clean package -DskipTests
```

### Gerar versão distribuída e zipada
Com build automático e usando a runtime padrão em `jre8`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1
```

Informando explicitamente a runtime e incluindo também o JAR no pacote staged:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1 -RuntimePath "C:\caminho\jre8" -IncludeJar
```

Se o build já tiver sido executado antes e você quiser apenas remontar o pacote:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\create-release.ps1 -SkipBuild
```

### Validar Word
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\validate-word-b23.ps1 -DocxPath "C:\caminho\arquivo.docx" -DocPath "C:\caminho\arquivo.doc"
```

### Probe G2
```powershell
mvn -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMG2Probe" "-Dexec.args=C:\arquivo1.doc C:\arquivo2.doc"
```

## 6) Troubleshooting rápido

- Prompt vazio: valide se a URL responde ou se o Word possui texto legível.
- Erro com `.doc`: confira configuração de conversão e fallback no `expertdev.properties`.
- Provider IA falhando: revise chave, endpoint e timeout.
- Preview confuso com múltiplos arquivos: selecione o arquivo correto na lista antes de processar.

## 7) Quando usar o consolidado

Abra `DOCUMENTACAO_CONSOLIDADA.md` quando você precisar de:
- histórico das etapas Pro e G2;
- resumos executivos antigos;
- detalhes de B2, B3, B4 e G2;
- governança de `lib/`;
- visão consolidada dos documentos legados.

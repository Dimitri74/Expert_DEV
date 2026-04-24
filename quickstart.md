# Quickstart - Expert Dev

Guia rápido para executar a aplicação e usar os fluxos principais sem navegar pela documentação histórica.

## Documentação que você realmente precisa

1. `README.md` — visão geral, build, stack e módulos.
2. `quickstart.md` — este passo a passo de uso.
3. `DOCUMENTACAO_CONSOLIDADA.md` — histórico e resumos técnicos das demais notas.

## 1) Como executar

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

## 2) O que a aplicação gera

- `prompt_para_junie_copilot.txt`
- `regras_extraidas.txt`
- `imagens_encontradas.txt`
- `contexto_com_imagens.docx`
- `contexto_com_imagens.pdf`
- `resumo_execucao.txt`
- `erros_processamento.txt` quando houver falhas

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

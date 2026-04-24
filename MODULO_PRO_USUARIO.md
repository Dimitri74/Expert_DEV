# 🚀 Expert Dev 2.4.0-BETA — Módulo Pro

## Visão Geral

O **Módulo Pro** agora está integrado no Expert Dev como uma aba separada (`⚡ Assistente Pro`), oferecendo:

1. **Abrir na IDE** — Abrir arquivo + linha na IDE JetBrains instalada
2. **Copiar Prompt** — Gerar prompt estruturado e copiar para clipboard
3. **Aplicar Checklist** — Criar checklist baseado na categoria do problema

---

## 🎯 Como Usar (Passo a Passo)

### 1. Abrir o Aplicativo
```bash
java -jar target/expert-dev-2.4.0-BETA.jar
```

Ou:
```bash
.\target\ExpertDev.exe
```

### 2. Navegar para Aba "Assistente Pro"
- Clique na aba `⚡ Assistente Pro` (quinta aba à direita)

### 3. Preencher Informações
- **Arquivo alvo:** caminho relativo ou absoluto do arquivo (`src/main/java/br/com/expertdev/ui/LoginDialog.java`)
- **Linha:** número da linha onde está o problema (ex: 120)
- **Categoria:** selecione o tipo de problema (UI/Layout, Auth, Build, DB, Outro)
- **Descrição:** descreva o problema em poucas palavras

### 4. Usar os 3 Botões

#### 🔵 **Abrir na IDE**
- **O que faz:** Detecta JetBrains IDE instalada e abre o arquivo na linha especificada
- **Resultado:** IDE abre com contexto exato do problema
- **Quando usar:** Quando precisa analisar o código na IDE

#### 🟢 **Copiar Prompt**
- **O que faz:** Gera prompt estruturado com contexto técnico e copia para clipboard
- **Resultado:** Prompt pronto para colar no Copilot Chat ou outro assistente IA
- **Quando usar:** Para gerar prompt amigável com contexto automático
- **Bonus:** Salva histórico em `~/.expertdev/prompts/`

#### 🟡 **Aplicar Checklist**
- **O que faz:** Cria checklist baseado na categoria do problema
- **Resultado:** Lista de itens para validar antes de considerar tarefa completa
- **Quando usar:** Para garantir qualidade e não esquecer testes

---

## 📊 Exemplo de Uso

**Cenário:** Bug em `RecoveryDialog.java` na linha 120 (campo não editável)

```
Preenchimento:
├─ Arquivo: src/main/java/br/com/expertdev/ui/RecoveryDialog.java
├─ Linha: 120
├─ Categoria: UI/Layout
└─ Descrição: Campo de texto não permite digitação. Deve estar editável.

Ações:
1. Clique "Abrir na IDE" → IDE abre o arquivo na linha exata
2. Clique "Copiar Prompt" → Prompt estruturado copiado
   - Colar no Copilot Chat para análise
3. Clique "Aplicar Checklist" → Checklist UI aparece
   - Validar antes de considerar bug resolvido
```

---

## ✅ Checklist UI (Exemplo)

Quando você clica "Aplicar Checklist" para categoria **UI/Layout**, aparece:

```
Checklist: UI/Layout
Score: 0%

[ ] Campos visíveis em 100% DPI
[ ] Inputs editáveis
[ ] Botões com fonte padronizada
[ ] Efeitos visuais (hover, foco)
[ ] Mensagens de erro claras
```

Marque conforme valida cada item.

---

## ✅ Checklist Auth (Exemplo)

Para categoria **Auth**:

```
Checklist: Auth
Score: 0%

[ ] Validação de credenciais
[ ] Fluxo de recuperação funcional
[ ] Sessão mantida corretamente
[ ] Mensagens de erro sem expor dados
```

---

## ✅ Checklist Build (Exemplo)

Para categoria **Build**:

```
Checklist: Build
Score: 0%

[ ] Compilação maven sem erros
[ ] JAR gerado corretamente
[ ] EXE Windows funcional
[ ] Sem warnings de compilação
```

---

## 📁 Estrutura do Módulo Pro

```
br.com.expertdev.pro/
├── model/
│   ├── IssueContext.java          # Contexto do problema
│   ├── PromptBundle.java          # Bundle do prompt
│   └── ChecklistResult.java       # Resultado do checklist
├── ide/
│   ├── IdeBridge.java             # Interface abstrata
│   ├── JetBrainsBridge.java       # Impl. JetBrains
│   └── IdeBridgeFactory.java      # Factory
├── workflow/
│   ├── ProWorkflowService.java    # Orquestrador
│   ├── PromptContextService.java  # Geração de prompts
│   ├── PromptDeliveryService.java # Entrega
│   └── ChecklistService.java      # Checklists
└── ui/
    └── ProAssistantPanel.java     # Painel visual
```

---

## 🔧 Técnico (Para Devs)

### Usar serviços Pro programaticamente:

```java
// Criar workflow service
ProWorkflowService workflow = new ProWorkflowService();

// Contexto
IssueContext contexto = new IssueContext();
contexto.setArquivoAlvo("src/main/java/br/com/expertdev/ui/LoginDialog.java");
contexto.setLinhaAlvo(120);
contexto.setDescricaoProblema("Campo não editável");
contexto.setCategoria("UI");

// Abrir na IDE
workflow.abrirNaIde(contexto);

// Gerar e copiar prompt
PromptBundle bundle = workflow.copiarPrompt(contexto, "UI_LAYOUT_FIX");

// Salvar prompt
String caminhoPrompt = workflow.salvarPrompt(bundle);

// Aplicar checklist
ChecklistResult checklist = workflow.aplicarChecklist(contexto);
System.out.println("Score: " + checklist.getScorePercentual() + "%");
```

---

## 🎨 Cores e Tema

O Módulo Pro reutiliza a paleta de cores do ExpertDev:
- **Botão Abrir na IDE:** Azul (destaque)
- **Botão Copiar Prompt:** Verde (sucesso)
- **Botão Aplicar Checklist:** Amarelo (aviso)

---

## 🐛 Troubleshooting

### P: "IDE não detectada"
**R:** Verifique se tem JetBrains IDE instalada e adicionada ao PATH do Windows.

### P: "Arquivo não encontrado"
**R:** Use caminho absoluto ou relativo do diretório do projeto.

### P: "Prompt não copiado"
**R:** Verifique se sua IDE de texto/VS Code não está bloqueando clipboard.

### P: "Aba Pro não aparece"
**R:** Verifique logs (console) se houve erro ao carregar. Pode ser falta de dependência.

---

## 📝 Notas de Versão

**v2.4.0-BETA (Etapa 1)**
- ✅ Arquitetura Pro modular
- ✅ 3 botões funcionais (Abrir IDE, Copiar Prompt, Aplicar Checklist)
- ✅ Integração com ExpertDevGUI sem quebra
- ✅ Detecção automática de IDE JetBrains
- ✅ Checklists por categoria
- ✅ Threading correto (sem travamento)

**Próximas features (Etapas 2-4):**
- Histórico persistente em BD
- VS Code bridge
- Prompts ainda mais especializados
- Testes unitários

---

## 🤝 Suporte

Para bugs ou sugestões no módulo Pro, relate em:
- Issue tracker do projeto
- Ou diretamente no painel Pro (feedback)

---

**Última atualização:** 23 de Abril, 2026  
**Versão:** 2.4.0-BETA (Etapa 1)  
**Mantido por:** Expert Dev Team


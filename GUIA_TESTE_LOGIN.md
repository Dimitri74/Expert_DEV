# 🧪 GUIA RÁPIDO DE TESTE - Login UI

## ⚡ Teste Rápido (5 minutos)

### 1. Compilar e Executar
```bash
cd C:\Users\marcu\workspace\Pessoal\expertDev
mvn clean package -DskipTests
java -jar target/expert-dev-2.2.3-BETA.jar
```

### 2. O que verificar na tela de Login

#### ✅ Visual
- [ ] Painel superior com logo \"Expert Dev\" em azul
- [ ] Informação de trial no canto superior direito
- [ ] Versão \"v2.2.3-BETA\" visível
- [ ] Fundo escuro profissional
- [ ] Campos com bordas suaves

#### ✅ Botões (verificar cores)
- [ ] Botão \"Cadastrar\" - 🟨 Amarelo
- [ ] Botão \"Recuperar Senha\" - 🟢 Verde
- [ ] Botão \"Continuar Trial\" - 🔵 Azul
- [ ] Botão \"Entrar\" - 🔵 Azul
- [ ] Botão \"Sair\" - 🔴 Vermelho

#### ✅ Interação (Mouse Over)
- [ ] Passar mouse em cada botão
- [ ] Botão aumenta brilho (15%)
- [ ] Cursor muda para mão (✋)
- [ ] Efeito ao sair (volta ao normal)

#### ✅ Tipografia
- [ ] Titulo \"Expert Dev\" em tamanho grande
- [ ] Rótulos \"Usuário ou email:\" em negrito
- [ ] Campos legíveis com fonte Segoe UI
- [ ] Botões com fonte clara

#### ✅ Funcionalidades
- [ ] Campo de usuário aceitando texto
- [ ] Campo de senha mascarando caracteres
- [ ] Botões respondendo aos cliques

---

## 🔍 Teste Detalhado (15 minutos)

### Teste 1: Cores Semânticas
```
Objetivo: Verificar se as cores fazem sentido

✅ CADASTRAR (Amarelo)
   - Cor de \"novo\" ou \"atenção\"
   - Contraste: texto PRETO no fundo AMARELO
   
✅ RECUPERAR (Verde)
   - Cor de \"ajuda\" ou \"segurança\"
   - Contraste: texto BRANCO no fundo VERDE
   
✅ ENTRAR (Azul)
   - Cor PRINCIPAL do projeto
   - Contraste: texto BRANCO no fundo AZUL
   
✅ SAIR (Vermelho)
   - Cor de \"perigo\" ou \"encerrar\"
   - Contraste: texto BRANCO no fundo VERMELHO
```

### Teste 2: Hover Effects
```
Objetivo: Verificar interação do mouse

Para cada botão:
1. Mover mouse PARA FORA → cor normal
2. Mover mouse PARA DENTRO → brilho aumenta
3. Verificar mudança suave
4. Cursor deve mostrar mão (✋)

Resultado esperado: 15% de aumento em RGB
```

### Teste 3: Contraste (Acessibilidade)
```
Objetivo: Garantir legibilidade

Visual Test:
1. Texto branco em painel escuro → Bom contraste
2. Texto cinza em painel → Contraste suficiente
3. Texto em botões → Totalmente legível

WCAG Compliance:
- Branco (#e2e8f0) em painel (#1c1c2d): ✅ 11.2:1 (AAA)
- Cinza (#94a3b8) em painel (#1c1c2d): ✅ 5.8:1 (AA)
- Texto botão: ✅ Acima de 4.5:1 em todos
```

### Teste 4: Tamanho e Espaçamento
```
Objetivo: Verificar usabilidade

Campos de Entrada:
- [ ] Altura adequada (>30px)
- [ ] Padding interno visível
- [ ] Fácil de clicar

Botões:
- [ ] Tamanho grande (140x40 px)
- [ ] Espaçamento entre eles (12px)
- [ ] Fáceis de clicar (não muito perto um do outro)

Painel Topo:
- [ ] Altura confortável (90px)
- [ ] Logo visível e proporcionada (40x40px)
- [ ] Informações legíveis
```

### Teste 5: Fonte (Typography)
```
Objetivo: Verificar tipografia consistente

Verificar que todas as fontes sejam Segoe UI:
- [ ] Título \"Expert Dev\" - 24pt Bold
- [ ] Rótulos de campos - 13pt Bold
- [ ] Campos de entrada - 13pt Regular
- [ ] Botões - 12pt Bold
- [ ] Info de trial - 11pt Regular

Caso não tenha Segoe UI:
- [ ] Verificar fallback para Arial
- [ ] Ainda legível
- [ ] Sem erros
```

---

## 🐛 Teste de Bug (Verificação)

### Cenário 1: Funcionamento básico
```
1. Clicar em \"Cadastrar\"
   Esperado: Abre diálogo de cadastro
   ✅ Diálogo estilizado com cores
   
2. Clicar em \"Recuperar Senha\"
   Esperado: Abre diálogo de recuperação
   ✅ Diálogo com painel topo e cores
   
3. Clicar em \"Entrar\" (sem credenciais)
   Esperado: Mensagem de erro
   
4. Clicar em \"Sair\"
   Esperado: Fecha a janela
```

### Cenário 2: Campos de entrada
```
1. Digitar no campo \"Usuário ou email\"
   ✅ Texto deve aparecer
   ✅ Cursor azul visível
   
2. Digitar no campo \"Senha\"
   ✅ Caracteres mascarados (•••)
   ✅ Cursor azul visível
   
3. TAB entre campos
   ✅ Deve navegar corretamente
```

### Cenário 3: Efeitos visuais
```
1. Hover em cada botão
   ✅ Brilho aumenta 15%
   ✅ Cursor muda para mão
   ✅ Volta ao normal ao sair
   
2. Múltiplos hovers
   ✅ Sem lag ou tremulação
   ✅ Efeito suave
```

---

## 📊 Checklist de Aceitação

### Visual
- [ ] Painel topo visível com logo
- [ ] Cores dos botões corretas
- [ ] Fonte legível (13pt+)
- [ ] Contraste alto
- [ ] Sem elementos distorcidos

### Interativo
- [ ] Botões respondendo ao clique
- [ ] Hover effects funcionando
- [ ] Campos aceitando entrada
- [ ] Diálogos abrindo corretamente

### Performance
- [ ] Sem lag ao passar mouse
- [ ] Sem tremulação de componentes
- [ ] Carregamento rápido
- [ ] Transições suaves

### Acessibilidade
- [ ] Texto legível para daltônicos
- [ ] Alto contraste garantido
- [ ] Fontes maiores que 11pt
- [ ] Navegação por teclado funciona

---

## 🎓 Teste em Diferentes Resoluções

### Telas Pequenas (1024x768)
```bash
# Verificar se ainda cabe
java -jar expert-dev-2.2.3-BETA.jar

✅ Janela de 800x420 deve caber
✅ Sem elementos cortados
✅ Sem barra de scroll horizontal
```

### Telas Médias (1366x768)
```
✅ Perfeito (a resolução padrão de testes)
✅ Todos os elementos visíveis
✅ Espaçamento confortável
```

### Telas Grandes (1920x1080)
```
✅ Janela mantém tamanho
✅ Não fica muito pequena
✅ Legível de qualquer distância
```

---

## 🔧 Teste de Compatibilidade

### Windows 10/11
```bash
.\target\ExpertDev.exe
✅ Executável deve funcionar
✅ Fonte Segoe UI disponível
✅ Cores renderizam correto
```

### Linux (com Java 8+)
```bash
java -jar target/expert-dev-2.2.3-BETA.jar
✅ Funciona com fallback de fonte
✅ Cores renderizam
✅ Hover effects funcionam
```

---

## 📝 Teste de Internacionalização

### Português (Atual)
- [ ] Todos os textos legíveis
- [ ] Sem caracteres corruptos
- [ ] Acentuação funciona

### Sugestão futura: Inglês
- [ ] \"Enter\" em vez de \"Entrar\"
- [ ] \"Register\" em vez de \"Cadastrar\"
- [ ] Etc...

---

## 🚀 Teste de Performance

### Tempo de Inicialização
```
Objetivo: < 5 segundos

Executar:
time java -jar target/expert-dev-2.2.3-BETA.jar

Esperado:
✅ Tempo total de inicialização < 5s
✅ Interface responsiva após abertura
```

### Uso de Memória
```
Objetivo: < 100MB inicialmente

Verificar:
- Tarefa Manager (Windows)
- Procurando por \"javaw.exe\"
- RAM inicial: ~80-100MB ✅
```

### Responsividade
```
Teste: Hover rápido em botões
- [ ] Sem lag
- [ ] Sem congelamento
- [ ] CPU em < 10% durante hover

Teste: Digitação rápida
- [ ] Todos os caracteres aparecem
- [ ] Sem delay no campo
```

---

## ✅ Resultado do Teste

Ao completar todos os testes acima, preencher:

```
DATA: ___/___/______
TESTADOR: ____________________

Visual:           ✅ PASS  ❌ FAIL
Interação:        ✅ PASS  ❌ FAIL
Performance:      ✅ PASS  ❌ FAIL
Acessibilidade:   ✅ PASS  ❌ FAIL
Compatibilidade:  ✅ PASS  ❌ FAIL

RESULTADO GERAL:  ✅ APROVADO  ❌ REPROVADO

Observações:
_________________________________________________
_________________________________________________
```

---

## 🐛 Reportar Bugs

Se encontrar problemas, registre:

1. **O que você fez:**
   - Ex: \"Passei mouse no botão Entrar\"

2. **O que esperava:**
   - Ex: \"Esperava brilho aumentar\"

3. **O que aconteceu:**
   - Ex: \"Nada mudou\"

4. **Ambiente:**
   - Versão Windows/Linux
   - Java version
   - Resolução de tela

5. **Como reproduzir:**
   - Passos específicos

---

**Status**: ✅ PRONTO PARA TESTE  
**Versão**: 2.2.3-BETA  
**Build**: expert-dev-2.2.3-BETA.jar (42.6 MB)


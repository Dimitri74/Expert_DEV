package br.com.expertdev.pro.workflow;

import br.com.expertdev.pro.model.ChecklistResult;
import br.com.expertdev.pro.model.ChecklistResult.ChecklistItem;
import br.com.expertdev.pro.model.ChecklistResult.StatusItem;

/**
 * Serviço para criar e gerenciar checklists de validação por tipo de tarefa.
 */
public class ChecklistService {

    /**
     * Cria checklist padrão para tarefa UI/Layout.
     */
    public ChecklistResult criarChecklistUI() {
        ChecklistResult result = new ChecklistResult("UI/Layout");

        result.adicionarItem(new ChecklistItem(
                "Campos visíveis em 100% DPI",
                "Todos os inputs visíveis sem corte",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Inputs editáveis",
                "Todos os campos permitem digitação",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Botões com fonte padronizada",
                "Fonte, tamanho e cor consistentes",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Efeitos visuais (hover, foco)",
                "Feedback visual em interações",
                false
        ));

        result.adicionarItem(new ChecklistItem(
                "Mensagens de erro claras",
                "Feedback ao usuário sobre problemas",
                false
        ));

        return result;
    }

    /**
     * Cria checklist padrão para tarefa de Autenticação.
     */
    public ChecklistResult criarChecklistAuth() {
        ChecklistResult result = new ChecklistResult("Auth");

        result.adicionarItem(new ChecklistItem(
                "Validação de credenciais",
                "Login valida usuario/senha corretamente",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Fluxo de recuperação funcional",
                "Recuperar Senha permite reset completo",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Sessão mantida corretamente",
                "Usuário não é deslogado aleatoriamente",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Mensagens de erro sem expor dados",
                "Não revela dados sensíveis em erros",
                true
        ));

        return result;
    }

    /**
     * Cria checklist padrão para compilação/build.
     */
    public ChecklistResult criarChecklistBuild() {
        ChecklistResult result = new ChecklistResult("Build");

        result.adicionarItem(new ChecklistItem(
                "Compilação maven sem erros",
                "mvn clean compile executa com sucesso",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "JAR gerado corretamente",
                "mvn package cria artefato válido",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "EXE Windows funcional",
                "Aplicativo executável sem erros",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Sem warnings de compilação",
                "Build clean sem avisos deprecados",
                false
        ));

        return result;
    }

    /**
     * Cria checklist padrão para testes de regressão.
     */
    public ChecklistResult criarChecklistRegressao() {
        ChecklistResult result = new ChecklistResult("Regressão");

        result.adicionarItem(new ChecklistItem(
                "Fluxo legado CLI funcional",
                "ExpertDev --cli executa sem quebra",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Abas existentes acessíveis",
                "Menu principal não foi quebrado",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Geração de prompts funciona",
                "Processamento de URLs continua OK",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Relatórios exportam corretamente",
                "DOCX, PDF e TXT exportam sem erro",
                false
        ));

        return result;
    }

    /**
     * Cria checklist genérico para tarefas classificadas como "Outro".
     */
    public ChecklistResult criarChecklistOutro() {
        ChecklistResult result = new ChecklistResult("Outro/Geral");

        result.adicionarItem(new ChecklistItem(
                "Mudança aplicada no arquivo alvo",
                "A alteração foi realizada no arquivo e linha informados",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Build sem quebra",
                "Compilação executou sem erro após a alteração",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Resultado esperado validado",
                "Comportamento solicitado foi confirmado manualmente",
                true
        ));

        result.adicionarItem(new ChecklistItem(
                "Sem impacto colateral visível",
                "Fluxos próximos continuam funcionando",
                false
        ));

        return result;
    }

    /**
     * Marca item como OK.
     */
    public void marcarOk(ChecklistResult result, int indiceItem) {
        if (indiceItem >= 0 && indiceItem < result.getItens().size()) {
            result.getItens().get(indiceItem).setStatus(StatusItem.OK);
            result.calcularScore();
        }
    }

    /**
     * Marca item como falho.
     */
    public void marcarFalhou(ChecklistResult result, int indiceItem, String detalhes) {
        if (indiceItem >= 0 && indiceItem < result.getItens().size()) {
            ChecklistItem item = result.getItens().get(indiceItem);
            item.setStatus(StatusItem.FALHOU);
            item.setDetalhes(detalhes);
            result.calcularScore();
        }
    }

    // =========================================================================
    // Novas categorias — Etapa 2
    // =========================================================================

    /**
     * Cria checklist para problemas de banco de dados.
     */
    public ChecklistResult criarChecklistDatabase() {
        ChecklistResult result = new ChecklistResult("Database");

        result.adicionarItem(new ChecklistItem(
                "Queries com PreparedStatement",
                "Nenhuma concatenação direta de SQL detectada",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Transações com rollback",
                "BEGIN/COMMIT/ROLLBACK aplicados corretamente",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Connection pool configurado",
                "Pool não esgota sob carga normal",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Índices nas colunas de busca frequente",
                "EXPLAIN QUERY PLAN sem full scan em tabelas grandes",
                false
        ));
        result.adicionarItem(new ChecklistItem(
                "Sem N+1 queries",
                "Lazy loading controlado, eager onde necessário",
                false
        ));

        return result;
    }

    /**
     * Cria checklist para problemas de performance.
     */
    public ChecklistResult criarChecklistPerformance() {
        ChecklistResult result = new ChecklistResult("Performance");

        result.adicionarItem(new ChecklistItem(
                "Tempo de resposta < 2s",
                "Operações principais completam em até 2 segundos",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Sem memory leak detectado",
                "Heap estável após operações repetidas",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "CPU < 80% em uso normal",
                "Sem loops infinitos ou processos trancados",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Cache aplicado onde adequado",
                "Resultados custosos são cacheados",
                false
        ));
        result.adicionarItem(new ChecklistItem(
                "Paginação em listas grandes",
                "Queries retornam no máximo N registros por vez",
                false
        ));

        return result;
    }

    /**
     * Cria checklist para segurança.
     */
    public ChecklistResult criarChecklistSecurity() {
        ChecklistResult result = new ChecklistResult("Security");

        result.adicionarItem(new ChecklistItem(
                "Sem SQL Injection",
                "Todas as queries usam parâmetros vinculados",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Sem dados sensíveis em logs",
                "Senhas, tokens e CPFs não aparecem em stack traces",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Inputs validados e sanitizados",
                "Todos os campos de entrada têm validação server-side",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "OWASP Top 10 verificado",
                "As principais vulnerabilidades verificadas no trecho",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Sem credenciais hardcoded",
                "Sem senhas ou tokens embutidos no código-fonte",
                true
        ));

        return result;
    }

    /**
     * Cria checklist para testes.
     */
    public ChecklistResult criarChecklistTests() {
        ChecklistResult result = new ChecklistResult("Tests");

        result.adicionarItem(new ChecklistItem(
                "Cobertura >= 80% no componente",
                "JaCoCo ou equivalente confirma cobertura adequada",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Casos de sucesso cobertos",
                "Happy path com assertions explícitas",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Casos de falha cobertos",
                "Exceções e validações de erro testadas",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Testes isolados (sem I/O real)",
                "Mocks para banco, rede e sistema de arquivos",
                false
        ));
        result.adicionarItem(new ChecklistItem(
                "Execução total < 5s",
                "Suite de testes unitários rápida",
                false
        ));

        return result;
    }

    /**
     * Cria checklist para refatoração.
     */
    public ChecklistResult criarChecklistRefactoring() {
        ChecklistResult result = new ChecklistResult("Refactoring");

        result.adicionarItem(new ChecklistItem(
                "Comportamento externo preservado",
                "Testes existentes passam sem modificação",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Duplicação eliminada (DRY)",
                "Blocos repetidos extraídos para métodos/classes",
                true
        ));
        result.adicionarItem(new ChecklistItem(
                "Complexidade ciclomática reduzida",
                "Métodos com CC > 10 revisados e simplificados",
                false
        ));
        result.adicionarItem(new ChecklistItem(
                "Responsabilidade única (SRP)",
                "Cada classe/método faz uma coisa bem",
                false
        ));
        result.adicionarItem(new ChecklistItem(
                "Nomes auto-descritivos",
                "Sem abreviações obscuras, sem comentários de obviedades",
                false
        ));

        return result;
    }

    /**
     * Fábrica central: retorna checklist pelo nome de categoria.
     */
    public ChecklistResult criarPorCategoria(String categoria) {
        if (categoria == null) return criarChecklistOutro();
        switch (categoria.toLowerCase()) {
            case "ui":        return criarChecklistUI();
            case "auth":      return criarChecklistAuth();
            case "build":     return criarChecklistBuild();
            case "regressao": return criarChecklistRegressao();
            case "database":  return criarChecklistDatabase();
            case "performance": return criarChecklistPerformance();
            case "security":  return criarChecklistSecurity();
            case "tests":     return criarChecklistTests();
            case "refactoring": return criarChecklistRefactoring();
            default:          return criarChecklistOutro();
        }
    }
}


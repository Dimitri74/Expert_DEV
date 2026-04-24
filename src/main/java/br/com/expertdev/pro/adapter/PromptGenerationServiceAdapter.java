package br.com.expertdev.pro.adapter;

import br.com.expertdev.pro.model.AiAssistantTarget;
import br.com.expertdev.pro.model.IssueContext;
import br.com.expertdev.pro.model.PromptBundle;
import br.com.expertdev.pro.service.ProHistoryService;
import br.com.expertdev.pro.workflow.PromptContextService;

/**
 * Adapter que envolve PromptContextService com auto-persistência no histórico SQLite.
 * Ponto único de geração de prompts para toda a camada Pro.
 */
public class PromptGenerationServiceAdapter {

    private final PromptContextService promptContextService;
    private final ProHistoryService historyService;
    private boolean persistirAutomaticamente = true;

    public PromptGenerationServiceAdapter() {
        this.promptContextService = new PromptContextService();
        this.historyService = new ProHistoryService();
    }

    public PromptGenerationServiceAdapter(PromptContextService promptContextService,
                                          ProHistoryService historyService) {
        this.promptContextService = promptContextService;
        this.historyService = historyService;
    }

    // -------------------------------------------------------------------------
    // Delegação com persistência automática
    // -------------------------------------------------------------------------

    public PromptBundle gerarPrompt(IssueContext contexto, String tipoPrompt) {
        PromptBundle bundle = promptContextService.gerarPrompt(contexto, tipoPrompt);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    public PromptBundle gerarPrompt(IssueContext contexto, String tipoPrompt, AiAssistantTarget assistente) {
        PromptBundle bundle = promptContextService.gerarPrompt(contexto, tipoPrompt, assistente);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    public PromptBundle gerarPromptUI(IssueContext contexto) {
        PromptBundle bundle = promptContextService.gerarPromptUI(contexto);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    public PromptBundle gerarPromptAuth(IssueContext contexto) {
        PromptBundle bundle = promptContextService.gerarPromptAuth(contexto);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    public PromptBundle gerarPromptBuild(IssueContext contexto) {
        PromptBundle bundle = promptContextService.gerarPromptBuild(contexto);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    // -------------------------------------------------------------------------
    // Templates especializados adicionais
    // -------------------------------------------------------------------------

    public PromptBundle gerarPromptDatabaseError(IssueContext contexto) {
        return gerarPromptDatabaseError(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptDatabaseError(IssueContext contexto, AiAssistantTarget assistente) {
        if (contexto.getDescricaoProblema() == null)
            contexto.setDescricaoProblema("Erro de banco de dados: query falhando ou conexão perdida");
        contexto.setCategoria("Database");
        PromptBundle bundle = promptContextService.gerarPromptDatabaseError(contexto, assistente);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    public PromptBundle gerarPromptPerformance(IssueContext contexto) {
        return gerarPromptPerformance(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptPerformance(IssueContext contexto, AiAssistantTarget assistente) {
        if (contexto.getDescricaoProblema() == null)
            contexto.setDescricaoProblema("Problema de performance: lentidão ou alto consumo de memória");
        contexto.setCategoria("Performance");
        PromptBundle bundle = promptContextService.gerarPromptPerformance(contexto, assistente);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    public PromptBundle gerarPromptSecurity(IssueContext contexto) {
        return gerarPromptSecurity(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptSecurity(IssueContext contexto, AiAssistantTarget assistente) {
        if (contexto.getDescricaoProblema() == null)
            contexto.setDescricaoProblema("Vulnerabilidade de segurança identificada");
        contexto.setCategoria("Security");
        PromptBundle bundle = promptContextService.gerarPromptSecurity(contexto, assistente);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    public PromptBundle gerarPromptRefactoring(IssueContext contexto) {
        return gerarPromptRefactoring(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptRefactoring(IssueContext contexto, AiAssistantTarget assistente) {
        if (contexto.getDescricaoProblema() == null)
            contexto.setDescricaoProblema("Refatoração necessária para reduzir complexidade");
        contexto.setCategoria("Refactoring");
        PromptBundle bundle = promptContextService.gerarPromptRefactoring(contexto, assistente);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    public PromptBundle gerarPromptTestCoverage(IssueContext contexto) {
        return gerarPromptTestCoverage(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptTestCoverage(IssueContext contexto, AiAssistantTarget assistente) {
        if (contexto.getDescricaoProblema() == null)
            contexto.setDescricaoProblema("Criação ou correção de testes unitários/integração");
        contexto.setCategoria("Tests");
        PromptBundle bundle = promptContextService.gerarPromptTestCoverage(contexto, assistente);
        if (persistirAutomaticamente) historyService.salvarPrompt(bundle);
        return bundle;
    }

    public PromptBundle gerarPromptTests(IssueContext contexto) {
        return gerarPromptTestCoverage(contexto);
    }

    // -------------------------------------------------------------------------
    // Configuração
    // -------------------------------------------------------------------------

    public void setPersistirAutomaticamente(boolean persistirAutomaticamente) {
        this.persistirAutomaticamente = persistirAutomaticamente;
    }

    public ProHistoryService getHistoryService() {
        return historyService;
    }
}


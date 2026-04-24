package br.com.expertdev.pro.workflow;

import br.com.expertdev.pro.ide.IdeBridge;
import br.com.expertdev.pro.ide.IdeBridgeFactory;
import br.com.expertdev.pro.adapter.AuditoriaServiceAdapter;
import br.com.expertdev.pro.adapter.AuthServiceAdapter;
import br.com.expertdev.pro.adapter.PromptGenerationServiceAdapter;
import br.com.expertdev.pro.model.AiAssistantTarget;
import br.com.expertdev.pro.model.IssueContext;
import br.com.expertdev.pro.model.PromptBundle;
import br.com.expertdev.pro.model.ChecklistResult;
import br.com.expertdev.pro.service.ProHistoryService;

/**
 * Orquestrador principal do fluxo Pro.
 * Coordena: IDE bridge, geração de prompts, checklists.
 */
public class ProWorkflowService {

    private IdeBridge ideBridge;
    private PromptGenerationServiceAdapter promptService;
    private PromptDeliveryService deliveryService;
    private ChecklistService checklistService;
    private AuthServiceAdapter authService;
    private AuditoriaServiceAdapter auditoriaService;
    private ProHistoryService historyService;

    public ProWorkflowService() {
        this.ideBridge = IdeBridgeFactory.criarBridge();
        this.promptService = new PromptGenerationServiceAdapter();
        this.deliveryService = new PromptDeliveryService();
        this.checklistService = new ChecklistService();
        this.authService = new AuthServiceAdapter();
        this.auditoriaService = new AuditoriaServiceAdapter();
        this.historyService = promptService.getHistoryService();
    }

    /**
     * Ação: Abrir na IDE.
     * Abre arquivo + linha na IDE detectada.
     */
    public void abrirNaIde(IssueContext contexto) throws Exception {
        if (!ideBridge.isIdeInstalada()) {
            throw new RuntimeException("Nenhuma IDE detectada no sistema");
        }

        String arquivo = normalizarCaminho(contexto.getArquivoAlvo());
        int linha = Math.max(1, contexto.getLinhaAlvo());

        ideBridge.abrirNaIde(arquivo, linha);
    }

    /**
     * Ação: Copiar Prompt.
     * Gera prompt e copia para clipboard.
     */
    public PromptBundle copiarPrompt(IssueContext contexto, String tipoPrompt) throws Exception {
        return copiarPrompt(contexto, tipoPrompt, AiAssistantTarget.COPILOT);
    }

    /**
     * Ação: Copiar Prompt para assistente IA selecionado.
     */
    public PromptBundle copiarPrompt(IssueContext contexto, String tipoPrompt, AiAssistantTarget assistenteAlvo) throws Exception {
        if (assistenteAlvo == null) {
            assistenteAlvo = AiAssistantTarget.COPILOT;
        }

        if (contexto != null) {
            authService.enriquecerContexto(contexto);
            if (contexto.getCategoria() == null || contexto.getCategoria().trim().isEmpty()) {
                contexto.setCategoria(tipoPrompt);
            }
        }

        PromptBundle bundle = gerarPromptPorCategoria(contexto, tipoPrompt, assistenteAlvo);
        deliveryService.copiarParaClipboard(bundle.getPromptGerado());

        try {
            auditoriaService.registrarGeracaoPrompt(bundle, contexto);
        } catch (Exception ignored) {
            // Falha de auditoria nao deve interromper copia para clipboard.
        }

        return bundle;
    }

    /**
     * Instrução curta para orientar o usuário no destino do prompt.
     */
    public String getInstrucaoDestino(AiAssistantTarget assistenteAlvo) {
        if (assistenteAlvo == null) {
            assistenteAlvo = AiAssistantTarget.COPILOT;
        }
        return assistenteAlvo.getInstrucaoUso();
    }

    /**
     * Ação: Salvar Prompt.
     * Salva prompt em arquivo local para referência.
     */
    public String salvarPrompt(PromptBundle bundle) throws Exception {
        return deliveryService.salvarPrompt(bundle);
    }

    /**
     * Ação: Aplicar Checklist.
     * Cria checklist baseado na categoria da tarefa.
     */
    public ChecklistResult aplicarChecklist(IssueContext contexto) {
        String categoria = normalizarCategoriaChecklist(contexto != null ? contexto.getCategoria() : null);

        ChecklistResult result;
        switch (categoria) {
            case "AUTH":
                result = checklistService.criarChecklistAuth();
                break;
            case "BUILD":
                result = checklistService.criarChecklistBuild();
                break;
            case "DATABASE":
                result = checklistService.criarChecklistDatabase();
                break;
            case "PERFORMANCE":
                result = checklistService.criarChecklistPerformance();
                break;
            case "SECURITY":
                result = checklistService.criarChecklistSecurity();
                break;
            case "TESTS":
                result = checklistService.criarChecklistTests();
                break;
            case "REFACTORING":
                result = checklistService.criarChecklistRefactoring();
                break;
            case "OUTRO":
                result = checklistService.criarChecklistOutro();
                break;
            case "REGRESSAO":
                result = checklistService.criarChecklistRegressao();
                break;
            case "UI":
            default:
                result = checklistService.criarChecklistUI();
                break;
        }

        historyService.salvarChecklist(result);

        return result;
    }

    /**
     * Retorna nome da IDE detectada.
     */
    public String getIdeName() {
        return ideBridge.getNomeIde();
    }

    /**
     * Retorna se IDE está disponível.
     */
    public boolean isIdeAvailable() {
        return ideBridge.isIdeInstalada();
    }

    private String normalizarCaminho(String caminho) {
        java.io.File f = new java.io.File(caminho);
        if (!f.isAbsolute()) {
            f = new java.io.File(System.getProperty("user.dir"), caminho);
        }
        return f.getAbsolutePath();
    }

    private String normalizarCategoriaChecklist(String categoriaBruta) {
        if (categoriaBruta == null || categoriaBruta.trim().isEmpty()) {
            return "UI";
        }

        String categoria = categoriaBruta.trim().toUpperCase();
        if ("DB".equals(categoria) || "DATABASE".equals(categoria) || "BANCO".equals(categoria)) {
            return "DATABASE";
        }
        if ("PERFORMANCE".equals(categoria) || "PERF".equals(categoria)) {
            return "PERFORMANCE";
        }
        if ("SEC".equals(categoria) || "SECURITY".equals(categoria) || "SEGURANCA".equals(categoria) || "SEGURANÇA".equals(categoria)) {
            return "SECURITY";
        }
        if ("TEST".equals(categoria) || "TESTE".equals(categoria) || "TESTES".equals(categoria) || "TESTS".equals(categoria)) {
            return "TESTS";
        }
        if ("REFACTOR".equals(categoria) || "REFACTORING".equals(categoria)) {
            return "REFACTORING";
        }
        if ("UI/LAYOUT".equals(categoria) || "UI_LAYOUT".equals(categoria)) {
            return "UI";
        }
        if ("OUTRO".equals(categoria) || "GERAL".equals(categoria)) {
            return "OUTRO";
        }
        if ("REGRESSAO".equals(categoria) || "REGRESSÃO".equals(categoria)) {
            return "REGRESSAO";
        }

        return categoria;
    }

    private PromptBundle gerarPromptPorCategoria(IssueContext contexto, String tipoPrompt, AiAssistantTarget assistenteAlvo) {
        String categoria = normalizarCategoriaChecklist(tipoPrompt);
        switch (categoria) {
            case "AUTH":
                return promptService.gerarPrompt(contexto, "AUTH_ISSUE", assistenteAlvo);
            case "BUILD":
                return promptService.gerarPrompt(contexto, "BUILD_ERROR", assistenteAlvo);
            case "DATABASE":
                return promptService.gerarPromptDatabaseError(contexto, assistenteAlvo);
            case "PERFORMANCE":
                return promptService.gerarPromptPerformance(contexto, assistenteAlvo);
            case "SECURITY":
                return promptService.gerarPromptSecurity(contexto, assistenteAlvo);
            case "TESTS":
                return promptService.gerarPromptTestCoverage(contexto, assistenteAlvo);
            case "REFACTORING":
                return promptService.gerarPromptRefactoring(contexto, assistenteAlvo);
            case "REGRESSAO":
                return promptService.gerarPrompt(contexto, "REGRESSION_CHECK", assistenteAlvo);
            case "OUTRO":
                return promptService.gerarPrompt(contexto, "GENERAL_ISSUE", assistenteAlvo);
            case "UI":
            default:
                if (contexto != null && (contexto.getDescricaoProblema() == null || contexto.getDescricaoProblema().trim().isEmpty())) {
                    contexto.setDescricaoProblema("Ajuste visual em componentes Swing");
                }
                return promptService.gerarPrompt(contexto, "UI_LAYOUT_FIX", assistenteAlvo);
        }
    }
}


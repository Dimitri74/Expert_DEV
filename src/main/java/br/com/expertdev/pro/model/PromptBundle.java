package br.com.expertdev.pro.model;

/**
 * Bundle com prompt e contexto relacionado.
 * Pronto para copiar/colar no Copilot Chat.
 */
public class PromptBundle {
    private String promptGerado;
    private IssueContext contexto;
    private long timestampCriacao;
    private String tipoPrompt;  // UI_FIX, AUTH_ISSUE, etc
    private String versaoExpertDev;
    private AiAssistantTarget assistenteAlvo;

    public PromptBundle() {
        this.timestampCriacao = System.currentTimeMillis();
        this.versaoExpertDev = "2.5.0-BETA";
    }

    public PromptBundle(String promptGerado, IssueContext contexto) {
        this();
        this.promptGerado = promptGerado;
        this.contexto = contexto;
    }

    // Getters e Setters
    public String getPromptGerado() {
        return promptGerado;
    }

    public void setPromptGerado(String promptGerado) {
        this.promptGerado = promptGerado;
    }

    public IssueContext getContexto() {
        return contexto;
    }

    public void setContexto(IssueContext contexto) {
        this.contexto = contexto;
    }

    public long getTimestampCriacao() {
        return timestampCriacao;
    }

    public String getTipoPrompt() {
        return tipoPrompt;
    }

    public void setTipoPrompt(String tipoPrompt) {
        this.tipoPrompt = tipoPrompt;
    }

    public String getVersaoExpertDev() {
        return versaoExpertDev;
    }

    public AiAssistantTarget getAssistenteAlvo() {
        return assistenteAlvo;
    }

    public void setAssistenteAlvo(AiAssistantTarget assistenteAlvo) {
        this.assistenteAlvo = assistenteAlvo;
    }

    public int getTamanhoPrompt() {
        return promptGerado != null ? promptGerado.length() : 0;
    }

    @Override
    public String toString() {
        return "PromptBundle{" +
                "tipo='" + tipoPrompt + '\'' +
                ", assistente='" + (assistenteAlvo != null ? assistenteAlvo.getRotulo() : "N/A") + '\'' +
                ", tamanho=" + getTamanhoPrompt() +
                ", timestamp=" + timestampCriacao +
                '}';
    }
}


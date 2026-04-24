package br.com.expertdev.pro.model;

/**
 * Assistentes IA suportados no fluxo Pro.
 */
// Comentado por Expert-Dev // via-copilot
public enum AiAssistantTarget {
    COPILOT("GitHub Copilot", "Cole no chat do GitHub Copilot na IDE"),
    JUNIE("Junie IA (IntelliJ)", "Cole no chat da Junie IA no IntelliJ");

    private final String rotulo;
    private final String instrucaoUso;

    AiAssistantTarget(String rotulo, String instrucaoUso) {
        this.rotulo = rotulo;
        this.instrucaoUso = instrucaoUso;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getInstrucaoUso() {
        return instrucaoUso;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}


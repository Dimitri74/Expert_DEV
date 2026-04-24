package br.com.expertdev.pro.model;

/**
 * Contexto técnico de um problema/tarefa.
 * Armazena informações para gerar prompts e checklists.
 */
public class IssueContext {
    private String arquivoAlvo;
    private int linhaAlvo;
    private String descricaoProblema;
    private String stackTrace;
    private String categoria;  // UI, Auth, DB, Build, etc
    private String objetivoTarefa;
    private String criteriosAceite;

    public IssueContext() {
    }

    public IssueContext(String arquivoAlvo, int linhaAlvo, String descricaoProblema) {
        this.arquivoAlvo = arquivoAlvo;
        this.linhaAlvo = linhaAlvo;
        this.descricaoProblema = descricaoProblema;
    }

    // Getters e Setters
    public String getArquivoAlvo() {
        return arquivoAlvo;
    }

    public void setArquivoAlvo(String arquivoAlvo) {
        this.arquivoAlvo = arquivoAlvo;
    }

    public int getLinhaAlvo() {
        return linhaAlvo;
    }

    public void setLinhaAlvo(int linhaAlvo) {
        this.linhaAlvo = linhaAlvo;
    }

    public String getDescricaoProblema() {
        return descricaoProblema;
    }

    public void setDescricaoProblema(String descricaoProblema) {
        this.descricaoProblema = descricaoProblema;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getObjetivoTarefa() {
        return objetivoTarefa;
    }

    public void setObjetivoTarefa(String objetivoTarefa) {
        this.objetivoTarefa = objetivoTarefa;
    }

    public String getCriteriosAceite() {
        return criteriosAceite;
    }

    public void setCriteriosAceite(String criteriosAceite) {
        this.criteriosAceite = criteriosAceite;
    }

    @Override
    public String toString() {
        return "IssueContext{" +
                "arquivoAlvo='" + arquivoAlvo + '\'' +
                ", linhaAlvo=" + linhaAlvo +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}


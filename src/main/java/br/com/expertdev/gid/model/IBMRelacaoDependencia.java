package br.com.expertdev.gid.model;

/**
 * Representa uma relação de dependência entre artefatos.
 * Exemplo: UC "Autenticar" -> Integração "Login" -> API "POST /auth/login"
 */
public class IBMRelacaoDependencia {

    private String codigoOrigem;     // ID do artefato de origem
    private String tipoOrigem;        // INTEGRACAO_UC, API_QUARKUS, etc
    private String codigoDestino;    // ID do artefato de destino
    private String tipoDestino;      // INTEGRACAO_DI, API_QUARKUS, etc
    private String tipoRelacao;      // "implementa", "depende_de", "notifica", "consome"
    private double confianca;        // Confiança da relação (0-100)
    private String descricao;        // Descrição opcional da relação

    // Construtores
    public IBMRelacaoDependencia() {}

    public IBMRelacaoDependencia(String codigoOrigem, String tipoOrigem,
                                  String codigoDestino, String tipoDestino,
                                  String tipoRelacao) {
        this.codigoOrigem = codigoOrigem;
        this.tipoOrigem = tipoOrigem;
        this.codigoDestino = codigoDestino;
        this.tipoDestino = tipoDestino;
        this.tipoRelacao = tipoRelacao;
        this.confianca = 100.0; // Padrão total
    }

    // Getters e Setters
    public String getCodigoOrigem() {
        return codigoOrigem;
    }

    public void setCodigoOrigem(String codigoOrigem) {
        this.codigoOrigem = codigoOrigem;
    }

    public String getTipoOrigem() {
        return tipoOrigem;
    }

    public void setTipoOrigem(String tipoOrigem) {
        this.tipoOrigem = tipoOrigem;
    }

    public String getCodigoDestino() {
        return codigoDestino;
    }

    public void setCodigoDestino(String codigoDestino) {
        this.codigoDestino = codigoDestino;
    }

    public String getTipoDestino() {
        return tipoDestino;
    }

    public void setTipoDestino(String tipoDestino) {
        this.tipoDestino = tipoDestino;
    }

    public String getTipoRelacao() {
        return tipoRelacao;
    }

    public void setTipoRelacao(String tipoRelacao) {
        this.tipoRelacao = tipoRelacao;
    }

    public double getConfianca() {
        return confianca;
    }

    public void setConfianca(double confianca) {
        this.confianca = confianca;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return String.format("[%s:%s] --(%s)--> [%s:%s] (conf: %.0f%%)",
                tipoOrigem, codigoOrigem, tipoRelacao, tipoDestino, codigoDestino, confianca);
    }
}


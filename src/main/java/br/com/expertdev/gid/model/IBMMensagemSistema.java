package br.com.expertdev.gid.model;

/**
 * Mensagem funcional de negocio catalogada no documento de mensagens.
 */
public class IBMMensagemSistema {

    private String codigo;
    private String descricao;
    private String condicaoDisparo;
    private String variaveis;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCondicaoDisparo() {
        return condicaoDisparo;
    }

    public void setCondicaoDisparo(String condicaoDisparo) {
        this.condicaoDisparo = condicaoDisparo;
    }

    public String getVariaveis() {
        return variaveis;
    }

    public void setVariaveis(String variaveis) {
        this.variaveis = variaveis;
    }
}


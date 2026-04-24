package br.com.expertdev.gid.model;

/**
 * Item de regra de negocio normalizado para consolidacao por RTC.
 */
public class IBMRegraNegocio {

    private String codigo;
    private String descricao;
    private String origemSecao;

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

    public String getOrigemSecao() {
        return origemSecao;
    }

    public void setOrigemSecao(String origemSecao) {
        this.origemSecao = origemSecao;
    }
}


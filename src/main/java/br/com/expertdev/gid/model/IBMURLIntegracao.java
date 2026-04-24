package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoURLIntegracao;

/**
 * URL de integracao detectada em documentos API/Suplementar.
 */
public class IBMURLIntegracao {

    private String url;
    private IBMTipoURLIntegracao tipo = IBMTipoURLIntegracao.OUTRA;
    private String origem;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public IBMTipoURLIntegracao getTipo() {
        return tipo;
    }

    public void setTipo(IBMTipoURLIntegracao tipo) {
        this.tipo = tipo;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }
}


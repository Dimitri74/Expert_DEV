package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracao tipada para catalogo de APIs Quarkus.
 */
public class IBMExtracaoAPI extends IBMArtefatoExtracao {

    private String nomeServico;
    private String metodoHttp;
    private String endpoint;
    private List<IBMParametro> parametros = new ArrayList<IBMParametro>();
    private List<String> codigosHttpRetorno = new ArrayList<String>();
    private List<IBMURLIntegracao> urlsRelacionadas = new ArrayList<IBMURLIntegracao>();

    public IBMExtracaoAPI() {
        setTipoArtefato(IBMTipoArtefato.API_QUARKUS);
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public void setNomeServico(String nomeServico) {
        this.nomeServico = nomeServico;
    }

    public String getMetodoHttp() {
        return metodoHttp;
    }

    public void setMetodoHttp(String metodoHttp) {
        this.metodoHttp = metodoHttp;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<IBMParametro> getParametros() {
        return parametros;
    }

    public void setParametros(List<IBMParametro> parametros) {
        this.parametros = parametros;
    }

    public List<String> getCodigosHttpRetorno() {
        return codigosHttpRetorno;
    }

    public void setCodigosHttpRetorno(List<String> codigosHttpRetorno) {
        this.codigosHttpRetorno = codigosHttpRetorno;
    }

    public List<IBMURLIntegracao> getUrlsRelacionadas() {
        return urlsRelacionadas;
    }

    public void setUrlsRelacionadas(List<IBMURLIntegracao> urlsRelacionadas) {
        this.urlsRelacionadas = urlsRelacionadas;
    }
}


package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracao tipada para especificacao suplementar.
 */
public class IBMExtracaoSuplementar extends IBMArtefatoExtracao {

    private List<String> requisitosNaoFuncionais = new ArrayList<String>();
    private List<IBMURLIntegracao> urlsIntegracao = new ArrayList<IBMURLIntegracao>();
    private List<String> sistemasExternos = new ArrayList<String>();
    private List<String> glossario = new ArrayList<String>();

    public IBMExtracaoSuplementar() {
        setTipoArtefato(IBMTipoArtefato.ESPECIFICACAO_SUPLEMENTAR);
    }

    public List<String> getRequisitosNaoFuncionais() {
        return requisitosNaoFuncionais;
    }

    public void setRequisitosNaoFuncionais(List<String> requisitosNaoFuncionais) {
        this.requisitosNaoFuncionais = requisitosNaoFuncionais;
    }

    public List<IBMURLIntegracao> getUrlsIntegracao() {
        return urlsIntegracao;
    }

    public void setUrlsIntegracao(List<IBMURLIntegracao> urlsIntegracao) {
        this.urlsIntegracao = urlsIntegracao;
    }

    public List<String> getSistemasExternos() {
        return sistemasExternos;
    }

    public void setSistemasExternos(List<String> sistemasExternos) {
        this.sistemasExternos = sistemasExternos;
    }

    public List<String> getGlossario() {
        return glossario;
    }

    public void setGlossario(List<String> glossario) {
        this.glossario = glossario;
    }
}


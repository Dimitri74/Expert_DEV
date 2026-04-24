package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracao tipada para especificacao de caso de uso (UC).
 */
public class IBMExtracaoUC extends IBMArtefatoExtracao {

    private String objetivo;
    private List<String> atores = new ArrayList<String>();
    private String preCondicao;
    private String posCondicao;
    private List<IBMRegraNegocio> regrasNegocio = new ArrayList<IBMRegraNegocio>();
    private List<IBMFluxo> fluxos = new ArrayList<IBMFluxo>();

    public IBMExtracaoUC() {
        setTipoArtefato(IBMTipoArtefato.INTEGRACAO_UC);
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public List<String> getAtores() {
        return atores;
    }

    public void setAtores(List<String> atores) {
        this.atores = atores;
    }

    public String getPreCondicao() {
        return preCondicao;
    }

    public void setPreCondicao(String preCondicao) {
        this.preCondicao = preCondicao;
    }

    public String getPosCondicao() {
        return posCondicao;
    }

    public void setPosCondicao(String posCondicao) {
        this.posCondicao = posCondicao;
    }

    public List<IBMRegraNegocio> getRegrasNegocio() {
        return regrasNegocio;
    }

    public void setRegrasNegocio(List<IBMRegraNegocio> regrasNegocio) {
        this.regrasNegocio = regrasNegocio;
    }

    public List<IBMFluxo> getFluxos() {
        return fluxos;
    }

    public void setFluxos(List<IBMFluxo> fluxos) {
        this.fluxos = fluxos;
    }
}


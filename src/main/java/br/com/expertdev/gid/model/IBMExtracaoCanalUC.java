package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracao tipada para UC de canais digitais.
 */
public class IBMExtracaoCanalUC extends IBMArtefatoExtracao {

    private String nomeCasoUso;
    private String canal;
    private List<IBMFluxo> fluxos = new ArrayList<IBMFluxo>();
    private List<IBMRegraNegocio> regrasNegocio = new ArrayList<IBMRegraNegocio>();

    public IBMExtracaoCanalUC() {
        setTipoArtefato(IBMTipoArtefato.CANAIS_UC);
    }

    public String getNomeCasoUso() {
        return nomeCasoUso;
    }

    public void setNomeCasoUso(String nomeCasoUso) {
        this.nomeCasoUso = nomeCasoUso;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public List<IBMFluxo> getFluxos() {
        return fluxos;
    }

    public void setFluxos(List<IBMFluxo> fluxos) {
        this.fluxos = fluxos;
    }

    public List<IBMRegraNegocio> getRegrasNegocio() {
        return regrasNegocio;
    }

    public void setRegrasNegocio(List<IBMRegraNegocio> regrasNegocio) {
        this.regrasNegocio = regrasNegocio;
    }
}


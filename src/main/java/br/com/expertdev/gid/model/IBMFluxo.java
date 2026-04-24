package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoFluxo;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluxo de UC/DI em ordem, com tipo semantico IBM.
 */
public class IBMFluxo {

    private IBMTipoFluxo tipoFluxo = IBMTipoFluxo.OUTRO;
    private String titulo;
    private List<String> passos = new ArrayList<String>();

    public IBMTipoFluxo getTipoFluxo() {
        return tipoFluxo;
    }

    public void setTipoFluxo(IBMTipoFluxo tipoFluxo) {
        this.tipoFluxo = tipoFluxo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getPassos() {
        return passos;
    }

    public void setPassos(List<String> passos) {
        this.passos = passos;
    }
}


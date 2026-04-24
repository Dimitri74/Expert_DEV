package br.com.expertdev.gid.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado do filtro de ruido do layout IBM.
 */
public class IBMRuidoFilterResult {

    private String textoLimpo;
    private int linhasDescartadas;
    private List<String> marcadoresRuido = new ArrayList<String>();

    public String getTextoLimpo() {
        return textoLimpo;
    }

    public void setTextoLimpo(String textoLimpo) {
        this.textoLimpo = textoLimpo;
    }

    public int getLinhasDescartadas() {
        return linhasDescartadas;
    }

    public void setLinhasDescartadas(int linhasDescartadas) {
        this.linhasDescartadas = linhasDescartadas;
    }

    public List<String> getMarcadoresRuido() {
        return marcadoresRuido;
    }

    public void setMarcadoresRuido(List<String> marcadoresRuido) {
        this.marcadoresRuido = marcadoresRuido;
    }
}


package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa o resultado da classificacao de layout para um documento IBM.
 */
public class IBMClassificacaoDocumento {

    private String nomeArquivo;
    private IBMTipoArtefato tipoDetectado = IBMTipoArtefato.DESCONHECIDO;
    private int confianca;
    private int textoBrutoCaracteres;
    private int textoLimpoCaracteres;
    private int linhasRuidoDescartadas;
    private String observacaoLeitura;
    private String previewTextoLimpo;
    private List<String> sinaisDeteccao = new ArrayList<String>();

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public IBMTipoArtefato getTipoDetectado() {
        return tipoDetectado;
    }

    public void setTipoDetectado(IBMTipoArtefato tipoDetectado) {
        this.tipoDetectado = tipoDetectado;
    }

    public int getConfianca() {
        return confianca;
    }

    public void setConfianca(int confianca) {
        this.confianca = confianca;
    }

    public int getTextoBrutoCaracteres() {
        return textoBrutoCaracteres;
    }

    public void setTextoBrutoCaracteres(int textoBrutoCaracteres) {
        this.textoBrutoCaracteres = textoBrutoCaracteres;
    }

    public int getTextoLimpoCaracteres() {
        return textoLimpoCaracteres;
    }

    public void setTextoLimpoCaracteres(int textoLimpoCaracteres) {
        this.textoLimpoCaracteres = textoLimpoCaracteres;
    }

    public int getLinhasRuidoDescartadas() {
        return linhasRuidoDescartadas;
    }

    public void setLinhasRuidoDescartadas(int linhasRuidoDescartadas) {
        this.linhasRuidoDescartadas = linhasRuidoDescartadas;
    }

    public String getObservacaoLeitura() {
        return observacaoLeitura;
    }

    public void setObservacaoLeitura(String observacaoLeitura) {
        this.observacaoLeitura = observacaoLeitura;
    }

    public String getPreviewTextoLimpo() {
        return previewTextoLimpo;
    }

    public void setPreviewTextoLimpo(String previewTextoLimpo) {
        this.previewTextoLimpo = previewTextoLimpo;
    }

    public List<String> getSinaisDeteccao() {
        return sinaisDeteccao;
    }

    public void setSinaisDeteccao(List<String> sinaisDeteccao) {
        this.sinaisDeteccao = sinaisDeteccao;
    }
}


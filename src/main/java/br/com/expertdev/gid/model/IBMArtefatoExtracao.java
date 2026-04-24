package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.ArrayList;
import java.util.List;

/**
 * Envelope base de extracao para qualquer documento IBM-GID.
 */
public class IBMArtefatoExtracao {

    private IBMTipoArtefato tipoArtefato = IBMTipoArtefato.DESCONHECIDO;
    private String rtcNumero;
    private String ucCodigo;
    private String nomeArquivoOrigem;
    private int confiancaDeteccao;
    private List<String> avisos = new ArrayList<String>();

    public IBMTipoArtefato getTipoArtefato() {
        return tipoArtefato;
    }

    public void setTipoArtefato(IBMTipoArtefato tipoArtefato) {
        this.tipoArtefato = tipoArtefato;
    }

    public String getRtcNumero() {
        return rtcNumero;
    }

    public void setRtcNumero(String rtcNumero) {
        this.rtcNumero = rtcNumero;
    }

    public String getUcCodigo() {
        return ucCodigo;
    }

    public void setUcCodigo(String ucCodigo) {
        this.ucCodigo = ucCodigo;
    }

    public String getNomeArquivoOrigem() {
        return nomeArquivoOrigem;
    }

    public void setNomeArquivoOrigem(String nomeArquivoOrigem) {
        this.nomeArquivoOrigem = nomeArquivoOrigem;
    }

    public int getConfiancaDeteccao() {
        return confiancaDeteccao;
    }

    public void setConfiancaDeteccao(int confiancaDeteccao) {
        this.confiancaDeteccao = confiancaDeteccao;
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public void setAvisos(List<String> avisos) {
        this.avisos = avisos;
    }
}


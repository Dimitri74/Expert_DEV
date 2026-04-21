package br.com.expertdev.model;

import java.util.ArrayList;
import java.util.List;

public class ResultadoProcessamento {

    private final String url;
    private boolean sucesso;
    private String textoExtraido;
    private List<ImagemInfo> imagens;
    private String erro;
    private String observacao;

    public ResultadoProcessamento(String url) {
        this.url = url;
        this.imagens = new ArrayList<>();
    }

    public String getUrl() {
        return url;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getTextoExtraido() {
        return textoExtraido;
    }

    public void setTextoExtraido(String textoExtraido) {
        this.textoExtraido = textoExtraido;
    }

    public List<ImagemInfo> getImagens() {
        return imagens;
    }

    public void setImagens(List<ImagemInfo> imagens) {
        if (imagens == null) {
            this.imagens = new ArrayList<>();
            return;
        }
        this.imagens = imagens;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}


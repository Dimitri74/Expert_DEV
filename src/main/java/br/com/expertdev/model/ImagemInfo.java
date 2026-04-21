package br.com.expertdev.model;

public class ImagemInfo {

    private final String paginaUrl;
    private final String src;
    private final String alt;

    public ImagemInfo(String paginaUrl, String src, String alt) {
        this.paginaUrl = paginaUrl;
        this.src = src;
        this.alt = alt;
    }

    public String getPaginaUrl() {
        return paginaUrl;
    }

    public String getSrc() {
        return src;
    }

    public String getAlt() {
        return alt;
    }
}


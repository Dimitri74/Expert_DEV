package br.com.expertdev.model;

public class ExecucaoConsolidada {

    private final String regrasExtraidas;
    private final String imagensEncontradas;
    private final String promptPronto;
    private final String resumoExecucao;
    private final String errosProcessamento;
    private final String arquivoWord;
    private final String arquivoPdf;
    private final int totalUrls;
    private final int urlsComSucesso;
    private final int urlsComFalha;
    private final int totalImagens;
    private final long tempoTotalSegundos;

    public ExecucaoConsolidada(String regrasExtraidas,
                               String imagensEncontradas,
                               String promptPronto,
                               String resumoExecucao,
                               String errosProcessamento,
                               String arquivoWord,
                               String arquivoPdf,
                               int totalUrls,
                               int urlsComSucesso,
                               int urlsComFalha,
                               int totalImagens,
                               long tempoTotalSegundos) {
        this.regrasExtraidas = regrasExtraidas;
        this.imagensEncontradas = imagensEncontradas;
        this.promptPronto = promptPronto;
        this.resumoExecucao = resumoExecucao;
        this.errosProcessamento = errosProcessamento;
        this.arquivoWord = arquivoWord;
        this.arquivoPdf = arquivoPdf;
        this.totalUrls = totalUrls;
        this.urlsComSucesso = urlsComSucesso;
        this.urlsComFalha = urlsComFalha;
        this.totalImagens = totalImagens;
        this.tempoTotalSegundos = tempoTotalSegundos;
    }

    public String getRegrasExtraidas() {
        return regrasExtraidas;
    }

    public String getImagensEncontradas() {
        return imagensEncontradas;
    }

    public String getPromptPronto() {
        return promptPronto;
    }

    public String getResumoExecucao() {
        return resumoExecucao;
    }

    public String getErrosProcessamento() {
        return errosProcessamento;
    }

    public String getArquivoWord() {
        return arquivoWord;
    }

    public String getArquivoPdf() {
        return arquivoPdf;
    }

    public int getTotalUrls() {
        return totalUrls;
    }

    public int getUrlsComSucesso() {
        return urlsComSucesso;
    }

    public int getUrlsComFalha() {
        return urlsComFalha;
    }

    public int getTotalImagens() {
        return totalImagens;
    }

    public long getTempoTotalSegundos() {
        return tempoTotalSegundos;
    }

    public boolean possuiSucesso() {
        return urlsComSucesso > 0;
    }

    public boolean possuiErros() {
        return errosProcessamento != null && !errosProcessamento.trim().isEmpty();
    }
}


package br.com.expertdev.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo de auditoria para rastreabilidade de processamentos.
 * Armazena RTC, UC, data e metadados do processamento.
 */
public class RegistroAuditoria {

    private long id;
    private String rtcNumero;
    private String ucCodigo;
    private String ucDescricao;
    private LocalDateTime dataProcessamento;
    private String modoGeracao;
    private String provider;
    private String urlsOuArquivo;
    private String status;
    private String promptGerado;

    // Construtores
    public RegistroAuditoria() {
    }

    public RegistroAuditoria(String rtcNumero, String ucCodigo, String ucDescricao,
                            String modoGeracao, String provider, String urlsOuArquivo) {
        this.rtcNumero = rtcNumero;
        this.ucCodigo = ucCodigo;
        this.ucDescricao = ucDescricao;
        this.dataProcessamento = LocalDateTime.now();
        this.modoGeracao = modoGeracao;
        this.provider = provider;
        this.urlsOuArquivo = urlsOuArquivo;
        this.status = "INICIADO";
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getUcDescricao() {
        return ucDescricao;
    }

    public void setUcDescricao(String ucDescricao) {
        this.ucDescricao = ucDescricao;
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }

    public void setDataProcessamento(LocalDateTime dataProcessamento) {
        this.dataProcessamento = dataProcessamento;
    }

    public String getModoGeracao() {
        return modoGeracao;
    }

    public void setModoGeracao(String modoGeracao) {
        this.modoGeracao = modoGeracao;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUrlsOuArquivo() {
        return urlsOuArquivo;
    }

    public void setUrlsOuArquivo(String urlsOuArquivo) {
        this.urlsOuArquivo = urlsOuArquivo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPromptGerado() {
        return promptGerado;
    }

    public void setPromptGerado(String promptGerado) {
        this.promptGerado = promptGerado;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] RTC %s | %s (%s) | %s",
                dataProcessamento.format(fmt),
                rtcNumero,
                ucCodigo,
                ucDescricao,
                status);
    }
}


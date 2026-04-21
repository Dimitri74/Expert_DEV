package br.com.expertdev.model;

import java.time.LocalDateTime;

/**
 * Modelo para métricas de performance comparativa entre Scrum tradicional e ExpertDev.
 */
public class MetricaPerformance {

    private Long id;
    private String rtcNumero;
    private Double estimativaPoker; // em horas
    private LocalDateTime inicioScrum;
    private LocalDateTime fimScrum;
    private LocalDateTime inicioExpertDev;
    private LocalDateTime fimExpertDev;
    private String complexidade;
    private String status;

    public MetricaPerformance() {
    }

    public MetricaPerformance(String rtcNumero) {
        this.rtcNumero = rtcNumero;
        this.status = "PENDENTE";
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRtcNumero() {
        return rtcNumero;
    }

    public void setRtcNumero(String rtcNumero) {
        this.rtcNumero = rtcNumero;
    }

    public Double getEstimativaPoker() {
        return estimativaPoker;
    }

    public void setEstimativaPoker(Double estimativaPoker) {
        this.estimativaPoker = estimativaPoker;
    }

    public LocalDateTime getInicioScrum() {
        return inicioScrum;
    }

    public void setInicioScrum(LocalDateTime inicioScrum) {
        this.inicioScrum = inicioScrum;
    }

    public LocalDateTime getFimScrum() {
        return fimScrum;
    }

    public void setFimScrum(LocalDateTime fimScrum) {
        this.fimScrum = fimScrum;
    }

    public LocalDateTime getInicioExpertDev() {
        return inicioExpertDev;
    }

    public void setInicioExpertDev(LocalDateTime inicioExpertDev) {
        this.inicioExpertDev = inicioExpertDev;
    }

    public LocalDateTime getFimExpertDev() {
        return fimExpertDev;
    }

    public void setFimExpertDev(LocalDateTime fimExpertDev) {
        this.fimExpertDev = fimExpertDev;
    }

    public String getComplexidade() {
        return complexidade;
    }

    public void setComplexidade(String complexidade) {
        this.complexidade = complexidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

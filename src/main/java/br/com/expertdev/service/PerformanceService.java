package br.com.expertdev.service;

import br.com.expertdev.model.MetricaPerformance;
import java.util.List;

/**
 * Serviço para gerenciar regras de negócio de performance.
 */
public class PerformanceService {

    private final AuditoriaService auditoriaService;

    public PerformanceService(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    public void salvarOuAtualizar(MetricaPerformance metrica) {
        MetricaPerformance existente = auditoriaService.buscarPerformancePorRTC(metrica.getRtcNumero());
        if (existente == null) {
            auditoriaService.inserirPerformance(metrica);
        } else {
            auditoriaService.atualizarPerformance(metrica);
        }
    }

    public MetricaPerformance obterPorRTC(String rtc) {
        return auditoriaService.buscarPerformancePorRTC(rtc);
    }

    public List<MetricaPerformance> listarTodos() {
        return auditoriaService.listarPerformance();
    }
}

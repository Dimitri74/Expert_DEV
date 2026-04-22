package br.com.expertdev.service;

import br.com.expertdev.model.MetricaPerformance;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Serviço para gerenciar regras de negócio de performance.
 */
public class PerformanceService {

    private final AuditoriaService auditoriaService;

    public PerformanceService(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    public void salvarOuAtualizar(MetricaPerformance metrica) {
        MetricaPerformance existente;
        if (metrica.getAuthUsername() != null && !metrica.getAuthUsername().trim().isEmpty()) {
            existente = auditoriaService.buscarPerformancePorRTCeUsuario(metrica.getRtcNumero(), metrica.getAuthUsername());
        } else {
            existente = auditoriaService.buscarPerformancePorRTC(metrica.getRtcNumero());
        }
        if (existente == null) {
            auditoriaService.inserirPerformance(metrica);
        } else {
            auditoriaService.atualizarPerformance(metrica);
        }
    }

    public MetricaPerformance obterPorRTC(String rtc) {
        return auditoriaService.buscarPerformancePorRTC(rtc);
    }

    public MetricaPerformance obterPorRTCeUsuario(String rtc, String authUsername) {
        return auditoriaService.buscarPerformancePorRTCeUsuario(rtc, authUsername);
    }

    public List<MetricaPerformance> listarTodos() {
        return auditoriaService.listarPerformance();
    }

    public List<MetricaPerformance> listarTodosPorUsuario(String authUsername) {
        return auditoriaService.listarPerformancePorUsuario(authUsername);
    }

    public List<String> sugerirRtcsPorUsuario(String authUsername, String filtro, int limite) {
        List<MetricaPerformance> metricas = listarTodosPorUsuario(authUsername);
        List<String> sugestoes = new ArrayList<>();
        Set<String> vistos = new HashSet<>();

        String prefixo = filtro == null ? "" : filtro.trim().toLowerCase();
        int max = Math.max(1, limite);

        // 1) Prioriza RTCs em aberto (PENDENTES) do mais recente para o mais antigo
        for (MetricaPerformance m : metricas) {
            if (!estaEmAberto(m)) {
                continue;
            }
            String rtc = m.getRtcNumero();
            if (!rtcValidoParaSugestao(rtc, prefixo, vistos)) {
                continue;
            }
            sugestoes.add("[PENDENTE] " + rtc);
            vistos.add(rtc);
        }

        // 2) Completa com os demais RTCs (CONCLUÍDOS)
        for (MetricaPerformance m : metricas) {
            String rtc = m.getRtcNumero();
            if (!rtcValidoParaSugestao(rtc, prefixo, vistos)) {
                continue;
            }
            sugestoes.add("[CONCLUÍDO] " + rtc);
            vistos.add(rtc);
        }

        // 3) Limita o resultado final para não sobrecarregar a UI
        if (sugestoes.size() > max) {
            return sugestoes.subList(0, max);
        }

        return sugestoes;
    }

    private boolean rtcValidoParaSugestao(String rtc, String prefixo, Set<String> vistos) {
        if (rtc == null || rtc.trim().isEmpty()) {
            return false;
        }
        if (vistos.contains(rtc)) {
            return false;
        }
        return prefixo.isEmpty() || rtc.toLowerCase().contains(prefixo);
    }

    private boolean estaEmAberto(MetricaPerformance metrica) {
        if (metrica == null) {
            return false;
        }
        if (metrica.getFimExpertDev() == null) {
            return true;
        }
        String status = metrica.getStatus();
        return status != null && !"CONCLUIDO".equalsIgnoreCase(status.trim());
    }
}

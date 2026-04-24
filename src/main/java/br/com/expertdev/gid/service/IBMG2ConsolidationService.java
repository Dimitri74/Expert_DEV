package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.*;
import java.util.*;

/**
 * Serviço de Consolidação (Etapa G2).
 * Agrupa artefatos extraídos (B4) por RTC, detecta referências cruzadas
 * e prepara dados para exportação em múltiplos formatos.
 */
public class IBMG2ConsolidationService {

    private Map<String, IBMContextoRTC> contextosPorRTC;
    private Map<String, IBMConsolidacaoDependencias> dependenciasPorRTC;
    private List<String> avisos;
    private long timestampInicio;

    // Construtores
    public IBMG2ConsolidationService() {
        this.contextosPorRTC = new HashMap<>();
        this.dependenciasPorRTC = new HashMap<>();
        this.avisos = new ArrayList<>();
        this.timestampInicio = System.currentTimeMillis();
    }

    /**
     * Consolida uma lista de artefatos extraídos, agrupando por RTC.
     * @param artefatos Lista de artefatos extraídos por B4
     * @return Mapa de contextos consolidados por RTC
     */
    public Map<String, IBMContextoRTC> consolidarPorRTC(List<IBMArtefatoExtracao> artefatos) {
        if (artefatos == null || artefatos.isEmpty()) {
            avisos.add("AVISO: Lista de artefatos vazia fornecida para consolidação.");
            return contextosPorRTC;
        }

        // Fase 1: Agrupar por RTC
        Map<String, List<IBMArtefatoExtracao>> agrupamentoRTC = new HashMap<>();
        for (IBMArtefatoExtracao artefato : artefatos) {
            if (artefato.getRtcNumero() == null || artefato.getRtcNumero().isEmpty()) {
                avisos.add("AVISO: Artefato sem RTC identificado - será ignorado: " +
                           artefato.getTipoArtefato());
                continue;
            }

            String rtc = artefato.getRtcNumero();
            agrupamentoRTC.computeIfAbsent(rtc, k -> new ArrayList<>()).add(artefato);
        }

        // Fase 2: Criar contextos consolidados
        for (Map.Entry<String, List<IBMArtefatoExtracao>> entry : agrupamentoRTC.entrySet()) {
            String rtc = entry.getKey();
            List<IBMArtefatoExtracao> artesArtefatos = entry.getValue();

            IBMContextoRTC contexto = new IBMContextoRTC();
            contexto.setRtcNumero(rtc);

            // Extrair UC (se houver)
            for (IBMArtefatoExtracao artefato : artesArtefatos) {
                if (artefato.getUcCodigo() != null && !artefato.getUcCodigo().isEmpty()) {
                    contexto.setUcCodigo(artefato.getUcCodigo());
                    break;
                }
            }

            // Anexar artefatos complementares
            List<IBMArtefatoExtracao> artefatosComplementares = contexto.getArtefatosComplementares();
            for (IBMArtefatoExtracao artefato : artesArtefatos) {
                artefatosComplementares.add(artefato);
            }

            contextosPorRTC.put(rtc, contexto);

            // Criar dependências para este RTC
            IBMConsolidacaoDependencias deps = construirDependenciasRTC(rtc, artesArtefatos);
            dependenciasPorRTC.put(rtc, deps);
        }

        avisos.add(String.format("INFO: Consolidação concluída. %d RTC(s) agrupados de %d artefatos.",
                                 contextosPorRTC.size(), artefatos.size()));
        return contextosPorRTC;
    }

    /**
     * Constrói o mapa de dependências para um RTC específico.
     */
    private IBMConsolidacaoDependencias construirDependenciasRTC(String rtc,
                                                                  List<IBMArtefatoExtracao> artefatos) {
        IBMConsolidacaoDependencias consolidacao = new IBMConsolidacaoDependencias(rtc);

        // Análise simplificada: cada artefato pode depender do anterior
        for (int i = 0; i < artefatos.size(); i++) {
            IBMArtefatoExtracao atual = artefatos.get(i);

            if (i > 0) {
                IBMArtefatoExtracao anterior = artefatos.get(i - 1);

                // Criar relação básica (tipo de relação inferido pelo tipo)
                String tipoAnterior = anterior.getTipoArtefato().toString();
                String tipoAtual = atual.getTipoArtefato().toString();
                String tipoRelacao = inferirTipoRelacao(tipoAnterior, tipoAtual);

                IBMRelacaoDependencia relacao = new IBMRelacaoDependencia(
                    anterior.getRtcNumero() + ":" + tipoAnterior,
                    tipoAnterior,
                    atual.getRtcNumero() + ":" + tipoAtual,
                    tipoAtual,
                    tipoRelacao
                );
                relacao.setConfianca(80.0); // Confiança padrão para relações inferidas
                relacao.setDescricao("Relação sequencial entre tipos");

                consolidacao.adicionarRelacao(relacao);
            }
        }

        return consolidacao;
    }

    /**
     * Infere o tipo de relação baseado nos tipos de artefato.
     */
    private String inferirTipoRelacao(String tipoOrigem, String tipoDestino) {
        // Lógica simplificada de inferência
        if (tipoOrigem.contains("INTEGRACAO_UC") && tipoDestino.contains("INTEGRACAO_DI")) {
            return "refina";
        } else if (tipoOrigem.contains("API") && tipoDestino.contains("ESPECIFICACAO")) {
            return "documenta";
        } else if (tipoOrigem.contains("CANAIS") && tipoDestino.contains("MSG")) {
            return "envia";
        } else {
            return "relacionada";
        }
    }

    /**
     * Obtém o contexto consolidado para um RTC específico.
     */
    public IBMContextoRTC obterContextoRTC(String rtc) {
        return contextosPorRTC.get(rtc);
    }

    /**
     * Obtém as dependências para um RTC específico.
     */
    public IBMConsolidacaoDependencias obterDependenciasRTC(String rtc) {
        return dependenciasPorRTC.get(rtc);
    }

    /**
     * Lista todos os RTCs consolidados.
     */
    public Set<String> listarRTCsConsolidados() {
        return contextosPorRTC.keySet();
    }

    /**
     * Obtém estatísticas da consolidação.
     */
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRTCs", contextosPorRTC.size());
        stats.put("totalArtefatos", contextosPorRTC.values().stream()
            .mapToInt(ctx -> ctx.getArtefatosComplementares().size())
            .sum());
        stats.put("totalRelacoes", dependenciasPorRTC.values().stream()
            .mapToInt(IBMConsolidacaoDependencias::getTotalRelacoes)
            .sum());
        stats.put("totalAvisos", avisos.size());
        stats.put("tempoExecucao", System.currentTimeMillis() - timestampInicio);
        return stats;
    }

    /**
     * Retorna lista de avisos acumulados.
     */
    public List<String> obterAvisos() {
        return new ArrayList<>(avisos);
    }

    /**
     * Limpa estado interno e reinicia.
     */
    public void limpar() {
        contextosPorRTC.clear();
        dependenciasPorRTC.clear();
        avisos.clear();
        timestampInicio = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("IBMG2ConsolidationService{RTCs:%d, avisos:%d}",
                contextosPorRTC.size(), avisos.size());
    }
}


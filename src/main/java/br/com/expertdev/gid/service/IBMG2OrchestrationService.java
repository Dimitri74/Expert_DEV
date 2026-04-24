package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.*;
import java.io.File;
import java.util.*;

/**
 * Serviço de Orquestração G2.
 * Coordena o pipeline completo: leitura (B2) -> classificação (B3) -> extração (B4) -> consolidação (G2).
 * Fornece interface única para processamento end-to-end de documentos.
 */
public class IBMG2OrchestrationService {

    private final IBMB3ClassificationService b3Service;
    private final IBMB4ExtractionService b4Service;
    private final IBMG2ConsolidationService g2Service;

    private List<IBMArtefatoExtracao> ultimosArtefatos;
    private Map<String, IBMContextoRTC> ultimaConsolidacao;
    private List<String> avisos;

    // Construtores
    public IBMG2OrchestrationService() {
        this.b3Service = new IBMB3ClassificationService();
        this.b4Service = new IBMB4ExtractionService();
        this.g2Service = new IBMG2ConsolidationService();
        this.ultimosArtefatos = new ArrayList<>();
        this.ultimaConsolidacao = new HashMap<>();
        this.avisos = new ArrayList<>();
    }

    /**
     * Executa pipeline completo B2->B3->B4->G2.
     * @param arquivos Arquivos Word para processar
     * @return Mapa consolidado de contextos por RTC
     */
    public Map<String, IBMContextoRTC> processarCompleto(List<File> arquivos) {
        limpar();

        if (arquivos == null || arquivos.isEmpty()) {
            avisos.add("ERRO: Lista de arquivos vazia para processamento.");
            return ultimaConsolidacao;
        }

        long tempoInicio = System.currentTimeMillis();

        try {
            // Etapa B3: Classificação
            avisos.add("INFO: Iniciando Etapa B3 (Classificação)...");
            List<IBMClassificacaoDocumento> classificacoes = b3Service.classificarArquivos(arquivos);
            if (classificacoes == null || classificacoes.isEmpty()) {
                avisos.add("AVISO: Nenhum documento classificado na Etapa B3.");
                return ultimaConsolidacao;
            }
            avisos.add(String.format("INFO: B3 concluído - %d documento(s) classificado(s).",
                                     classificacoes.size()));

            // Etapa B4: Extração Semântica
            avisos.add("INFO: Iniciando Etapa B4 (Extração Semântica)...");
            List<IBMArtefatoExtracao> artefatos = b4Service.extrairArquivos(arquivos);
            if (artefatos == null || artefatos.isEmpty()) {
                avisos.add("AVISO: Nenhum artefato extraído na Etapa B4.");
                return ultimaConsolidacao;
            }
            this.ultimosArtefatos = new ArrayList<>(artefatos);
            avisos.add(String.format("INFO: B4 concluído - %d artefato(s) extraído(s).",
                                     artefatos.size()));

            // Etapa G2: Consolidação
            avisos.add("INFO: Iniciando Etapa G2 (Consolidação)...");
            this.ultimaConsolidacao = g2Service.consolidarPorRTC(artefatos);
            avisos.addAll(g2Service.obterAvisos());
            avisos.add(String.format("INFO: G2 concluído - %d RTC(s) consolidado(s).",
                                     ultimaConsolidacao.size()));

        } catch (Exception e) {
            avisos.add("ERRO em pipeline: " + e.getMessage());
            e.printStackTrace();
        }

        long tempoTotal = System.currentTimeMillis() - tempoInicio;
        avisos.add(String.format("INFO: Pipeline completo finalizado em %.2f segundos.",
                                 tempoTotal / 1000.0));

        return ultimaConsolidacao;
    }

    /**
     * Executa apenas B4 + G2 (útil quando B3 já foi executado).
     */
    public Map<String, IBMContextoRTC> processarExtracacaoConsolidacao(List<File> arquivos) {
        limpar();

        if (arquivos == null || arquivos.isEmpty()) {
            avisos.add("ERRO: Lista de arquivos vazia.");
            return ultimaConsolidacao;
        }

        try {
            avisos.add("INFO: Iniciando B4 + G2...");

            // B4
            List<IBMArtefatoExtracao> artefatos = b4Service.extrairArquivos(arquivos);
            if (artefatos == null || artefatos.isEmpty()) {
                avisos.add("AVISO: Nenhum artefato extraído.");
                return ultimaConsolidacao;
            }
            this.ultimosArtefatos = new ArrayList<>(artefatos);

            // G2
            this.ultimaConsolidacao = g2Service.consolidarPorRTC(artefatos);
            avisos.addAll(g2Service.obterAvisos());

            avisos.add(String.format("INFO: B4+G2 completo - %d RTC(s) consolidado(s).",
                                     ultimaConsolidacao.size()));

        } catch (Exception e) {
            avisos.add("ERRO: " + e.getMessage());
        }

        return ultimaConsolidacao;
    }

    /**
     * Processa apenas G2 com artefatos já extraídos.
     */
    public Map<String, IBMContextoRTC> consolidarArtefatos(List<IBMArtefatoExtracao> artefatos) {
        limpar();

        if (artefatos == null || artefatos.isEmpty()) {
            avisos.add("ERRO: Lista de artefatos vazia.");
            return ultimaConsolidacao;
        }

        this.ultimosArtefatos = new ArrayList<>(artefatos);
        this.ultimaConsolidacao = g2Service.consolidarPorRTC(artefatos);
        avisos.addAll(g2Service.obterAvisos());

        return ultimaConsolidacao;
    }

    /**
     * Obtém última consolidação realizada.
     */
    public Map<String, IBMContextoRTC> obterUltimaConsolidacao() {
        return ultimaConsolidacao;
    }

    /**
     * Obtém último conjunto de artefatos extraídos.
     */
    public List<IBMArtefatoExtracao> obterUltimosArtefatos() {
        return ultimosArtefatos;
    }

    /**
     * Obtém dependências para RTC específico.
     */
    public IBMConsolidacaoDependencias obterDependenciasRTC(String rtc) {
        return g2Service.obterDependenciasRTC(rtc);
    }

    /**
     * Lista todos os RTCs consolidados.
     */
    public Set<String> listarRTCsConsolidados() {
        return ultimaConsolidacao.keySet();
    }

    /**
     * Obtém estatísticas gerais.
     */
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("rtcsConsolidados", ultimaConsolidacao.size());
        stats.put("artefatosExtraidos", ultimosArtefatos.size());
        stats.put("totalAvisos", avisos.size());

        if (ultimaConsolidacao.size() > 0) {
            stats.putAll(g2Service.obterEstatisticas());
        }

        return stats;
    }

    /**
     * Retorna todos os avisos acumulados.
     */
    public List<String> obterAvisos() {
        return new ArrayList<>(avisos);
    }

    /**
     * Limpa estado e reinicia.
     */
    public void limpar() {
        ultimosArtefatos.clear();
        ultimaConsolidacao.clear();
        avisos.clear();
        g2Service.limpar();
    }

    @Override
    public String toString() {
        return String.format("IBMG2OrchestrationService{RTCs consolidados:%d, avisos:%d}",
                ultimaConsolidacao.size(), avisos.size());
    }
}


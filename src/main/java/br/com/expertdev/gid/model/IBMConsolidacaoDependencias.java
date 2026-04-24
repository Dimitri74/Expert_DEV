package br.com.expertdev.gid.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa a consolidação de dependências entre artefatos extraídos.
 * Agrupa relações por RTC e mantém mapa de referências cruzadas.
 */
public class IBMConsolidacaoDependencias {

    private String rtcNumero;
    private List<IBMRelacaoDependencia> relacoes;
    private Map<String, Integer> contadorPorTipo;  // Conta relações por tipo
    private int totalRelacoes;
    private long timestampConsolidacao;
    private List<String> avisosCiclos;  // Avisos de ciclos detectados
    private List<String> avisos;        // Avisos gerais da consolidação

    // Construtores
    public IBMConsolidacaoDependencias() {
        this.relacoes = new ArrayList<>();
        this.contadorPorTipo = new HashMap<>();
        this.avisosCiclos = new ArrayList<>();
        this.avisos = new ArrayList<>();
        this.timestampConsolidacao = System.currentTimeMillis();
    }

    public IBMConsolidacaoDependencias(String rtcNumero) {
        this();
        this.rtcNumero = rtcNumero;
    }

    // Métodos de manipulação
    public void adicionarRelacao(IBMRelacaoDependencia relacao) {
        if (relacao == null) return;
        this.relacoes.add(relacao);
        this.totalRelacoes++;

        // Atualizar contador por tipo de relação
        String tipo = relacao.getTipoRelacao();
        this.contadorPorTipo.put(tipo, contadorPorTipo.getOrDefault(tipo, 0) + 1);
    }

    public void adicionarAviso(String aviso) {
        if (aviso != null && !aviso.isEmpty()) {
            this.avisos.add(aviso);
        }
    }

    public void adicionarAvisoCiclo(String cicloDescricao) {
        if (cicloDescricao != null && !cicloDescricao.isEmpty()) {
            this.avisosCiclos.add(cicloDescricao);
        }
    }

    public List<IBMRelacaoDependencia> obterRelacoesOrigem(String codigo) {
        List<IBMRelacaoDependencia> resultado = new ArrayList<>();
        for (IBMRelacaoDependencia rel : relacoes) {
            if (rel.getCodigoOrigem().equals(codigo)) {
                resultado.add(rel);
            }
        }
        return resultado;
    }

    public List<IBMRelacaoDependencia> obterRelacoesDestino(String codigo) {
        List<IBMRelacaoDependencia> resultado = new ArrayList<>();
        for (IBMRelacaoDependencia rel : relacoes) {
            if (rel.getCodigoDestino().equals(codigo)) {
                resultado.add(rel);
            }
        }
        return resultado;
    }

    public int contarRelacoesDoTipo(String tipo) {
        return contadorPorTipo.getOrDefault(tipo, 0);
    }

    // Getters e Setters
    public String getRtcNumero() {
        return rtcNumero;
    }

    public void setRtcNumero(String rtcNumero) {
        this.rtcNumero = rtcNumero;
    }

    public List<IBMRelacaoDependencia> getRelacoes() {
        return relacoes;
    }

    public void setRelacoes(List<IBMRelacaoDependencia> relacoes) {
        this.relacoes = relacoes;
        this.totalRelacoes = relacoes != null ? relacoes.size() : 0;
    }

    public Map<String, Integer> getContadorPorTipo() {
        return contadorPorTipo;
    }

    public int getTotalRelacoes() {
        return totalRelacoes;
    }

    public long getTimestampConsolidacao() {
        return timestampConsolidacao;
    }

    public List<String> getAvisosCiclos() {
        return avisosCiclos;
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public boolean temCiclos() {
        return !avisosCiclos.isEmpty();
    }

    public int contarAvisos() {
        return avisos.size() + avisosCiclos.size();
    }

    @Override
    public String toString() {
        return String.format("IBMConsolidacaoDependencias{RTC:%s, relacoes:%d, avisos:%d, ciclos:%d}",
                rtcNumero, totalRelacoes, avisos.size(), avisosCiclos.size());
    }
}


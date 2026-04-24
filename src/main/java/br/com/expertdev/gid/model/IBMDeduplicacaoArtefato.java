package br.com.expertdev.gid.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa o resultado de deduplicação de um artefato.
 * Mantém registro de quais itens foram marcados como duplicados e merged.
 */
public class IBMDeduplicacaoArtefato {

    private String tipoArtefato;      // REGRA, MENSAGEM, URL, PARAMETRO, etc
    private int totalAntes;           // Total de itens antes da deduplicação
    private int totalDepois;          // Total de itens após a deduplicação
    private int duplicadosRemovidos;  // Quantidade removida
    private double percentualReducao; // (totalAntes - totalDepois) / totalAntes * 100
    private List<String> itemsMergados;  // Registro de merges realizados
    private List<String> avisos;      // Avisos durante deduplicação

    // Construtores
    public IBMDeduplicacaoArtefato() {
        this.itemsMergados = new ArrayList<>();
        this.avisos = new ArrayList<>();
    }

    public IBMDeduplicacaoArtefato(String tipoArtefato, int totalAntes) {
        this();
        this.tipoArtefato = tipoArtefato;
        this.totalAntes = totalAntes;
        this.totalDepois = totalAntes;
    }

    // Métodos
    public void registrarMerge(String origem, String destino, String motivo) {
        String registro = String.format("MERGE [%s] -> [%s] (%s)", origem, destino, motivo);
        itemsMergados.add(registro);
        atualizarEstatisticas();
    }

    public void adicionarAviso(String aviso) {
        if (aviso != null && !aviso.isEmpty()) {
            avisos.add(aviso);
        }
    }

    private void atualizarEstatisticas() {
        this.duplicadosRemovidos = totalAntes - totalDepois;
        if (totalAntes > 0) {
            this.percentualReducao = (double) duplicadosRemovidos / totalAntes * 100.0;
        }
    }

    public void finalizarDeduplicacao(int novoTotal) {
        this.totalDepois = novoTotal;
        atualizarEstatisticas();
    }

    // Getters e Setters
    public String getTipoArtefato() {
        return tipoArtefato;
    }

    public void setTipoArtefato(String tipoArtefato) {
        this.tipoArtefato = tipoArtefato;
    }

    public int getTotalAntes() {
        return totalAntes;
    }

    public int getTotalDepois() {
        return totalDepois;
    }

    public void setTotalDepois(int totalDepois) {
        this.totalDepois = totalDepois;
        atualizarEstatisticas();
    }

    public int getDuplicadosRemovidos() {
        return duplicadosRemovidos;
    }

    public double getPercentualReducao() {
        return percentualReducao;
    }

    public List<String> getItemsMergados() {
        return itemsMergados;
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public boolean haveRemovedItems() {
        return duplicadosRemovidos > 0;
    }

    @Override
    public String toString() {
        return String.format("Dedup[%s]: %d -> %d (-%d, %.1f%% redução)",
                tipoArtefato, totalAntes, totalDepois, duplicadosRemovidos, percentualReducao);
    }
}


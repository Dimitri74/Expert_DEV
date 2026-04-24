package br.com.expertdev.pro.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de avaliação de checklist.
 * Tracks: pendente, OK, falhou, crítico.
 */
public class ChecklistResult {
    public enum StatusItem {
        PENDENTE("Pendente"),
        OK("✔ OK"),
        AVISO("⚠ Aviso"),
        FALHOU("✗ Falhou");

        private String label;

        StatusItem(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class ChecklistItem {
        private String titulo;
        private String descricao;
        private StatusItem status;
        private boolean critico;
        private String detalhes;

        public ChecklistItem(String titulo, String descricao, boolean critico) {
            this.titulo = titulo;
            this.descricao = descricao;
            this.critico = critico;
            this.status = StatusItem.PENDENTE;
        }

        // Getters e Setters
        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public StatusItem getStatus() {
            return status;
        }

        public void setStatus(StatusItem status) {
            this.status = status;
        }

        public boolean isCritico() {
            return critico;
        }

        public void setCritico(boolean critico) {
            this.critico = critico;
        }

        public String getDetalhes() {
            return detalhes;
        }

        public void setDetalhes(String detalhes) {
            this.detalhes = detalhes;
        }
    }

    private List<ChecklistItem> itens;
    private String categoria;
    private long timestampAvaliacao;
    private int scorePercentual;

    public ChecklistResult(String categoria) {
        this.categoria = categoria;
        this.itens = new ArrayList<>();
        this.timestampAvaliacao = System.currentTimeMillis();
        this.scorePercentual = 0;
    }

    public void adicionarItem(ChecklistItem item) {
        itens.add(item);
    }

    public void calcularScore() {
        if (itens.isEmpty()) {
            scorePercentual = 0;
            return;
        }
        long okCount = itens.stream()
                .filter(it -> it.status == StatusItem.OK)
                .count();
        scorePercentual = (int) ((okCount * 100) / itens.size());
    }

    public int getItensCriticosPendentes() {
        return (int) itens.stream()
                .filter(it -> it.critico && it.status == StatusItem.PENDENTE)
                .count();
    }

    // Getters
    public List<ChecklistItem> getItens() {
        return itens;
    }

    public String getCategoria() {
        return categoria;
    }

    public long getTimestampAvaliacao() {
        return timestampAvaliacao;
    }

    public int getScorePercentual() {
        return scorePercentual;
    }

    public boolean isCompleto() {
        return getItensCriticosPendentes() == 0;
    }

    @Override
    public String toString() {
        return "ChecklistResult{" +
                "categoria='" + categoria + '\'' +
                ", score=" + scorePercentual + "%" +
                ", itens=" + itens.size() +
                ", criticoPendentes=" + getItensCriticosPendentes() +
                '}';
    }
}


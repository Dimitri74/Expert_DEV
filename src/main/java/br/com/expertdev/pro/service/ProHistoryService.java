package br.com.expertdev.pro.service;

import br.com.expertdev.pro.model.ChecklistResult;
import br.com.expertdev.pro.model.PromptBundle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de persistência para histórico de prompts e checklists no SQLite.
 * Cria as tabelas pro_prompt_history e pro_checklist_history automaticamente.
 */
public class ProHistoryService {

    private static final String DEFAULT_DB_PATH = "expertdev.db";
    private final String dbPath;

    public ProHistoryService() {
        this(DEFAULT_DB_PATH);
    }

    public ProHistoryService(String dbPath) {
        this.dbPath = dbPath;
        inicializarBanco();
    }

    // -------------------------------------------------------------------------
    // Inicialização
    // -------------------------------------------------------------------------

    private void inicializarBanco() {
        try (Connection conn = obterConexao(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS pro_prompt_history (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  tipo_prompt TEXT NOT NULL," +
                "  assistente_alvo TEXT," +
                "  arquivo_alvo TEXT," +
                "  linha_alvo INTEGER," +
                "  categoria TEXT," +
                "  prompt_gerado TEXT NOT NULL," +
                "  versao TEXT," +
                "  timestamp_criacao INTEGER NOT NULL," +
                "  tamanho_prompt INTEGER" +
                ")"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS pro_checklist_history (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  tipo_checklist TEXT NOT NULL," +
                "  score REAL," +
                "  total_itens INTEGER," +
                "  itens_ok INTEGER," +
                "  itens_falhos INTEGER," +
                "  resumo TEXT," +
                "  timestamp_criacao INTEGER NOT NULL" +
                ")"
            );
        } catch (SQLException e) {
            System.err.println("[ProHistoryService] Erro ao inicializar banco: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Prompts
    // -------------------------------------------------------------------------

    /**
     * Persiste um PromptBundle gerado.
     * @return id gerado, ou -1 em caso de erro.
     */
    public long salvarPrompt(PromptBundle bundle) {
        String sql = "INSERT INTO pro_prompt_history " +
                "(tipo_prompt, assistente_alvo, arquivo_alvo, linha_alvo, categoria, " +
                " prompt_gerado, versao, timestamp_criacao, tamanho_prompt) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, bundle.getTipoPrompt());
            ps.setString(2, bundle.getAssistenteAlvo() != null ? bundle.getAssistenteAlvo().name() : null);
            ps.setString(3, bundle.getContexto() != null ? bundle.getContexto().getArquivoAlvo() : null);
            ps.setInt(4, bundle.getContexto() != null ? bundle.getContexto().getLinhaAlvo() : 0);
            ps.setString(5, bundle.getContexto() != null ? bundle.getContexto().getCategoria() : null);
            ps.setString(6, bundle.getPromptGerado());
            ps.setString(7, bundle.getVersaoExpertDev());
            ps.setLong(8, bundle.getTimestampCriacao());
            ps.setInt(9, bundle.getTamanhoPrompt());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("[ProHistoryService] Erro ao salvar prompt: " + e.getMessage());
        }
        return -1L;
    }

    /**
     * Retorna os últimos N prompts salvos.
     */
    public List<PromptHistoryEntry> listarUltimosPrompts(int limite) {
        List<PromptHistoryEntry> lista = new ArrayList<>();
        String sql = "SELECT id, tipo_prompt, assistente_alvo, arquivo_alvo, categoria, " +
                     "tamanho_prompt, timestamp_criacao FROM pro_prompt_history " +
                     "ORDER BY timestamp_criacao DESC LIMIT ?";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PromptHistoryEntry e = new PromptHistoryEntry();
                    e.id = rs.getLong("id");
                    e.tipoPrompt = rs.getString("tipo_prompt");
                    e.assistenteAlvo = rs.getString("assistente_alvo");
                    e.arquivoAlvo = rs.getString("arquivo_alvo");
                    e.categoria = rs.getString("categoria");
                    e.tamanhoPrompt = rs.getInt("tamanho_prompt");
                    e.timestampCriacao = rs.getLong("timestamp_criacao");
                    lista.add(e);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProHistoryService] Erro ao listar prompts: " + e.getMessage());
        }
        return lista;
    }

    // -------------------------------------------------------------------------
    // Checklists
    // -------------------------------------------------------------------------

    /**
     * Persiste um ChecklistResult concluído.
     * @return id gerado, ou -1 em caso de erro.
     */
    public long salvarChecklist(ChecklistResult checklist) {
        String sql = "INSERT INTO pro_checklist_history " +
                "(tipo_checklist, score, total_itens, itens_ok, itens_falhos, resumo, timestamp_criacao) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int ok = (int) checklist.getItens().stream()
                    .filter(i -> i.getStatus() == ChecklistResult.StatusItem.OK).count();
            int falhos = (int) checklist.getItens().stream()
                    .filter(i -> i.getStatus() == ChecklistResult.StatusItem.FALHOU).count();

            ps.setString(1, checklist.getCategoria());
            ps.setDouble(2, checklist.getScorePercentual());
            ps.setInt(3, checklist.getItens().size());
            ps.setInt(4, ok);
            ps.setInt(5, falhos);
            ps.setString(6, String.format("Score: %d%% | OK: %d | Falhos: %d", checklist.getScorePercentual(), ok, falhos));
            ps.setLong(7, System.currentTimeMillis());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("[ProHistoryService] Erro ao salvar checklist: " + e.getMessage());
        }
        return -1L;
    }

    /**
     * Retorna os últimos N checklists salvos.
     */
    public List<ChecklistHistoryEntry> listarUltimosChecklists(int limite) {
        List<ChecklistHistoryEntry> lista = new ArrayList<>();
        String sql = "SELECT id, tipo_checklist, score, total_itens, itens_ok, itens_falhos, timestamp_criacao " +
                     "FROM pro_checklist_history ORDER BY timestamp_criacao DESC LIMIT ?";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChecklistHistoryEntry e = new ChecklistHistoryEntry();
                    e.id = rs.getLong("id");
                    e.tipoChecklist = rs.getString("tipo_checklist");
                    e.score = rs.getDouble("score");
                    e.totalItens = rs.getInt("total_itens");
                    e.itensOk = rs.getInt("itens_ok");
                    e.itensFalhos = rs.getInt("itens_falhos");
                    e.timestampCriacao = rs.getLong("timestamp_criacao");
                    lista.add(e);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProHistoryService] Erro ao listar checklists: " + e.getMessage());
        }
        return lista;
    }

    // -------------------------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------------------------

    private Connection obterConexao() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    // -------------------------------------------------------------------------
    // DTOs de histórico
    // -------------------------------------------------------------------------

    public static class PromptHistoryEntry {
        public long id;
        public String tipoPrompt;
        public String assistenteAlvo;
        public String arquivoAlvo;
        public String categoria;
        public int tamanhoPrompt;
        public long timestampCriacao;

        @Override
        public String toString() {
            return String.format("[%d] %s | %s | %s | %d chars | ts=%d",
                    id, tipoPrompt, categoria, arquivoAlvo, tamanhoPrompt, timestampCriacao);
        }
    }

    public static class ChecklistHistoryEntry {
        public long id;
        public String tipoChecklist;
        public double score;
        public int totalItens;
        public int itensOk;
        public int itensFalhos;
        public long timestampCriacao;

        @Override
        public String toString() {
            return String.format("[%d] %s | Score: %.0f%% | OK:%d Falhos:%d | ts=%d",
                    id, tipoChecklist, score, itensOk, itensFalhos, timestampCriacao);
        }
    }
}


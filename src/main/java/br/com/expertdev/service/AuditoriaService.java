package br.com.expertdev.service;

import br.com.expertdev.model.RegistroAuditoria;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servico de auditoria com banco SQLite embarcado.
 * Persiste e recupera informacoes de processamento (RTC, UC, metadados).
 */
public class AuditoriaService {

    private static final String DB_URL = "jdbc:sqlite:expertdev.db";
    private static final String DRIVER = "org.sqlite.JDBC";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Falha ao carregar driver SQLite: " + e.getMessage(), e);
        }
    }

    public AuditoriaService() {
        inicializarBanco();
    }

    /**
     * Inicializa o banco e cria tabelas se nao existirem.
     */
    public void inicializarBanco() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS auditoria_processamento (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "rtc_numero TEXT NOT NULL," +
                    "uc_codigo TEXT NOT NULL," +
                    "uc_descricao TEXT NOT NULL," +
                    "data_processamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "modo_geracao TEXT," +
                    "provider TEXT," +
                    "urls_ou_arquivo TEXT," +
                    "status TEXT," +
                    "prompt_gerado TEXT" +
                    ")";

            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco: " + e.getMessage());
        }
    }

    /**
     * Insere um novo registro de auditoria.
     */
    public long inserir(RegistroAuditoria registro) {
        String sql = "INSERT INTO auditoria_processamento " +
                "(rtc_numero, uc_codigo, uc_descricao, data_processamento, " +
                "modo_geracao, provider, urls_ou_arquivo, status, prompt_gerado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, registro.getRtcNumero());
            pstmt.setString(2, registro.getUcCodigo());
            pstmt.setString(3, registro.getUcDescricao());
            pstmt.setTimestamp(4, Timestamp.valueOf(registro.getDataProcessamento()));
            pstmt.setString(5, registro.getModoGeracao());
            pstmt.setString(6, registro.getProvider());
            pstmt.setString(7, registro.getUrlsOuArquivo());
            pstmt.setString(8, registro.getStatus());
            pstmt.setString(9, registro.getPromptGerado());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir auditoria: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Atualiza um registro existente (usado para marcar como CONCLUIDO).
     */
    public void atualizar(long id, String status, String promptGerado) {
        String sql = "UPDATE auditoria_processamento SET status = ?, prompt_gerado = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, promptGerado);
            pstmt.setLong(3, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar auditoria: " + e.getMessage());
        }
    }

    /**
     * Exclui um registro de auditoria por ID.
     */
    public boolean deletarPorId(long id) {
        String sql = "DELETE FROM auditoria_processamento WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao deletar auditoria por ID: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exclui registros de auditoria por RTC.
     */
    public int deletarPorRTC(String rtcNumero) {
        String sql = "DELETE FROM auditoria_processamento WHERE rtc_numero = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rtcNumero);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao deletar auditoria por RTC: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Recupera um registro por ID.
     */
    public RegistroAuditoria obterPorId(long id) {
        String sql = "SELECT * FROM auditoria_processamento WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter auditoria: " + e.getMessage());
        }
        return null;
    }

    /**
     * Recupera todos os registros, ordenados por data decrescente.
     */
    public List<RegistroAuditoria> obterTodos() {
        List<RegistroAuditoria> registros = new ArrayList<>();
        String sql = "SELECT * FROM auditoria_processamento ORDER BY data_processamento DESC LIMIT 100";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                registros.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar auditoria: " + e.getMessage());
        }
        return registros;
    }

    /**
     * Recupera registros por RTC.
     */
    public List<RegistroAuditoria> obterPorRTC(String rtcNumero) {
        List<RegistroAuditoria> registros = new ArrayList<>();
        String sql = "SELECT * FROM auditoria_processamento WHERE rtc_numero = ? " +
                "ORDER BY data_processamento DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rtcNumero);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar por RTC: " + e.getMessage());
        }
        return registros;
    }

    /**
     * Recupera o ultimo registro (mais recente).
     */
    public RegistroAuditoria obterUltimo() {
        String sql = "SELECT * FROM auditoria_processamento ORDER BY data_processamento DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return mapearResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter ultimo registro: " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper para mapear ResultSet em RegistroAuditoria.
     */
    private RegistroAuditoria mapearResultSet(ResultSet rs) throws SQLException {
        RegistroAuditoria reg = new RegistroAuditoria();
        reg.setId(rs.getLong("id"));
        reg.setRtcNumero(rs.getString("rtc_numero"));
        reg.setUcCodigo(rs.getString("uc_codigo"));
        reg.setUcDescricao(rs.getString("uc_descricao"));

        Timestamp ts = rs.getTimestamp("data_processamento");
        if (ts != null) {
            reg.setDataProcessamento(ts.toLocalDateTime());
        }

        reg.setModoGeracao(rs.getString("modo_geracao"));
        reg.setProvider(rs.getString("provider"));
        reg.setUrlsOuArquivo(rs.getString("urls_ou_arquivo"));
        reg.setStatus(rs.getString("status"));
        reg.setPromptGerado(rs.getString("prompt_gerado"));

        return reg;
    }
}


package br.com.expertdev.service;

import br.com.expertdev.model.RegistroAuditoria;
import br.com.expertdev.model.MetricaPerformance;
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

            String sqlPerformance = "CREATE TABLE IF NOT EXISTS performance_comparativa (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "rtc_numero TEXT NOT NULL," +
                    "estimativa_poker REAL," +
                    "inicio_scrum TIMESTAMP," +
                    "fim_scrum TIMESTAMP," +
                    "inicio_expertdev TIMESTAMP," +
                    "fim_expertdev TIMESTAMP," +
                    "complexidade TEXT," +
                    "status TEXT" +
                    ")";
            stmt.execute(sqlPerformance);
            
            String sqlCache = "CREATE TABLE IF NOT EXISTS cache_processamento (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "url TEXT UNIQUE NOT NULL," +
                    "texto_extraido TEXT NOT NULL," +
                    "data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            stmt.execute(sqlCache);

            String sqlCacheImagens = "CREATE TABLE IF NOT EXISTS cache_imagens (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "cache_id INTEGER NOT NULL," +
                    "src TEXT NOT NULL," +
                    "alt TEXT," +
                    "FOREIGN KEY (cache_id) REFERENCES cache_processamento(id)" +
                    ")";
            stmt.execute(sqlCacheImagens);
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

    // --- Métodos para MetricaPerformance ---

    public long inserirPerformance(MetricaPerformance metrica) {
        String sql = "INSERT INTO performance_comparativa " +
                "(rtc_numero, estimativa_poker, inicio_scrum, fim_scrum, " +
                "inicio_expertdev, fim_expertdev, complexidade, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, metrica.getRtcNumero());
            if (metrica.getEstimativaPoker() != null) pstmt.setDouble(2, metrica.getEstimativaPoker()); else pstmt.setNull(2, java.sql.Types.DOUBLE);
            pstmt.setTimestamp(3, metrica.getInicioScrum() != null ? Timestamp.valueOf(metrica.getInicioScrum()) : null);
            pstmt.setTimestamp(4, metrica.getFimScrum() != null ? Timestamp.valueOf(metrica.getFimScrum()) : null);
            pstmt.setTimestamp(5, metrica.getInicioExpertDev() != null ? Timestamp.valueOf(metrica.getInicioExpertDev()) : null);
            pstmt.setTimestamp(6, metrica.getFimExpertDev() != null ? Timestamp.valueOf(metrica.getFimExpertDev()) : null);
            pstmt.setString(7, metrica.getComplexidade());
            pstmt.setString(8, metrica.getStatus());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir performance: " + e.getMessage());
        }
        return -1;
    }

    public void atualizarPerformance(MetricaPerformance metrica) {
        String sql = "UPDATE performance_comparativa SET " +
                "estimativa_poker = ?, inicio_scrum = ?, fim_scrum = ?, " +
                "inicio_expertdev = ?, fim_expertdev = ?, complexidade = ?, status = ? " +
                "WHERE rtc_numero = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (metrica.getEstimativaPoker() != null) pstmt.setDouble(1, metrica.getEstimativaPoker()); else pstmt.setNull(1, java.sql.Types.DOUBLE);
            pstmt.setTimestamp(2, metrica.getInicioScrum() != null ? Timestamp.valueOf(metrica.getInicioScrum()) : null);
            pstmt.setTimestamp(3, metrica.getFimScrum() != null ? Timestamp.valueOf(metrica.getFimScrum()) : null);
            pstmt.setTimestamp(4, metrica.getInicioExpertDev() != null ? Timestamp.valueOf(metrica.getInicioExpertDev()) : null);
            pstmt.setTimestamp(5, metrica.getFimExpertDev() != null ? Timestamp.valueOf(metrica.getFimExpertDev()) : null);
            pstmt.setString(6, metrica.getComplexidade());
            pstmt.setString(7, metrica.getStatus());
            pstmt.setString(8, metrica.getRtcNumero());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar performance: " + e.getMessage());
        }
    }

    public MetricaPerformance buscarPerformancePorRTC(String rtcNumero) {
        String sql = "SELECT * FROM performance_comparativa WHERE rtc_numero = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rtcNumero);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    MetricaPerformance m = new MetricaPerformance();
                    m.setId(rs.getLong("id"));
                    m.setRtcNumero(rs.getString("rtc_numero"));
                    m.setEstimativaPoker(rs.getDouble("estimativa_poker"));
                    m.setInicioScrum(rs.getTimestamp("inicio_scrum") != null ? rs.getTimestamp("inicio_scrum").toLocalDateTime() : null);
                    m.setFimScrum(rs.getTimestamp("fim_scrum") != null ? rs.getTimestamp("fim_scrum").toLocalDateTime() : null);
                    m.setInicioExpertDev(rs.getTimestamp("inicio_expertdev") != null ? rs.getTimestamp("inicio_expertdev").toLocalDateTime() : null);
                    m.setFimExpertDev(rs.getTimestamp("fim_expertdev") != null ? rs.getTimestamp("fim_expertdev").toLocalDateTime() : null);
                    m.setComplexidade(rs.getString("complexidade"));
                    m.setStatus(rs.getString("status"));
                    return m;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar performance: " + e.getMessage());
        }
        return null;
    }

    public List<MetricaPerformance> listarPerformance() {
        List<MetricaPerformance> lista = new ArrayList<>();
        String sql = "SELECT * FROM performance_comparativa ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MetricaPerformance m = new MetricaPerformance();
                m.setId(rs.getLong("id"));
                m.setRtcNumero(rs.getString("rtc_numero"));
                m.setEstimativaPoker(rs.getDouble("estimativa_poker"));
                m.setInicioScrum(rs.getTimestamp("inicio_scrum") != null ? rs.getTimestamp("inicio_scrum").toLocalDateTime() : null);
                m.setFimScrum(rs.getTimestamp("fim_scrum") != null ? rs.getTimestamp("fim_scrum").toLocalDateTime() : null);
                m.setInicioExpertDev(rs.getTimestamp("inicio_expertdev") != null ? rs.getTimestamp("inicio_expertdev").toLocalDateTime() : null);
                m.setFimExpertDev(rs.getTimestamp("fim_expertdev") != null ? rs.getTimestamp("fim_expertdev").toLocalDateTime() : null);
                m.setComplexidade(rs.getString("complexidade"));
                m.setStatus(rs.getString("status"));
                lista.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar performance: " + e.getMessage());
        }
        return lista;
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


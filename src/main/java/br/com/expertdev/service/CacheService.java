package br.com.expertdev.service;

import br.com.expertdev.model.ImagemInfo;
import br.com.expertdev.model.ResultadoProcessamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de Cache para resultados de processamento de URLs.
 * Utiliza o banco SQLite para persistir textos e metadados de imagens.
 */
public class CacheService {

    private static final String DB_URL = "jdbc:sqlite:expertdev.db";

    public CacheService() {
        // As tabelas são criadas pelo AuditoriaService.inicializarBanco()
    }

    public ResultadoProcessamento buscarNoCache(String url) {
        String sql = "SELECT * FROM cache_processamento WHERE url = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, url);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    ResultadoProcessamento resultado = new ResultadoProcessamento(url);
                    resultado.setSucesso(true);
                    resultado.setTextoExtraido(rs.getString("texto_extraido"));
                    resultado.setObservacao("Recuperado do cache em " + rs.getTimestamp("data_criacao"));
                    
                    resultado.setImagens(buscarImagensNoCache(id, url));
                    return resultado;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar no cache: " + e.getMessage());
        }
        return null;
    }

    private List<ImagemInfo> buscarImagensNoCache(long cacheId, String paginaUrl) {
        List<ImagemInfo> imagens = new ArrayList<>();
        String sql = "SELECT * FROM cache_imagens WHERE cache_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, cacheId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    imagens.add(new ImagemInfo(paginaUrl, rs.getString("src"), rs.getString("alt")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar imagens no cache: " + e.getMessage());
        }
        return imagens;
    }

    public void salvarNoCache(ResultadoProcessamento resultado) {
        if (resultado == null || !resultado.isSucesso() || resultado.getTextoExtraido() == null) {
            return;
        }

        String sqlCache = "INSERT INTO cache_processamento (url, texto_extraido) VALUES (?, ?)";
        String sqlImagens = "INSERT INTO cache_imagens (cache_id, src, alt) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtCache = conn.prepareStatement(sqlCache, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmtCache.setString(1, resultado.getUrl());
                pstmtCache.setString(2, resultado.getTextoExtraido());
                pstmtCache.executeUpdate();

                long cacheId = -1;
                try (ResultSet generatedKeys = pstmtCache.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        cacheId = generatedKeys.getLong(1);
                    }
                }

                if (cacheId != -1 && resultado.getImagens() != null && !resultado.getImagens().isEmpty()) {
                    try (PreparedStatement pstmtImagens = conn.prepareStatement(sqlImagens)) {
                        for (ImagemInfo img : resultado.getImagens()) {
                            pstmtImagens.setLong(1, cacheId);
                            pstmtImagens.setString(2, img.getSrc());
                            pstmtImagens.setString(3, img.getAlt());
                            pstmtImagens.addBatch();
                        }
                        pstmtImagens.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar no cache: " + e.getMessage());
        }
    }

    public void limparCache() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM cache_imagens");
            stmt.execute("DELETE FROM cache_processamento");
            System.out.println("Cache limpo com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao limpar cache: " + e.getMessage());
        }
    }
}

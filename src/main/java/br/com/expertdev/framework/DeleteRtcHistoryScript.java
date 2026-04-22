package br.com.expertdev.framework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Script para deletar o histórico de RTC do usuário marcusdimitri7@gmail.com,
 * exceto o RTC número 202020.
 *
 * Tabelas afetadas:
 * - performance_comparativa
 * - auditoria_processamento
 *
 * Execução:
 * mvn -q compile exec:java "-Dexec.mainClass=br.com.expertdev.framework.DeleteRtcHistoryScript"
 */
public class DeleteRtcHistoryScript {

    private static final String DB_URL = "jdbc:sqlite:expertdev.db";
    private static final String USUARIO_ALVO_EMAIL = "marcusdimitri7@gmail.com";
    private static final String RTC_EXCECAO = "202020";

    public static void main(String[] args) {
        System.out.println("Iniciando limpeza de histórico de RTC...");
        System.out.println("Usuário (E-mail): " + USUARIO_ALVO_EMAIL);
        System.out.println("Exceção: RTC " + RTC_EXCECAO);

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                // 1. Limpar performance_comparativa
                // Deletar onde auth_email é o alvo, exceto o RTC 202020
                String sqlPerf = "DELETE FROM performance_comparativa WHERE auth_email = ? AND rtc_numero != ?";
                int deletadosPerf = executarDelete(conn, sqlPerf, USUARIO_ALVO_EMAIL, RTC_EXCECAO);
                System.out.println("Registros deletados em performance_comparativa: " + deletadosPerf);

                // 2. Limpar auditoria_processamento
                String sqlAud = "DELETE FROM auditoria_processamento WHERE auth_email = ? AND rtc_numero != ?";
                int deletadosAud = executarDelete(conn, sqlAud, USUARIO_ALVO_EMAIL, RTC_EXCECAO);
                System.out.println("Registros deletados em auditoria_processamento: " + deletadosAud);

                conn.commit();
                System.out.println("Limpeza concluída com sucesso!");

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            System.err.println("Erro ao executar script de limpeza: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int executarDelete(Connection conn, String sql, String usuario, String rtcExcecao) throws Exception {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, rtcExcecao);
            return pstmt.executeUpdate();
        }
    }
}

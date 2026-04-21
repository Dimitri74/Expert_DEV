package br.com.expertdev.framework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * Script simples para SELECT na tabela de auditoria.
 *
 * Execucao:
 * mvn -q compile exec:java "-Dexec.mainClass=br.com.expertdev.framework.ScriptAuditoria"
 */
public class ScriptAuditoria {

    private static final String DB_URL = "jdbc:sqlite:expertdev.db";

    public static void main(String[] args) {
        String sql = "SELECT * FROM auditoria_processamento ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int totalColunas = meta.getColumnCount();

            // Cabecalho com nome dos campos
            StringBuilder cabecalho = new StringBuilder();
            for (int i = 1; i <= totalColunas; i++) {
                cabecalho.append(meta.getColumnName(i));
                if (i < totalColunas) {
                    cabecalho.append(" | ");
                }
            }
            System.out.println(cabecalho);
            System.out.println(repetir("-", cabecalho.length()));

            // Linhas com os dados
            int linhas = 0;
            while (rs.next()) {
                StringBuilder linha = new StringBuilder();
                for (int i = 1; i <= totalColunas; i++) {
                    Object valor = rs.getObject(i);
                    linha.append(valor == null ? "" : valor.toString());
                    if (i < totalColunas) {
                        linha.append(" | ");
                    }
                }
                System.out.println(linha);
                linhas++;
            }

            if (linhas == 0) {
                System.out.println("(nenhum registro encontrado)");
            }

        } catch (Exception e) {
            System.err.println("Erro ao executar SELECT de auditoria: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String repetir(String texto, int vezes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vezes; i++) {
            sb.append(texto);
        }
        return sb.toString();
    }
}


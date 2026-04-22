package br.com.expertdev.service;

import br.com.expertdev.model.AuthSession;
import br.com.expertdev.model.LicenseStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servico de autenticacao com lockout, expiracao de senha e ciclo de trial.
 */
public class AuthService {

    private static final String DB_URL = "jdbc:sqlite:expertdev.db";
    private static final int TRIAL_DIAS_PADRAO = 15;
    private static final int RESET_EXPIRACAO_MINUTOS = 15;
    private static final int MAX_TENTATIVAS_FALHAS = 5;
    private static final int LOCKOUT_MINUTOS = 5;
    private static final int SENHA_EXPIRACAO_DIAS = 30;
    private static final int AVISO_EXPIRACAO_DIAS = 5;

    private final PasswordHasher passwordHasher = new PasswordHasher();

    public AuthService() {
        inicializarTabelasAuth();
    }

    // ── Inicializacao ─────────────────────────────────────────────────────────

    public void inicializarTabelasAuth() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS auth_users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT NOT NULL UNIQUE,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "password_hash TEXT NOT NULL,"
                    + "password_salt TEXT NOT NULL,"
                    + "is_premium INTEGER NOT NULL DEFAULT 1,"
                    + "failed_attempts INTEGER NOT NULL DEFAULT 0,"
                    + "locked_until TIMESTAMP,"
                    + "password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")");

            executarMigracao(stmt, "ALTER TABLE auth_users ADD COLUMN failed_attempts INTEGER NOT NULL DEFAULT 0");
            executarMigracao(stmt, "ALTER TABLE auth_users ADD COLUMN locked_until TIMESTAMP");
            executarMigracao(stmt, "ALTER TABLE auth_users ADD COLUMN password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

            stmt.execute("CREATE TABLE IF NOT EXISTS auth_password_reset ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER NOT NULL,"
                    + "reset_code_hash TEXT NOT NULL,"
                    + "expires_at TIMESTAMP NOT NULL,"
                    + "used_at TIMESTAMP,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (user_id) REFERENCES auth_users(id)"
                    + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS auth_license_state ("
                    + "id INTEGER PRIMARY KEY CHECK (id = 1),"
                    + "first_run_at TIMESTAMP NOT NULL,"
                    + "trial_days INTEGER NOT NULL DEFAULT 15,"
                    + "last_check_at TIMESTAMP"
                    + ")");

        } catch (SQLException e) {
            System.err.println("Erro ao inicializar tabelas de autenticacao: " + e.getMessage());
        }
    }

    private void executarMigracao(Statement stmt, String sql) {
        try { stmt.execute(sql); } catch (SQLException ignored) {}
    }

    // ── Cadastro ──────────────────────────────────────────────────────────────

    public boolean existeUsuarioCadastrado() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(1) FROM auth_users")) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public String criarUsuario(String username, String email, String senha, boolean premium) {
        if (username == null || username.trim().isEmpty()) return "Informe um nome de usuario.";
        if (email == null || email.trim().isEmpty()) return "Informe um email.";
        if (senha == null || senha.length() < 6) return "A senha deve ter no minimo 6 caracteres.";

        String salt = passwordHasher.gerarSaltBase64();
        String hash = passwordHasher.hashSenha(senha, salt);

        String sql = "INSERT INTO auth_users (username,email,password_hash,password_salt,is_premium,password_changed_at) VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username.trim());
            p.setString(2, email.trim().toLowerCase());
            p.setString(3, hash);
            p.setString(4, salt);
            p.setInt(5, premium ? 1 : 0);
            p.executeUpdate();
            return null;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique"))
                return "Usuario ou email ja cadastrados.";
            return "Falha ao cadastrar usuario: " + e.getMessage();
        }
    }

    // ── Autenticacao com lockout e expiracao de senha ─────────────────────────

    /**
     * Autentica o usuario.
     * - Lockout apos 5 tentativas erradas (5 minutos).
     * - Senha expira em 30 dias.
     * - Ao expirar, usuario cai em ciclo trial enquanto nao renovar.
     * Retorna null se usuario nao encontrado ou senha errada.
     * Retorna sessao com LicenseStatus.EXPIRED se conta bloqueada (username contem mensagem).
     * Retorna sessao com LicenseStatus.PASSWORD_EXPIRED se senha expirou.
     */
    public AuthSession autenticar(String loginOuEmail, String senha) {
        if (loginOuEmail == null || loginOuEmail.trim().isEmpty() || senha == null) return null;

        String sql = "SELECT id,username,email,password_hash,password_salt,is_premium,failed_attempts,locked_until,password_changed_at "
                + "FROM auth_users WHERE lower(username)=lower(?) OR lower(email)=lower(?) LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {

            p.setString(1, loginOuEmail.trim());
            p.setString(2, loginOuEmail.trim());

            try (ResultSet rs = p.executeQuery()) {
                if (!rs.next()) return null;

                long userId = rs.getLong("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                Timestamp lockedUntilTs = rs.getTimestamp("locked_until");

                // Verificar lockout ativo
                if (lockedUntilTs != null && lockedUntilTs.toLocalDateTime().isAfter(LocalDateTime.now())) {
                    long min = Duration.between(LocalDateTime.now(), lockedUntilTs.toLocalDateTime()).toMinutes() + 1;
                    return new AuthSession("BLOQUEADO: aguarde " + min + " min.", "", LicenseStatus.EXPIRED, 0);
                }

                boolean senhaOk = passwordHasher.validarSenha(senha, rs.getString("password_hash"), rs.getString("password_salt"));
                if (!senhaOk) {
                    incrementarFalha(conn, userId, rs.getInt("failed_attempts") + 1);
                    return null;
                }

                resetarFalhas(conn, userId);

                boolean premium = rs.getInt("is_premium") == 1;
                Timestamp pwChangedTs = rs.getTimestamp("password_changed_at");

                // Verificar expiracao de senha
                if (pwChangedTs != null) {
                    long diasDesde = Duration.between(
                            pwChangedTs.toLocalDateTime().toLocalDate().atStartOfDay(),
                            LocalDate.now().atStartOfDay()).toDays();

                    if (diasDesde >= SENHA_EXPIRACAO_DIAS) {
                        if (premium) degradarParaTrial(conn, userId);
                        return new AuthSession(username, email, LicenseStatus.PASSWORD_EXPIRED, getDiasRestantesTrial());
                    }
                }

                return premium ? AuthSession.premium(username, email)
                        : new AuthSession(username, email, LicenseStatus.TRIAL, getDiasRestantesTrial());
            }
        } catch (SQLException e) {
            System.err.println("Erro ao autenticar: " + e.getMessage());
            return null;
        }
    }

    // ── Renovacao de senha (usuario logado ou em PASSWORD_EXPIRED) ────────────

    /**
     * Renova a senha confirmando a senha atual.
     * Ao renovar com sucesso: is_premium = 1, failed_attempts = 0, locked_until = NULL.
     * @return null em sucesso, mensagem de erro caso contrario.
     */
    public String renovarSenha(String loginOuEmail, String senhaAtual, String novaSenha, String confirmacao) {
        if (novaSenha == null || novaSenha.length() < 6) return "A nova senha deve ter no minimo 6 caracteres.";
        if (!novaSenha.equals(confirmacao)) return "A confirmacao da nova senha nao confere.";

        AuthIdentity identity = buscarUsuario(loginOuEmail);
        if (identity == null) return "Usuario nao encontrado.";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Verificar senha atual
            try (PreparedStatement p = conn.prepareStatement(
                    "SELECT password_hash, password_salt FROM auth_users WHERE id = ?")) {
                p.setLong(1, identity.userId);
                try (ResultSet rs = p.executeQuery()) {
                    if (!rs.next()) return "Usuario nao encontrado.";
                    if (!passwordHasher.validarSenha(senhaAtual, rs.getString("password_hash"), rs.getString("password_salt")))
                        return "Senha atual incorreta.";
                }
            }

            String novoSalt = passwordHasher.gerarSaltBase64();
            String novoHash = passwordHasher.hashSenha(novaSenha, novoSalt);

            try (PreparedStatement p = conn.prepareStatement(
                    "UPDATE auth_users SET password_hash=?,password_salt=?,password_changed_at=CURRENT_TIMESTAMP,"
                            + "is_premium=1,failed_attempts=0,locked_until=NULL,updated_at=CURRENT_TIMESTAMP WHERE id=?")) {
                p.setString(1, novoHash);
                p.setString(2, novoSalt);
                p.setLong(3, identity.userId);
                p.executeUpdate();
            }
            return null;
        } catch (SQLException e) {
            return "Falha ao renovar senha: " + e.getMessage();
        }
    }

    /**
     * Dias restantes para expiracao da senha (0 se ja expirou, -1 se nao encontrado).
     */
    public int getDiasParaExpirarSenha(String loginOuEmail) {
        AuthIdentity identity = buscarUsuario(loginOuEmail);
        if (identity == null) return -1;
        String sql = "SELECT password_changed_at FROM auth_users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setLong(1, identity.userId);
            try (ResultSet rs = p.executeQuery()) {
                if (!rs.next()) return -1;
                Timestamp ts = rs.getTimestamp("password_changed_at");
                if (ts == null) return 0;
                long diasDecorridos = Duration.between(
                        ts.toLocalDateTime().toLocalDate().atStartOfDay(),
                        LocalDate.now().atStartOfDay()).toDays();
                return Math.max(0, SENHA_EXPIRACAO_DIAS - (int) diasDecorridos);
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    /** true se a senha expira em <= 5 dias. */
    public boolean deveAvisarExpiracaoSenha(String loginOuEmail) {
        int dias = getDiasParaExpirarSenha(loginOuEmail);
        return dias >= 0 && dias <= AVISO_EXPIRACAO_DIAS;
    }

    // ── Trial ─────────────────────────────────────────────────────────────────

    public AuthSession criarSessaoTrial() {
        int dias = getDiasRestantesTrial();
        return dias <= 0 ? AuthSession.expired() : AuthSession.trial(dias);
    }

    public int getDiasRestantesTrial() {
        TrialState state = obterOuCriarEstadoTrial();
        if (state == null) return 0;
        long diasDecorridos = Duration.between(
                state.firstRunAt.toLocalDate().atStartOfDay(),
                LocalDate.now().atStartOfDay()).toDays();
        int restantes = state.trialDays - (int) diasDecorridos;
        atualizarUltimaVerificacao();
        return Math.max(0, restantes);
    }

    // ── Reset via codigo ──────────────────────────────────────────────────────

    public String gerarCodigoReset(String loginOuEmail) {
        AuthIdentity identity = buscarUsuario(loginOuEmail);
        if (identity == null) return null;
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String sql = "INSERT INTO auth_password_reset (user_id,reset_code_hash,expires_at) VALUES (?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setLong(1, identity.userId);
            p.setString(2, sha256(code));
            p.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(RESET_EXPIRACAO_MINUTOS)));
            p.executeUpdate();
            return code;
        } catch (SQLException e) {
            return null;
        }
    }

    public String obterUsernamePorIdentificador(String loginOuEmail) {
        AuthIdentity identity = buscarUsuario(loginOuEmail);
        return identity == null ? null : identity.username;
    }

    public String redefinirSenha(String loginOuEmail, String codigo, String novaSenha) {
        if (novaSenha == null || novaSenha.length() < 6) return "A nova senha deve ter no minimo 6 caracteres.";
        AuthIdentity identity = buscarUsuario(loginOuEmail);
        if (identity == null) return "Usuario nao encontrado.";

        String sqlBusca = "SELECT id,expires_at,used_at FROM auth_password_reset WHERE user_id=? AND reset_code_hash=? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(sqlBusca)) {
            p.setLong(1, identity.userId);
            p.setString(2, sha256(codigo == null ? "" : codigo.trim()));
            try (ResultSet rs = p.executeQuery()) {
                if (!rs.next()) return "Codigo de recuperacao invalido.";
                if (rs.getTimestamp("used_at") != null) return "Esse codigo ja foi utilizado.";
                Timestamp exp = rs.getTimestamp("expires_at");
                if (exp == null || exp.toLocalDateTime().isBefore(LocalDateTime.now()))
                    return "Codigo expirado. Gere um novo codigo.";

                String novoSalt = passwordHasher.gerarSaltBase64();
                String novoHash = passwordHasher.hashSenha(novaSenha, novoSalt);

                conn.setAutoCommit(false);
                try (PreparedStatement pUser = conn.prepareStatement(
                             "UPDATE auth_users SET password_hash=?,password_salt=?,password_changed_at=CURRENT_TIMESTAMP,"
                                     + "is_premium=1,failed_attempts=0,locked_until=NULL,updated_at=CURRENT_TIMESTAMP WHERE id=?");
                     PreparedStatement pReset = conn.prepareStatement(
                             "UPDATE auth_password_reset SET used_at=CURRENT_TIMESTAMP WHERE id=?")) {
                    pUser.setString(1, novoHash);
                    pUser.setString(2, novoSalt);
                    pUser.setLong(3, identity.userId);
                    pUser.executeUpdate();
                    pReset.setLong(1, rs.getLong("id"));
                    pReset.executeUpdate();
                    conn.commit();
                    conn.setAutoCommit(true);
                    return null;
                } catch (SQLException e) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return "Falha ao atualizar senha: " + e.getMessage();
                }
            }
        } catch (SQLException e) {
            return "Falha na recuperacao de senha: " + e.getMessage();
        }
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private void incrementarFalha(Connection conn, long userId, int novaContagem) throws SQLException {
        boolean lockout = novaContagem >= MAX_TENTATIVAS_FALHAS;
        String sql = lockout
                ? "UPDATE auth_users SET failed_attempts=?,locked_until=? WHERE id=?"
                : "UPDATE auth_users SET failed_attempts=? WHERE id=?";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, novaContagem);
            if (lockout) {
                p.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTOS)));
                p.setLong(3, userId);
            } else {
                p.setLong(2, userId);
            }
            p.executeUpdate();
        }
    }

    private void resetarFalhas(Connection conn, long userId) throws SQLException {
        try (PreparedStatement p = conn.prepareStatement(
                "UPDATE auth_users SET failed_attempts=0,locked_until=NULL WHERE id=?")) {
            p.setLong(1, userId);
            p.executeUpdate();
        }
    }

    private void degradarParaTrial(Connection conn, long userId) throws SQLException {
        try (PreparedStatement p = conn.prepareStatement(
                "UPDATE auth_users SET is_premium=0 WHERE id=?")) {
            p.setLong(1, userId);
            p.executeUpdate();
        }
    }

    private AuthIdentity buscarUsuario(String loginOuEmail) {
        if (loginOuEmail == null || loginOuEmail.trim().isEmpty()) return null;
        String sql = "SELECT id,username,email FROM auth_users WHERE lower(username)=lower(?) OR lower(email)=lower(?) LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, loginOuEmail.trim());
            p.setString(2, loginOuEmail.trim());
            try (ResultSet rs = p.executeQuery()) {
                if (!rs.next()) return null;
                return new AuthIdentity(rs.getLong("id"), rs.getString("username"), rs.getString("email"));
            }
        } catch (SQLException e) {
            return null;
        }
    }

    private TrialState obterOuCriarEstadoTrial() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT first_run_at,trial_days FROM auth_license_state WHERE id=1")) {
            if (rs.next()) {
                return new TrialState(rs.getTimestamp("first_run_at").toLocalDateTime(), rs.getInt("trial_days"));
            }
        } catch (SQLException e) {
            return null;
        }
        LocalDateTime agora = LocalDateTime.now();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(
                     "INSERT INTO auth_license_state (id,first_run_at,trial_days,last_check_at) VALUES (1,?,?,?)")) {
            p.setTimestamp(1, Timestamp.valueOf(agora));
            p.setInt(2, TRIAL_DIAS_PADRAO);
            p.setTimestamp(3, Timestamp.valueOf(agora));
            p.executeUpdate();
            return new TrialState(agora, TRIAL_DIAS_PADRAO);
        } catch (SQLException e) {
            return null;
        }
    }

    private void atualizarUltimaVerificacao() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(
                     "UPDATE auth_license_state SET last_check_at=? WHERE id=1")) {
            p.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            p.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel no runtime.", e);
        }
    }

    // ── Classes internas ──────────────────────────────────────────────────────

    private static class TrialState {
        private final LocalDateTime firstRunAt;
        private final int trialDays;
        private TrialState(LocalDateTime f, int d) { this.firstRunAt = f; this.trialDays = d; }
    }

    private static class AuthIdentity {
        private final long userId;
        private final String username;
        private final String email;
        private AuthIdentity(long u, String n, String e) { this.userId = u; this.username = n; this.email = e; }
    }
}


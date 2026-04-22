package br.com.expertdev.ui;

import br.com.expertdev.model.AuthSession;
import br.com.expertdev.model.LicenseStatus;
import br.com.expertdev.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {

    private final AuthService authService;
    private AuthSession session;

    private JTextField txtIdentity;
    private JPasswordField txtSenha;
    private JLabel lblTrial;

    public LoginDialog(Window owner, AuthService authService) {
        super(owner, "Acesso Expert Dev 2.2.3-BETA", ModalityType.APPLICATION_MODAL);
        this.authService = authService;
        construir();
    }

    public AuthSession getSession() {
        return session;
    }

    private void construir() {
        setLayout(new BorderLayout(10, 10));

        JPanel painel = new JPanel(new GridLayout(0, 2, 8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 12, 2, 12));

        txtIdentity = new JTextField();
        txtSenha = new JPasswordField();

        painel.add(new JLabel("Usuario ou email:"));
        painel.add(txtIdentity);
        painel.add(new JLabel("Senha:"));
        painel.add(txtSenha);

        int diasTrial = authService.getDiasRestantesTrial();
        lblTrial = new JLabel("Trial restante: " + diasTrial + " dia(s)");
        lblTrial.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEntrar = new JButton("Entrar");
        JButton btnCadastro = new JButton("Cadastrar");
        JButton btnRecuperar = new JButton("Recuperar acesso");
        JButton btnTrial = new JButton("Continuar Trial");
        JButton btnSair = new JButton("Sair");

        btnEntrar.addActionListener(e -> autenticar());
        btnCadastro.addActionListener(e -> cadastrar());
        btnRecuperar.addActionListener(e -> recuperar());
        btnTrial.addActionListener(e -> continuarTrial());
        btnSair.addActionListener(e -> { session = null; dispose(); });

        botoes.add(btnCadastro);
        botoes.add(btnRecuperar);
        botoes.add(btnTrial);
        botoes.add(btnEntrar);
        botoes.add(btnSair);

        add(painel, BorderLayout.CENTER);
        add(lblTrial, BorderLayout.NORTH);
        add(botoes, BorderLayout.SOUTH);

        setSize(620, 220);
        setLocationRelativeTo(getOwner());
    }

    // ── Autenticacao ─────────────────────────────────────────────────────────

    private void autenticar() {
        String identity = txtIdentity.getText().trim();
        String senha = new String(txtSenha.getPassword());
        AuthSession tentativa = authService.autenticar(identity, senha);

        if (tentativa == null) {
            JOptionPane.showMessageDialog(this,
                    "Credenciais invalidas. Verifique usuario/email e senha.",
                    "Login",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Conta bloqueada por lockout
        if (tentativa.getLicenseStatus() == LicenseStatus.EXPIRED
                && tentativa.getUsername().startsWith("BLOQUEADO")) {
            JOptionPane.showMessageDialog(this,
                    tentativa.getUsername(),
                    "Conta bloqueada",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Senha expirada — usuario deve renovar agora
        if (tentativa.getLicenseStatus() == LicenseStatus.PASSWORD_EXPIRED) {
            JOptionPane.showMessageDialog(this,
                    "Sua senha expirou (validade: 30 dias).\n"
                            + "Voce entrou no ciclo Trial de " + tentativa.getTrialDaysRemaining() + " dia(s).\n"
                            + "Renove agora para voltar ao Premium.",
                    "Senha expirada",
                    JOptionPane.WARNING_MESSAGE);

            String erro = abrirModalRenovacaoSenha(identity);
            if (erro == null) {
                // Renovada com sucesso — reautenticar com nova senha
                JOptionPane.showMessageDialog(this,
                        "Senha renovada! Voce e Premium novamente.\nFaca login com a nova senha.",
                        "Senha atualizada",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Nao renovou — entra como trial se ainda disponivel
                AuthSession trialSession = authService.criarSessaoTrial();
                if (trialSession.getLicenseStatus() == LicenseStatus.EXPIRED) {
                    JOptionPane.showMessageDialog(this,
                            "Trial expirado. Renove sua senha para continuar.",
                            "Acesso bloqueado",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                session = trialSession;
                dispose();
            }
            return;
        }

        // Login valido: verificar aviso de expirar em breve
        if (tentativa.isPremium() && authService.deveAvisarExpiracaoSenha(identity)) {
            int dias = authService.getDiasParaExpirarSenha(identity);
            int opcao = JOptionPane.showConfirmDialog(this,
                    "Sua senha expira em " + dias + " dia(s)!\n"
                            + "Deseja renovar agora para nao perder o acesso Premium?",
                    "Aviso de expiracao de senha",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (opcao == JOptionPane.YES_OPTION) {
                abrirModalRenovacaoSenha(identity);
            }
        }

        session = tentativa;
        dispose();
    }

    /**
     * Abre modal de renovacao de senha. Retorna null em sucesso, mensagem de erro caso contrario.
     */
    private String abrirModalRenovacaoSenha(String identity) {
        JPasswordField txtAtual = new JPasswordField();
        JPasswordField txtNova = new JPasswordField();
        JPasswordField txtConf = new JPasswordField();

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Senha atual:"));      p.add(txtAtual);
        p.add(new JLabel("Nova senha:"));        p.add(txtNova);
        p.add(new JLabel("Confirmar nova:"));    p.add(txtConf);

        int opcao = JOptionPane.showConfirmDialog(this, p, "Renovar senha", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) {
            return "Cancelado";
        }

        String erro = authService.renovarSenha(
                identity,
                new String(txtAtual.getPassword()),
                new String(txtNova.getPassword()),
                new String(txtConf.getPassword())
        );

        if (erro != null) {
            JOptionPane.showMessageDialog(this, erro, "Renovacao de senha", JOptionPane.ERROR_MESSAGE);
        }
        return erro;
    }

    // ── Trial ────────────────────────────────────────────────────────────────

    private void continuarTrial() {
        AuthSession trial = authService.criarSessaoTrial();
        if (trial.getLicenseStatus() == LicenseStatus.EXPIRED) {
            JOptionPane.showMessageDialog(this,
                    "Periodo Trial expirado. Efetue login para continuar.",
                    "Trial expirado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        session = trial;
        dispose();
    }

    // ── Recuperacao ──────────────────────────────────────────────────────────

    private void recuperar() {
        RecoveryDialog dialog = new RecoveryDialog(this, authService);
        dialog.setVisible(true);
    }

    // ── Cadastro ─────────────────────────────────────────────────────────────

    private void cadastrar() {
        JTextField txtUsuario = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtNovaSenha = new JPasswordField();
        JPasswordField txtConfirmacao = new JPasswordField();

        JPanel painel = new JPanel(new GridLayout(0, 2, 8, 8));
        painel.add(new JLabel("Usuario:"));           painel.add(txtUsuario);
        painel.add(new JLabel("Email:"));             painel.add(txtEmail);
        painel.add(new JLabel("Senha:"));             painel.add(txtNovaSenha);
        painel.add(new JLabel("Confirmar senha:"));   painel.add(txtConfirmacao);

        int opcao = JOptionPane.showConfirmDialog(this, painel, "Cadastro de credencial",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) return;

        String senha = new String(txtNovaSenha.getPassword());
        String confirmacao = new String(txtConfirmacao.getPassword());
        if (!senha.equals(confirmacao)) {
            JOptionPane.showMessageDialog(this, "A confirmacao da senha nao confere.", "Cadastro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String erro = authService.criarUsuario(txtUsuario.getText(), txtEmail.getText(), senha, true);
        if (erro != null) {
            JOptionPane.showMessageDialog(this, erro, "Cadastro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Usuario cadastrado com sucesso. Use as credenciais para entrar.",
                "Cadastro",
                JOptionPane.INFORMATION_MESSAGE);
    }
}


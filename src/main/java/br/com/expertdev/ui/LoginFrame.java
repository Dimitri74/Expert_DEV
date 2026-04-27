package br.com.expertdev.ui;

import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.model.AuthSession;
import br.com.expertdev.model.LicenseStatus;
import br.com.expertdev.service.AuthService;
import br.com.expertdev.ui.components.BadgeLabel;
import br.com.expertdev.ui.components.StyledTextField;
import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Tela de login — Expert Dev v2.6.0-BETA.
 * JFrame standalone (460x520), não redimensionável, centralizado na tela.
 * Toda a lógica de autenticação é preservada do LoginDialog original.
 */
public class LoginFrame extends JFrame {

    private static final int LOGO_HEIGHT = 52;

    private final AuthService authService;
    private final ExpertDevConfig config;
    private AuthSession session;

    private StyledTextField txtIdentity;
    private JPasswordField txtSenha;

    public LoginFrame(AuthService authService, ExpertDevConfig config) {
        super("Expert Dev 2.6.0-BETA");
        this.authService = authService;
        this.config = config;
        construir();
    }

    public AuthSession getSession() {
        return session;
    }

    private void construir() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(460, 520);
        setMinimumSize(new Dimension(460, 520));
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ExpertDevTheme.BG_APP);
        setLayout(new BorderLayout());

        // Ícone da janela
        try {
            InputStream iconStream = getClass().getResourceAsStream("/icons/logo_transparente.png");
            if (iconStream != null) {
                setIconImage(ImageIO.read(iconStream));
            }
        } catch (Exception ignored) {}

        add(buildTopStripe(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmarSaida();
            }
        });
    }

    /** Faixa colorida 4px no topo com gradiente AMBER → BLUE_MID → DARK */
    private JPanel buildTopStripe() {
        JPanel stripe = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    GradientPaint gp = new GradientPaint(
                        0, 0, ExpertDevTheme.BRAND_AMBER,
                        getWidth() / 2f, 0, ExpertDevTheme.BRAND_BLUE_MID
                    );
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth() / 2, getHeight());
                    GradientPaint gp2 = new GradientPaint(
                        getWidth() / 2f, 0, ExpertDevTheme.BRAND_BLUE_MID,
                        getWidth(), 0, ExpertDevTheme.BRAND_DARK
                    );
                    g2.setPaint(gp2);
                    g2.fillRect(getWidth() / 2, 0, getWidth() / 2, getHeight());
                } finally {
                    g2.dispose();
                }
            }
        };
        stripe.setPreferredSize(new Dimension(0, 4));
        stripe.setOpaque(false);
        return stripe;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(ExpertDevTheme.BG_APP);

        body.add(buildHeader(), BorderLayout.NORTH);
        body.add(buildForm(), BorderLayout.CENTER);

        return body;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(ExpertDevTheme.SURFACE_ALT);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ExpertDevTheme.BORDER),
            new EmptyBorder(16, 24, 14, 24)
        ));

        // Lado esquerdo: logo + tagline
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel logoLabel = buildLogoLabel();
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(logoLabel);
        left.add(Box.createVerticalStrut(4));

        JLabel tagline = new JLabel("Enterprise AI Context Generator");
        tagline.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        tagline.setForeground(ExpertDevTheme.TEXT_MUTED);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(tagline);

        // Lado direito: badge trial
        int diasTrial = authService.getDiasRestantesTrial();
        BadgeLabel trialBadge = new BadgeLabel(
            "Trial: " + diasTrial + " dia(s)",
            ExpertDevTheme.WARNING_BG,
            ExpertDevTheme.WARNING,
            ExpertDevTheme.BORDER,
            ExpertDevTheme.RADIUS_LG
        );
        trialBadge.setFont(ExpertDevTheme.FONT_LABEL.deriveFont(10f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(trialBadge);

        header.add(left, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(24, 36, 28, 36));

        // Versão
        JLabel lblVersao = new JLabel("v2.6.0-BETA");
        lblVersao.setFont(ExpertDevTheme.FONT_MONO_SM);
        lblVersao.setForeground(ExpertDevTheme.TEXT_MUTED);
        lblVersao.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(lblVersao);
        form.add(Box.createVerticalStrut(20));

        // Campo usuário
        form.add(buildFieldLabel("USUÁRIO OU E-MAIL"));
        form.add(Box.createVerticalStrut(4));
        txtIdentity = new StyledTextField(42);
        if (config != null) txtIdentity.setText(config.getAuthLastUser());
        txtIdentity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtIdentity.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtIdentity);
        form.add(Box.createVerticalStrut(14));

        // Campo senha
        form.add(buildFieldLabel("SENHA"));
        form.add(Box.createVerticalStrut(4));
        txtSenha = buildPasswordField();
        form.add(txtSenha);
        form.add(Box.createVerticalStrut(20));

        // Botão Entrar
        JButton btnEntrar = buildPrimaryButton("Entrar \u2192");
        btnEntrar.addActionListener(e -> autenticar());
        form.add(btnEntrar);
        form.add(Box.createVerticalStrut(16));

        // Separador + "ou"
        form.add(buildSeparatorOu());
        form.add(Box.createVerticalStrut(12));

        // Grid 2 colunas: Cadastrar + Recuperar
        JPanel grid1 = new JPanel(new GridLayout(1, 2, 8, 0));
        grid1.setOpaque(false);
        grid1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JButton btnCadastrar = buildGhostButton("Cadastrar");
        JButton btnRecuperar = buildGhostButton("Recuperar senha");
        btnCadastrar.addActionListener(e -> cadastrar());
        btnRecuperar.addActionListener(e -> recuperar());
        grid1.add(btnCadastrar);
        grid1.add(btnRecuperar);
        form.add(grid1);
        form.add(Box.createVerticalStrut(8));

        // Grid 2 colunas: Trial + Sair
        JPanel grid2 = new JPanel(new GridLayout(1, 2, 8, 0));
        grid2.setOpaque(false);
        grid2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JButton btnTrial = buildOutlineButton("Continuar Trial");
        JButton btnSair  = buildDangerButton("Sair");
        btnTrial.addActionListener(e -> continuarTrial());
        btnSair.addActionListener(e -> confirmarSaida());
        grid2.add(btnTrial);
        grid2.add(btnSair);
        form.add(grid2);

        return form;
    }

    // ── Helpers de label e botão ────────────────────────────────────────────

    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ExpertDevTheme.FONT_LABEL);
        lbl.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPasswordField buildPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(ExpertDevTheme.FONT_BODY);
        field.setBackground(ExpertDevTheme.WHITE);
        field.setForeground(ExpertDevTheme.TEXT_BODY);
        field.setCaretColor(ExpertDevTheme.PRIMARY);
        field.setColumns(24);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        field.setPreferredSize(new Dimension(360, 42));
        field.setMinimumSize(new Dimension(360, 42));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JButton buildPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed() ? ExpertDevTheme.PRIMARY_DARK
                              : getModel().isRollover() ? ExpertDevTheme.PRIMARY_DARK
                              : ExpertDevTheme.PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(btn, ExpertDevTheme.WHITE, ExpertDevTheme.FONT_BOLD.deriveFont(14f), 46);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return btn;
    }

    private JButton buildGhostButton(String text) {
        final Color[] hover = { null };
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(ExpertDevTheme.GRAY_100);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
                    }
                    g2.setColor(ExpertDevTheme.BORDER);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(btn, ExpertDevTheme.TEXT_SECONDARY, ExpertDevTheme.FONT_BODY, ExpertDevTheme.BTN_HEIGHT);
        return btn;
    }

    private JButton buildOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(ExpertDevTheme.PRIMARY_LIGHT);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
                    }
                    g2.setColor(ExpertDevTheme.PRIMARY);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(btn, ExpertDevTheme.PRIMARY, ExpertDevTheme.FONT_BODY, ExpertDevTheme.BTN_HEIGHT);
        return btn;
    }

    private JButton buildDangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(ExpertDevTheme.DANGER_BG);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
                    }
                    g2.setColor(ExpertDevTheme.DANGER);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(btn, ExpertDevTheme.DANGER, ExpertDevTheme.FONT_BODY, ExpertDevTheme.BTN_HEIGHT);
        return btn;
    }

    private void styleButton(JButton btn, Color fg, Font font, int height) {
        btn.setForeground(fg);
        btn.setFont(font);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, height));
    }

    private JPanel buildSeparatorOu() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(ExpertDevTheme.BORDER);
        row.add(sep1, gbc);

        gbc.weightx = 0;
        gbc.insets = new Insets(0, 8, 0, 8);
        JLabel ou = new JLabel("ou");
        ou.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        ou.setForeground(ExpertDevTheme.TEXT_MUTED);
        row.add(ou, gbc);

        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(ExpertDevTheme.BORDER);
        row.add(sep2, gbc);

        return row;
    }

    private JLabel buildLogoLabel() {
        JLabel label = new JLabel("Expert Dev");
        label.setFont(ExpertDevTheme.FONT_HEADER);
        label.setForeground(ExpertDevTheme.BRAND_DARK);

        ImageIcon logo = loadLogo(LOGO_HEIGHT);
        if (logo != null) {
            label.setIcon(logo);
            label.setText(null);
        }
        return label;
    }

    private ImageIcon loadLogo(int targetHeight) {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream("/icons/logo_transparente.png");
            if (is == null) return null;
            BufferedImage raw = ImageIO.read(is);
            if (raw == null) return null;
            int w = raw.getWidth() * targetHeight / raw.getHeight();
            Image scaled = raw.getScaledInstance(w, targetHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        } finally {
            if (is != null) { try { is.close(); } catch (Exception ignored) {} }
        }
    }

    // ── Diálogo de confirmação de saída ───────────────────────────────────────

    private void confirmarSaida() {
        int opt = JOptionPane.showConfirmDialog(
            this,
            "Deseja sair do Expert Dev?",
            "Confirmação",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (opt == JOptionPane.YES_OPTION) {
            session = null;
            dispose();
        }
    }

    // ── Autenticação ──────────────────────────────────────────────────────────

    private void autenticar() {
        String identity = txtIdentity.getText().trim();
        String senha = new String(txtSenha.getPassword());
        AuthSession tentativa = authService.autenticar(identity, senha);

        if (tentativa == null) {
            JOptionPane.showMessageDialog(this,
                "Credenciais invalidas. Verifique usuario/email e senha.",
                "Login", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (tentativa.getLicenseStatus() == LicenseStatus.EXPIRED
                && tentativa.getUsername().startsWith("BLOQUEADO")) {
            JOptionPane.showMessageDialog(this, tentativa.getUsername(),
                "Conta bloqueada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tentativa.getLicenseStatus() == LicenseStatus.PASSWORD_EXPIRED) {
            JOptionPane.showMessageDialog(this,
                "Sua senha expirou (validade: 30 dias).\n"
                + "Voce entrou no ciclo Trial de " + tentativa.getTrialDaysRemaining() + " dia(s).\n"
                + "Renove agora para voltar ao Premium.",
                "Senha expirada", JOptionPane.WARNING_MESSAGE);

            String erro = abrirModalRenovacaoSenha(identity);
            if (erro == null) {
                JOptionPane.showMessageDialog(this,
                    "Senha renovada! Voce e Premium novamente.\nFaca login com a nova senha.",
                    "Senha atualizada", JOptionPane.INFORMATION_MESSAGE);
            } else {
                AuthSession trialSession = authService.criarSessaoTrial();
                if (trialSession.getLicenseStatus() == LicenseStatus.EXPIRED) {
                    JOptionPane.showMessageDialog(this,
                        "Trial expirado. Renove sua senha para continuar.",
                        "Acesso bloqueado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                session = trialSession;
                dispose();
            }
            return;
        }

        if (tentativa.isPremium() && authService.deveAvisarExpiracaoSenha(identity)) {
            int dias = authService.getDiasParaExpirarSenha(identity);
            int opcao = JOptionPane.showConfirmDialog(this,
                "Sua senha expira em " + dias + " dia(s)!\n"
                + "Deseja renovar agora para nao perder o acesso Premium?",
                "Aviso de expiracao de senha",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opcao == JOptionPane.YES_OPTION) {
                abrirModalRenovacaoSenha(identity);
            }
        }

        ExpertDevConfig.salvarUltimoUsuario(identity);
        session = tentativa;
        dispose();
    }

    private String abrirModalRenovacaoSenha(String identity) {
        JPasswordField txtAtual = new JPasswordField();
        JPasswordField txtNova  = new JPasswordField();
        JPasswordField txtConf  = new JPasswordField();

        JPanel p = new JPanel(new GridLayout(0, 2, 12, 12));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));
        p.add(new JLabel("Senha atual:"));   p.add(txtAtual);
        p.add(new JLabel("Nova senha:"));    p.add(txtNova);
        p.add(new JLabel("Confirmar nova:")); p.add(txtConf);

        JOptionPane pane = new JOptionPane(p, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(this, "Renovar senha");
        dialog.setVisible(true);

        Object val = pane.getValue();
        if (!(val instanceof Integer) || (Integer) val != JOptionPane.OK_OPTION) return "Cancelado";

        String erro = authService.renovarSenha(identity,
            new String(txtAtual.getPassword()),
            new String(txtNova.getPassword()),
            new String(txtConf.getPassword()));

        if (erro != null) {
            JOptionPane.showMessageDialog(this, erro, "Renovação de senha", JOptionPane.ERROR_MESSAGE);
        }
        return erro;
    }

    // ── Trial ─────────────────────────────────────────────────────────────────

    private void continuarTrial() {
        AuthSession trial = authService.criarSessaoTrial();
        if (trial.getLicenseStatus() == LicenseStatus.EXPIRED) {
            JOptionPane.showMessageDialog(this,
                "Periodo Trial expirado. Efetue login para continuar.",
                "Trial expirado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        session = trial;
        dispose();
    }

    // ── Recuperação ───────────────────────────────────────────────────────────

    private void recuperar() {
        JTextField txtIdentity = new JTextField();

        JPanel painelGeracao = new JPanel(new GridLayout(0, 2, 12, 12));
        painelGeracao.setBorder(new EmptyBorder(15, 15, 15, 15));
        painelGeracao.add(new JLabel("Usuário ou email:"));
        painelGeracao.add(txtIdentity);

        JOptionPane paneGeracao = new JOptionPane(
            painelGeracao,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION
        );
        JDialog dialogGeracao = paneGeracao.createDialog(this, "Recuperar acesso");
        dialogGeracao.setVisible(true);

        Object valGeracao = paneGeracao.getValue();
        if (!(valGeracao instanceof Integer) || (Integer) valGeracao != JOptionPane.OK_OPTION) return;

        String identity = txtIdentity.getText() == null ? "" : txtIdentity.getText().trim();
        if (identity.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Informe seu usuário ou email para gerar o código.",
                "Recuperação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String codigo = authService.gerarCodigoReset(identity);
        String username = authService.obterUsernamePorIdentificador(identity);
        if (codigo == null || username == null) {
            JOptionPane.showMessageDialog(this,
                "Usuário ou email não encontrado no sistema.",
                "Recuperação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Usuário encontrado: " + username + "\n"
                + "Código de recuperação: " + codigo + "\n"
                + "Validade: 15 minutos.",
            "Código Gerado", JOptionPane.INFORMATION_MESSAGE);

        JTextField txtCodigo = new JTextField(codigo);
        JPasswordField txtNovaSenha = new JPasswordField();
        JPasswordField txtConfirmarSenha = new JPasswordField();

        JPanel painelReset = new JPanel(new GridLayout(0, 2, 12, 12));
        painelReset.setBorder(new EmptyBorder(15, 15, 15, 15));
        painelReset.add(new JLabel("Usuário ou email:"));
        painelReset.add(new JLabel(identity));
        painelReset.add(new JLabel("Código de recuperação:"));
        painelReset.add(txtCodigo);
        painelReset.add(new JLabel("Nova senha:"));
        painelReset.add(txtNovaSenha);
        painelReset.add(new JLabel("Confirmar senha:"));
        painelReset.add(txtConfirmarSenha);

        JOptionPane paneReset = new JOptionPane(
            painelReset,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION
        );
        JDialog dialogReset = paneReset.createDialog(this, "Recuperar acesso");
        dialogReset.setVisible(true);

        Object valReset = paneReset.getValue();
        if (!(valReset instanceof Integer) || (Integer) valReset != JOptionPane.OK_OPTION) return;

        String senha = new String(txtNovaSenha.getPassword());
        String confirmar = new String(txtConfirmarSenha.getPassword());
        if (!senha.equals(confirmar)) {
            JOptionPane.showMessageDialog(this,
                "A confirmação da senha não confere.",
                "Recuperação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String erro = authService.redefinirSenha(identity, txtCodigo.getText().trim(), senha);
        if (erro != null) {
            JOptionPane.showMessageDialog(this, erro, "Recuperação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Senha atualizada com sucesso.",
            "Recuperação", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Cadastro ──────────────────────────────────────────────────────────────

    private void cadastrar() {
        JTextField txtUsuario   = new JTextField();
        JTextField txtEmail     = new JTextField();
        JPasswordField txtNovaSenha     = new JPasswordField();
        JPasswordField txtConfirmacao   = new JPasswordField();

        JPanel painel = new JPanel(new GridLayout(0, 2, 12, 12));
        painel.setBorder(new EmptyBorder(15, 15, 15, 15));
        painel.add(new JLabel("Usuário:"));          painel.add(txtUsuario);
        painel.add(new JLabel("Email:"));            painel.add(txtEmail);
        painel.add(new JLabel("Senha:"));            painel.add(txtNovaSenha);
        painel.add(new JLabel("Confirmar senha:"));  painel.add(txtConfirmacao);

        JOptionPane pane = new JOptionPane(painel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(this, "Cadastro de credencial");
        dialog.setVisible(true);

        Object val = pane.getValue();
        if (!(val instanceof Integer) || (Integer) val != JOptionPane.OK_OPTION) return;

        String senha = new String(txtNovaSenha.getPassword());
        String confirmacao = new String(txtConfirmacao.getPassword());
        if (!senha.equals(confirmacao)) {
            JOptionPane.showMessageDialog(this,
                "A confirmação da senha não confere.", "Cadastro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String erro = authService.criarUsuario(txtUsuario.getText(), txtEmail.getText(), senha, true);
        if (erro != null) {
            JOptionPane.showMessageDialog(this, erro, "Cadastro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
            "Usuário cadastrado com sucesso. Use as credenciais para entrar.",
            "Cadastro", JOptionPane.INFORMATION_MESSAGE);
    }
}

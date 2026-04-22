package br.com.expertdev.ui;

import br.com.expertdev.model.AuthSession;
import br.com.expertdev.model.LicenseStatus;
import br.com.expertdev.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class LoginDialog extends JDialog {

    // ─── Cores do projeto (SEM TEMA-DARK) ──────────────────────────────────
    private static final Color COR_FUNDO = new Color(245, 247, 251);          // Fundo claro
    private static final Color COR_PAINEL = new Color(255, 255, 255);         // Branco puro
    private static final Color COR_PAINEL_TOPO = new Color(240, 244, 249);    // Cinzento claro
    private static final Color COR_AZUL = new Color(79, 70, 229);             // Azul do projeto
    private static final Color COR_VERMELHO_CLARO = new Color(239, 68, 68);   // Vermelho claro
    private static final Color COR_AMARELO_CLARO = new Color(245, 158, 11);   // Amarelo claro
    private static final Color COR_VERDE_CLARO = new Color(34, 197, 94);      // Verde claro
    private static final Color COR_ROSA_CLARO = new Color(236, 72, 153);      // Rosa claro para Trial
    private static final Color COR_TEXTO = new Color(15, 23, 42);             // Texto escuro
    private static final Color COR_TEXTO_SUAVE = new Color(71, 85, 105);      // Texto cinzento
    private static final Color COR_BORDA = new Color(203, 213, 225);          // Borda cinzenta

    // ─── Fontes ───────────────────────────────────────────────────────────────
    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONTE_ROTULO = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 12);

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
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COR_FUNDO);

        // ─── Painel superior com logo e info ───────────────────────────────────
        JPanel painelTopo = criarPainelTopo();

        // ─── Painel central (campos de entrada) ────────────────────────────────
        JPanel painel = new JPanel(new GridLayout(0, 2, 20, 15));
        painel.setBackground(COR_PAINEL);
        painel.setBorder(new EmptyBorder(30, 40, 30, 40));

        txtIdentity = criarCampoTexto();
        txtSenha = criarCampoSenha();

        JLabel lblUsuario = new JLabel("Usuário ou email:");
        lblUsuario.setFont(FONTE_ROTULO);
        lblUsuario.setForeground(COR_TEXTO);

        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setFont(FONTE_ROTULO);
        lblSenha.setForeground(COR_TEXTO);

        painel.add(lblUsuario);
        painel.add(txtIdentity);
        painel.add(lblSenha);
        painel.add(txtSenha);

        // ─── Painel de botões ──────────────────────────────────────────────────
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        botoes.setBackground(COR_FUNDO);
        botoes.setBorder(new EmptyBorder(15, 20, 20, 20));

        JButton btnCadastro = criarBotao("Cadastrar", COR_AMARELO_CLARO);
        JButton btnRecuperar = criarBotao("Recuperar senha", COR_VERDE_CLARO);
        JButton btnTrial = criarBotao("Continuar Trial", COR_ROSA_CLARO);
        JButton btnEntrar = criarBotao("Entrar", COR_AZUL);
        JButton btnSair = criarBotao("Sair", COR_VERMELHO_CLARO);

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

        add(painelTopo, BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        setSize(900, 380);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Adicionar listener para o botão X (fechar)
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmarSaida();
            }
        });
    }

    private void confirmarSaida() {
        // Criar painel com mensagem e botões customizados
        JPanel painelDialogo = new JPanel(new BorderLayout(15, 15));
        painelDialogo.setBackground(COR_PAINEL);
        painelDialogo.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Mensagem
        JLabel lblMensagem = new JLabel("Deseja sair do Expert Dev?");
        lblMensagem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMensagem.setForeground(COR_TEXTO);

        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        painelBotoes.setBackground(COR_PAINEL);

        JButton btnSim = criarBotao("Sim", COR_VERDE_CLARO);
        JButton btnNao = criarBotao("Não", COR_VERMELHO_CLARO);

        btnSim.addActionListener(e -> {
            // SIM = Sair da aplicação (fechar)
            JDialog dialogPai = (JDialog) SwingUtilities.getWindowAncestor(painelDialogo);
            if (dialogPai != null) {
                dialogPai.dispose();
            }
            LoginDialog.this.dispose();
        });

        btnNao.addActionListener(e -> {
            // NÃO = Permanecer no sistema (apenas fecha o diálogo)
            JDialog dialogPai = (JDialog) SwingUtilities.getWindowAncestor(painelDialogo);
            if (dialogPai != null) {
                dialogPai.dispose();
            }
        });

        painelBotoes.add(btnSim);
        painelBotoes.add(btnNao);

        painelDialogo.add(lblMensagem, BorderLayout.CENTER);
        painelDialogo.add(painelBotoes, BorderLayout.SOUTH);

        // Criar o diálogo
        JDialog dialogConfirmacao = new JDialog(this, "Confirmação", ModalityType.APPLICATION_MODAL);
        dialogConfirmacao.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialogConfirmacao.setContentPane(painelDialogo);
        dialogConfirmacao.setSize(400, 180);
        dialogConfirmacao.setLocationRelativeTo(this);
        dialogConfirmacao.getContentPane().setBackground(COR_PAINEL);
        dialogConfirmacao.setVisible(true);
    }

    private JPanel criarPainelTopo() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_PAINEL_TOPO);
        painel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA));
        painel.setPreferredSize(new Dimension(0, 85));

        // Lado esquerdo: logo
        JLabel lblLogo = criarLabelLogo();
        JPanel painelLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        painelLogo.setBackground(COR_PAINEL_TOPO);
        painelLogo.add(lblLogo);

        // Lado direito: informação de trial
        JPanel painelInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 12));
        painelInfo.setBackground(COR_PAINEL_TOPO);

        int diasTrial = authService.getDiasRestantesTrial();
        lblTrial = new JLabel("Trial restante: " + diasTrial + " dia(s)");
        lblTrial.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTrial.setForeground(COR_TEXTO_SUAVE);

        JLabel lblVersao = new JLabel("v2.2.3-BETA");
        lblVersao.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVersao.setForeground(COR_TEXTO_SUAVE);

        painelInfo.add(lblTrial);
        painelInfo.add(Box.createHorizontalStrut(15));
        painelInfo.add(lblVersao);

        painel.add(painelLogo, BorderLayout.WEST);
        painel.add(painelInfo, BorderLayout.EAST);

        return painel;
    }

    private JLabel criarLabelLogo() {
        JLabel label = new JLabel("Expert Dev");
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(COR_AZUL);

        try {
            URL resourceUrl = getClass().getClassLoader().getResource("icons/logo_transparente.png");
            if (resourceUrl != null) {
                BufferedImage img = ImageIO.read(resourceUrl);
                if (img != null) {
                    Image scaledImg = img.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImg));
                    label.setIconTextGap(10);
                }
            }
        } catch (IOException e) {
            // Se não conseguir carregar a imagem, apenas mostra o texto
        }

        return label;
    }

    // ─── Métodos auxiliares para criação de componentes ──────────────────────

    private JTextField criarCampoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(FONTE_NORMAL);
        campo.setBackground(new Color(248, 250, 252));
        campo.setForeground(COR_TEXTO);
        campo.setCaretColor(COR_AZUL);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        campo.setPreferredSize(new Dimension(250, 35));
        return campo;
    }

    private JPasswordField criarCampoSenha() {
        JPasswordField campo = new JPasswordField();
        campo.setFont(FONTE_NORMAL);
        campo.setBackground(new Color(248, 250, 252));
        campo.setForeground(COR_TEXTO);
        campo.setCaretColor(COR_AZUL);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        campo.setPreferredSize(new Dimension(250, 35));
        return campo;
    }

    private JButton criarBotao(String texto, Color cor) {
        JButton botao = new JButton(texto);
        botao.setFont(FONTE_BOTAO);
        botao.setBackground(cor);

        // Determinar cor de texto baseado em contraste
        Color corTexto = obterCorTextoParaBotao(cor);
        botao.setForeground(corTexto);

        // Forçar renderização correta
        botao.setContentAreaFilled(true);
        botao.setOpaque(true);
        botao.setBorderPainted(true);
        botao.setBorder(BorderFactory.createLineBorder(cor, 2));
        botao.setFocusPainted(false);

        botao.setPreferredSize(new Dimension(140, 40));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efeito hover
        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(aumentarBrilho(cor, 1.1f));
                botao.setForeground(corTexto);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(cor);
                botao.setForeground(corTexto);
            }
        });

        return botao;
    }

    private Color obterCorTextoParaBotao(Color cor) {
        // Determinar cor de texto baseado no botão
        // AMARELO e VERDE → Texto PRETO (cores claras)
        // AZUL, ROSA e VERMELHO → Texto PRETO (como solicitado)

        // Se é amarelo (cadastrar) ou verde (recuperar) → texto preto
        if (cor.equals(COR_AMARELO_CLARO) || cor.equals(COR_VERDE_CLARO)) {
            return new Color(15, 23, 42);  // Preto/Cinzento escuro
        }

        // Se é azul (entrar), rosa (trial) ou vermelho (sair) → texto preto
        if (cor.equals(COR_AZUL) || cor.equals(COR_ROSA_CLARO) || cor.equals(COR_VERMELHO_CLARO)) {
            return new Color(15, 23, 42);  // Preto/Cinzento escuro
        }

        // Fallback: calcular luminância percebida (fórmula WCAG)
        double luminancia = (0.299 * cor.getRed() + 0.587 * cor.getGreen() + 0.114 * cor.getBlue()) / 255.0;
        if (luminancia > 0.5) {
            return new Color(15, 23, 42);  // Preto para cores claras
        } else {
            return Color.WHITE;  // Branco para cores escuras
        }
    }


    private Color aumentarBrilho(Color cor, float fator) {
        int r = Math.min(255, (int) (cor.getRed() * fator));
        int g = Math.min(255, (int) (cor.getGreen() * fator));
        int b = Math.min(255, (int) (cor.getBlue() * fator));
        return new Color(r, g, b);
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
        JPasswordField txtAtual = criarCampoSenha();
        JPasswordField txtNova = criarCampoSenha();
        JPasswordField txtConf = criarCampoSenha();

        JPanel p = new JPanel(new GridLayout(0, 2, 12, 12));
        p.setBackground(COR_PAINEL);
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel[] labels = {
                criarLabel("Senha atual:"),
                criarLabel("Nova senha:"),
                criarLabel("Confirmar nova:")
        };

        p.add(labels[0]);      p.add(txtAtual);
        p.add(labels[1]);      p.add(txtNova);
        p.add(labels[2]);      p.add(txtConf);

        JOptionPane pane = new JOptionPane(p, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(this, "Renovar senha");
        dialog.getContentPane().setBackground(COR_FUNDO);
        dialog.setVisible(true);

        int opcao = (Integer) pane.getValue();
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
            JOptionPane.showMessageDialog(this, erro, "Renovação de senha", JOptionPane.ERROR_MESSAGE);
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
        JTextField txtUsuario = criarCampoTexto();
        JTextField txtEmail = criarCampoTexto();
        JPasswordField txtNovaSenha = criarCampoSenha();
        JPasswordField txtConfirmacao = criarCampoSenha();

        JPanel painel = new JPanel(new GridLayout(0, 2, 12, 12));
        painel.setBackground(COR_PAINEL);
        painel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel[] labels = {
                criarLabel("Usuário:"),
                criarLabel("Email:"),
                criarLabel("Senha:"),
                criarLabel("Confirmar senha:")
        };

        painel.add(labels[0]);           painel.add(txtUsuario);
        painel.add(labels[1]);           painel.add(txtEmail);
        painel.add(labels[2]);           painel.add(txtNovaSenha);
        painel.add(labels[3]);           painel.add(txtConfirmacao);

        JOptionPane pane = new JOptionPane(painel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(this, "Cadastro de credencial");
        dialog.getContentPane().setBackground(COR_FUNDO);
        dialog.setVisible(true);

        int opcao = (Integer) pane.getValue();
        if (opcao != JOptionPane.OK_OPTION) return;

        String senha = new String(txtNovaSenha.getPassword());
        String confirmacao = new String(txtConfirmacao.getPassword());
        if (!senha.equals(confirmacao)) {
            JOptionPane.showMessageDialog(this, "A confirmação da senha não confere.", "Cadastro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String erro = authService.criarUsuario(txtUsuario.getText(), txtEmail.getText(), senha, true);
        if (erro != null) {
            JOptionPane.showMessageDialog(this, erro, "Cadastro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Usuário cadastrado com sucesso. Use as credenciais para entrar.",
                "Cadastro",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_ROTULO);
        label.setForeground(COR_TEXTO);
        return label;
    }
}


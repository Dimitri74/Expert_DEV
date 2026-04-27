package br.com.expertdev.ui;

import br.com.expertdev.service.*;
import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.model.AuthSession;
import br.com.expertdev.model.LicenseStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ExpertDevGUI extends JFrame {

    // ─── Tema, fábrica, controller e construtor de painéis ───────────────────
    private AppTheme theme;
    private UiFactory uiFactory;
    GuiController controller;
    private GuiPanelBuilder panelBuilder;

    // ─── Componentes principais (package-private para acesso pelo controller) ─
    JTabbedPane abas;
    JTextArea areaUrls;
    JLabel labelArquivoWord;
    JLabel labelResumoArquivosWord;
    JTextArea areaPreviewWord;
    JPanel painelPreviewWord;
    JButton btnAbrirPreviewWord;
    DefaultListModel<File> modeloArquivosWord;
    JList<File> listaArquivosWord;
    JPanel painelLogCards;
    JScrollPane scrollLog;
    JTextArea areaPrompt;
    JProgressBar barraProgresso;
    JButton btnProcessar;
    JButton btnCopiarPrompt;
    JButton btnSalvar;
    JComboBox<String> comboModoGeracao;
    JComboBox<String> comboProviderIa;
    JComboBox<String> comboPerfilPrompt;
    JPasswordField campoApiKey;
    JCheckBox chkSalvarApiKey;
    JButton btnTestarConexaoIa;
    JButton btnLimparApiKey;
    JLabel lblAvisoModoEconomicoIa;
    JLabel lblEstimativaIa;
    JTextField campoRTC;
    JTextField campoUC;
    JTextArea areaHistorico;
    JTextField campoEstimativaPoker;
    JTextField campoSprint;
    JComboBox<String> comboComplexidade;
    JPanel painelGrafico;
    JPanel painelTendencia;
    JLabel lblGanhoProdutividade;
    JButton btnIniciarScrum;
    JButton btnFinalizarScrum;
    JButton btnFinalizarExpertDev;
    final AuthSession authSession;

    public ExpertDevGUI() {
        this(new AuthSession("Local", "", LicenseStatus.PREMIUM, 0));
    }

    public ExpertDevGUI(AuthSession authSession) {
        this.authSession = authSession == null
                ? new AuthSession("Local", "", LicenseStatus.PREMIUM, 0)
                : authSession;
        theme = new AppTheme(true);
        uiFactory = new UiFactory(theme);
        controller = new GuiController(this, theme, uiFactory, this.authSession);
        panelBuilder = new GuiPanelBuilder(this, theme, uiFactory, controller);
        ExpertDevConfig configUi = ExpertDevConfig.carregar();
        controller.modoGeracaoSelecionado = configUi.getUiModoGeracao().equalsIgnoreCase("IA") ? "IA" : "LOCAL";
        controller.providerIaSelecionado = controller.normalizarProvider(configUi.getAiProvider());
        controller.perfilPromptSelecionado = configUi.getPromptProfile();
        configurarJanela();
        construirInterface();
        controller.inicializar();
        controller.preencherConfigIaInicial(configUi);
        controller.atualizarEstadoOpcoesIa();
        setVisible(true);
    }

    private void configurarJanela() {
        setTitle("Expert Dev 2.6.0-BETA — Gerador de Contexto para IA");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 780);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(theme.corFundo);

        BufferedImage logo = uiFactory.carregarImagemLogoPorTema();
        if (logo != null) {
            setIconImage(logo);
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controller.confirmarSaidaExpertDev();
            }
        });
    }

    protected void construirInterface() {
        setLayout(new BorderLayout(0, 0));
        add(panelBuilder.criarCabecalho(), BorderLayout.NORTH);
        add(panelBuilder.criarCorpo(), BorderLayout.CENTER);
        add(panelBuilder.criarRodape(), BorderLayout.SOUTH);
    }

    void adicionarCardLog(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;

        SwingUtilities.invokeLater(() -> {
            LogCard card = new LogCard(msg);
            painelLogCards.add(card);
            painelLogCards.add(Box.createVerticalStrut(4));
            painelLogCards.revalidate();
            painelLogCards.repaint();

            JScrollBar vertical = scrollLog.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private class LogCard extends JPanel {
        public LogCard(String msg) {
            setLayout(new BorderLayout(10, 0));
            setBackground(theme.isClaroAtivo() ? new Color(250, 250, 252) : new Color(20, 20, 30));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            String texto = msg;
            Icon icone;
            Color corTexto = theme.corTexto;

            if (msg.startsWith("→")) {
                icone = uiFactory.criarIconeInfo(14);
                texto = msg.substring(1).trim();
            } else if (msg.startsWith("✓") || msg.startsWith("✅")) {
                icone = uiFactory.criarIconeSucesso(14);
                corTexto = theme.corSucesso;
                texto = msg.substring(1).trim();
            } else if (msg.startsWith("⚠")) {
                icone = uiFactory.criarIconeAlerta(14);
                corTexto = new Color(210, 140, 0);
                texto = msg.substring(1).trim();
            } else if (msg.startsWith("❌") || msg.startsWith("ERRO:")) {
                icone = uiFactory.criarIconeErro(14);
                corTexto = theme.corErro;
                texto = msg.replace("❌", "").replace("ERRO:", "").trim();
            } else {
                icone = uiFactory.criarIconeInfo(14);
            }

            add(new JLabel(icone), BorderLayout.WEST);

            JLabel lblTexto = new JLabel(texto);
            lblTexto.setFont(AppTheme.FONTE_NORMAL);
            lblTexto.setForeground(corTexto);
            add(lblTexto, BorderLayout.CENTER);

            setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, theme.corBorda),
                new EmptyBorder(8, 12, 8, 12)
            ));
        }
    }

    void alternarTema(boolean claro) {
        ExpertDevConfig.salvarPreferenciaTemaClaro(claro);

        String urlsTexto = areaUrls != null ? areaUrls.getText() : "";
        String previewTexto = areaPreviewWord != null ? areaPreviewWord.getText() : "";
        String promptTexto = areaPrompt != null ? areaPrompt.getText() : "";
        String apiKeyTexto = campoApiKey != null ? new String(campoApiKey.getPassword()) : "";
        boolean salvarKey = chkSalvarApiKey != null && chkSalvarApiKey.isSelected();
        String modoSelecionado = comboModoGeracao != null
                ? String.valueOf(comboModoGeracao.getSelectedItem())
                : controller.modoGeracaoSelecionado;
        int abaSelecionada = abas != null ? abas.getSelectedIndex() : 0;

        theme.aplicar(claro);
        uiFactory = new UiFactory(theme);
        panelBuilder = new GuiPanelBuilder(this, theme, uiFactory, controller);

        getContentPane().removeAll();
        getContentPane().setBackground(theme.corFundo);
        construirInterface();

        if (areaUrls != null) areaUrls.setText(urlsTexto);
        if (areaPreviewWord != null && !previewTexto.trim().isEmpty()) areaPreviewWord.setText(previewTexto);
        if (areaPrompt != null) areaPrompt.setText(promptTexto);
        if (comboModoGeracao != null) {
            comboModoGeracao.setSelectedItem(modoSelecionado);
            controller.modoGeracaoSelecionado = modoSelecionado;
        }
        if (comboProviderIa != null) comboProviderIa.setSelectedItem(controller.providerIaSelecionado);
        if (comboPerfilPrompt != null) comboPerfilPrompt.setSelectedItem(controller.perfilPromptSelecionado);
        if (campoApiKey != null) campoApiKey.setText(apiKeyTexto);
        if (chkSalvarApiKey != null) {
            chkSalvarApiKey.setSelected(salvarKey);
            controller.salvarApiKeySelecionada = salvarKey;
        }
        controller.atualizarEstadoOpcoesIa();
        if (abas != null && abaSelecionada >= 0 && abaSelecionada < abas.getTabCount()) {
            abas.setSelectedIndex(abaSelecionada);
        }

        revalidate();
        repaint();
    }

    public static void lancar() {
        br.com.expertdev.ui.theme.ExpertDevLaf.install();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ExpertDevConfig config = ExpertDevConfig.carregar();
                if (!config.isAuthEnabled()) {
                    new MainFrame(new AuthSession("Local", "", LicenseStatus.PREMIUM, 0));
                    return;
                }

                AuthService authService = new AuthService();
                LoginFrame loginFrame = new LoginFrame(authService, config);
                loginFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        AuthSession session = loginFrame.getSession();
                        if (session == null || session.getLicenseStatus() == LicenseStatus.EXPIRED) {
                            JOptionPane.showMessageDialog(null,
                                    "Sem acesso valido. Encerrando o Expert Dev.",
                                    "Acesso",
                                    JOptionPane.WARNING_MESSAGE);
                            System.exit(0);
                            return;
                        }
                        new MainFrame(session);
                    }
                });
                loginFrame.setVisible(true);
            }
        });
    }
}

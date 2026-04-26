package br.com.expertdev.ui;

import br.com.expertdev.service.*;
import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.io.DefaultTextFileWriter;
import br.com.expertdev.ui.presentation.PresentationMessageService;
import br.com.expertdev.ui.tabs.ExpertDevTabsBuilder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.axis.DateAxis;
import java.text.SimpleDateFormat;
import br.com.expertdev.model.*;
import java.time.ZoneId;
import java.util.Date;
import java.util.TreeMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.awt.Desktop;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interface gráfica principal do Expert Dev.
 * Permite processar URLs ou fazer upload de um arquivo Word (.doc/.docx)
 * para gerar o prompt de contexto para IA.
 */
public class ExpertDevGUI extends JFrame {

    // ─── Tema, fábrica e controller ──────────────────────────────────────────
    private AppTheme theme;
    private UiFactory uiFactory;
    GuiController controller;

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
    private final AuthSession authSession;
    private final PresentationMessageService presentationMessageService;

    public ExpertDevGUI() {
        this(new AuthSession("Local", "", LicenseStatus.PREMIUM, 0));
    }

    public ExpertDevGUI(AuthSession authSession) {
        this.authSession = authSession == null
                ? new AuthSession("Local", "", LicenseStatus.PREMIUM, 0)
                : authSession;
        this.presentationMessageService = new PresentationMessageService(this);
        theme = new AppTheme(true); // Sempre inicia no modo Light
        uiFactory = new UiFactory(theme);
        controller = new GuiController(this, theme, uiFactory, this.authSession);
        ExpertDevConfig configUi = ExpertDevConfig.carregar();
        controller.modoGeracaoSelecionado = configUi.getUiModoGeracao().equalsIgnoreCase("IA") ? "IA" : "LOCAL";
        controller.providerIaSelecionado = controller.normalizarProvider(configUi.getAiProvider());
        controller.perfilPromptSelecionado = configUi.getPromptProfile();
        configurarJanela();
        construirInterface();
        controller.inicializar(); // inicia clipboard monitor e tray (precisa de componentes prontos)
        controller.preencherConfigIaInicial(configUi);
        controller.atualizarEstadoOpcoesIa();
        setVisible(true);
    }


    private void aplicarTema(boolean claro) {
        theme.aplicar(claro);
    }

    // ─── Configuração da Janela ────────────────────────────────────────────────

    private void configurarJanela() {
        setTitle("Expert Dev — Gerador de Contexto para IA");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 780);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(theme.corFundo);

        // Define a logo do projeto como ícone da janela (ícone do .jar ao rodar)
        BufferedImage logo = uiFactory.carregarImagemLogoPorTema();
        if (logo != null) {
            setIconImage(logo);
        }

        // Adicionar WindowListener para confirmar saída ao clicar X
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmarSaidaExpertDev();
            }
        });
    }

    // ─── Construção da Interface ───────────────────────────────────────────────

    private void construirInterface() {
        setLayout(new BorderLayout(0, 0));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarCorpo(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
    }

    /** Cabeçalho com título e subtítulo */
    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(theme.corPainel);
        cabecalho.setBorder(new EmptyBorder(20, 28, 16, 28));

        JPanel textosPanel = new JPanel();
        textosPanel.setLayout(new BoxLayout(textosPanel, BoxLayout.Y_AXIS));
        textosPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Expert Dev");
        Icon logoProjeto = uiFactory.carregarLogoProjeto(AppTheme.HEADER_LOGO_LARGURA, AppTheme.HEADER_LOGO_ALTURA);
        if (logoProjeto != null) {
            lblTitulo.setText("");
            lblTitulo.setIcon(logoProjeto);
            lblTitulo.setPreferredSize(new Dimension(AppTheme.HEADER_LOGO_LARGURA, AppTheme.HEADER_LOGO_ALTURA));
        } else {
            lblTitulo.setIcon(uiFactory.criarIconeJava(22));
            lblTitulo.setIconTextGap(8);
        }
        lblTitulo.setFont(AppTheme.FONTE_TITULO);
        lblTitulo.setForeground(theme.corDestaque);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Automatizador inteligente de contexto e gerador de prompts para IA ");
        lblSubtitulo.setFont(AppTheme.FONTE_SUBTITULO);
        lblSubtitulo.setForeground(theme.corTextoSuave);
        lblSubtitulo.setBorder(new EmptyBorder(2, 2, 0, 0));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        textosPanel.add(lblTitulo);
        textosPanel.add(Box.createVerticalStrut(4));
        textosPanel.add(lblSubtitulo);
        cabecalho.add(textosPanel, BorderLayout.WEST);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        direita.setOpaque(false);

        JCheckBox chkTemaClaro = new JCheckBox("Fundo branco");
        chkTemaClaro.setSelected(theme.isClaroAtivo());
        chkTemaClaro.setBackground(theme.corPainel);
        chkTemaClaro.setForeground(theme.corTexto);
        chkTemaClaro.setFocusPainted(false);
        chkTemaClaro.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkTemaClaro.setVisible(false); // Mantendo o código, mas deixando invisível
        chkTemaClaro.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                alternarTema(chkTemaClaro.isSelected());
            }
        });

        JLabel lblVersao = new JLabel("v 2.4.1-BETA");
        lblVersao.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVersao.setForeground(theme.corDestaque2);
        lblVersao.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblLicenca = new JLabel(controller.obterTextoLicenca());
        lblLicenca.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLicenca.setForeground(controller.obterCorLicenca());
        lblLicenca.setBorder(new EmptyBorder(0, 8, 0, 8));

        // ── Bloco de usuario: ícone circular + nome ──────────────────────────
        ImageIcon iconeUsuario = uiFactory.criarIconeUsuarioCircular(36);
        JLabel lblIconeUsuario = new JLabel(iconeUsuario);
        lblIconeUsuario.setBorder(new EmptyBorder(0, 0, 0, 4));

        JLabel lblNomeUsuario = new JLabel(authSession.getDisplayName());
        lblNomeUsuario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNomeUsuario.setForeground(theme.corTexto);

        String emailSessao = authSession.getEmail();
        String tooltipUsuario = emailSessao != null && !emailSessao.trim().isEmpty()
                ? authSession.getDisplayName() + "  •  " + emailSessao
                : authSession.getDisplayName();
        lblIconeUsuario.setToolTipText(tooltipUsuario);
        lblNomeUsuario.setToolTipText(tooltipUsuario);

        JPanel painelUsuario = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        painelUsuario.setOpaque(false);
        painelUsuario.add(lblIconeUsuario);
        painelUsuario.add(lblNomeUsuario);

        direita.add(chkTemaClaro);
        direita.add(lblVersao);
        direita.add(lblLicenca);
        direita.add(painelUsuario);
        cabecalho.add(direita, BorderLayout.EAST);

        // Linha separadora inferior
        JSeparator sep = new JSeparator();
        sep.setForeground(theme.corBorda);
        sep.setBackground(theme.corBorda);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(theme.corPainel);
        wrapper.add(cabecalho, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }



    /** Corpo principal — painel esquerdo (entrada) + direito (saída) */
    private JSplitPane criarCorpo() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                criarPainelEntrada(), criarPainelSaida());
        split.setDividerLocation(490);
        split.setDividerSize(6);
        split.setBackground(theme.corFundo);
        split.setForeground(theme.corBorda);
        split.setBorder(null);
        return split;
    }

    // ─── Painel de Entrada (esquerdo) ─────────────────────────────────────────

    private JPanel criarPainelEntrada() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(theme.corFundo);
        painel.setBorder(new EmptyBorder(16, 16, 0, 8));

        // Painel de auditoria (RTC + UC)
        painel.add(criarPainelAuditoria(), BorderLayout.NORTH);

        abas = new JTabbedPane();
        estilizarAbas(abas);
        ExpertDevTabsBuilder tabsBuilder = new ExpertDevTabsBuilder();
        tabsBuilder.addCoreTabs(abas,
                this::criarAbaUrls,
                this::criarAbaUpload,
                this::criarAbaHistorico,
                this::criarAbaPerformance);
        // RN: somente credencial Premium acessa o Assistente Pro.
        try {
            if (authSession != null && authSession.isPremium()) {
                br.com.expertdev.pro.ui.ProAssistantPanel painelPro = new br.com.expertdev.pro.ui.ProAssistantPanel();
                JScrollPane scrollPainelPro = new JScrollPane(painelPro);
                scrollPainelPro.setBorder(null);
                scrollPainelPro.getVerticalScrollBar().setUnitIncrement(16);
                abas.addTab("  ⚡  Assistente Pro  ", scrollPainelPro);
            } else {
                JPanel painelBloqueado = new JPanel(new BorderLayout());
                painelBloqueado.setBackground(theme.corPainelAlt);
                JLabel lblBloqueado = new JLabel("Assistente Pro disponivel apenas para credenciais Premium. Upgrade para Premium.", SwingConstants.CENTER);
                lblBloqueado.setForeground(theme.corTextoSuave);
                lblBloqueado.setFont(AppTheme.FONTE_ROTULO);
                painelBloqueado.add(lblBloqueado, BorderLayout.CENTER);

                abas.addTab("  ⚡  Assistente Pro  ", painelBloqueado);
                int indicePro = abas.getTabCount() - 1;
                abas.setEnabledAt(indicePro, false);
                abas.setToolTipTextAt(indicePro, "Upgrade para Premium");

                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        "Recurso indisponivel para sua credencial atual.\nUpgrade para Premium para acessar o Assistente Pro.",
                        "Assistente Pro",
                        JOptionPane.INFORMATION_MESSAGE
                ));
            }
        } catch (Exception ex) {
            System.err.println("Erro ao carregar painel Pro: " + ex.getMessage());
        }
        abas.addChangeListener(e -> {
            atualizarEstimativaIa();
            if (abas.getSelectedIndex() == 3) {
                atualizarGraficoPerformance();
                if (campoRTC != null && campoRTC.isFocusOwner()) {
                    controller.solicitarAtualizacaoSugestoesRtc();
                }
            } else {
                controller.ocultarPopupSugestoesRtc();
             }
        });
        painel.add(abas, BorderLayout.CENTER);

        painel.add(criarPainelAcoes(), BorderLayout.SOUTH);
        return painel;
    }

    private void estilizarAbas(JTabbedPane abas) {
        abas.setBackground(theme.corPainel);
        abas.setForeground(theme.corTexto);
        abas.setFont(AppTheme.FONTE_ROTULO);
        abas.setOpaque(true);
        UIManager.put("TabbedPane.selected", theme.corPainelAlt);
        UIManager.put("TabbedPane.contentAreaColor", theme.corPainelAlt);
    }

    private JPanel criarPainelAuditoria() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda),
                new EmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblRTC = uiFactory.criarRotulo("RTC:");
        lblRTC.setFont(AppTheme.FONTE_ROTULO);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        painel.add(lblRTC, gbc);

        campoRTC = new JTextField();
        campoRTC.setFont(AppTheme.FONTE_NORMAL);
        campoRTC.setToolTipText("Ex: 256421");
        campoRTC.setBackground(theme.corFundo);
        campoRTC.setForeground(theme.corTexto);
        campoRTC.setCaretColor(theme.corDestaque);
        campoRTC.setSelectionColor(theme.corDestaque);
        campoRTC.setSelectedTextColor(Color.WHITE);
        campoRTC.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda),
                new EmptyBorder(6, 8, 6, 8)
        ));
        campoRTC.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aoAlterarCampoRtc(); }
            @Override public void removeUpdate(DocumentEvent e) { aoAlterarCampoRtc(); }
            @Override public void changedUpdate(DocumentEvent e) { aoAlterarCampoRtc(); }

            private void aoAlterarCampoRtc() {
                verificarEPrefilPerformance();
                if (!controller.atualizandoRtcProgramaticamente) {
                    controller.solicitarAtualizacaoSugestoesRtc();
                }
            }
         });
         controller.configurarAutocompleteRtc();
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        painel.add(campoRTC, gbc);

        JLabel lblUC = uiFactory.criarRotulo("Caso de Uso:");
        lblUC.setFont(AppTheme.FONTE_ROTULO);
        gbc.gridx = 2;
        gbc.weightx = 0;
        painel.add(lblUC, gbc);

        campoUC = new JTextField();
        campoUC.setFont(AppTheme.FONTE_NORMAL);
        campoUC.setToolTipText("Ex: UC01 – Registrar Usuário");
        campoUC.setBackground(theme.corFundo);
        campoUC.setForeground(theme.corTexto);
        campoUC.setCaretColor(theme.corDestaque);
        campoUC.setSelectionColor(theme.corDestaque);
        campoUC.setSelectedTextColor(Color.WHITE);
        campoUC.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda),
                new EmptyBorder(6, 8, 6, 8)
        ));
        gbc.gridx = 3;
        gbc.weightx = 0.6;
        painel.add(campoUC, gbc);

        return painel;
    }

    /** Aba para entrada de URLs */
    private JPanel criarAbaUrls() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel instrucao = uiFactory.criarRotulo(
                "Cole uma ou mais URLs (uma por linha ou separadas por vírgula):");
        painel.add(instrucao, BorderLayout.NORTH);

        areaUrls = new JTextArea();
        areaUrls.setFont(AppTheme.FONTE_MONO);
        areaUrls.setBackground(theme.corFundo);
        areaUrls.setForeground(theme.corTexto);
        areaUrls.setCaretColor(theme.corDestaque);
        areaUrls.setLineWrap(true);
        areaUrls.setWrapStyleWord(true);
        areaUrls.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaUrls.setToolTipText("Ex: https://github.com/user/repo/README.md");
        areaUrls.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarEstimativaIa();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizarEstimativaIa();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                atualizarEstimativaIa();
            }
        });

        JScrollPane scroll = uiFactory.criarScrollPane(areaUrls);
        painel.add(scroll, BorderLayout.CENTER);

        // Botão de limpar
        JPanel botoesUrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoesUrl.setOpaque(false);
        JButton btnLimpar = uiFactory.criarBotaoSecundario("✖  Limpar");
        btnLimpar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                areaUrls.setText("");
                if (campoRTC != null) {
                    controller.definirCampoRtcProgramaticamente("");
                    verificarEPrefilPerformance();
                 }
                if (campoUC != null) {
                    campoUC.setText("");
                }
                areaUrls.requestFocus();
            }
        });
        botoesUrl.add(btnLimpar);
        painel.add(botoesUrl, BorderLayout.SOUTH);

        return painel;
    }

    /** Aba para upload de Word */
    private JPanel criarAbaUpload() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Topo: instrução + botão selecionar
        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setOpaque(false);

        JLabel instrucao = uiFactory.criarRotulo("Selecione um arquivo Word (.doc/.docx) com as regras de negócio:");
        topo.add(instrucao, BorderLayout.NORTH);

        JPanel seletorPanel = new JPanel(new BorderLayout(10, 0));
        seletorPanel.setOpaque(false);
        seletorPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        labelArquivoWord = new JLabel("Nenhum arquivo selecionado");
        labelArquivoWord.setFont(AppTheme.FONTE_MONO);
        labelArquivoWord.setForeground(theme.corTextoSuave);
        labelArquivoWord.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda),
                new EmptyBorder(8, 12, 8, 12)));
        labelArquivoWord.setBackground(theme.corFundo);
        labelArquivoWord.setOpaque(true);

        JButton btnSelecionar = uiFactory.criarBotaoPrimario("📂  Adicionar");
        btnSelecionar.setPreferredSize(new Dimension(140, 38));
        btnSelecionar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selecionarArquivoWord();
            }
        });

        JPanel painelAcoesLista = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painelAcoesLista.setOpaque(false);
        JButton btnRemoverSelecionado = uiFactory.criarBotaoSecundario("🗑 Remover");
        btnRemoverSelecionado.addActionListener(e -> {
            File selecionado = listaArquivosWord == null ? null : listaArquivosWord.getSelectedValue();
            if (selecionado != null && modeloArquivosWord != null) {
                modeloArquivosWord.removeElement(selecionado);
                atualizarResumoArquivosWord();
                carregarPreviewWord();
            }
        });
        JButton btnLimparLista = uiFactory.criarBotaoSecundario("✖ Limpar");
        btnLimparLista.addActionListener(e -> {
            if (modeloArquivosWord != null) {
                modeloArquivosWord.clear();
                atualizarResumoArquivosWord();
                carregarPreviewWord();
            }
        });
        btnAbrirPreviewWord = uiFactory.criarBotaoSecundario("👁 Abrir Prévia");
        btnAbrirPreviewWord.setToolTipText("Abre a pré-visualização textual do arquivo Word selecionado.");
        btnAbrirPreviewWord.addActionListener(e -> abrirDialogoPreviewWordSelecionado());
        btnAbrirPreviewWord.setEnabled(false);
        painelAcoesLista.add(btnRemoverSelecionado);
        painelAcoesLista.add(btnLimparLista);
        painelAcoesLista.add(btnAbrirPreviewWord);

        JPanel esquerda = new JPanel(new BorderLayout(0, 6));
        esquerda.setOpaque(false);
        esquerda.add(labelArquivoWord, BorderLayout.NORTH);
        modeloArquivosWord = new DefaultListModel<File>();
        listaArquivosWord = new JList<File>(modeloArquivosWord);
        listaArquivosWord.setBackground(theme.corFundo);
        listaArquivosWord.setForeground(theme.corTexto);
        listaArquivosWord.setSelectionBackground(theme.corDestaque);
        listaArquivosWord.setSelectionForeground(Color.WHITE);
        listaArquivosWord.setFont(AppTheme.FONTE_NORMAL);
        listaArquivosWord.setVisibleRowCount(5);
        listaArquivosWord.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    File f = (File) value;
                    lbl.setText(f.getName() + "  (" + f.getAbsolutePath() + ")");
                }
                return lbl;
            }
        });
        listaArquivosWord.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarPreviewWord();
            }
        });
        listaArquivosWord.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && listaArquivosWord.getSelectedValue() != null) {
                    abrirDialogoPreviewWordSelecionado();
                }
            }
        });
        JScrollPane scrollArquivos = uiFactory.criarScrollPane(listaArquivosWord);
        scrollArquivos.setPreferredSize(new Dimension(100, 110));
        configurarDropArquivosWord(scrollArquivos);
        configurarDropArquivosWord(listaArquivosWord);
        esquerda.add(scrollArquivos, BorderLayout.CENTER);

        JPanel direita = new JPanel(new BorderLayout(0, 8));
        direita.setOpaque(false);
        direita.add(btnSelecionar, BorderLayout.NORTH);
        direita.add(painelAcoesLista, BorderLayout.SOUTH);

        seletorPanel.add(esquerda, BorderLayout.CENTER);
        seletorPanel.add(direita, BorderLayout.EAST);

        labelResumoArquivosWord = uiFactory.criarRotulo("Arquivos Word: 0");
        labelResumoArquivosWord.setFont(AppTheme.FONTE_SUBTITULO);
        labelResumoArquivosWord.setBorder(new EmptyBorder(4, 2, 0, 0));
        topo.add(labelResumoArquivosWord, BorderLayout.SOUTH);
        topo.add(seletorPanel, BorderLayout.CENTER);
        painel.add(topo, BorderLayout.NORTH);

        // Área de preview do conteúdo Word
        JLabel lblPreview = uiFactory.criarRotulo("Pré-visualização do conteúdo:");
        lblPreview.setBorder(new EmptyBorder(12, 0, 4, 0));

        areaPreviewWord = new JTextArea();
        areaPreviewWord.setFont(AppTheme.FONTE_MONO);
        areaPreviewWord.setBackground(theme.corFundo);
        areaPreviewWord.setForeground(new Color(180, 190, 210));
        areaPreviewWord.setEditable(false);
        areaPreviewWord.setRows(8);
        areaPreviewWord.setLineWrap(true);
        areaPreviewWord.setWrapStyleWord(true);
        areaPreviewWord.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaPreviewWord.setText("(adicione um ou mais arquivos .doc/.docx e selecione um item para pré-visualizar)");

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(lblPreview, BorderLayout.NORTH);
        JScrollPane scrollPreviewWord = uiFactory.criarScrollPane(areaPreviewWord);
        scrollPreviewWord.setPreferredSize(new Dimension(100, 160));
        scrollPreviewWord.setMinimumSize(new Dimension(100, 120));
        centro.add(scrollPreviewWord, BorderLayout.CENTER);
        painelPreviewWord = centro;
        painel.add(centro, BorderLayout.CENTER);

        atualizarResumoArquivosWord();
        atualizarVisibilidadePreviewWord(0);

        return painel;
    }

    /** Aba para exibição do histórico */
    private JPanel criarAbaHistorico() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel instrucao = uiFactory.criarRotulo("Histórico de processamentos (últimos 100):");
        instrucao.setBorder(new EmptyBorder(0, 0, 8, 0));
        painel.add(instrucao, BorderLayout.NORTH);

        areaHistorico = new JTextArea();
        areaHistorico.setFont(AppTheme.FONTE_MONO);
        areaHistorico.setBackground(theme.corFundo);
        areaHistorico.setForeground(theme.corTextoSuave);
        areaHistorico.setEditable(false);
        areaHistorico.setLineWrap(true);
        areaHistorico.setWrapStyleWord(true);
        areaHistorico.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaHistorico.setText("(histórico será preenchido após o primeiro processamento)");
        painel.add(uiFactory.criarScrollPane(areaHistorico), BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoes.setOpaque(false);
        JButton btnAtualizarHistorico = uiFactory.criarBotaoSecundario("🔄  Atualizar");
        btnAtualizarHistorico.addActionListener(e -> atualizarAbaHistorico());
        botoes.add(btnAtualizarHistorico);

        JButton btnLimparCache = uiFactory.criarBotaoSecundario("🧹  Limpar Cache");
        btnLimparCache.setToolTipText("Apaga o cache local de URLs processadas para forçar novo download");
        btnLimparCache.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Deseja realmente limpar o cache de URLs processadas?",
                    "Limpar Cache", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new CacheService().limparCache();
                mostrarMensagem("Cache de processamento limpo com sucesso!");
            }
        });
        botoes.add(Box.createHorizontalStrut(10));
        botoes.add(btnLimparCache);

        painel.add(botoes, BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarAbaPerformance() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Topo: Entrada de dados
        JPanel topo = new JPanel(new GridBagLayout());
        topo.setBackground(theme.corPainelAlt);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        topo.add(uiFactory.criarRotulo("Estimativa Poker (Horas):"), gbc);

        campoEstimativaPoker = new JTextField(5);
        campoEstimativaPoker.setBackground(theme.corFundo);
        campoEstimativaPoker.setForeground(theme.corTexto);
        gbc.gridx = 1;
        topo.add(campoEstimativaPoker, gbc);

        gbc.gridx = 2;
        topo.add(uiFactory.criarRotulo("Sprint:"), gbc);

        campoSprint = new JTextField(5);
        campoSprint.setBackground(theme.corFundo);
        campoSprint.setForeground(theme.corTexto);
        gbc.gridx = 3;
        topo.add(campoSprint, gbc);

        gbc.gridx = 4;
        topo.add(uiFactory.criarRotulo("Complexidade:"), gbc);

        comboComplexidade = new JComboBox<>(new String[]{"Baixa", "Média", "Alta"});
        gbc.gridx = 5;
        topo.add(comboComplexidade, gbc);

        painel.add(topo, BorderLayout.NORTH);

        // Centro: Gráfico e Dashboard
        JPanel centro = new JPanel(new GridLayout(1, 2, 10, 0));
        centro.setBackground(theme.corPainelAlt);

        painelGrafico = new JPanel(new BorderLayout());
        painelGrafico.setBackground(theme.corPainel);
        painelGrafico.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(theme.corBorda), "RTC Atual", 0, 0, AppTheme.FONTE_ROTULO, theme.corTexto));
        centro.add(painelGrafico);

        painelTendencia = new JPanel(new BorderLayout());
        painelTendencia.setBackground(theme.corPainel);
        painelTendencia.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(theme.corBorda), "Tendência de Aprendizado", 0, 0, AppTheme.FONTE_ROTULO, theme.corTexto));
        centro.add(painelTendencia);

        painel.add(centro, BorderLayout.CENTER);

        // Base: Ações e KPI
        JPanel base = new JPanel(new BorderLayout(10, 0));
        base.setBackground(theme.corPainelAlt);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        botoes.setBackground(theme.corPainelAlt);

        btnIniciarScrum = uiFactory.criarBotaoSecundario("Iniciar Scrum");
        btnIniciarScrum.setToolTipText("Inicia cronômetro do Scrum. ExpertDev já foi iniciado automaticamente ao processar.");
        btnIniciarScrum.addActionListener(e -> gerenciarAcaoPerformance("INICIAR_SCRUM"));
        
        btnFinalizarScrum = uiFactory.criarBotaoSecundario("Finalizar Scrum");
        btnFinalizarScrum.setToolTipText("Finaliza cronômetro do Scrum manualmente (fecha automaticamente ao finalizar ExpertDev).");
        btnFinalizarScrum.addActionListener(e -> gerenciarAcaoPerformance("FINALIZAR_SCRUM"));

        btnFinalizarExpertDev = uiFactory.criarBotaoSecundario("Finalizar ExpertDev");
        btnFinalizarExpertDev.setToolTipText("Finaliza ExpertDev, Scrum (se ativo) e marca tarefa como CONCLUIDO. Exibe economia se foi mais rápido que estimado.");
        btnFinalizarExpertDev.addActionListener(e -> gerenciarAcaoPerformance("FINALIZAR_EXPERTDEV"));

        JButton btnExportarRelatorio = uiFactory.criarBotaoSecundario("📊 Exportar ROI");
        btnExportarRelatorio.addActionListener(e -> exportarRelatorioROI());

        botoes.add(btnIniciarScrum);
        botoes.add(btnFinalizarScrum);
        botoes.add(btnFinalizarExpertDev);
        botoes.add(btnExportarRelatorio);
        base.add(botoes, BorderLayout.WEST);

        lblGanhoProdutividade = uiFactory.criarRotulo("Ganho: 0%");
        lblGanhoProdutividade.setFont(AppTheme.FONTE_TITULO);
        lblGanhoProdutividade.setForeground(theme.corSucesso);
        base.add(lblGanhoProdutividade, BorderLayout.EAST);

        painel.add(base, BorderLayout.SOUTH);

        // Listener para salvar campos ao perder o foco
        campoEstimativaPoker.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                salvarDadosPerformanceAtuais();
            }
        });
        campoSprint.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                salvarDadosPerformanceAtuais();
            }
        });
        comboComplexidade.addActionListener(e -> salvarDadosPerformanceAtuais());

        return painel;
    }

    private void salvarDadosPerformanceAtuais() {
        String rtc = campoRTC.getText().trim();
        if (rtc.isEmpty()) return;

        String authUsername = controller.obterUsuarioSessao();
        String authEmail = controller.obterEmailSessao();

        MetricaPerformance m = controller.performanceService.obterPorRTCeUsuario(rtc, authUsername);
        if (m == null) m = new MetricaPerformance(rtc, authUsername, authEmail);
        m.setAuthUsername(authUsername);
        m.setAuthEmail(authEmail);

        try {
            String estStr = campoEstimativaPoker.getText().trim();
            if (!estStr.isEmpty()) {
                m.setEstimativaPoker(Double.parseDouble(estStr.replace(",", ".")));
            }
        } catch (NumberFormatException e) { }
        
        try {
            String sprintStr = campoSprint.getText().trim();
            if (!sprintStr.isEmpty()) {
                m.setSprint(Integer.parseInt(sprintStr));
            } else {
                m.setSprint(null);
            }
        } catch (NumberFormatException e) { }

        m.setComplexidade((String) comboComplexidade.getSelectedItem());
        
        controller.performanceService.salvarOuAtualizar(m);
        atualizarGraficoPerformance();
    }

    private void gerenciarAcaoPerformance(String acao) {
        String rtc = campoRTC.getText().trim();
        if (rtc.isEmpty()) {
            mostrarErro("Informe o RTC no topo antes de gerenciar performance.");
            return;
        }

        String authUsername = controller.obterUsuarioSessao();
        String authEmail = controller.obterEmailSessao();

        MetricaPerformance m = controller.performanceService.obterPorRTCeUsuario(rtc, authUsername);
        if (m == null) {
            m = new MetricaPerformance(rtc, authUsername, authEmail);
        }
        m.setAuthUsername(authUsername);
        m.setAuthEmail(authEmail);

        try {
            String estStr = campoEstimativaPoker.getText().trim();
            if (!estStr.isEmpty()) {
                m.setEstimativaPoker(Double.parseDouble(estStr.replace(",", ".")));
            }
        } catch (NumberFormatException e) {
            // Ignora
        }
        m.setComplexidade((String) comboComplexidade.getSelectedItem());

        LocalDateTime agora = LocalDateTime.now();

         switch (acao) {
             case "INICIAR_SCRUM":
                 m.setInicioScrum(agora);
                 m.setStatus("DESENVOLVIMENTO_SCRUM");
                 break;
             case "FINALIZAR_SCRUM":
                 if (m.getInicioScrum() == null) {
                     mostrarErro("Inicie o Scrum antes de finalizar.");
                     return;
                 }
                 m.setFimScrum(agora);
                 break;
             case "FINALIZAR_EXPERTDEV":
                 // ExpertDev deve ter sido iniciado automaticamente ao processar
                 if (m.getInicioExpertDev() == null) {
                     mostrarErro("O tempo ExpertDev deve ter sido iniciado ao processar a tarefa.");
                     return;
                 }
                 m.setFimExpertDev(agora);

                 // Finalizar Scrum se ainda não foi finalizado
                 if (m.getInicioScrum() != null && m.getFimScrum() == null) {
                     m.setFimScrum(agora);
                 }

                 // Calcular economia e exibir aviso se foi mais rápido que estimado
                 double horasExpert = java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes() / 60.0;
                 double horasEstimadas = m.getEstimativaPoker() != null ? m.getEstimativaPoker() : 0;

                 if (horasEstimadas > 0 && horasExpert < horasEstimadas) {
                     double economia = horasEstimadas - horasExpert;
                     double percEconomia = (economia / horasEstimadas) * 100;
                     mostrarMensagem(String.format("🎉 Excelente!\n\nVocê foi mais rápido que o estimado:\n" +
                             "Estimativa: %.2f h\n" +
                             "Realizado: %.2f h\n" +
                             "Economia: %.2f h (%.1f%%)",
                             horasEstimadas, horasExpert, economia, percEconomia));
                 }

                 m.setStatus("CONCLUIDO");
                 break;
         }

         controller.performanceService.salvarOuAtualizar(m);
         atualizarGraficoPerformance();
    }

    private void verificarEPrefilPerformance() {
        String rtc = campoRTC.getText().trim();
        if (rtc.isEmpty()) {
            if (campoEstimativaPoker != null) campoEstimativaPoker.setText("");
            if (campoSprint != null) campoSprint.setText("");
            if (lblGanhoProdutividade != null) lblGanhoProdutividade.setText("Ganho: 0%");
            return;
        }

        MetricaPerformance m = controller.performanceService.obterPorRTCeUsuario(rtc, controller.obterUsuarioSessao());
        if (m != null) {
            if (campoEstimativaPoker != null) {
                campoEstimativaPoker.setText(m.getEstimativaPoker() != null ? String.valueOf(m.getEstimativaPoker()) : "");
            }
            if (campoSprint != null) {
                campoSprint.setText(m.getSprint() != null ? String.valueOf(m.getSprint()) : "");
            }
            if (comboComplexidade != null) {
                comboComplexidade.setSelectedItem(m.getComplexidade() != null ? m.getComplexidade() : "Média");
            }
        } else {
            // Limpar se não encontrou registro para este RTC
            if (campoEstimativaPoker != null) campoEstimativaPoker.setText("");
            if (campoSprint != null) campoSprint.setText("");
            if (comboComplexidade != null) comboComplexidade.setSelectedItem("Média");
        }
        if (abas != null && abas.getSelectedIndex() == 3) {
            atualizarGraficoPerformance();
            atualizarDashboardTendencia();
        }
    }

    private void exportarRelatorioROI() {
        try {
            List<MetricaPerformance> metricas = controller.performanceService.listarTodosPorUsuario(controller.obterUsuarioSessao());
            if (metricas.isEmpty()) {
                mostrarAviso("Não há dados de performance para exportar.");
                return;
            }
            String path = controller.reportService.exportarRelatorioExecutivo(metricas, controller.chartTendenciaAtual);
            mostrarMensagem("Relatório exportado com sucesso!\nCaminho: " + path);
            
            // Abrir no browser
            try {
                File pdfFile = new File(path);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(pdfFile.toURI());
                }
            } catch (Exception ex) {
                adicionarCardLog("⚠ Não foi possível abrir o PDF automaticamente: " + ex.getMessage());
            }
        } catch (Exception e) {
            mostrarErro("Erro ao exportar relatório: " + e.getMessage());
        }
    }

    private void mostrarAviso(String msg) {
        presentationMessageService.showWarning(msg);
    }

    private void mostrarMensagem(String msg) {
        presentationMessageService.showInfo(msg);
    }

    private void atualizarDashboardTendencia() {
        List<MetricaPerformance> metricas = controller.performanceService.listarTodosPorUsuario(controller.obterUsuarioSessao());
        if (metricas.isEmpty()) {
            painelTendencia.removeAll();
            painelTendencia.add(new JLabel("Sem dados históricos", SwingConstants.CENTER));
            painelTendencia.revalidate();
            return;
        }

        // Agrupar ganho por data (usando TreeMap para ordenar por data)
        Map<Date, Double> ganhoPorData = new TreeMap<>();

        for (MetricaPerformance m : metricas) {
            if (m.getFimExpertDev() != null) {
                double hExpert = java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes() / 60.0;
                double hScrum = 0;
                if (m.getFimScrum() != null && m.getInicioScrum() != null) {
                    hScrum = java.time.Duration.between(m.getInicioScrum(), m.getFimScrum()).toMinutes() / 60.0;
                } else if (m.getEstimativaPoker() != null) {
                    hScrum = m.getEstimativaPoker();
                }

                if (hScrum > 0) {
                    double ganho = ((hScrum - hExpert) / hScrum) * 100;
                    // Usar data de fim do ExpertDev como referência temporal
                    Date data = Date.from(m.getFimExpertDev().atZone(ZoneId.systemDefault()).toInstant());
                    ganhoPorData.put(data, ganho);
                }
            }
        }

        XYSeries series = new XYSeries("Ganho de Produtividade (%)");
        for (Map.Entry<Date, Double> entry : ganhoPorData.entrySet()) {
            series.add(entry.getKey().getTime(), entry.getValue());
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Curva de Aprendizado",
                "Data de Conclusão",
                "Ganho (%)",
                dataset,
                false, true, false
        );

        chart.setBackgroundPaint(theme.corPainel);
        org.jfree.chart.plot.XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(theme.corFundo);
        plot.setDomainGridlinePaint(theme.corBorda);
        plot.setRangeGridlinePaint(theme.corBorda);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, theme.corSucesso);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM"));

        controller.chartTendenciaAtual = chart; // Guardar para o relatório PDF

        painelTendencia.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(theme.corPainel);
        painelTendencia.add(cp, BorderLayout.CENTER);
        painelTendencia.revalidate();
        painelTendencia.repaint();
    }

    private void atualizarGraficoPerformance() {
        String rtc = campoRTC.getText().trim();
        if (rtc.isEmpty()) return;

        MetricaPerformance m = controller.performanceService.obterPorRTCeUsuario(rtc, controller.obterUsuarioSessao());
        if (m == null) {
            painelGrafico.removeAll();
            painelGrafico.add(new JLabel("Sem dados para o RTC " + rtc, SwingConstants.CENTER));
            painelGrafico.revalidate();
            return;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (m.getEstimativaPoker() != null) {
            dataset.addValue(m.getEstimativaPoker(), "Horas", "Estimativa Poker");
        }

        if (m.getInicioScrum() != null && m.getFimScrum() != null) {
            long diff = java.time.Duration.between(m.getInicioScrum(), m.getFimScrum()).toMinutes();
            dataset.addValue(diff / 60.0, "Horas", "Tempo Real Scrum");
        } else if (m.getInicioScrum() != null) {
            long diff = java.time.Duration.between(m.getInicioScrum(), LocalDateTime.now()).toMinutes();
            dataset.addValue(diff / 60.0, "Horas", "Real Scrum (Andamento)");
        }

        if (m.getInicioExpertDev() != null && m.getFimExpertDev() != null) {
            long diff = java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes();
            dataset.addValue(diff / 60.0, "Horas", "ExpertDev (Dev+Teste)");
        } else if (m.getInicioExpertDev() != null) {
            long diff = java.time.Duration.between(m.getInicioExpertDev(), LocalDateTime.now()).toMinutes();
            dataset.addValue(diff / 60.0, "Horas", "ExpertDev (Andamento)");
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Comparativo de Performance - RTC " + rtc,
                "Etapa",
                "Horas",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        chart.setBackgroundPaint(theme.corPainel);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(theme.corFundo);
        plot.setRangeGridlinePaint(theme.corBorda);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, theme.corDestaque);
        
        painelGrafico.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(theme.corPainel);
        painelGrafico.add(cp, BorderLayout.CENTER);
        painelGrafico.revalidate();
        painelGrafico.repaint();

        if (m.getFimScrum() != null && m.getFimExpertDev() != null) {
            double hScrum = java.time.Duration.between(m.getInicioScrum(), m.getFimScrum()).toMinutes() / 60.0;
            double hExpert = java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes() / 60.0;
            if (hScrum > 0) {
                double ganho = ((hScrum - hExpert) / hScrum) * 100;
                lblGanhoProdutividade.setText(String.format("Ganho: %.1f%%", ganho));
            }
        } else if (m.getEstimativaPoker() != null && m.getEstimativaPoker() > 0 && m.getFimExpertDev() != null) {
             double hExpert = java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes() / 60.0;
             double ganho = ((m.getEstimativaPoker() - hExpert) / m.getEstimativaPoker()) * 100;
             lblGanhoProdutividade.setText(String.format("Ganho Est.: %.1f%%", ganho));
        }
    }

    private void atualizarAbaHistorico() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                java.util.List<RegistroAuditoria> registros = controller.auditoriaService.obterTodosPorUsuario(controller.obterUsuarioSessao());
                if (registros.isEmpty()) {
                    return "(nenhum registro encontrado)";
                }
                StringBuilder sb = new StringBuilder();
                for (RegistroAuditoria reg : registros) {
                    sb.append(reg).append("\n");
                }
                return sb.toString();
            }

            @Override
            protected void done() {
                try {
                    areaHistorico.setText(get());
                    areaHistorico.setCaretPosition(0);
                } catch (Exception e) {
                    areaHistorico.setText("Erro ao carregar histórico: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /** Barra de ações com botão Processar e barra de progresso */
    private JPanel criarPainelAcoes() {
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(10, 0, 12, 0));

        JPanel topoAcoes = new JPanel(new BorderLayout(0, 8));
        topoAcoes.setOpaque(false);
        topoAcoes.add(criarPainelConfigGeracao(), BorderLayout.NORTH);

        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setStringPainted(true);
        barraProgresso.setString("Aguardando...");
        barraProgresso.setFont(AppTheme.FONTE_NORMAL);
        barraProgresso.setBackground(theme.corPainel);
        barraProgresso.setForeground(theme.corDestaque);
        barraProgresso.setBorderPainted(false);
        barraProgresso.setPreferredSize(new Dimension(0, 22));
        topoAcoes.add(barraProgresso, BorderLayout.SOUTH);

        painel.add(topoAcoes, BorderLayout.NORTH);

        btnProcessar = uiFactory.criarBotaoAcao("▶  Gerar Prompt");
        btnProcessar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iniciarProcessamento();
            }
        });
        painel.add(btnProcessar, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarPainelConfigGeracao() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda),
                new EmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblModo = uiFactory.criarRotulo("Modo:");
        lblModo.setFont(AppTheme.FONTE_SUBTITULO);
        lblModo.setIcon(uiFactory.criarIconeIa(14));
        lblModo.setIconTextGap(5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        painel.add(lblModo, gbc);

        comboModoGeracao = new JComboBox<String>(new String[]{"LOCAL", "IA"});
        comboModoGeracao.setSelectedItem(controller.modoGeracaoSelecionado);
        comboModoGeracao.setFont(AppTheme.FONTE_NORMAL);
        final Icon iconIaCombo = uiFactory.criarIconeIa(14);
        final Icon iconLocalCombo = uiFactory.criarIconeLocal(14);
        comboModoGeracao.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String modo = value == null ? "" : value.toString();
                label.setIcon("IA".equalsIgnoreCase(modo) ? iconIaCombo : iconLocalCombo);
                label.setIconTextGap(6);
                return label;
            }
        });
        comboModoGeracao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.modoGeracaoSelecionado = String.valueOf(comboModoGeracao.getSelectedItem());
                ExpertDevConfig.salvarPreferenciaModoGeracao(controller.modoGeracaoSelecionado);
                ExpertDevConfig.salvarPreferenciaAiHabilitada(isModoIaSelecionado());
                atualizarEstadoOpcoesIa();
                atualizarEstimativaIa();
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        painel.add(comboModoGeracao, gbc);

        JLabel lblPerfil = uiFactory.criarRotulo("Perfil:");
        lblPerfil.setFont(AppTheme.FONTE_SUBTITULO);
        gbc.gridx = 4;
        gbc.weightx = 0;
        painel.add(lblPerfil, gbc);

        comboPerfilPrompt = new JComboBox<String>(new String[]{"tecnico", "negocial"});
        comboPerfilPrompt.setSelectedItem(controller.perfilPromptSelecionado);
        comboPerfilPrompt.setFont(AppTheme.FONTE_NORMAL);
        comboPerfilPrompt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.perfilPromptSelecionado = String.valueOf(comboPerfilPrompt.getSelectedItem());
                ExpertDevConfig.salvarPromptProfile(controller.perfilPromptSelecionado);
                atualizarEstimativaIa();
            }
        });
        gbc.gridx = 5;
        gbc.weightx = 0.2;
        painel.add(comboPerfilPrompt, gbc);

        JLabel lblProvider = uiFactory.criarRotulo("Provider:");
        lblProvider.setFont(AppTheme.FONTE_SUBTITULO);
        lblProvider.setIcon(uiFactory.criarIconeIa(14));
        lblProvider.setIconTextGap(5);
        gbc.gridx = 2;
        gbc.weightx = 0;
        painel.add(lblProvider, gbc);

        comboProviderIa = new JComboBox<String>(new String[]{"openai", "claude"});
        comboProviderIa.setSelectedItem(controller.providerIaSelecionado);
        comboProviderIa.setFont(AppTheme.FONTE_NORMAL);
        comboProviderIa.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String provider = value == null ? "" : value.toString();
                label.setIcon("claude".equalsIgnoreCase(provider) ? uiFactory.criarIconeClaude(14) : uiFactory.criarIconeIa(14));
                label.setIconTextGap(6);
                return label;
            }
        });
        comboProviderIa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.providerIaSelecionado = normalizarProvider(String.valueOf(comboProviderIa.getSelectedItem()));
                ExpertDevConfig.salvarConfiguracaoAiProvider(controller.providerIaSelecionado);
                atualizarEstimativaIa();
            }
        });
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        painel.add(comboProviderIa, gbc);

        JLabel lblApi = uiFactory.criarRotulo("API Key:");
        lblApi.setFont(AppTheme.FONTE_SUBTITULO);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        painel.add(lblApi, gbc);

        campoApiKey = new JPasswordField();
        campoApiKey.setFont(AppTheme.FONTE_NORMAL);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        painel.add(campoApiKey, gbc);

        btnLimparApiKey = uiFactory.criarBotaoSecundario("Limpar");
        btnLimparApiKey.setFont(AppTheme.FONTE_SUBTITULO);
        btnLimparApiKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                limparApiKey();
            }
        });
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        painel.add(btnLimparApiKey, gbc);

        btnTestarConexaoIa = uiFactory.criarBotaoSecundario("Testar IA");
        btnTestarConexaoIa.setFont(AppTheme.FONTE_SUBTITULO);
        btnTestarConexaoIa.setIcon(uiFactory.criarIconeIa(14));
        btnTestarConexaoIa.setIconTextGap(5);
        btnTestarConexaoIa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testarConexaoIa();
            }
        });
        gbc.gridx = 4;
        gbc.weightx = 0;
        painel.add(btnTestarConexaoIa, gbc);

        chkSalvarApiKey = new JCheckBox("Salvar chave localmente");
        chkSalvarApiKey.setOpaque(false);
        chkSalvarApiKey.setForeground(theme.corTextoSuave);
        chkSalvarApiKey.setFont(AppTheme.FONTE_SUBTITULO);
        chkSalvarApiKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.salvarApiKeySelecionada = chkSalvarApiKey.isSelected();
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        painel.add(chkSalvarApiKey, gbc);

        lblAvisoModoEconomicoIa = new JLabel("Modo econômico de IA ativo: contexto reduzido e menos tokens.");
        lblAvisoModoEconomicoIa.setFont(AppTheme.FONTE_SUBTITULO);
        lblAvisoModoEconomicoIa.setForeground(theme.corDestaque2);
        lblAvisoModoEconomicoIa.setIcon(uiFactory.criarIconeEconomico(14));
        lblAvisoModoEconomicoIa.setIconTextGap(5);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        painel.add(lblAvisoModoEconomicoIa, gbc);

        lblEstimativaIa = new JLabel("Estimativa IA: aguardando contexto...");
        lblEstimativaIa.setFont(AppTheme.FONTE_SUBTITULO);
        lblEstimativaIa.setForeground(theme.corTextoSuave);
        lblEstimativaIa.setIcon(uiFactory.criarIconeEconomico(14));
        lblEstimativaIa.setIconTextGap(5);
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        painel.add(lblEstimativaIa, gbc);

        return painel;
    }

    // ─── Painel de Saída (direito) ─────────────────────────────────────────────

    private JPanel criarPainelSaida() {
        JPanel painel = new JPanel(new BorderLayout(0, 0));
        painel.setBackground(theme.corFundo);
        painel.setBorder(new EmptyBorder(16, 8, 0, 16));

        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                criarPainelLog(), criarPainelPrompt());
        splitVertical.setDividerLocation(200);
        splitVertical.setDividerSize(6);
        splitVertical.setBackground(theme.corFundo);
        splitVertical.setBorder(null);

        painel.add(splitVertical, BorderLayout.CENTER);
        return painel;
    }

    /** Painel de log de execução */
    private JPanel criarPainelLog() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBackground(theme.corFundo);

        JLabel lbl = uiFactory.criarRotulo("📋  Log de Execução");
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        painel.add(lbl, BorderLayout.NORTH);

        painelLogCards = new JPanel();
        painelLogCards.setLayout(new BoxLayout(painelLogCards, BoxLayout.Y_AXIS));
        painelLogCards.setBackground(theme.isClaroAtivo() ? Color.WHITE : new Color(10, 10, 18));
        
        scrollLog = new JScrollPane(painelLogCards);
        scrollLog.setBorder(new LineBorder(theme.corBorda, 1));
        scrollLog.getVerticalScrollBar().setUnitIncrement(16);
        
        // Inicializar com mensagem de pronto
        adicionarCardLog("Pronto para processar.");

        painel.add(scrollLog, BorderLayout.CENTER);
        return painel;
    }

    /** Adiciona um card de log ao painel */
    void adicionarCardLog(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;
        
        SwingUtilities.invokeLater(() -> {
            LogCard card = new LogCard(msg);
            painelLogCards.add(card);
            painelLogCards.add(Box.createVerticalStrut(4));
            painelLogCards.revalidate();
            painelLogCards.repaint();
            
            // Scroll para o final
            JScrollBar vertical = scrollLog.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /** Classe para representar um card de log visual */
    private class LogCard extends JPanel {
        public LogCard(String msg) {
            setLayout(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setBackground(theme.isClaroAtivo() ? new Color(250, 250, 252) : new Color(20, 20, 30));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            // Identificar status pelo prefixo
            String texto = msg;
            Icon icone = null;
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
                icone = uiFactory.criarIconeInfo(14); // Padrão
            }

            JLabel lblIcone = new JLabel(icone);
            add(lblIcone, BorderLayout.WEST);

            JLabel lblTexto = new JLabel(texto);
            lblTexto.setFont(AppTheme.FONTE_NORMAL);
            lblTexto.setForeground(corTexto);
            add(lblTexto, BorderLayout.CENTER);
            
            // Adicionar borda inferior sutil
            setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, theme.corBorda),
                new EmptyBorder(8, 12, 8, 12)
            ));
        }
    }


    /** Painel com o prompt gerado e botões de ação */
    private JPanel criarPainelPrompt() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBackground(theme.corFundo);
        painel.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Cabeçalho do prompt
        JPanel cabecalhoPrompt = new JPanel(new BorderLayout());
        cabecalhoPrompt.setOpaque(false);

        JLabel lbl = uiFactory.criarRotulo("Prompt Gerado");
        lbl.setIcon(uiFactory.criarIconeIa(14));
        lbl.setIconTextGap(6);
        cabecalhoPrompt.add(lbl, BorderLayout.WEST);

        JPanel botoesSaida = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botoesSaida.setOpaque(false);

        btnCopiarPrompt = uiFactory.criarBotaoSecundario("📋  Copiar");
        btnCopiarPrompt.setEnabled(false);
        btnCopiarPrompt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copiarPromptParaClipboard();
            }
        });

        btnSalvar = uiFactory.criarBotaoSecundario("💾  Salvar Arquivos");
        btnSalvar.setEnabled(false);
        btnSalvar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirDialogoSalvar();
            }
        });

        botoesSaida.add(btnCopiarPrompt);
        botoesSaida.add(btnSalvar);
        cabecalhoPrompt.add(botoesSaida, BorderLayout.EAST);
        painel.add(cabecalhoPrompt, BorderLayout.NORTH);

        areaPrompt = new JTextArea();
        areaPrompt.setFont(AppTheme.FONTE_MONO);
        areaPrompt.setBackground(theme.corPainel);
        areaPrompt.setForeground(theme.corTexto);
        areaPrompt.setEditable(false);
        areaPrompt.setLineWrap(true);
        areaPrompt.setWrapStyleWord(true);
        areaPrompt.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaPrompt.setText("O prompt aparecerá aqui após o processamento...");

        painel.add(uiFactory.criarScrollPane(areaPrompt), BorderLayout.CENTER);
        return painel;
    }

    /** Rodapé com status */
    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(theme.corPainel);
        rodape.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel lblStatus = new JLabel("Expert Dev  •  Enterprise Ready  •  Apache POI + JSoup + PDFBox");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(theme.corTextoSuave);
        rodape.add(lblStatus, BorderLayout.WEST);

        JLabel lblAssinatura = new JLabel(" A solution engineered by Marcus Dimitri");
        lblAssinatura.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAssinatura.setForeground(theme.corDestaque2);
        lblAssinatura.setHorizontalAlignment(SwingConstants.RIGHT);
        rodape.add(lblAssinatura, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(theme.corBorda);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(theme.corPainel);
        wrapper.add(sep, BorderLayout.NORTH);
        wrapper.add(rodape, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Diálogo de Confirmação de Saída ────────────────────────────────────
    private void confirmarSaidaExpertDev() {
        // Cores para o diálogo
        Color corFundo = theme.corFundo;
        Color corPainel = theme.corPainel;
        Color corVerde = new Color(34, 197, 94);      // Verde
        Color corVermelho = new Color(239, 68, 68);   // Vermelho
        Color corTexto = theme.corTexto;

        // Criar painel com mensagem e botões customizados
        JPanel painelDialogo = new JPanel(new BorderLayout(15, 15));
        painelDialogo.setBackground(corPainel);
        painelDialogo.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Mensagem
        JLabel lblMensagem = new JLabel("Deseja sair do Expert Dev?");
        lblMensagem.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMensagem.setForeground(corTexto);

        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        painelBotoes.setBackground(corPainel);

        // Botão SIM (Verde com texto preto) - FECHA A APLICAÇÃO
        JButton btnSim = uiFactory.criarBotaoDialogo("Sim", corVerde);

        // Botão NÃO (Vermelho com texto preto) - PERMANECE NO SISTEMA
        JButton btnNao = uiFactory.criarBotaoDialogo("Não", corVermelho);

        btnSim.addActionListener(e -> {
            // SIM = Sair da aplicação (fechar)
            SwingUtilities.getWindowAncestor(painelDialogo).dispose();
            ExpertDevGUI.this.dispose();
            System.exit(0);
        });

        btnNao.addActionListener(e -> {
            // NÃO = Permanecer no sistema (apenas fecha o diálogo)
            SwingUtilities.getWindowAncestor(painelDialogo).dispose();
        });

        painelBotoes.add(btnSim);
        painelBotoes.add(btnNao);

        painelDialogo.add(lblMensagem, BorderLayout.CENTER);
        painelDialogo.add(painelBotoes, BorderLayout.SOUTH);

        // Criar o diálogo
        JDialog dialogConfirmacao = new JDialog(this, "Confirmação", Dialog.ModalityType.APPLICATION_MODAL);
        dialogConfirmacao.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialogConfirmacao.setContentPane(painelDialogo);
        dialogConfirmacao.setSize(450, 200);
        dialogConfirmacao.setLocationRelativeTo(this);
        dialogConfirmacao.getContentPane().setBackground(corPainel);
        dialogConfirmacao.setVisible(true);
    }


    // ─── Lógica de Processamento ───────────────────────────────────────────────

    private void selecionarArquivoWord() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar arquivos Word (.doc/.docx)");
        chooser.setFileFilter(new FileNameExtensionFilter("Documentos Word (*.doc, *.docx)", "doc", "docx"));
        chooser.setMultiSelectionEnabled(true);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File[] selecionados = chooser.getSelectedFiles();
            if (selecionados == null || selecionados.length == 0) {
                File unico = chooser.getSelectedFile();
                if (unico != null) {
                    selecionados = new File[] { unico };
                }
            }
            adicionarArquivosWord(selecionados);
            carregarPreviewWord();
        }
    }

    private void carregarPreviewWord() {
        List<File> arquivos = obterArquivosWordSelecionados();
        if (arquivos.isEmpty()) {
            areaPreviewWord.setText("(adicione um ou mais arquivos .doc/.docx e selecione um item para pré-visualizar)");
            areaPreviewWord.setCaretPosition(0);
            atualizarEstimativaIa();
            return;
        }

        File selecionadoLista = listaArquivosWord != null ? listaArquivosWord.getSelectedValue() : null;
        if (selecionadoLista == null && listaArquivosWord != null && listaArquivosWord.getModel().getSize() > 0) {
            listaArquivosWord.setSelectedIndex(0);
            selecionadoLista = listaArquivosWord.getSelectedValue();
        }
        final File arquivoPreview = selecionadoLista != null ? selecionadoLista : arquivos.get(0);
        areaPreviewWord.setText("Carregando pré-visualização...");
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            protected String doInBackground() {
                WordDocumentReader reader = new WordDocumentReader();
                ResultadoProcessamento r = reader.ler(arquivoPreview);
                if (r.isSucesso()) {
                    String texto = r.getTextoExtraido();
                    if (texto == null) {
                        texto = "";
                    }
                    texto = texto.trim();

                    if (texto.isEmpty()) {
                        StringBuilder vazio = new StringBuilder();
                        vazio.append("Arquivo de prévia: ").append(arquivoPreview.getName()).append("\n");
                        vazio.append("Total na fila: ").append(obterArquivosWordSelecionados().size()).append("\n\n");
                        vazio.append("[Pré-visualização textual indisponível neste arquivo. O conteúdo pode estar em tabelas/imagens ou exigir OCR.]");
                        return vazio.toString();
                    }

                    String textoPreview = texto.length() > 3000
                            ? texto.substring(0, 3000) + "\n\n[... conteúdo truncado para pré-visualização ...]"
                            : texto;
                    List<String> statusIngestao = extrairMensagensStatusWord(r);
                    if (statusIngestao.isEmpty()) {
                        return textoPreview;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("Arquivo de prévia: ").append(arquivoPreview.getName()).append("\n");
                    sb.append("Total na fila: ").append(obterArquivosWordSelecionados().size()).append("\n\n");
                    for (String status : statusIngestao) {
                        sb.append(status).append("\n");
                    }
                    sb.append("\n").append(textoPreview);
                    return sb.toString();
                }
                return "[Erro ao carregar: " + r.getErro() + "]";
            }

            protected void done() {
                try {
                    areaPreviewWord.setText(get());
                    areaPreviewWord.setCaretPosition(0);
                    atualizarEstimativaIa();
                } catch (Exception e) {
                    areaPreviewWord.setText("[Erro inesperado: " + e.getMessage() + "]");
                    atualizarEstimativaIa();
                }
            }
        };
        worker.execute();
    }

    private void adicionarArquivosWord(File[] arquivos) {
        if (arquivos == null || arquivos.length == 0) {
            return;
        }
        if (modeloArquivosWord == null) {
            modeloArquivosWord = new DefaultListModel<File>();
        }

        int adicionados = 0;
        int ignorados = 0;
        for (File arquivo : arquivos) {
            if (!ehArquivoWordValido(arquivo)) {
                ignorados++;
                continue;
            }
            boolean jaExiste = false;
            for (int i = 0; i < modeloArquivosWord.size(); i++) {
                File existente = modeloArquivosWord.get(i);
                if (existente.getAbsolutePath().equalsIgnoreCase(arquivo.getAbsolutePath())) {
                    jaExiste = true;
                    break;
                }
            }
            if (!jaExiste) {
                modeloArquivosWord.addElement(arquivo);
                adicionados++;
            }
        }

        if (adicionados > 0) {
            labelArquivoWord.setText("Arquivos carregados para processamento");
            labelArquivoWord.setForeground(theme.corSucesso);
        }
        if (ignorados > 0) {
            adicionarCardLog("⚠ Alguns arquivos foram ignorados por formato inválido (use .doc/.docx).");
        }
        atualizarResumoArquivosWord();
    }

    private boolean ehArquivoWordValido(File arquivo) {
        if (arquivo == null || !arquivo.exists() || !arquivo.isFile()) {
            return false;
        }
        String nome = arquivo.getName().toLowerCase();
        return nome.endsWith(".doc") || nome.endsWith(".docx");
    }

    private List<File> obterArquivosWordSelecionados() {
        List<File> arquivos = new ArrayList<File>();
        if (modeloArquivosWord == null) {
            return arquivos;
        }
        for (int i = 0; i < modeloArquivosWord.size(); i++) {
            File arquivo = modeloArquivosWord.get(i);
            if (arquivo != null && arquivo.exists()) {
                arquivos.add(arquivo);
            }
        }
        return arquivos;
    }

    private void atualizarResumoArquivosWord() {
        int total = modeloArquivosWord == null ? 0 : modeloArquivosWord.getSize();
        if (labelResumoArquivosWord != null) {
            labelResumoArquivosWord.setText("Arquivos Word: " + total);
        }
        if (labelArquivoWord != null && total == 0) {
            labelArquivoWord.setText("Nenhum arquivo selecionado");
            labelArquivoWord.setForeground(theme.corTextoSuave);
        }
        atualizarVisibilidadePreviewWord(total);
    }

    private void atualizarVisibilidadePreviewWord(int totalArquivos) {
        boolean mostrarPreviewEmbutida = totalArquivos <= AppTheme.MAX_ARQUIVOS_PREVIEW_EMBUTIDA;

        if (painelPreviewWord != null) {
            painelPreviewWord.setVisible(mostrarPreviewEmbutida);
            painelPreviewWord.revalidate();
            painelPreviewWord.repaint();
        }

        if (btnAbrirPreviewWord != null) {
            btnAbrirPreviewWord.setEnabled(totalArquivos > 0);
            btnAbrirPreviewWord.setVisible(totalArquivos > 0);
            btnAbrirPreviewWord.setText(totalArquivos > 1 ? "👁 Abrir Prévia (selecionado)" : "👁 Abrir Prévia");
        }
    }

    private void abrirDialogoPreviewWordSelecionado() {
        List<File> arquivos = obterArquivosWordSelecionados();
        if (arquivos.isEmpty()) {
            mostrarErro("Nenhum arquivo Word foi selecionado para pré-visualização.");
            return;
        }

        File selecionado = listaArquivosWord != null ? listaArquivosWord.getSelectedValue() : null;
        if (selecionado == null) {
            selecionado = arquivos.get(0);
            if (listaArquivosWord != null && listaArquivosWord.getModel().getSize() > 0) {
                listaArquivosWord.setSelectedIndex(0);
            }
            carregarPreviewWord();
        }

        String textoPreview = areaPreviewWord != null ? areaPreviewWord.getText() : "";
        if (textoPreview == null || textoPreview.trim().isEmpty()) {
            textoPreview = "Carregando pré-visualização...";
        }

        JDialog dialogo = new JDialog(this, "Pré-visualização - " + selecionado.getName(), true);
        dialogo.setLayout(new BorderLayout(8, 8));

        JTextArea areaDialogo = new JTextArea();
        areaDialogo.setEditable(false);
        areaDialogo.setLineWrap(true);
        areaDialogo.setWrapStyleWord(true);
        areaDialogo.setFont(AppTheme.FONTE_MONO);
        areaDialogo.setBackground(theme.corFundo);
        areaDialogo.setForeground(theme.corTexto);
        areaDialogo.setText(textoPreview);
        areaDialogo.setCaretPosition(0);

        JLabel lblArquivo = new JLabel("Arquivo: " + selecionado.getAbsolutePath());
        lblArquivo.setBorder(new EmptyBorder(8, 10, 0, 10));
        lblArquivo.setFont(AppTheme.FONTE_SUBTITULO);
        lblArquivo.setForeground(theme.corTextoSuave);

        JButton btnFechar = uiFactory.criarBotaoSecundario("Fechar");
        btnFechar.addActionListener(e -> dialogo.dispose());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.setOpaque(false);
        rodape.add(btnFechar);

        dialogo.add(lblArquivo, BorderLayout.NORTH);
        dialogo.add(uiFactory.criarScrollPane(areaDialogo), BorderLayout.CENTER);
        dialogo.add(rodape, BorderLayout.SOUTH);
        dialogo.setSize(900, 620);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void configurarDropArquivosWord(JComponent componente) {
        if (componente == null) {
            return;
        }
        componente.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    @SuppressWarnings("unchecked")
                    List<File> arquivos = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    adicionarArquivosWord(arquivos.toArray(new File[0]));
                    carregarPreviewWord();
                    return true;
                } catch (Exception e) {
                    adicionarCardLog("⚠ Falha no drag-and-drop de Word: " + e.getMessage());
                    return false;
                }
            }
        });
    }

    private void iniciarProcessamento() {
        int abaSelecionada = abas.getSelectedIndex();
        // Se a aba Performance & ROI (3) está ativa, detecta automaticamente o modo
        // com base no que foi preenchido (URL ou Word)
        boolean viaUrl;
        if (abaSelecionada == 3) {
            boolean temUrls = areaUrls != null && !areaUrls.getText().trim().isEmpty();
            boolean temWord = !obterArquivosWordSelecionados().isEmpty();
            if (!temUrls && !temWord) {
                mostrarErro("Informe URLs (aba 'Via URLs') ou selecione um arquivo Word (aba 'Upload Word') antes de gerar o prompt.");
                return;
            }
            // Preferência: Word se disponível, senão URL
            viaUrl = temUrls && !temWord;
        } else {
            viaUrl = abaSelecionada == 0;
        }

        // Validação crítica: RTC obrigatório para rastreabilidade/auditoria
        String rtcInformado = campoRTC != null ? campoRTC.getText().trim() : "";
        if (rtcInformado.isEmpty()) {
            mostrarErro("Informe o número do RTC antes de processar. Este campo é obrigatório.");
            if (campoRTC != null) {
                campoRTC.requestFocusInWindow();
            }
            return;
        }

        // Validação da Estimativa Poker (Obrigatória para ROI)
        String estPoker = campoEstimativaPoker != null ? campoEstimativaPoker.getText().trim() : "";
        if (estPoker.isEmpty()) {
            mostrarErro("Informe a Estimativa Poker (Horas) na aba Performance antes de processar.\nIsso é necessário para o cálculo de ROI.");
            abas.setSelectedIndex(3); // Alterna para a aba Performance & ROI
            if (campoEstimativaPoker != null) {
                campoEstimativaPoker.requestFocusInWindow();
            }
            return;
        }

        // Validação da Sprint (Recomendado)
        String sprintInf = campoSprint != null ? campoSprint.getText().trim() : "";
        if (sprintInf.isEmpty()) {
            int resposta = JOptionPane.showConfirmDialog(this, 
                "O número da Sprint não foi informado. Deseja informar agora para melhor organização do ROI?",
                "Sprint não informada", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (resposta == JOptionPane.YES_OPTION) {
                abas.setSelectedIndex(3);
                if (campoSprint != null) campoSprint.requestFocusInWindow();
                return;
            }
        }
        if (viaUrl) {
            String textoUrls = areaUrls.getText().trim();
            if (textoUrls.isEmpty()) {
                mostrarErro("Informe pelo menos uma URL para processar.");
                return;
            }
        } else {
            if (obterArquivosWordSelecionados().isEmpty()) {
                mostrarErro("Adicione ao menos um arquivo Word (.doc ou .docx) antes de processar.");
                return;
            }
        }

        // Desabilita controles durante o processamento
        btnProcessar.setEnabled(false);
        btnCopiarPrompt.setEnabled(false);
        btnSalvar.setEnabled(false);
        
        painelLogCards.removeAll();
        adicionarCardLog("Iniciando processamento...");
        
        areaPrompt.setText("Processando...");
        barraProgresso.setIndeterminate(true);
        barraProgresso.setString("Processando...");

        final boolean modoUrl = viaUrl;

        SwingWorker<ExecucaoConsolidada, String> worker = new SwingWorker<ExecucaoConsolidada, String>() {

            protected ExecucaoConsolidada doInBackground() throws Exception {
                ExpertDevConfig config = ExpertDevConfig.carregar();
                Instant inicio = Instant.now();
                List<ResultadoProcessamento> resultados;
                final boolean modoIa = isModoIaSelecionado();

                // Registrar auditoria
                String rtcNumero = campoRTC != null ? campoRTC.getText().trim() : "";
                String ucCodigo = campoUC != null ? campoUC.getText().trim() : "";
                String modoGeracao = controller.modoGeracaoSelecionado;
                String provider = controller.providerIaSelecionado;
                String authUsername = controller.obterUsuarioSessao();
                String authEmail = controller.obterEmailSessao();

                publish("→ RTC: " + (rtcNumero.isEmpty() ? "(não informado)" : rtcNumero));
                publish("→ UC: " + (ucCodigo.isEmpty() ? "(não informado)" : ucCodigo));

                RegistroAuditoria regAuditoria = new RegistroAuditoria(rtcNumero, ucCodigo, ucCodigo,
                        modoGeracao, provider, "", authUsername, authEmail);
                controller.ultimoRegistroAuditoriaId = controller.auditoriaService.inserir(regAuditoria);

                // Métricas de performance
                MetricaPerformance mPerf = controller.performanceService.obterPorRTCeUsuario(rtcNumero, authUsername);
                if (mPerf == null) {
                    mPerf = new MetricaPerformance(rtcNumero, authUsername, authEmail);
                }
                mPerf.setAuthUsername(authUsername);
                mPerf.setAuthEmail(authEmail);
                if (mPerf.getInicioExpertDev() == null) {
                    mPerf.setInicioExpertDev(LocalDateTime.now());
                }
                controller.performanceService.salvarOuAtualizar(mPerf);

                if (modoUrl) {
                    publish("→ Modo: Via URLs");
                    String textoUrls = areaUrls.getText().trim();
                    UrlParser urlParser = new UrlParser();
                    List<String> urls = urlParser.parsear(textoUrls);
                    publish("→ URLs encontradas: " + urls.size());

                    if (urls.isEmpty()) {
                        throw new IllegalArgumentException("Nenhuma URL válida foi detectada.");
                    }

                    int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
                    publish("→ Iniciando processamento paralelo com " + numThreads + " thread(s)...");

                    ParallelUrlProcessor processor = new ParallelUrlProcessor(config, numThreads);
                    resultados = processor.processar(urls);
                    publish("→ Processamento de URLs concluído.");
                } else {
                    publish("→ Modo: Upload Word");
                    List<File> arquivosWord = obterArquivosWordSelecionados();
                    publish("→ Arquivos Word na fila: " + arquivosWord.size());
                    WordDocumentReader reader = new WordDocumentReader();
                    resultados = new ArrayList<ResultadoProcessamento>();
                    int index = 0;
                    for (File arquivoWord : arquivosWord) {
                        index++;
                        publish("→ Lendo arquivo Word " + index + "/" + arquivosWord.size() + ": " + arquivoWord.getName());
                        ResultadoProcessamento r = reader.ler(arquivoWord);
                        if (!r.isSucesso()) {
                            throw new RuntimeException(montarMensagemErroWord(r.getErro()));
                        }
                        publish("→ Documento Word lido com sucesso: " + arquivoWord.getName());
                        for (String statusWord : extrairMensagensStatusWord(r)) {
                            publish(statusWord);
                        }
                        if (r.getObservacao() != null && !r.getObservacao().trim().isEmpty()) {
                            publish("→ Rastreamento Word: " + r.getObservacao());
                        }
                        publish("→ Caracteres extraídos: " + (r.getTextoExtraido() != null
                                ? r.getTextoExtraido().length() : 0));
                        resultados.add(r);
                    }
                }

                // Gerar Word (com imagens)
                publish("→ Gerando documento Word de saída...");
                String arquivoWordSaida = "";
                try {
                    ImageDownloader imgDl = new ImageDownloader();
                    WordDocumentBuilder wordBuilder = new WordDocumentBuilder(imgDl);
                    arquivoWordSaida = wordBuilder.gerar(resultados);
                    publish("✓ Word gerado: " + arquivoWordSaida);
                } catch (Exception e) {
                    publish("⚠ Word de saída não gerado: " + e.getMessage());
                }

                // Gerar PDF
                publish("→ Gerando PDF de saída...");
                String arquivoPdf = "";
                try {
                    ImageDownloader imgDl = new ImageDownloader();
                    PdfDocumentBuilder pdfBuilder = new PdfDocumentBuilder(imgDl);
                    arquivoPdf = pdfBuilder.gerar(resultados);
                    publish("✓ PDF gerado: " + arquivoPdf);
                } catch (Exception e) {
                    publish("⚠ PDF de saída não gerado: " + e.getMessage());
                }

                // Consolidar
                publish("→ Consolidando resultados e gerando prompt...");
                List<String> urlsReferencia = new ArrayList<>();
                for (ResultadoProcessamento r : resultados) {
                    urlsReferencia.add(r.getUrl());

                }

                PromptGenerationService servicoPrompt = criarServicoPrompt(config, modoIa);
                ExecucaoConsolidada execucao;
                try {
                    if (modoIa && config.isAiModoEconomico()) {
                        publish("→ Modo economico de IA ativo (contexto reduzido + menos tokens).");
                    }
                    publish("→ Gerador de prompt: " + servicoPrompt.getNomeModo());
                    execucao = new ResultConsolidator(servicoPrompt)
                            .consolidar(resultados, urlsReferencia.size(), inicio, Instant.now(),
                                    arquivoWordSaida, arquivoPdf, rtcNumero);
                } catch (RuntimeException e) {
                    if (modoIa) {
                        publish("⚠ IA indisponível. Aplicando fallback para modo local...");
                        PromptGenerationService fallbackLocal = new LocalPromptGenerationService(
                                new PromptGenerator(controller.perfilPromptSelecionado));
                        execucao = new ResultConsolidator(fallbackLocal)
                                .consolidar(resultados, urlsReferencia.size(), inicio, Instant.now(),
                                        arquivoWordSaida, arquivoPdf, rtcNumero);
                        publish("✓ Prompt gerado em fallback local.");
                    } else {
                        throw e;
                    }
                }

                // Injetar auditoria no prompt
                String promptFinal = execucao.getPromptPronto();
                if (!rtcNumero.isEmpty() || !ucCodigo.isEmpty()) {
                    StringBuilder header = new StringBuilder();
                    if (!rtcNumero.isEmpty()) {
                        header.append("// RTC: ").append(rtcNumero).append("\n");
                    }
                    if (!ucCodigo.isEmpty()) {
                        header.append("// UC: ").append(ucCodigo).append("\n");
                    }
                    if (header.length() > 0) {
                        header.append("\n");
                        promptFinal = header.toString() + promptFinal;
                    }
                }

                // Atualizar registro de auditoria com status final
                if (controller.ultimoRegistroAuditoriaId > 0) {
                    controller.auditoriaService.atualizar(controller.ultimoRegistroAuditoriaId, "CONCLUIDO", promptFinal);
                }

                // Guardar prompt com auditoria em variável de instância para exibir no done()
                controller.promptComAuditoria = promptFinal;

                // Salvar arquivos de texto
                publish("→ Salvando arquivos de saída...");
                try {
                    DefaultTextFileWriter writer = new DefaultTextFileWriter();
                    writer.write(config.getArquivoResumo(), execucao.getResumoExecucao());
                    publish("✓ " + config.getArquivoResumo());

                    if (execucao.possuiSucesso()) {
                        writer.write(config.getArquivoRegras(), execucao.getRegrasExtraidas());
                        publish("✓ " + config.getArquivoRegras());
                        writer.write(config.getArquivoImagens(), execucao.getImagensEncontradas());
                        publish("✓ " + config.getArquivoImagens());
                        writer.write(config.getArquivoPrompt(), execucao.getPromptPronto());
                        publish("✓ " + config.getArquivoPrompt());
                    }
                    if (execucao.possuiErros()) {
                        writer.write(config.getArquivoErros(), execucao.getErrosProcessamento());
                        publish("✓ " + config.getArquivoErros());
                    }
                } catch (Exception e) {
                    publish("⚠ Erro ao salvar arquivos: " + e.getMessage());
                }

                publish("✅ Concluído! URLs: " + execucao.getTotalUrls()
                        + " | Sucesso: " + execucao.getUrlsComSucesso()
                        + " | Falhas: " + execucao.getUrlsComFalha()
                        + " | Imagens: " + execucao.getTotalImagens()
                        + " | Tempo: " + execucao.getTempoTotalSegundos() + "s");

                return execucao;
            }

            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    adicionarCardLog(msg);
                }
            }

            protected void done() {
                barraProgresso.setIndeterminate(false);
                btnProcessar.setEnabled(true);
                try {
                    ExecucaoConsolidada execucao = get();
                    // Exibe o prompt que ja inclui auditoria (RTC + UC)
                    areaPrompt.setText(controller.promptComAuditoria);
                    areaPrompt.setCaretPosition(0);
                    atualizarAbaHistorico();
                    areaPrompt.setCaretPosition(0);
                    barraProgresso.setValue(100);
                    barraProgresso.setString("Concluído com sucesso!");
                    barraProgresso.setForeground(theme.corSucesso);
                    btnCopiarPrompt.setEnabled(true);
                    btnSalvar.setEnabled(true);
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    adicionarCardLog("❌ ERRO: " + msg);
                    areaPrompt.setText("Ocorreu um erro durante o processamento.\nVerifique o log.");
                    barraProgresso.setValue(0);
                    barraProgresso.setString("Erro!");
                    barraProgresso.setForeground(theme.corErro);
                    mostrarErro("Erro no processamento:\n" + msg);
                }
            }
        };

        worker.execute();
    }

    private List<String> extrairMensagensStatusWord(ResultadoProcessamento resultado) {
        List<String> mensagens = new ArrayList<String>();
        if (resultado == null || resultado.getObservacao() == null) {
            return mensagens;
        }

        String obs = resultado.getObservacao();
        if (obs.contains("Formato DOCX detectado: leitura direta.")) {
            mensagens.add("✓ Word: ingestão direta de DOCX (sem conversão).");
        }
        if (obs.contains("Conversao concluida com sucesso.")) {
            mensagens.add("✓ Word: conversão DOC -> DOCX concluída via LibreOffice.");
        }
        if (obs.contains("Conversao DOC desativada por configuracao.")) {
            mensagens.add("⚠ Word: conversão DOC desativada por configuração, usando parser DOC direto.");
        }
        if (obs.contains("LibreOffice indisponivel") || obs.contains("LibreOffice nao encontrado")) {
            mensagens.add("⚠ Word: LibreOffice não detectado; aplicado fallback para parser DOC legado.");
        }
        if (obs.contains("Fallback direto para parser DOC")) {
            mensagens.add("⚠ Word: fallback DOC legado acionado.");
        }
        return mensagens;
    }

    private String montarMensagemErroWord(String erroBase) {
        String erro = erroBase == null ? "Erro desconhecido ao processar Word." : erroBase;
        String lower = erro.toLowerCase();

        if (lower.contains("libreoffice") && (lower.contains("nao encontrado") || lower.contains("indisponivel"))) {
            return "Falha ao ler o Word: " + erro
                    + "\nDica: instale o LibreOffice ou configure 'word.libreoffice.path' em expertdev.properties."
                    + "\nAlternativa: mantenha 'word.doc.fallback.to.direct.read=true' para parser DOC legado.";
        }
        if (lower.contains("conversao de doc desativada")) {
            return "Falha ao ler o Word: " + erro
                    + "\nDica: habilite 'word.doc.conversion.enabled=true' para tentar DOC -> DOCX automaticamente.";
        }
        return "Falha ao ler o Word: " + erro;
    }

    private void copiarPromptParaClipboard() {
        String texto = areaPrompt.getText();
        if (texto != null && !texto.isEmpty()) {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(texto), null);
            JOptionPane.showMessageDialog(this,
                    "Prompt copiado para a área de transferência!",
                    "Copiado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void abrirDialogoSalvar() {
        JOptionPane.showMessageDialog(this,
                "Os arquivos já foram salvos automaticamente durante o processamento.\n\n" +
                "Arquivos gerados no diretório de execução:\n" +
                "  • prompt_para_junie_copilot.txt\n" +
                "  • regras_extraidas.txt\n" +
                "  • imagens_encontradas.txt\n" +
                "  • resumo_execucao.txt\n" +
                "  • contexto_com_imagens.docx\n" +
                "  • contexto_com_imagens.pdf",
                "Arquivos Salvos", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarErro(String mensagem) {
        presentationMessageService.showError(mensagem);
    }

    private void alternarTema(boolean claro) {
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

        aplicarTema(claro);
        uiFactory = new UiFactory(theme);

        getContentPane().removeAll();
        getContentPane().setBackground(theme.corFundo);
        construirInterface();

        if (areaUrls != null) {
            areaUrls.setText(urlsTexto);
        }
        if (areaPreviewWord != null && previewTexto != null && !previewTexto.trim().isEmpty()) {
            areaPreviewWord.setText(previewTexto);
        }
        if (areaPrompt != null) {
            areaPrompt.setText(promptTexto);
        }
        if (comboModoGeracao != null) {
            comboModoGeracao.setSelectedItem(modoSelecionado);
            controller.modoGeracaoSelecionado = modoSelecionado;
        }
        if (comboProviderIa != null) {
            comboProviderIa.setSelectedItem(controller.providerIaSelecionado);
        }
        if (comboPerfilPrompt != null) {
            comboPerfilPrompt.setSelectedItem(controller.perfilPromptSelecionado);
        }
        if (campoApiKey != null) {
            campoApiKey.setText(apiKeyTexto);
        }
        if (chkSalvarApiKey != null) {
            chkSalvarApiKey.setSelected(salvarKey);
            controller.salvarApiKeySelecionada = salvarKey;
        }
        atualizarEstadoOpcoesIa();
        if (abas != null && abaSelecionada >= 0 && abaSelecionada < abas.getTabCount()) {
            abas.setSelectedIndex(abaSelecionada);
        }

        revalidate();
        repaint();
    }

    private void preencherConfigIaInicial(ExpertDevConfig configUi) {
        if (comboModoGeracao != null) {
            comboModoGeracao.setSelectedItem(controller.modoGeracaoSelecionado);
        }
        if (comboProviderIa != null) {
            comboProviderIa.setSelectedItem(controller.providerIaSelecionado);
        }
        if (comboPerfilPrompt != null) {
            comboPerfilPrompt.setSelectedItem(controller.perfilPromptSelecionado);
        }
        if (campoApiKey != null) {
            campoApiKey.setText(configUi.getAiApiKeyResolvida());
            controller.apiKeyDigitada = new String(campoApiKey.getPassword());
        }
        if (chkSalvarApiKey != null) {
            boolean possuiKeySalva = configUi.getAiApiKeyResolvida() != null
                    && !configUi.getAiApiKeyResolvida().trim().isEmpty();
            chkSalvarApiKey.setSelected(possuiKeySalva);
            controller.salvarApiKeySelecionada = possuiKeySalva;
        }
        if (lblAvisoModoEconomicoIa != null) {
            lblAvisoModoEconomicoIa.setVisible(configUi.isAiModoEconomico());
        }
        atualizarEstimativaIa();
    }

    private void atualizarEstadoOpcoesIa() {
        boolean iaAtiva = isModoIaSelecionado();
        if (comboProviderIa != null) {
            comboProviderIa.setEnabled(iaAtiva);
        }
        if (comboPerfilPrompt != null) {
            comboPerfilPrompt.setEnabled(true);
        }
        if (campoApiKey != null) {
            campoApiKey.setEnabled(iaAtiva);
        }
        if (btnLimparApiKey != null) {
            btnLimparApiKey.setEnabled(iaAtiva);
        }
        if (chkSalvarApiKey != null) {
            chkSalvarApiKey.setEnabled(iaAtiva);
        }
        if (btnTestarConexaoIa != null) {
            btnTestarConexaoIa.setEnabled(iaAtiva);
        }
        if (lblAvisoModoEconomicoIa != null) {
            lblAvisoModoEconomicoIa.setVisible(iaAtiva);
        }
        if (lblEstimativaIa != null) {
            lblEstimativaIa.setVisible(true);
        }
    }

    private boolean isModoIaSelecionado() {
        if (comboModoGeracao == null || comboModoGeracao.getSelectedItem() == null) {
            return "IA".equalsIgnoreCase(controller.modoGeracaoSelecionado);
        }
        return "IA".equalsIgnoreCase(String.valueOf(comboModoGeracao.getSelectedItem()));
    }

    private String normalizarProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            return "openai";
        }
        String normalized = provider.trim().toLowerCase();
        if ("claude-code".equals(normalized) || "anthropic".equals(normalized)) {
            return "claude";
        }
        return normalized;
    }

    private PromptGenerationService criarServicoPrompt(ExpertDevConfig config, boolean modoIa) {
        if (!modoIa) {
            return new LocalPromptGenerationService(new PromptGenerator(controller.perfilPromptSelecionado));
        }

        String apiKeyInformada = campoApiKey != null ? new String(campoApiKey.getPassword()).trim() : "";
        String apiKey = apiKeyInformada.isEmpty() ? config.getAiApiKeyResolvida() : apiKeyInformada;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Modo IA selecionado, mas nenhuma API Key foi informada.");
        }

        if (controller.salvarApiKeySelecionada && !apiKeyInformada.isEmpty()) {
            ExpertDevConfig.salvarApiKeyIA(apiKeyInformada);
        }

        return new AiPromptGenerationService(
                controller.providerIaSelecionado,
                config.getAiEndpoint(),
                config.getAiModel(),
                apiKey,
                config.getAiTimeoutMs(),
                config.getAiTemperature(),
                config.getAiMaxTokens(),
                config.getAiMaxContextChars(),
                config.isAiModoEconomico(),
                controller.perfilPromptSelecionado
        );
    }

    private void testarConexaoIa() {
        ExpertDevConfig config = ExpertDevConfig.carregar();
        final String apiKeyInformada = campoApiKey != null ? new String(campoApiKey.getPassword()).trim() : "";
        final String apiKey = apiKeyInformada.isEmpty() ? config.getAiApiKeyResolvida() : apiKeyInformada;
        if (apiKey.isEmpty()) {
            mostrarErro("Informe uma API Key para testar a conexão de IA.");
            return;
        }

        btnTestarConexaoIa.setEnabled(false);
        adicionarCardLog("→ Testando conexão com IA...");

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                AiPromptGenerationService ia = new AiPromptGenerationService(
                        controller.providerIaSelecionado,
                        config.getAiEndpoint(),
                        config.getAiModel(),
                        apiKey,
                        config.getAiTimeoutMs(),
                        config.getAiTemperature(),
                        config.getAiMaxTokens(),
                        config.getAiMaxContextChars(),
                        config.isAiModoEconomico(),
                        controller.perfilPromptSelecionado
                );
                ia.testarConexao();
                return null;
            }

            @Override
            protected void done() {
                btnTestarConexaoIa.setEnabled(true);
                try {
                    get();
                    adicionarCardLog("✓ Conexão com IA validada com sucesso.");
                    if (controller.salvarApiKeySelecionada && !apiKeyInformada.isEmpty()) {
                        ExpertDevConfig.salvarApiKeyIA(apiKeyInformada);
                    }
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    adicionarCardLog("⚠ Falha no teste de IA: " + msg);
                    mostrarErro("Falha ao testar IA:\n" + msg);
                }
            }
        };
        worker.execute();
    }

    private void limparApiKey() {
        if (campoApiKey != null) {
            campoApiKey.setText("");
        }
        controller.apiKeyDigitada = "";
        controller.salvarApiKeySelecionada = false;
        if (chkSalvarApiKey != null) {
            chkSalvarApiKey.setSelected(false);
        }
        ExpertDevConfig.salvarApiKeyIA("");
        adicionarCardLog("→ API Key de IA limpa da interface e da configuração local.");
    }



    private void atualizarEstimativaIa() {
        if (lblEstimativaIa == null) {
            return;
        }

        ExpertDevConfig cfg = ExpertDevConfig.carregar();
        String contexto;
        if (abas != null && abas.getSelectedIndex() == 1 && areaPreviewWord != null) {
            contexto = areaPreviewWord.getText();
        } else {
            contexto = areaUrls != null ? areaUrls.getText() : "";
        }
        if (contexto == null) {
            contexto = "";
        }

        int chars = contexto.trim().length();
        int inputTokens = Math.max(1, chars / 4 + 280);
        int outputTokens = cfg.getAiMaxTokens();

        double inputRate;
        double outputRate;
        if ("claude".equalsIgnoreCase(controller.providerIaSelecionado)) {
            inputRate = 3.0d;   // USD por 1M input tokens (estimativa)
            outputRate = 15.0d; // USD por 1M output tokens (estimativa)
        } else {
            inputRate = 0.15d;  // gpt-4o-mini estimativa
            outputRate = 0.60d;
        }

        double estimativaCusto = (inputTokens / 1_000_000.0d) * inputRate
                + (outputTokens / 1_000_000.0d) * outputRate;
        DecimalFormat df = new DecimalFormat("0.0000");
        lblEstimativaIa.setText("Estimativa IA: in~" + inputTokens
                + " tok, out max~" + outputTokens
                + " tok, custo~US$" + df.format(estimativaCusto));
    }


    // ─── Entry Point ──────────────────────────────────────────────────────────

    public static void lancar() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // mantém o LAF padrão
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ExpertDevConfig config = ExpertDevConfig.carregar();
                if (!config.isAuthEnabled()) {
                    new ExpertDevGUI(new AuthSession("Local", "", LicenseStatus.PREMIUM, 0));
                    return;
                }

                AuthService authService = new AuthService();
                LoginDialog loginDialog = new LoginDialog(null, authService, config);
                loginDialog.setVisible(true);
                AuthSession session = loginDialog.getSession();
                if (session == null || session.getLicenseStatus() == LicenseStatus.EXPIRED) {
                    JOptionPane.showMessageDialog(null,
                            "Sem acesso valido. Encerrando o Expert Dev.",
                            "Acesso",
                            JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                    return;
                }
                new ExpertDevGUI(session);
            }
        });
    }
}


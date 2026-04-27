package br.com.expertdev.ui;

import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.service.CacheService;
import br.com.expertdev.ui.tabs.ExpertDevTabsBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Constrói todos os painéis da GUI principal.
 * Responsabilidade única: layout e criação de componentes Swing.
 * Toda lógica de negócio é delegada ao GuiController via callbacks.
 */
class GuiPanelBuilder {

    private final ExpertDevGUI gui;
    private final AppTheme theme;
    private final UiFactory uiFactory;
    private final GuiController controller;

    GuiPanelBuilder(ExpertDevGUI gui, AppTheme theme, UiFactory uiFactory, GuiController controller) {
        this.gui = gui;
        this.theme = theme;
        this.uiFactory = uiFactory;
        this.controller = controller;
    }

    // ─── Estrutura principal ──────────────────────────────────────────────────

    JPanel criarCabecalho() {
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
        chkTemaClaro.setVisible(false);
        chkTemaClaro.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gui.alternarTema(chkTemaClaro.isSelected());
            }
        });

        JLabel lblVersao = new JLabel("v 2.6.0-BETA");
        lblVersao.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVersao.setForeground(theme.corDestaque2);
        lblVersao.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblLicenca = new JLabel(controller.obterTextoLicenca());
        lblLicenca.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLicenca.setForeground(controller.obterCorLicenca());
        lblLicenca.setBorder(new EmptyBorder(0, 8, 0, 8));

        ImageIcon iconeUsuario = uiFactory.criarIconeUsuarioCircular(36);
        JLabel lblIconeUsuario = new JLabel(iconeUsuario);
        lblIconeUsuario.setBorder(new EmptyBorder(0, 0, 0, 4));

        JLabel lblNomeUsuario = new JLabel(gui.authSession.getDisplayName());
        lblNomeUsuario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNomeUsuario.setForeground(theme.corTexto);

        String emailSessao = gui.authSession.getEmail();
        String tooltipUsuario = emailSessao != null && !emailSessao.trim().isEmpty()
                ? gui.authSession.getDisplayName() + "  •  " + emailSessao
                : gui.authSession.getDisplayName();
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

        JSeparator sep = new JSeparator();
        sep.setForeground(theme.corBorda);
        sep.setBackground(theme.corBorda);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(theme.corPainel);
        wrapper.add(cabecalho, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }

    JSplitPane criarCorpo() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                criarPainelEntrada(), criarPainelSaida());
        split.setDividerLocation(490);
        split.setDividerSize(6);
        split.setBackground(theme.corFundo);
        split.setForeground(theme.corBorda);
        split.setBorder(null);
        return split;
    }

    JPanel criarRodape() {
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

    // ─── Painel de Entrada ────────────────────────────────────────────────────

    private JPanel criarPainelEntrada() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(theme.corFundo);
        painel.setBorder(new EmptyBorder(16, 16, 0, 8));

        painel.add(criarPainelAuditoria(), BorderLayout.NORTH);

        gui.abas = new JTabbedPane();
        estilizarAbas(gui.abas);
        ExpertDevTabsBuilder tabsBuilder = new ExpertDevTabsBuilder();
        tabsBuilder.addCoreTabs(gui.abas,
                this::criarAbaUrls,
                this::criarAbaUpload,
                this::criarAbaHistorico,
                this::criarAbaPerformance);

        try {
            if (gui.authSession != null && gui.authSession.isPremium()) {
                br.com.expertdev.pro.ui.ProAssistantPanel painelPro = new br.com.expertdev.pro.ui.ProAssistantPanel();
                JScrollPane scrollPainelPro = new JScrollPane(painelPro);
                scrollPainelPro.setBorder(null);
                scrollPainelPro.getVerticalScrollBar().setUnitIncrement(16);
                gui.abas.addTab("Assistente Pro", scrollPainelPro);
            } else {
                JPanel painelBloqueado = new JPanel(new BorderLayout());
                painelBloqueado.setBackground(theme.corPainelAlt);
                JLabel lblBloqueado = new JLabel(
                        "Assistente Pro disponivel apenas para credenciais Premium. Upgrade para Premium.",
                        SwingConstants.CENTER);
                lblBloqueado.setForeground(theme.corTextoSuave);
                lblBloqueado.setFont(AppTheme.FONTE_ROTULO);
                painelBloqueado.add(lblBloqueado, BorderLayout.CENTER);
                gui.abas.addTab("Assistente Pro", painelBloqueado);
                int indicePro = gui.abas.getTabCount() - 1;
                gui.abas.setEnabledAt(indicePro, false);
                gui.abas.setToolTipTextAt(indicePro, "Upgrade para Premium");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(gui,
                        "Recurso indisponivel para sua credencial atual.\nUpgrade para Premium para acessar o Assistente Pro.",
                        "Assistente Pro", JOptionPane.INFORMATION_MESSAGE));
            }
        } catch (Exception ex) {
            System.err.println("Erro ao carregar painel Pro: " + ex.getMessage());
        }

        gui.abas.addChangeListener(e -> {
            controller.atualizarEstimativaIa();
            if (gui.abas.getSelectedIndex() == 3) {
                controller.atualizarGraficoPerformance();
                if (gui.campoRTC != null && gui.campoRTC.isFocusOwner()) {
                    controller.solicitarAtualizacaoSugestoesRtc();
                }
            } else {
                controller.ocultarPopupSugestoesRtc();
            }
        });
        painel.add(gui.abas, BorderLayout.CENTER);
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
                new EmptyBorder(8, 10, 8, 10)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblRTC = uiFactory.criarRotulo("RTC:");
        lblRTC.setFont(AppTheme.FONTE_ROTULO);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        painel.add(lblRTC, gbc);

        gui.campoRTC = new JTextField();
        gui.campoRTC.setFont(AppTheme.FONTE_NORMAL);
        gui.campoRTC.setToolTipText("Ex: 256421");
        gui.campoRTC.setBackground(theme.corFundo);
        gui.campoRTC.setForeground(theme.corTexto);
        gui.campoRTC.setCaretColor(theme.corDestaque);
        gui.campoRTC.setSelectionColor(theme.corDestaque);
        gui.campoRTC.setSelectedTextColor(Color.WHITE);
        gui.campoRTC.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda), new EmptyBorder(6, 8, 6, 8)));
        gui.campoRTC.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { aoAlterarCampoRtc(); }
            @Override public void removeUpdate(DocumentEvent e)  { aoAlterarCampoRtc(); }
            @Override public void changedUpdate(DocumentEvent e) { aoAlterarCampoRtc(); }
            private void aoAlterarCampoRtc() {
                controller.verificarEPrefilPerformance();
                if (!controller.atualizandoRtcProgramaticamente) {
                    controller.solicitarAtualizacaoSugestoesRtc();
                }
            }
        });
        controller.configurarAutocompleteRtc();
        gbc.gridx = 1; gbc.weightx = 0.4;
        painel.add(gui.campoRTC, gbc);

        JLabel lblUC = uiFactory.criarRotulo("Caso de Uso:");
        lblUC.setFont(AppTheme.FONTE_ROTULO);
        gbc.gridx = 2; gbc.weightx = 0;
        painel.add(lblUC, gbc);

        gui.campoUC = new JTextField();
        gui.campoUC.setFont(AppTheme.FONTE_NORMAL);
        gui.campoUC.setToolTipText("Ex: UC01 – Registrar Usuário");
        gui.campoUC.setBackground(theme.corFundo);
        gui.campoUC.setForeground(theme.corTexto);
        gui.campoUC.setCaretColor(theme.corDestaque);
        gui.campoUC.setSelectionColor(theme.corDestaque);
        gui.campoUC.setSelectedTextColor(Color.WHITE);
        gui.campoUC.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda), new EmptyBorder(6, 8, 6, 8)));
        gbc.gridx = 3; gbc.weightx = 0.6;
        painel.add(gui.campoUC, gbc);

        return painel;
    }

    // ─── Abas ─────────────────────────────────────────────────────────────────

    private JPanel criarAbaUrls() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        painel.add(uiFactory.criarRotulo(
                "Cole uma ou mais URLs (uma por linha ou separadas por vírgula):"), BorderLayout.NORTH);

        gui.areaUrls = new JTextArea();
        gui.areaUrls.setFont(AppTheme.FONTE_MONO);
        gui.areaUrls.setBackground(theme.corFundo);
        gui.areaUrls.setForeground(theme.corTexto);
        gui.areaUrls.setCaretColor(theme.corDestaque);
        gui.areaUrls.setLineWrap(true);
        gui.areaUrls.setWrapStyleWord(true);
        gui.areaUrls.setBorder(new EmptyBorder(10, 10, 10, 10));
        gui.areaUrls.setToolTipText("Ex: https://github.com/user/repo/README.md");
        gui.areaUrls.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { controller.atualizarEstimativaIa(); }
            @Override public void removeUpdate(DocumentEvent e)  { controller.atualizarEstimativaIa(); }
            @Override public void changedUpdate(DocumentEvent e) { controller.atualizarEstimativaIa(); }
        });
        painel.add(uiFactory.criarScrollPane(gui.areaUrls), BorderLayout.CENTER);

        JPanel botoesUrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoesUrl.setOpaque(false);
        JButton btnLimpar = uiFactory.criarBotaoSecundario("✖  Limpar");
        btnLimpar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gui.areaUrls.setText("");
                if (gui.campoRTC != null) {
                    controller.definirCampoRtcProgramaticamente("");
                    controller.verificarEPrefilPerformance();
                }
                if (gui.campoUC != null) gui.campoUC.setText("");
                gui.areaUrls.requestFocus();
            }
        });
        botoesUrl.add(btnLimpar);
        painel.add(botoesUrl, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarAbaUpload() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setOpaque(false);
        topo.add(uiFactory.criarRotulo(
                "Selecione um arquivo Word (.doc/.docx) com as regras de negócio:"), BorderLayout.NORTH);

        JPanel seletorPanel = new JPanel(new BorderLayout(10, 0));
        seletorPanel.setOpaque(false);
        seletorPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        gui.labelArquivoWord = new JLabel("Nenhum arquivo selecionado");
        gui.labelArquivoWord.setFont(AppTheme.FONTE_MONO);
        gui.labelArquivoWord.setForeground(theme.corTextoSuave);
        gui.labelArquivoWord.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda), new EmptyBorder(8, 12, 8, 12)));
        gui.labelArquivoWord.setBackground(theme.corFundo);
        gui.labelArquivoWord.setOpaque(true);

        JButton btnSelecionar = uiFactory.criarBotaoPrimario("📂  Adicionar");
        btnSelecionar.setPreferredSize(new Dimension(140, 38));
        btnSelecionar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { controller.selecionarArquivoWord(); }
        });

        JPanel painelAcoesLista = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painelAcoesLista.setOpaque(false);

        JButton btnRemover = uiFactory.criarBotaoSecundario("🗑 Remover");
        btnRemover.addActionListener(e -> {
            File sel = gui.listaArquivosWord == null ? null : gui.listaArquivosWord.getSelectedValue();
            if (sel != null && gui.modeloArquivosWord != null) {
                gui.modeloArquivosWord.removeElement(sel);
                controller.atualizarResumoArquivosWord();
                controller.carregarPreviewWord();
            }
        });

        JButton btnLimpar = uiFactory.criarBotaoSecundario("✖ Limpar");
        btnLimpar.addActionListener(e -> {
            if (gui.modeloArquivosWord != null) {
                gui.modeloArquivosWord.clear();
                controller.atualizarResumoArquivosWord();
                controller.carregarPreviewWord();
            }
        });

        gui.btnAbrirPreviewWord = uiFactory.criarBotaoSecundario("👁 Abrir Prévia");
        gui.btnAbrirPreviewWord.setToolTipText("Abre a pré-visualização textual do arquivo Word selecionado.");
        gui.btnAbrirPreviewWord.addActionListener(e -> controller.abrirDialogoPreviewWordSelecionado());
        gui.btnAbrirPreviewWord.setEnabled(false);

        painelAcoesLista.add(btnRemover);
        painelAcoesLista.add(btnLimpar);
        painelAcoesLista.add(gui.btnAbrirPreviewWord);

        JPanel esquerda = new JPanel(new BorderLayout(0, 6));
        esquerda.setOpaque(false);
        esquerda.add(gui.labelArquivoWord, BorderLayout.NORTH);

        gui.modeloArquivosWord = new DefaultListModel<File>();
        gui.listaArquivosWord = new JList<File>(gui.modeloArquivosWord);
        gui.listaArquivosWord.setBackground(theme.corFundo);
        gui.listaArquivosWord.setForeground(theme.corTexto);
        gui.listaArquivosWord.setSelectionBackground(theme.corDestaque);
        gui.listaArquivosWord.setSelectionForeground(Color.WHITE);
        gui.listaArquivosWord.setFont(AppTheme.FONTE_NORMAL);
        gui.listaArquivosWord.setVisibleRowCount(5);
        gui.listaArquivosWord.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    File f = (File) value;
                    lbl.setText(f.getName() + "  (" + f.getAbsolutePath() + ")");
                }
                return lbl;
            }
        });
        gui.listaArquivosWord.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) controller.carregarPreviewWord();
        });
        gui.listaArquivosWord.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && gui.listaArquivosWord.getSelectedValue() != null) {
                    controller.abrirDialogoPreviewWordSelecionado();
                }
            }
        });

        JScrollPane scrollArquivos = uiFactory.criarScrollPane(gui.listaArquivosWord);
        scrollArquivos.setPreferredSize(new Dimension(100, 110));
        controller.configurarDropArquivosWord(scrollArquivos);
        controller.configurarDropArquivosWord(gui.listaArquivosWord);
        esquerda.add(scrollArquivos, BorderLayout.CENTER);

        JPanel direita = new JPanel(new BorderLayout(0, 8));
        direita.setOpaque(false);
        direita.add(btnSelecionar, BorderLayout.NORTH);
        direita.add(painelAcoesLista, BorderLayout.SOUTH);

        seletorPanel.add(esquerda, BorderLayout.CENTER);
        seletorPanel.add(direita, BorderLayout.EAST);

        gui.labelResumoArquivosWord = uiFactory.criarRotulo("Arquivos Word: 0");
        gui.labelResumoArquivosWord.setFont(AppTheme.FONTE_SUBTITULO);
        gui.labelResumoArquivosWord.setBorder(new EmptyBorder(4, 2, 0, 0));
        topo.add(gui.labelResumoArquivosWord, BorderLayout.SOUTH);
        topo.add(seletorPanel, BorderLayout.CENTER);
        painel.add(topo, BorderLayout.NORTH);

        JLabel lblPreview = uiFactory.criarRotulo("Pré-visualização do conteúdo:");
        lblPreview.setBorder(new EmptyBorder(12, 0, 4, 0));

        gui.areaPreviewWord = new JTextArea();
        gui.areaPreviewWord.setFont(AppTheme.FONTE_MONO);
        gui.areaPreviewWord.setBackground(theme.corFundo);
        gui.areaPreviewWord.setForeground(new Color(180, 190, 210));
        gui.areaPreviewWord.setEditable(false);
        gui.areaPreviewWord.setRows(8);
        gui.areaPreviewWord.setLineWrap(true);
        gui.areaPreviewWord.setWrapStyleWord(true);
        gui.areaPreviewWord.setBorder(new EmptyBorder(10, 10, 10, 10));
        gui.areaPreviewWord.setText("(adicione um ou mais arquivos .doc/.docx e selecione um item para pré-visualizar)");

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(lblPreview, BorderLayout.NORTH);
        JScrollPane scrollPreview = uiFactory.criarScrollPane(gui.areaPreviewWord);
        scrollPreview.setPreferredSize(new Dimension(100, 160));
        scrollPreview.setMinimumSize(new Dimension(100, 120));
        centro.add(scrollPreview, BorderLayout.CENTER);
        gui.painelPreviewWord = centro;
        painel.add(centro, BorderLayout.CENTER);

        controller.atualizarResumoArquivosWord();
        controller.atualizarVisibilidadePreviewWord(0);
        return painel;
    }

    private JPanel criarAbaHistorico() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel instrucao = uiFactory.criarRotulo("Histórico de processamentos (últimos 100):");
        instrucao.setBorder(new EmptyBorder(0, 0, 8, 0));
        painel.add(instrucao, BorderLayout.NORTH);

        gui.areaHistorico = new JTextArea();
        gui.areaHistorico.setFont(AppTheme.FONTE_MONO);
        gui.areaHistorico.setBackground(theme.corFundo);
        gui.areaHistorico.setForeground(theme.corTextoSuave);
        gui.areaHistorico.setEditable(false);
        gui.areaHistorico.setLineWrap(true);
        gui.areaHistorico.setWrapStyleWord(true);
        gui.areaHistorico.setBorder(new EmptyBorder(10, 10, 10, 10));
        gui.areaHistorico.setText("(histórico será preenchido após o primeiro processamento)");
        painel.add(uiFactory.criarScrollPane(gui.areaHistorico), BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoes.setOpaque(false);

        JButton btnAtualizar = uiFactory.criarBotaoSecundario("🔄  Atualizar");
        btnAtualizar.addActionListener(e -> controller.atualizarAbaHistorico());
        botoes.add(btnAtualizar);

        JButton btnLimparCache = uiFactory.criarBotaoSecundario("🧹  Limpar Cache");
        btnLimparCache.setToolTipText("Apaga o cache local de URLs processadas para forçar novo download");
        btnLimparCache.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(gui,
                    "Deseja realmente limpar o cache de URLs processadas?",
                    "Limpar Cache", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new CacheService().limparCache();
                controller.mostrarMensagem("Cache de processamento limpo com sucesso!");
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

        JPanel topo = new JPanel(new GridBagLayout());
        topo.setBackground(theme.corPainelAlt);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        topo.add(uiFactory.criarRotulo("Estimativa Poker (Horas):"), gbc);

        gui.campoEstimativaPoker = new JTextField(5);
        gui.campoEstimativaPoker.setBackground(theme.corFundo);
        gui.campoEstimativaPoker.setForeground(theme.corTexto);
        gbc.gridx = 1;
        topo.add(gui.campoEstimativaPoker, gbc);

        gbc.gridx = 2;
        topo.add(uiFactory.criarRotulo("Sprint:"), gbc);

        gui.campoSprint = new JTextField(5);
        gui.campoSprint.setBackground(theme.corFundo);
        gui.campoSprint.setForeground(theme.corTexto);
        gbc.gridx = 3;
        topo.add(gui.campoSprint, gbc);

        gbc.gridx = 4;
        topo.add(uiFactory.criarRotulo("Complexidade:"), gbc);

        gui.comboComplexidade = new JComboBox<>(new String[]{"Baixa", "Média", "Alta"});
        gbc.gridx = 5;
        topo.add(gui.comboComplexidade, gbc);
        painel.add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 10, 0));
        centro.setBackground(theme.corPainelAlt);

        gui.painelGrafico = new JPanel(new BorderLayout());
        gui.painelGrafico.setBackground(theme.corPainel);
        gui.painelGrafico.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(theme.corBorda),
                "RTC Atual", 0, 0, AppTheme.FONTE_ROTULO, theme.corTexto));
        centro.add(gui.painelGrafico);

        gui.painelTendencia = new JPanel(new BorderLayout());
        gui.painelTendencia.setBackground(theme.corPainel);
        gui.painelTendencia.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(theme.corBorda),
                "Tendência de Aprendizado", 0, 0, AppTheme.FONTE_ROTULO, theme.corTexto));
        centro.add(gui.painelTendencia);
        painel.add(centro, BorderLayout.CENTER);

        JPanel base = new JPanel(new BorderLayout(10, 0));
        base.setBackground(theme.corPainelAlt);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        botoes.setBackground(theme.corPainelAlt);

        gui.btnIniciarScrum = uiFactory.criarBotaoSecundario("Iniciar Scrum");
        gui.btnIniciarScrum.setToolTipText("Inicia cronômetro do Scrum.");
        gui.btnIniciarScrum.addActionListener(e -> controller.gerenciarAcaoPerformance("INICIAR_SCRUM"));

        gui.btnFinalizarScrum = uiFactory.criarBotaoSecundario("Finalizar Scrum");
        gui.btnFinalizarScrum.setToolTipText("Finaliza cronômetro do Scrum.");
        gui.btnFinalizarScrum.addActionListener(e -> controller.gerenciarAcaoPerformance("FINALIZAR_SCRUM"));

        gui.btnFinalizarExpertDev = uiFactory.criarBotaoSecundario("Finalizar ExpertDev");
        gui.btnFinalizarExpertDev.setToolTipText("Finaliza ExpertDev e marca tarefa como CONCLUIDO.");
        gui.btnFinalizarExpertDev.addActionListener(e -> controller.gerenciarAcaoPerformance("FINALIZAR_EXPERTDEV"));

        JButton btnExportarRelatorio = uiFactory.criarBotaoSecundario("📊 Exportar ROI");
        btnExportarRelatorio.addActionListener(e -> controller.exportarRelatorioROI());

        botoes.add(gui.btnIniciarScrum);
        botoes.add(gui.btnFinalizarScrum);
        botoes.add(gui.btnFinalizarExpertDev);
        botoes.add(btnExportarRelatorio);
        base.add(botoes, BorderLayout.WEST);

        gui.lblGanhoProdutividade = uiFactory.criarRotulo("Ganho: 0%");
        gui.lblGanhoProdutividade.setFont(AppTheme.FONTE_TITULO);
        gui.lblGanhoProdutividade.setForeground(theme.corSucesso);
        base.add(gui.lblGanhoProdutividade, BorderLayout.EAST);

        painel.add(base, BorderLayout.SOUTH);

        gui.campoEstimativaPoker.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                controller.salvarDadosPerformanceAtuais();
            }
        });
        gui.campoSprint.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                controller.salvarDadosPerformanceAtuais();
            }
        });
        gui.comboComplexidade.addActionListener(e -> controller.salvarDadosPerformanceAtuais());

        return painel;
    }

    // ─── Painel de Ações e Configuração de IA ────────────────────────────────

    private JPanel criarPainelAcoes() {
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(10, 0, 12, 0));

        JPanel topoAcoes = new JPanel(new BorderLayout(0, 8));
        topoAcoes.setOpaque(false);
        topoAcoes.add(criarPainelConfigGeracao(), BorderLayout.NORTH);

        gui.barraProgresso = new JProgressBar(0, 100);
        gui.barraProgresso.setStringPainted(true);
        gui.barraProgresso.setString("Aguardando...");
        gui.barraProgresso.setFont(AppTheme.FONTE_NORMAL);
        gui.barraProgresso.setBackground(theme.corPainel);
        gui.barraProgresso.setForeground(theme.corDestaque);
        gui.barraProgresso.setBorderPainted(false);
        gui.barraProgresso.setPreferredSize(new Dimension(0, 22));
        topoAcoes.add(gui.barraProgresso, BorderLayout.SOUTH);
        painel.add(topoAcoes, BorderLayout.NORTH);

        gui.btnProcessar = uiFactory.criarBotaoAcao("▶  Gerar Prompt");
        gui.btnProcessar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { controller.iniciarProcessamento(); }
        });
        painel.add(gui.btnProcessar, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelConfigGeracao() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(theme.corPainelAlt);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda), new EmptyBorder(8, 10, 8, 10)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblModo = uiFactory.criarRotulo("Modo:");
        lblModo.setFont(AppTheme.FONTE_SUBTITULO);
        lblModo.setIcon(uiFactory.criarIconeIa(14));
        lblModo.setIconTextGap(5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        painel.add(lblModo, gbc);

        gui.comboModoGeracao = new JComboBox<String>(new String[]{"LOCAL", "IA"});
        gui.comboModoGeracao.setSelectedItem(controller.modoGeracaoSelecionado);
        gui.comboModoGeracao.setFont(AppTheme.FONTE_NORMAL);
        final Icon iconIaCombo    = uiFactory.criarIconeIa(14);
        final Icon iconLocalCombo = uiFactory.criarIconeLocal(14);
        gui.comboModoGeracao.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String modo = value == null ? "" : value.toString();
                label.setIcon("IA".equalsIgnoreCase(modo) ? iconIaCombo : iconLocalCombo);
                label.setIconTextGap(6);
                return label;
            }
        });
        gui.comboModoGeracao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.modoGeracaoSelecionado = String.valueOf(gui.comboModoGeracao.getSelectedItem());
                ExpertDevConfig.salvarPreferenciaModoGeracao(controller.modoGeracaoSelecionado);
                ExpertDevConfig.salvarPreferenciaAiHabilitada(controller.isModoIaSelecionado());
                controller.atualizarEstadoOpcoesIa();
                controller.atualizarEstimativaIa();
            }
        });
        gbc.gridx = 1; gbc.weightx = 0.3;
        painel.add(gui.comboModoGeracao, gbc);

        JLabel lblPerfil = uiFactory.criarRotulo("Perfil:");
        lblPerfil.setFont(AppTheme.FONTE_SUBTITULO);
        gbc.gridx = 4; gbc.weightx = 0;
        painel.add(lblPerfil, gbc);

        gui.comboPerfilPrompt = new JComboBox<String>(new String[]{"tecnico", "negocial"});
        gui.comboPerfilPrompt.setSelectedItem(controller.perfilPromptSelecionado);
        gui.comboPerfilPrompt.setFont(AppTheme.FONTE_NORMAL);
        gui.comboPerfilPrompt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.perfilPromptSelecionado = String.valueOf(gui.comboPerfilPrompt.getSelectedItem());
                ExpertDevConfig.salvarPromptProfile(controller.perfilPromptSelecionado);
                controller.atualizarEstimativaIa();
            }
        });
        gbc.gridx = 5; gbc.weightx = 0.2;
        painel.add(gui.comboPerfilPrompt, gbc);

        JLabel lblProvider = uiFactory.criarRotulo("Provider:");
        lblProvider.setFont(AppTheme.FONTE_SUBTITULO);
        lblProvider.setIcon(uiFactory.criarIconeIa(14));
        lblProvider.setIconTextGap(5);
        gbc.gridx = 2; gbc.weightx = 0;
        painel.add(lblProvider, gbc);

        gui.comboProviderIa = new JComboBox<String>(new String[]{"openai", "claude"});
        gui.comboProviderIa.setSelectedItem(controller.providerIaSelecionado);
        gui.comboProviderIa.setFont(AppTheme.FONTE_NORMAL);
        gui.comboProviderIa.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String provider = value == null ? "" : value.toString();
                label.setIcon("claude".equalsIgnoreCase(provider)
                        ? uiFactory.criarIconeClaude(14) : uiFactory.criarIconeIa(14));
                label.setIconTextGap(6);
                return label;
            }
        });
        gui.comboProviderIa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.providerIaSelecionado = controller.normalizarProvider(
                        String.valueOf(gui.comboProviderIa.getSelectedItem()));
                ExpertDevConfig.salvarConfiguracaoAiProvider(controller.providerIaSelecionado);
                controller.atualizarEstimativaIa();
            }
        });
        gbc.gridx = 3; gbc.weightx = 0.3;
        painel.add(gui.comboProviderIa, gbc);

        JLabel lblApi = uiFactory.criarRotulo("API Key:");
        lblApi.setFont(AppTheme.FONTE_SUBTITULO);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        painel.add(lblApi, gbc);

        gui.campoApiKey = new JPasswordField();
        gui.campoApiKey.setFont(AppTheme.FONTE_NORMAL);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.8;
        painel.add(gui.campoApiKey, gbc);

        gui.btnLimparApiKey = uiFactory.criarBotaoSecundario("Limpar");
        gui.btnLimparApiKey.setFont(AppTheme.FONTE_SUBTITULO);
        gui.btnLimparApiKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { controller.limparApiKey(); }
        });
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0;
        painel.add(gui.btnLimparApiKey, gbc);

        gui.btnTestarConexaoIa = uiFactory.criarBotaoSecundario("Testar IA");
        gui.btnTestarConexaoIa.setFont(AppTheme.FONTE_SUBTITULO);
        gui.btnTestarConexaoIa.setIcon(uiFactory.criarIconeIa(14));
        gui.btnTestarConexaoIa.setIconTextGap(5);
        gui.btnTestarConexaoIa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { controller.testarConexaoIa(); }
        });
        gbc.gridx = 4; gbc.weightx = 0;
        painel.add(gui.btnTestarConexaoIa, gbc);

        gui.chkSalvarApiKey = new JCheckBox("Salvar chave localmente");
        gui.chkSalvarApiKey.setOpaque(false);
        gui.chkSalvarApiKey.setForeground(theme.corTextoSuave);
        gui.chkSalvarApiKey.setFont(AppTheme.FONTE_SUBTITULO);
        gui.chkSalvarApiKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.salvarApiKeySelecionada = gui.chkSalvarApiKey.isSelected();
            }
        });
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        painel.add(gui.chkSalvarApiKey, gbc);

        gui.lblAvisoModoEconomicoIa = new JLabel("Modo econômico de IA ativo: contexto reduzido e menos tokens.");
        gui.lblAvisoModoEconomicoIa.setFont(AppTheme.FONTE_SUBTITULO);
        gui.lblAvisoModoEconomicoIa.setForeground(theme.corDestaque2);
        gui.lblAvisoModoEconomicoIa.setIcon(uiFactory.criarIconeEconomico(14));
        gui.lblAvisoModoEconomicoIa.setIconTextGap(5);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3; gbc.weightx = 1;
        painel.add(gui.lblAvisoModoEconomicoIa, gbc);

        gui.lblEstimativaIa = new JLabel("Estimativa IA: aguardando contexto...");
        gui.lblEstimativaIa.setFont(AppTheme.FONTE_SUBTITULO);
        gui.lblEstimativaIa.setForeground(theme.corTextoSuave);
        gui.lblEstimativaIa.setIcon(uiFactory.criarIconeEconomico(14));
        gui.lblEstimativaIa.setIconTextGap(5);
        gbc.gridx = 3; gbc.gridy = 3; gbc.gridwidth = 3; gbc.weightx = 1;
        painel.add(gui.lblEstimativaIa, gbc);

        return painel;
    }

    // ─── Painel de Saída ──────────────────────────────────────────────────────

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

    private JPanel criarPainelLog() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBackground(theme.corFundo);

        JLabel lbl = uiFactory.criarRotulo("📋  Log de Execução");
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        painel.add(lbl, BorderLayout.NORTH);

        gui.painelLogCards = new JPanel();
        gui.painelLogCards.setLayout(new BoxLayout(gui.painelLogCards, BoxLayout.Y_AXIS));
        gui.painelLogCards.setBackground(theme.isClaroAtivo() ? Color.WHITE : new Color(10, 10, 18));

        gui.scrollLog = new JScrollPane(gui.painelLogCards);
        gui.scrollLog.setBorder(new LineBorder(theme.corBorda, 1));
        gui.scrollLog.getVerticalScrollBar().setUnitIncrement(16);

        gui.adicionarCardLog("Pronto para processar.");
        painel.add(gui.scrollLog, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelPrompt() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBackground(theme.corFundo);
        painel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel cabecalhoPrompt = new JPanel(new BorderLayout());
        cabecalhoPrompt.setOpaque(false);

        JLabel lbl = uiFactory.criarRotulo("Prompt Gerado");
        lbl.setIcon(uiFactory.criarIconeIa(14));
        lbl.setIconTextGap(6);
        cabecalhoPrompt.add(lbl, BorderLayout.WEST);

        JPanel botoesSaida = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botoesSaida.setOpaque(false);

        gui.btnCopiarPrompt = uiFactory.criarBotaoSecundario("📋  Copiar");
        gui.btnCopiarPrompt.setEnabled(false);
        gui.btnCopiarPrompt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { controller.copiarPromptParaClipboard(); }
        });

        gui.btnSalvar = uiFactory.criarBotaoSecundario("💾  Salvar Arquivos");
        gui.btnSalvar.setEnabled(false);
        gui.btnSalvar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { controller.abrirDialogoSalvar(); }
        });

        botoesSaida.add(gui.btnCopiarPrompt);
        botoesSaida.add(gui.btnSalvar);
        cabecalhoPrompt.add(botoesSaida, BorderLayout.EAST);
        painel.add(cabecalhoPrompt, BorderLayout.NORTH);

        gui.areaPrompt = new JTextArea();
        gui.areaPrompt.setFont(AppTheme.FONTE_MONO);
        gui.areaPrompt.setBackground(theme.corPainel);
        gui.areaPrompt.setForeground(theme.corTexto);
        gui.areaPrompt.setEditable(false);
        gui.areaPrompt.setLineWrap(true);
        gui.areaPrompt.setWrapStyleWord(true);
        gui.areaPrompt.setBorder(new EmptyBorder(10, 10, 10, 10));
        gui.areaPrompt.setText("O prompt aparecerá aqui após o processamento...");

        painel.add(uiFactory.criarScrollPane(gui.areaPrompt), BorderLayout.CENTER);
        return painel;
    }
}

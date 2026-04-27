package br.com.expertdev.ui;

import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.model.AuthSession;
import br.com.expertdev.model.LicenseStatus;
import br.com.expertdev.service.CacheService;
import br.com.expertdev.ui.components.*;
import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * Janela principal redesenhada — Expert Dev v2.6.0-BETA.
 * Estende ExpertDevGUI para herdar todos os campos package-private
 * exigidos pelo GuiController sem alteração no controller.
 */
public class MainFrame extends ExpertDevGUI {

    private static final int LOGO_H_HEADER = 36;

    public MainFrame(AuthSession authSession) {
        super(authSession);
    }

    @Override
    protected void construirInterface() {
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ExpertDevTheme.BG_APP);

        setLayout(new BorderLayout(0, 0));

        // Região norte: AppHeader (58px) + ContextBar (42px)
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(ExpertDevTheme.BG_HEADER);
        northPanel.add(buildAppHeader(), BorderLayout.NORTH);
        northPanel.add(buildContextBar(), BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Centro: split pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(0.54);
        split.setResizeWeight(0.54);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(ExpertDevTheme.BG_APP);
        add(split, BorderLayout.CENTER);

        // Sul: footer
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── AppHeader ─────────────────────────────────────────────────────────────

    private JPanel buildAppHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ExpertDevTheme.BG_HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, ExpertDevTheme.BORDER),
            new EmptyBorder(0, 16, 0, 16)
        ));
        header.setPreferredSize(new Dimension(0, ExpertDevTheme.HEADER_HEIGHT));

        // Lado esquerdo: logo + separador + tagline
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel logoLabel = buildLogoLabel(LOGO_H_HEADER);
        left.add(logoLabel);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setForeground(ExpertDevTheme.BORDER);
        sep.setPreferredSize(new Dimension(1, 22));
        left.add(sep);

        JLabel tagline = new JLabel("Enterprise AI Context Generator");
        tagline.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        tagline.setForeground(ExpertDevTheme.TEXT_MUTED);
        left.add(tagline);

        // Lado direito: badges + user button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        BadgeLabel versionBadge = new BadgeLabel(
            "v2.6.0-BETA",
            ExpertDevTheme.GRAY_100,
            ExpertDevTheme.TEXT_SECONDARY,
            ExpertDevTheme.BORDER,
            ExpertDevTheme.RADIUS_SM
        );
        versionBadge.setFont(ExpertDevTheme.FONT_MONO_SM);
        right.add(versionBadge);

        // Badge premium/trial
        if (authSession != null && authSession.isPremium()) {
            BadgeLabel premiumBadge = new BadgeLabel(
                "PREMIUM",
                ExpertDevTheme.PREMIUM_LIGHT,
                ExpertDevTheme.PREMIUM,
                ExpertDevTheme.RADIUS_SM
            );
            premiumBadge.setFont(ExpertDevTheme.FONT_LABEL.deriveFont(10f));
            right.add(premiumBadge);
        } else if (authSession != null && authSession.isTrial()) {
            BadgeLabel trialBadge = new BadgeLabel(
                "TRIAL " + authSession.getTrialDaysRemaining() + "d",
                ExpertDevTheme.WARNING_BG,
                ExpertDevTheme.WARNING,
                ExpertDevTheme.RADIUS_SM
            );
            trialBadge.setFont(ExpertDevTheme.FONT_LABEL.deriveFont(10f));
            right.add(trialBadge);
        }

        // User button
        String displayName = authSession != null ? authSession.getDisplayName() : "Visitante";
        if (displayName == null || displayName.trim().isEmpty()) displayName = "Visitante";
        JPanel userBtn = buildUserButton(displayName);
        right.add(userBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildUserButton(String name) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        AvatarLabel avatar = new AvatarLabel(name, 28);
        panel.add(avatar);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(ExpertDevTheme.FONT_BODY);
        nameLabel.setForeground(ExpertDevTheme.TEXT_BODY);
        panel.add(nameLabel);

        return panel;
    }

    // ── ContextBar ────────────────────────────────────────────────────────────

    private JPanel buildContextBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        bar.setBackground(ExpertDevTheme.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, ExpertDevTheme.BORDER),
            new EmptyBorder(5, 16, 5, 16)
        ));
        bar.setPreferredSize(new Dimension(0, ExpertDevTheme.CONTEXT_BAR_H));

        JLabel lblRtc = buildContextLabel("RTC");
        bar.add(lblRtc);

        // campoRTC — campo para GuiController usar
        campoRTC = new JTextField();
        styleContextField(campoRTC, 130, 32);
        campoRTC.setToolTipText("Ex: 256421");
        bar.add(campoRTC);

        JLabel lblUC = buildContextLabel("CASO DE USO");
        bar.add(lblUC);

        // campoUC — campo para GuiController usar
        campoUC = new JTextField();
        styleContextField(campoUC, 400, 32);
        campoUC.setToolTipText("Ex: UC01 - Registrar Usuário");
        bar.add(campoUC);

        // Listeners de RTC (replicados do GuiPanelBuilder)
        campoRTC.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { aoAlterarRtc(); }
            public void removeUpdate(DocumentEvent e)  { aoAlterarRtc(); }
            public void changedUpdate(DocumentEvent e) { aoAlterarRtc(); }
            private void aoAlterarRtc() {
                controller.verificarEPrefilPerformance();
                if (!controller.atualizandoRtcProgramaticamente) {
                    controller.solicitarAtualizacaoSugestoesRtc();
                }
            }
        });
        controller.configurarAutocompleteRtc();

        return bar;
    }

    private JLabel buildContextLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ExpertDevTheme.FONT_LABEL);
        lbl.setForeground(ExpertDevTheme.TEXT_MUTED);
        return lbl;
    }

    private void styleContextField(JTextField field, int width, int height) {
        field.setFont(ExpertDevTheme.FONT_BODY);
        field.setBackground(ExpertDevTheme.SURFACE_ALT);
        field.setForeground(ExpertDevTheme.TEXT_BODY);
        field.setCaretColor(ExpertDevTheme.PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true),
            new EmptyBorder(0, 8, 0, 8)
        ));
        field.setPreferredSize(new Dimension(width, height));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ExpertDevTheme.BORDER_FOCUS, 2, true),
                    new EmptyBorder(0, 7, 0, 7)
                ));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true),
                    new EmptyBorder(0, 8, 0, 8)
                ));
            }
        });
    }

    // ── Left Panel ────────────────────────────────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ExpertDevTheme.BG_PANEL);
        panel.setBorder(new MatteBorder(0, 0, 0, 1, ExpertDevTheme.BORDER));

        // Tabs
        abas = new JTabbedPane();
        abas.setBackground(ExpertDevTheme.SURFACE_ALT);
        abas.setFont(ExpertDevTheme.FONT_BODY);

        JPanel tabUrls    = buildTabUrls();
        JPanel tabUpload  = buildTabUpload();
        JPanel tabHist    = buildTabHistorico();
        JPanel tabPerf    = buildTabPerformance();

        abas.addTab("Via URLs", tabUrls);
        abas.addTab("Upload Word", tabUpload);
        abas.addTab("Historico", tabHist);
        abas.addTab("Performance & ROI", tabPerf);

        // Aba Assistente Pro
        addAssistenteProTab();

        // TabHeaderRenderer para cada aba
        for (int i = 0; i < abas.getTabCount(); i++) {
            final int idx = i;
            TabHeaderRenderer renderer = new TabHeaderRenderer(abas, i, abas.getTitleAt(i), createTabIconForIndex(i));
            abas.setTabComponentAt(i, renderer);
            abas.addChangeListener(e -> renderer.refreshState());
        }

        abas.addChangeListener(e -> {
            controller.atualizarEstimativaIa();
            if (abas.getSelectedIndex() == 3) {
                controller.atualizarGraficoPerformance();
            }
        });

        panel.add(abas, BorderLayout.CENTER);

        // Config bar + status + gerar
        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setBackground(ExpertDevTheme.WHITE);
        bottom.setBorder(new MatteBorder(1, 0, 0, 0, ExpertDevTheme.BORDER));
        bottom.add(buildConfigBar(), BorderLayout.NORTH);
        bottom.add(buildStatusAndGenerate(), BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void addAssistenteProTab() {
        try {
            if (authSession != null && authSession.isPremium()) {
                br.com.expertdev.pro.ui.ProAssistantPanel painelPro =
                    new br.com.expertdev.pro.ui.ProAssistantPanel();
                JScrollPane scroll = new JScrollPane(painelPro);
                scroll.setBorder(null);
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                abas.addTab("Assistente Pro", scroll);
            } else {
                JPanel bloqueado = new JPanel(new BorderLayout());
                bloqueado.setBackground(ExpertDevTheme.SURFACE_ALT);
                JLabel lbl = new JLabel(
                    "<html><center>Assistente Pro disponível apenas para Premium.</center></html>",
                    SwingConstants.CENTER);
                lbl.setForeground(ExpertDevTheme.TEXT_MUTED);
                lbl.setFont(ExpertDevTheme.FONT_BODY);
                bloqueado.add(lbl, BorderLayout.CENTER);
                abas.addTab("Assistente Pro", bloqueado);
                int idx = abas.getTabCount() - 1;
                abas.setEnabledAt(idx, false);
            }
        } catch (Exception ex) {
            System.err.println("Erro ao carregar painel Pro: " + ex.getMessage());
        }
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    private JPanel buildTabUrls() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(ExpertDevTheme.BG_PANEL);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = buildSectionLabel("URLS PARA PROCESSAR");
        JButton btnLimpar = buildSmButton("Limpar");
        btnLimpar.addActionListener(e -> {
            areaUrls.setText("");
            controller.definirCampoRtcProgramaticamente("");
            controller.verificarEPrefilPerformance();
            if (campoUC != null) campoUC.setText("");
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(btnLimpar, BorderLayout.EAST);

        JLabel desc = new JLabel("Uma URL por linha. Suporta Confluence, SharePoint e portais internos.");
        desc.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        desc.setForeground(ExpertDevTheme.TEXT_MUTED);

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(desc, BorderLayout.SOUTH);

        areaUrls = new JTextArea();
        areaUrls.setFont(ExpertDevTheme.FONT_MONO);
        areaUrls.setBackground(ExpertDevTheme.SURFACE_ALT);
        areaUrls.setForeground(ExpertDevTheme.TEXT_BODY);
        areaUrls.setCaretColor(ExpertDevTheme.PRIMARY);
        areaUrls.setLineWrap(true);
        areaUrls.setWrapStyleWord(false);
        areaUrls.setBorder(new EmptyBorder(10, 12, 10, 12));
        areaUrls.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { controller.atualizarEstimativaIa(); }
            public void removeUpdate(DocumentEvent e)  { controller.atualizarEstimativaIa(); }
            public void changedUpdate(DocumentEvent e) { controller.atualizarEstimativaIa(); }
        });

        JScrollPane scroll = new JScrollPane(areaUrls);
        scroll.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true));
        scroll.setBackground(ExpertDevTheme.SURFACE_ALT);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTabUpload() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ExpertDevTheme.BG_PANEL);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = buildSectionLabel("ARQUIVO WORD (.DOC/.DOCX)");

        DropZonePanel dropZone = new DropZonePanel(files -> {
            controller.adicionarArquivosWord(files.toArray(new File[0]));
            controller.carregarPreviewWord();
        });
        dropZone.setPreferredSize(new Dimension(0, 150));
        dropZone.setToolTipText("Arraste arquivos .doc/.docx ou clique para selecionar.");
        dropZone.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    controller.selecionarArquivoWord();
                }
            }
        });
        controller.configurarDropArquivosWord(dropZone);

        JButton btnSelecionar = buildPrimarySmButton("Selecionar arquivos");
        btnSelecionar.addActionListener(e -> controller.selecionarArquivoWord());

        JButton btnRemover = buildSmButton("Remover");
        JButton btnLimpar  = buildSmButton("Limpar");
        btnAbrirPreviewWord = buildSmButton("Ver previa");
        btnAbrirPreviewWord.setEnabled(false);
        btnAbrirPreviewWord.addActionListener(e -> controller.abrirDialogoPreviewWordSelecionado());

        // Label arquivo
        labelArquivoWord = new JLabel("Nenhum arquivo selecionado");
        labelArquivoWord.setFont(ExpertDevTheme.FONT_MONO_SM);
        labelArquivoWord.setForeground(ExpertDevTheme.TEXT_MUTED);
        labelArquivoWord.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        labelArquivoWord.setBackground(ExpertDevTheme.SURFACE_ALT);
        labelArquivoWord.setOpaque(true);

        // Lista arquivos
        modeloArquivosWord = new DefaultListModel<File>();
        listaArquivosWord  = new JList<File>(modeloArquivosWord);
        listaArquivosWord.setBackground(ExpertDevTheme.BG_PANEL);
        listaArquivosWord.setForeground(ExpertDevTheme.TEXT_BODY);
        listaArquivosWord.setFont(ExpertDevTheme.FONT_MONO_SM);
        listaArquivosWord.setSelectionBackground(ExpertDevTheme.PRIMARY_LIGHT);
        listaArquivosWord.setSelectionForeground(ExpertDevTheme.PRIMARY);
        listaArquivosWord.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean hasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                if (value instanceof File) lbl.setText(((File) value).getName());
                lbl.setFont(ExpertDevTheme.FONT_MONO_SM);
                if (!isSelected) {
                    lbl.setBackground(index % 2 == 0 ? ExpertDevTheme.BG_PANEL : ExpertDevTheme.SURFACE_ALT);
                }
                lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
                return lbl;
            }
        });
        listaArquivosWord.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) controller.carregarPreviewWord();
        });
        listaArquivosWord.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && listaArquivosWord.getSelectedValue() != null) {
                    controller.abrirDialogoPreviewWordSelecionado();
                }
            }
        });

        JScrollPane scrollLista = new JScrollPane(listaArquivosWord);
        scrollLista.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1));
        scrollLista.setPreferredSize(new Dimension(0, 96));
        controller.configurarDropArquivosWord(scrollLista);
        controller.configurarDropArquivosWord(listaArquivosWord);

        btnRemover.addActionListener(e -> {
            File sel = listaArquivosWord.getSelectedValue();
            if (sel != null) {
                modeloArquivosWord.removeElement(sel);
                controller.atualizarResumoArquivosWord();
                controller.carregarPreviewWord();
            }
        });
        btnLimpar.addActionListener(e -> {
            modeloArquivosWord.clear();
            controller.atualizarResumoArquivosWord();
            controller.carregarPreviewWord();
        });

        labelResumoArquivosWord = new JLabel("Arquivos Word: 0");
        labelResumoArquivosWord.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        labelResumoArquivosWord.setForeground(ExpertDevTheme.TEXT_MUTED);

        // Preview
        JLabel previewTitle = buildSectionLabel("PRE-VISUALIZACAO");

        areaPreviewWord = new JTextArea();
        areaPreviewWord.setFont(ExpertDevTheme.FONT_MONO_SM);
        areaPreviewWord.setBackground(ExpertDevTheme.SURFACE_ALT);
        areaPreviewWord.setForeground(ExpertDevTheme.TEXT_BODY);
        areaPreviewWord.setEditable(false);
        areaPreviewWord.setLineWrap(true);
        areaPreviewWord.setWrapStyleWord(true);
        areaPreviewWord.setBorder(new EmptyBorder(8, 10, 8, 10));
        areaPreviewWord.setText("(adicione arquivos .doc/.docx para pre-visualizar)");

        JScrollPane scrollPreview = new JScrollPane(areaPreviewWord);
        scrollPreview.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true));
        scrollPreview.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPreview.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPreview.setPreferredSize(new Dimension(0, 220));
        scrollPreview.setMinimumSize(new Dimension(0, 160));

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionsRow.setOpaque(false);
        actionsRow.add(btnSelecionar);
        actionsRow.add(btnRemover);
        actionsRow.add(btnLimpar);
        actionsRow.add(btnAbrirPreviewWord);

        JPanel topArea = new JPanel(new BorderLayout(0, 8));
        topArea.setOpaque(false);
        topArea.add(title, BorderLayout.NORTH);
        topArea.add(dropZone, BorderLayout.CENTER);

        JPanel infoArea = new JPanel(new BorderLayout(0, 6));
        infoArea.setOpaque(false);
        infoArea.add(labelArquivoWord, BorderLayout.NORTH);

        JPanel resumoAcoes = new JPanel(new BorderLayout(0, 4));
        resumoAcoes.setOpaque(false);
        resumoAcoes.add(labelResumoArquivosWord, BorderLayout.NORTH);
        resumoAcoes.add(actionsRow, BorderLayout.SOUTH);

        JPanel queueArea = new JPanel(new BorderLayout(0, 6));
        queueArea.setOpaque(false);
        queueArea.add(scrollLista, BorderLayout.CENTER);
        queueArea.add(resumoAcoes, BorderLayout.SOUTH);
        infoArea.add(queueArea, BorderLayout.CENTER);

        JPanel previewArea = new JPanel(new BorderLayout(0, 4));
        previewArea.setOpaque(false);
        previewArea.add(previewTitle, BorderLayout.NORTH);
        previewArea.add(scrollPreview, BorderLayout.CENTER);
        previewArea.setMinimumSize(new Dimension(0, 200));

        painelPreviewWord = previewArea;

        panel.add(topArea, BorderLayout.NORTH);
        JSplitPane splitUpload = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoArea, previewArea);
        splitUpload.setBorder(null);
        splitUpload.setOpaque(false);
        splitUpload.setResizeWeight(0.45);
        splitUpload.setDividerLocation(190);
        splitUpload.setDividerSize(6);
        panel.add(splitUpload, BorderLayout.CENTER);

        controller.atualizarResumoArquivosWord();
        controller.atualizarVisibilidadePreviewWord(0);
        return panel;
    }

    private JPanel buildTabHistorico() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(ExpertDevTheme.BG_PANEL);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = buildSectionLabel("HISTORICO DE PROCESSAMENTOS (ULTIMOS 100)");
        JButton btnAtualizar  = buildSmButton("Atualizar");
        JButton btnLimparCache = buildSmButton("Limpar cache");
        btnAtualizar.addActionListener(e -> controller.atualizarAbaHistorico());
        btnLimparCache.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                "Deseja limpar o cache de URLs processadas?",
                "Limpar Cache", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                new CacheService().limparCache();
                controller.mostrarMensagem("Cache limpo com sucesso!");
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btns.setOpaque(false);
        btns.add(btnAtualizar);
        btns.add(btnLimparCache);
        header.add(btns, BorderLayout.EAST);

        areaHistorico = new JTextArea();
        areaHistorico.setFont(ExpertDevTheme.FONT_MONO_SM);
        areaHistorico.setBackground(ExpertDevTheme.SURFACE_ALT);
        areaHistorico.setForeground(ExpertDevTheme.TEXT_MUTED);
        areaHistorico.setEditable(false);
        areaHistorico.setLineWrap(true);
        areaHistorico.setWrapStyleWord(true);
        areaHistorico.setBorder(new EmptyBorder(10, 12, 10, 12));
        areaHistorico.setText("(historico sera preenchido apos o primeiro processamento)");

        JScrollPane scroll = new JScrollPane(areaHistorico);
        scroll.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTabPerformance() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ExpertDevTheme.BG_PANEL);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Row de inputs
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        inputRow.setOpaque(false);

        inputRow.add(buildFieldLabel("Estimativa Poker (h):"));
        campoEstimativaPoker = new JTextField(5);
        styleSmField(campoEstimativaPoker);
        campoEstimativaPoker.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { controller.salvarDadosPerformanceAtuais(); }
        });
        inputRow.add(campoEstimativaPoker);

        inputRow.add(buildFieldLabel("Sprint:"));
        campoSprint = new JTextField(5);
        styleSmField(campoSprint);
        campoSprint.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { controller.salvarDadosPerformanceAtuais(); }
        });
        inputRow.add(campoSprint);

        inputRow.add(buildFieldLabel("Complexidade:"));
        comboComplexidade = new JComboBox<String>(new String[]{"Baixa", "Media", "Alta"});
        comboComplexidade.setFont(ExpertDevTheme.FONT_BODY);
        comboComplexidade.setBackground(ExpertDevTheme.SURFACE_ALT);
        comboComplexidade.addActionListener(e -> controller.salvarDadosPerformanceAtuais());
        inputRow.add(comboComplexidade);

        // Painéis RTC + Tendência
        JPanel grid = new JPanel(new GridLayout(1, 2, 10, 0));
        grid.setOpaque(false);

        painelGrafico = buildInfoPanel("RTC ATUAL");
        painelTendencia = buildInfoPanel("TENDENCIA DE APRENDIZADO");
        grid.add(painelGrafico);
        grid.add(painelTendencia);

        // Botões
        btnIniciarScrum    = buildSmButton("Iniciar Scrum");
        btnFinalizarScrum  = buildSmButton("Finalizar Scrum");
        btnFinalizarExpertDev = buildSmButton("Finalizar ExpertDev");
        JButton btnExportar    = buildSmButton("Exportar ROI");

        btnIniciarScrum.addActionListener(e    -> controller.gerenciarAcaoPerformance("INICIAR_SCRUM"));
        btnFinalizarScrum.addActionListener(e  -> controller.gerenciarAcaoPerformance("FINALIZAR_SCRUM"));
        btnFinalizarExpertDev.addActionListener(e -> controller.gerenciarAcaoPerformance("FINALIZAR_EXPERTDEV"));
        btnExportar.addActionListener(e            -> controller.exportarRelatorioROI());

        lblGanhoProdutividade = new JLabel("Ganho: 0%");
        lblGanhoProdutividade.setFont(ExpertDevTheme.FONT_TITLE);
        lblGanhoProdutividade.setForeground(ExpertDevTheme.SUCCESS);

        JPanel acoes = new JPanel(new BorderLayout(8, 0));
        acoes.setOpaque(false);
        JPanel botoesAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        botoesAcoes.setOpaque(false);
        botoesAcoes.add(btnIniciarScrum);
        botoesAcoes.add(btnFinalizarScrum);
        botoesAcoes.add(btnFinalizarExpertDev);
        botoesAcoes.add(btnExportar);
        acoes.add(botoesAcoes, BorderLayout.WEST);
        acoes.add(lblGanhoProdutividade, BorderLayout.EAST);

        panel.add(inputRow, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        panel.add(acoes, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildInfoPanel(String headerText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1));
        p.setBackground(ExpertDevTheme.BG_PANEL);
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        h.setBackground(ExpertDevTheme.SURFACE_ALT);
        JLabel l = new JLabel(headerText);
        l.setFont(ExpertDevTheme.FONT_LABEL);
        l.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        h.add(l);
        p.add(h, BorderLayout.NORTH);
        return p;
    }

    private static Icon createTabIconForIndex(int index) {
        switch (index) {
            case 0:
                return createLinkIcon(14, ExpertDevTheme.TEXT_MUTED);
            case 1:
                return createDocumentIcon(14, ExpertDevTheme.TEXT_MUTED);
            case 2:
                return createClockIcon(14, ExpertDevTheme.TEXT_MUTED);
            case 3:
                return createChartIcon(14, ExpertDevTheme.TEXT_MUTED);
            default:
                return createSparkIcon(14, ExpertDevTheme.TEXT_MUTED);
        }
    }

    private static Icon createLinkIcon(final int size, final Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 1, y + 4, 6, 4, 3, 3);
                    g2.drawRoundRect(x + size - 7, y + 4, 6, 4, 3, 3);
                    g2.drawLine(x + 5, y + 6, x + size - 5, y + 6);
                } finally {
                    g2.dispose();
                }
            }
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }
        };
    }

    private static Icon createDocumentIcon(final int size, final Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 2, y + 1, size - 5, size - 3, 2, 2);
                    g2.drawLine(x + 4, y + 5, x + size - 6, y + 5);
                    g2.drawLine(x + 4, y + 8, x + size - 6, y + 8);
                } finally {
                    g2.dispose();
                }
            }
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }
        };
    }

    private static Icon createClockIcon(final int size, final Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(x + 1, y + 1, size - 3, size - 3);
                    g2.drawLine(x + size / 2, y + size / 2, x + size / 2, y + 4);
                    g2.drawLine(x + size / 2, y + size / 2, x + size - 4, y + size / 2);
                } finally {
                    g2.dispose();
                }
            }
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }
        };
    }

    private static Icon createChartIcon(final int size, final Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fillRect(x + 2, y + 8, 2, 4);
                    g2.fillRect(x + 6, y + 6, 2, 6);
                    g2.fillRect(x + 10, y + 3, 2, 9);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawLine(x + 1, y + 12, x + size - 1, y + 12);
                } finally {
                    g2.dispose();
                }
            }
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }
        };
    }

    private static Icon createSparkIcon(final int size, final Color color) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    Polygon p = new Polygon();
                    p.addPoint(x + size / 2, y + 1);
                    p.addPoint(x + size / 2 + 2, y + size / 2 - 1);
                    p.addPoint(x + size - 1, y + size / 2);
                    p.addPoint(x + size / 2 + 2, y + size / 2 + 1);
                    p.addPoint(x + size / 2, y + size - 1);
                    p.addPoint(x + size / 2 - 2, y + size / 2 + 1);
                    p.addPoint(x + 1, y + size / 2);
                    p.addPoint(x + size / 2 - 2, y + size / 2 - 1);
                    g2.fillPolygon(p);
                } finally {
                    g2.dispose();
                }
            }
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }
        };
    }

    // ── Config Bar ────────────────────────────────────────────────────────────

    private JPanel buildConfigBar() {
        JPanel config = new JPanel(new GridBagLayout());
        config.setBackground(ExpertDevTheme.WHITE);
        config.setBorder(new EmptyBorder(10, 16, 10, 16));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 3, 2, 3);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Modo | Provider | Perfil
        g.gridy = 0;
        g.gridx = 0; g.weightx = 0;
        config.add(buildConfigLabel("Modo:"), g);

        comboModoGeracao = new JComboBox<String>(new String[]{"LOCAL", "IA"});
        comboModoGeracao.setFont(ExpertDevTheme.FONT_BODY);
        comboModoGeracao.setSelectedItem(controller.modoGeracaoSelecionado);
        comboModoGeracao.addActionListener(e -> {
            controller.modoGeracaoSelecionado = String.valueOf(comboModoGeracao.getSelectedItem());
            ExpertDevConfig.salvarPreferenciaModoGeracao(controller.modoGeracaoSelecionado);
            ExpertDevConfig.salvarPreferenciaAiHabilitada(controller.isModoIaSelecionado());
            controller.atualizarEstadoOpcoesIa();
            controller.atualizarEstimativaIa();
        });
        g.gridx = 1; g.weightx = 0.3;
        config.add(comboModoGeracao, g);

        g.gridx = 2; g.weightx = 0;
        config.add(buildConfigLabel("Provider:"), g);

        comboProviderIa = new JComboBox<String>(new String[]{"openai", "claude"});
        comboProviderIa.setFont(ExpertDevTheme.FONT_BODY);
        comboProviderIa.setSelectedItem(controller.providerIaSelecionado);
        comboProviderIa.addActionListener(e -> {
            controller.providerIaSelecionado = controller.normalizarProvider(
                String.valueOf(comboProviderIa.getSelectedItem()));
            ExpertDevConfig.salvarConfiguracaoAiProvider(controller.providerIaSelecionado);
            controller.atualizarEstimativaIa();
        });
        g.gridx = 3; g.weightx = 0.3;
        config.add(comboProviderIa, g);

        g.gridx = 4; g.weightx = 0;
        config.add(buildConfigLabel("Perfil:"), g);

        comboPerfilPrompt = new JComboBox<String>(new String[]{"tecnico", "negocial"});
        comboPerfilPrompt.setFont(ExpertDevTheme.FONT_BODY);
        comboPerfilPrompt.setSelectedItem(controller.perfilPromptSelecionado);
        comboPerfilPrompt.addActionListener(e -> {
            controller.perfilPromptSelecionado = String.valueOf(comboPerfilPrompt.getSelectedItem());
            ExpertDevConfig.salvarPromptProfile(controller.perfilPromptSelecionado);
            controller.atualizarEstimativaIa();
        });
        g.gridx = 5; g.weightx = 0.2;
        config.add(comboPerfilPrompt, g);

        // Row 2: API Key | Limpar | Testar | Estimativa
        g.gridy = 1;
        g.gridx = 0; g.weightx = 0;
        config.add(buildConfigLabel("API Key:"), g);

        campoApiKey = new JPasswordField();
        campoApiKey.setFont(ExpertDevTheme.FONT_MONO_SM);
        campoApiKey.setBackground(ExpertDevTheme.SURFACE_ALT);
        g.gridx = 1; g.gridwidth = 2; g.weightx = 0.8;
        config.add(campoApiKey, g);
        g.gridwidth = 1;

        btnLimparApiKey = buildSmButton("Limpar");
        btnLimparApiKey.addActionListener(e -> controller.limparApiKey());
        g.gridx = 3; g.weightx = 0;
        config.add(btnLimparApiKey, g);

        btnTestarConexaoIa = buildSmButton("Testar IA");
        btnTestarConexaoIa.addActionListener(e -> controller.testarConexaoIa());
        g.gridx = 4;
        config.add(btnTestarConexaoIa, g);

        lblEstimativaIa = new JLabel("Estimativa IA: aguardando...");
        lblEstimativaIa.setFont(ExpertDevTheme.FONT_MONO_SM);
        lblEstimativaIa.setForeground(ExpertDevTheme.TEXT_MUTED);
        g.gridx = 5; g.weightx = 1;
        config.add(lblEstimativaIa, g);

        // Row 3: checkbox + aviso
        g.gridy = 2;
        chkSalvarApiKey = new JCheckBox("Salvar chave localmente");
        chkSalvarApiKey.setOpaque(false);
        chkSalvarApiKey.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        chkSalvarApiKey.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        chkSalvarApiKey.addActionListener(e -> {
            controller.salvarApiKeySelecionada = chkSalvarApiKey.isSelected();
        });
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        config.add(chkSalvarApiKey, g);
        g.gridwidth = 1;

        lblAvisoModoEconomicoIa = new JLabel("Modo economico de IA ativo.");
        lblAvisoModoEconomicoIa.setFont(ExpertDevTheme.FONT_BODY.deriveFont(11f));
        lblAvisoModoEconomicoIa.setForeground(ExpertDevTheme.WARNING);
        g.gridx = 3; g.gridwidth = 3; g.weightx = 1;
        config.add(lblAvisoModoEconomicoIa, g);
        g.gridwidth = 1;

        return config;
    }

    private JPanel buildStatusAndGenerate() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ExpertDevTheme.SURFACE_ALT);
        panel.setBorder(new MatteBorder(1, 0, 0, 0, ExpertDevTheme.BORDER));

        // Status bar
        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setStringPainted(true);
        barraProgresso.setString("Aguardando...");
        barraProgresso.setFont(ExpertDevTheme.FONT_MONO_SM);
        barraProgresso.setBackground(ExpertDevTheme.SURFACE_ALT);
        barraProgresso.setForeground(ExpertDevTheme.PRIMARY);
        barraProgresso.setBorderPainted(false);
        barraProgresso.setPreferredSize(new Dimension(0, 20));
        barraProgresso.setBorder(new EmptyBorder(4, 16, 4, 16));

        // Botão Gerar Prompt (gradiente)
        GradientButton btnGerar = new GradientButton(
            "Gerar Prompt",
            ExpertDevTheme.PRIMARY,
            ExpertDevTheme.BRAND_DARK
        );
        btnGerar.setPreferredSize(new Dimension(0, ExpertDevTheme.BTN_HEIGHT + 10));
        btnGerar.addActionListener(e -> controller.iniciarProcessamento());

        // Referência para GuiController (btnProcessar)
        btnProcessar = btnGerar;

        panel.add(barraProgresso, BorderLayout.NORTH);
        panel.add(btnGerar, BorderLayout.CENTER);
        return panel;
    }

    // ── Right Panel ───────────────────────────────────────────────────────────

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ExpertDevTheme.SURFACE_ALT);

        JSplitPane splitV = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildLogSection(), buildPromptSection());
        splitV.setDividerLocation(200);
        splitV.setDividerSize(4);
        splitV.setBorder(null);
        splitV.setBackground(ExpertDevTheme.SURFACE_ALT);

        panel.add(splitV, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLogSection() {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setBackground(ExpertDevTheme.SURFACE_ALT);
        section.setBorder(new EmptyBorder(14, 16, 8, 16));

        // Header azul
        JPanel header = buildSectionHeader("LOG DE EXECUCAO", ExpertDevTheme.INFO);
        section.add(header, BorderLayout.NORTH);

        painelLogCards = new JPanel();
        painelLogCards.setLayout(new BoxLayout(painelLogCards, BoxLayout.Y_AXIS));
        painelLogCards.setBackground(ExpertDevTheme.WHITE);

        scrollLog = new JScrollPane(painelLogCards);
        scrollLog.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1));
        scrollLog.setBackground(ExpertDevTheme.WHITE);
        scrollLog.getVerticalScrollBar().setUnitIncrement(16);
        scrollLog.setPreferredSize(new Dimension(0, 160));
        scrollLog.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        section.add(scrollLog, BorderLayout.CENTER);
        adicionarCardLog("Pronto para processar.");
        return section;
    }

    private JPanel buildPromptSection() {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setBackground(ExpertDevTheme.SURFACE_ALT);
        section.setBorder(new EmptyBorder(8, 16, 14, 16));

        // Header roxo + botões
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(buildSectionHeader("PROMPT GERADO", ExpertDevTheme.PREMIUM), BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        btnCopiarPrompt = buildSmButton("Copiar");
        btnCopiarPrompt.setEnabled(false);
        btnCopiarPrompt.addActionListener(e -> controller.copiarPromptParaClipboard());

        btnSalvar = buildSmButton("Salvar Arquivos");
        btnSalvar.setEnabled(false);
        btnSalvar.addActionListener(e -> controller.abrirDialogoSalvar());

        btns.add(btnCopiarPrompt);
        btns.add(btnSalvar);
        header.add(btns, BorderLayout.EAST);

        areaPrompt = new JTextArea();
        areaPrompt.setFont(ExpertDevTheme.FONT_MONO.deriveFont(12.5f));
        areaPrompt.setBackground(ExpertDevTheme.WHITE);
        areaPrompt.setForeground(ExpertDevTheme.TEXT_BODY);
        areaPrompt.setEditable(false);
        areaPrompt.setLineWrap(true);
        areaPrompt.setWrapStyleWord(true);
        areaPrompt.setBorder(new EmptyBorder(10, 12, 10, 12));
        areaPrompt.setText("O prompt aparecera aqui apos o processamento...");

        JScrollPane scroll = new JScrollPane(areaPrompt);
        scroll.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1));
        scroll.setBackground(ExpertDevTheme.WHITE);

        section.add(header, BorderLayout.NORTH);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    private JPanel buildSectionHeader(String text, Color accentColor) {
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(0, 0, 6, 0));

        // Dot colorido
        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillOval(0, 0, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(10, 10));

        JLabel lbl = new JLabel(text);
        lbl.setFont(ExpertDevTheme.FONT_LABEL);
        lbl.setForeground(ExpertDevTheme.TEXT_SECONDARY);

        h.add(dot);
        h.add(lbl);
        return h;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(ExpertDevTheme.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, ExpertDevTheme.BORDER),
            new EmptyBorder(5, 20, 5, 20)
        ));

        JLabel left = new JLabel("Expert Dev  •  Enterprise Ready  •  Apache POI + JSoup + PDFBox");
        left.setFont(ExpertDevTheme.FONT_BODY.deriveFont(11f));
        left.setForeground(ExpertDevTheme.TEXT_MUTED);

        JLabel right = new JLabel("A solution engineered by Marcus Dimitri");
        right.setFont(ExpertDevTheme.FONT_BOLD.deriveFont(11f));
        right.setForeground(ExpertDevTheme.BRAND_BLUE_MID);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    // ── Logo ─────────────────────────────────────────────────────────────────

    private JLabel buildLogoLabel(int targetHeight) {
        JLabel label = new JLabel("Expert Dev");
        label.setFont(ExpertDevTheme.FONT_HEADER);
        label.setForeground(ExpertDevTheme.BRAND_DARK);

        InputStream is = null;
        try {
            is = getClass().getResourceAsStream("/icons/logo_transparente.png");
            if (is != null) {
                BufferedImage raw = ImageIO.read(is);
                if (raw != null) {
                    int w = raw.getWidth() * targetHeight / raw.getHeight();
                    Image scaled = raw.getScaledInstance(w, targetHeight, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaled));
                    label.setText(null);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (is != null) { try { is.close(); } catch (Exception ignored) {} }
        }
        return label;
    }

    // ── Builders de componentes pequenos ─────────────────────────────────────

    private static JLabel buildSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ExpertDevTheme.FONT_LABEL);
        l.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        return l;
    }

    private static JLabel buildFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        l.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        return l;
    }

    private static JLabel buildConfigLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        l.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        return l;
    }

    static JButton buildSmButton(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(ExpertDevTheme.GRAY_100);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ExpertDevTheme.RADIUS_SM * 2, ExpertDevTheme.RADIUS_SM * 2);
                    }
                    g2.setColor(ExpertDevTheme.BORDER);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ExpertDevTheme.RADIUS_SM * 2, ExpertDevTheme.RADIUS_SM * 2);
                } finally { g2.dispose(); }
                super.paintComponent(g);
            }
        };
        btn.setFont(ExpertDevTheme.FONT_BODY.deriveFont(11f));
        btn.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 16, ExpertDevTheme.BTN_SM_HEIGHT));
        return btn;
    }

    private static JButton buildPrimarySmButton(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? ExpertDevTheme.PRIMARY_DARK : ExpertDevTheme.PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), ExpertDevTheme.RADIUS_SM * 2, ExpertDevTheme.RADIUS_SM * 2);
                } finally { g2.dispose(); }
                super.paintComponent(g);
            }
        };
        btn.setFont(ExpertDevTheme.FONT_BODY.deriveFont(11f));
        btn.setForeground(ExpertDevTheme.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 16, ExpertDevTheme.BTN_SM_HEIGHT));
        return btn;
    }

    private static void styleSmField(JTextField field) {
        field.setFont(ExpertDevTheme.FONT_BODY);
        field.setBackground(ExpertDevTheme.SURFACE_ALT);
        field.setForeground(ExpertDevTheme.TEXT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
    }
}

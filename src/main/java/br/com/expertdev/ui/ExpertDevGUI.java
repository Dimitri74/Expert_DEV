package br.com.expertdev.ui;

import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.io.DefaultTextFileWriter;
import br.com.expertdev.model.ExecucaoConsolidada;
import br.com.expertdev.model.ResultadoProcessamento;
import br.com.expertdev.model.RegistroAuditoria;
import br.com.expertdev.service.ImageDownloader;
import br.com.expertdev.service.AuditoriaService;
import br.com.expertdev.service.AiPromptGenerationService;
import br.com.expertdev.service.LocalPromptGenerationService;
import br.com.expertdev.service.ParallelUrlProcessor;
import br.com.expertdev.service.PdfDocumentBuilder;
import br.com.expertdev.service.PromptGenerationService;
import br.com.expertdev.service.PromptGenerator;
import br.com.expertdev.service.ResultConsolidator;
import br.com.expertdev.service.UrlParser;
import br.com.expertdev.service.WordDocumentBuilder;
import br.com.expertdev.service.WordDocumentReader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interface gráfica principal do Expert Dev.
 * Permite processar URLs ou fazer upload de um arquivo Word (.docx)
 * para gerar o prompt de contexto para IA.
 */
public class ExpertDevGUI extends JFrame {

    // ─── Cores base (tema escuro) ─────────────────────────────────────────────
    private static final Color COR_FUNDO_ESCURO       = new Color(18, 18, 30);
    private static final Color COR_PAINEL_ESCURO      = new Color(28, 28, 45);
    private static final Color COR_PAINEL_ALT_ESCURO  = new Color(35, 35, 55);
    private static final Color COR_DESTAQUE_ESCURO    = new Color(99, 102, 241);
    private static final Color COR_DESTAQUE2_ESCURO   = new Color(139, 92, 246);
    private static final Color COR_SUCESSO_ESCURO     = new Color(52, 211, 153);
    private static final Color COR_ERRO_ESCURO        = new Color(248, 113, 113);
    private static final Color COR_TEXTO_ESCURO       = new Color(226, 232, 240);
    private static final Color COR_TEXTO_SUAVE_ESCURO = new Color(148, 163, 184);
    private static final Color COR_BORDA_ESCURO       = new Color(51, 65, 85);

    // ─── Cores base (tema claro/fundo branco) ────────────────────────────────
    private static final Color COR_FUNDO_CLARO       = new Color(245, 247, 251);
    private static final Color COR_PAINEL_CLARO      = new Color(255, 255, 255);
    private static final Color COR_PAINEL_ALT_CLARO  = new Color(250, 251, 255);
    private static final Color COR_DESTAQUE_CLARO    = new Color(79, 70, 229);
    private static final Color COR_DESTAQUE2_CLARO   = new Color(124, 58, 237);
    private static final Color COR_SUCESSO_CLARO     = new Color(22, 163, 74);
    private static final Color COR_ERRO_CLARO        = new Color(220, 38, 38);
    private static final Color COR_TEXTO_CLARO       = new Color(15, 23, 42);
    private static final Color COR_TEXTO_SUAVE_CLARO = new Color(71, 85, 105);
    private static final Color COR_BORDA_CLARO       = new Color(203, 213, 225);

    // ─── Cores ativas (mudam conforme o tema) ─────────────────────────────────
    private Color COR_FUNDO;
    private Color COR_PAINEL;
    private Color COR_PAINEL_ALT;
    private Color COR_DESTAQUE;
    private Color COR_DESTAQUE2;
    private Color COR_SUCESSO;
    private Color COR_ERRO;
    private Color COR_TEXTO;
    private Color COR_TEXTO_SUAVE;
    private Color COR_BORDA;

    // ─── Fontes ───────────────────────────────────────────────────────────────
    private static final Font FONTE_TITULO      = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONTE_SUBTITULO   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONTE_ROTULO      = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONTE_NORMAL      = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONTE_MONO        = new Font("Consolas", Font.PLAIN, 12);
    private static final Font FONTE_BOTAO       = new Font("Segoe UI", Font.BOLD, 13);
    private static final int HEADER_LOGO_LARGURA = 320;
    private static final int HEADER_LOGO_ALTURA = 74;

    // ─── Componentes principais ───────────────────────────────────────────────
    private JTabbedPane abas;
    private JTextArea areaUrls;
    private JLabel labelArquivoWord;
    private JTextArea areaPreviewWord;
    private JTextArea areaLog;
    private JTextArea areaPrompt;
    private JProgressBar barraProgresso;
    private JButton btnProcessar;
    private JButton btnCopiarPrompt;
    private JButton btnSalvar;
    private JComboBox<String> comboModoGeracao;
    private JComboBox<String> comboProviderIa;
    private JComboBox<String> comboPerfilPrompt;
    private JPasswordField campoApiKey;
    private JCheckBox chkSalvarApiKey;
    private JButton btnTestarConexaoIa;
    private JButton btnLimparApiKey;
    private JLabel lblAvisoModoEconomicoIa;
    private JLabel lblEstimativaIa;
    private File arquivoWordSelecionado;
    private boolean temaClaroAtivo;
    private JTextField campoRTC;
    private JTextField campoUC;
    private JTextArea areaHistorico;
    private AuditoriaService auditoriaService;
    private long ultimoRegistroAuditoriaId = -1;
    private String promptComAuditoria = "";  // Para guardar prompt com auditoria
    private String modoGeracaoSelecionado = "LOCAL";
    private String providerIaSelecionado = "openai";
    private String perfilPromptSelecionado = "tecnico";
    private boolean salvarApiKeySelecionada;
    private String apiKeyDigitada = "";

    public ExpertDevGUI() {
        auditoriaService = new AuditoriaService();
        ExpertDevConfig configUi = ExpertDevConfig.carregar();
        aplicarTema(configUi.isTemaClaroPadrao());
        modoGeracaoSelecionado = configUi.getUiModoGeracao().equalsIgnoreCase("IA") ? "IA" : "LOCAL";
        providerIaSelecionado = normalizarProvider(configUi.getAiProvider());
        perfilPromptSelecionado = configUi.getPromptProfile();
        configurarJanela();
        construirInterface();
        preencherConfigIaInicial(configUi);
        atualizarEstadoOpcoesIa();
        setVisible(true);
    }

    private void aplicarTema(boolean claro) {
        temaClaroAtivo = claro;
        if (claro) {
            COR_FUNDO = COR_FUNDO_CLARO;
            COR_PAINEL = COR_PAINEL_CLARO;
            COR_PAINEL_ALT = COR_PAINEL_ALT_CLARO;
            COR_DESTAQUE = COR_DESTAQUE_CLARO;
            COR_DESTAQUE2 = COR_DESTAQUE2_CLARO;
            COR_SUCESSO = COR_SUCESSO_CLARO;
            COR_ERRO = COR_ERRO_CLARO;
            COR_TEXTO = COR_TEXTO_CLARO;
            COR_TEXTO_SUAVE = COR_TEXTO_SUAVE_CLARO;
            COR_BORDA = COR_BORDA_CLARO;
        } else {
            COR_FUNDO = COR_FUNDO_ESCURO;
            COR_PAINEL = COR_PAINEL_ESCURO;
            COR_PAINEL_ALT = COR_PAINEL_ALT_ESCURO;
            COR_DESTAQUE = COR_DESTAQUE_ESCURO;
            COR_DESTAQUE2 = COR_DESTAQUE2_ESCURO;
            COR_SUCESSO = COR_SUCESSO_ESCURO;
            COR_ERRO = COR_ERRO_ESCURO;
            COR_TEXTO = COR_TEXTO_ESCURO;
            COR_TEXTO_SUAVE = COR_TEXTO_SUAVE_ESCURO;
            COR_BORDA = COR_BORDA_ESCURO;
        }
    }

    // ─── Configuração da Janela ────────────────────────────────────────────────

    private void configurarJanela() {
        setTitle("Expert Dev — Gerador de Contexto para IA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 780);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
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
        cabecalho.setBackground(COR_PAINEL);
        cabecalho.setBorder(new EmptyBorder(20, 28, 16, 28));

        JPanel textosPanel = new JPanel();
        textosPanel.setLayout(new BoxLayout(textosPanel, BoxLayout.Y_AXIS));
        textosPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Expert Dev");
        Icon logoProjeto = carregarLogoProjeto(HEADER_LOGO_LARGURA, HEADER_LOGO_ALTURA);
        if (logoProjeto != null) {
            lblTitulo.setText("");
            lblTitulo.setIcon(logoProjeto);
            lblTitulo.setPreferredSize(new Dimension(HEADER_LOGO_LARGURA, HEADER_LOGO_ALTURA));
        } else {
            lblTitulo.setIcon(criarIconeJava(22));
            lblTitulo.setIconTextGap(8);
        }
        lblTitulo.setFont(FONTE_TITULO);
        lblTitulo.setForeground(COR_DESTAQUE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Automatizador inteligente de contexto e gerador de prompts para IA ");
        lblSubtitulo.setFont(FONTE_SUBTITULO);
        lblSubtitulo.setForeground(COR_TEXTO_SUAVE);
        lblSubtitulo.setBorder(new EmptyBorder(2, 2, 0, 0));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        textosPanel.add(lblTitulo);
        textosPanel.add(Box.createVerticalStrut(4));
        textosPanel.add(lblSubtitulo);
        cabecalho.add(textosPanel, BorderLayout.WEST);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        direita.setOpaque(false);

        JCheckBox chkTemaClaro = new JCheckBox("Fundo branco");
        chkTemaClaro.setSelected(temaClaroAtivo);
        chkTemaClaro.setBackground(COR_PAINEL);
        chkTemaClaro.setForeground(COR_TEXTO);
        chkTemaClaro.setFocusPainted(false);
        chkTemaClaro.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkTemaClaro.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                alternarTema(chkTemaClaro.isSelected());
            }
        });

        JLabel lblVersao = new JLabel("v 2.0");
        lblVersao.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVersao.setForeground(COR_DESTAQUE2);
        lblVersao.setHorizontalAlignment(SwingConstants.RIGHT);

        direita.add(chkTemaClaro);
        direita.add(lblVersao);
        cabecalho.add(direita, BorderLayout.EAST);

        // Linha separadora inferior
        JSeparator sep = new JSeparator();
        sep.setForeground(COR_BORDA);
        sep.setBackground(COR_BORDA);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COR_PAINEL);
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
        split.setBackground(COR_FUNDO);
        split.setForeground(COR_BORDA);
        split.setBorder(null);
        return split;
    }

    // ─── Painel de Entrada (esquerdo) ─────────────────────────────────────────

    private JPanel criarPainelEntrada() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(16, 16, 0, 8));

        // Painel de auditoria (RTC + UC)
        painel.add(criarPainelAuditoria(), BorderLayout.NORTH);

        abas = new JTabbedPane();
        estilizarAbas(abas);
        abas.addTab("  🌐  Via URLs  ", criarAbaUrls());
        abas.addTab("  📄  Upload Word  ", criarAbaUpload());
        abas.addTab("  📋  Histórico  ", criarAbaHistorico());
        abas.addChangeListener(e -> atualizarEstimativaIa());
        painel.add(abas, BorderLayout.CENTER);

        painel.add(criarPainelAcoes(), BorderLayout.SOUTH);
        return painel;
    }

    private void estilizarAbas(JTabbedPane abas) {
        abas.setBackground(COR_PAINEL);
        abas.setForeground(COR_TEXTO);
        abas.setFont(FONTE_ROTULO);
        abas.setOpaque(true);
        UIManager.put("TabbedPane.selected", COR_PAINEL_ALT);
        UIManager.put("TabbedPane.contentAreaColor", COR_PAINEL_ALT);
    }

    private JPanel criarPainelAuditoria() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(COR_PAINEL_ALT);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                new EmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblRTC = criarRotulo("RTC:");
        lblRTC.setFont(FONTE_ROTULO);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        painel.add(lblRTC, gbc);

        campoRTC = new JTextField();
        campoRTC.setFont(FONTE_NORMAL);
        campoRTC.setToolTipText("Ex: 256421");
        campoRTC.setBackground(COR_FUNDO);
        campoRTC.setForeground(COR_TEXTO);
        campoRTC.setCaretColor(COR_DESTAQUE);
        campoRTC.setSelectionColor(COR_DESTAQUE);
        campoRTC.setSelectedTextColor(Color.WHITE);
        campoRTC.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                new EmptyBorder(6, 8, 6, 8)
        ));
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        painel.add(campoRTC, gbc);

        JLabel lblUC = criarRotulo("Caso de Uso:");
        lblUC.setFont(FONTE_ROTULO);
        gbc.gridx = 2;
        gbc.weightx = 0;
        painel.add(lblUC, gbc);

        campoUC = new JTextField();
        campoUC.setFont(FONTE_NORMAL);
        campoUC.setToolTipText("Ex: UC01 – Registrar Usuário");
        campoUC.setBackground(COR_FUNDO);
        campoUC.setForeground(COR_TEXTO);
        campoUC.setCaretColor(COR_DESTAQUE);
        campoUC.setSelectionColor(COR_DESTAQUE);
        campoUC.setSelectedTextColor(Color.WHITE);
        campoUC.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA),
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
        painel.setBackground(COR_PAINEL_ALT);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel instrucao = criarRotulo(
                "Cole uma ou mais URLs (uma por linha ou separadas por vírgula):");
        painel.add(instrucao, BorderLayout.NORTH);

        areaUrls = new JTextArea();
        areaUrls.setFont(FONTE_MONO);
        areaUrls.setBackground(COR_FUNDO);
        areaUrls.setForeground(COR_TEXTO);
        areaUrls.setCaretColor(COR_DESTAQUE);
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

        JScrollPane scroll = criarScrollPane(areaUrls);
        painel.add(scroll, BorderLayout.CENTER);

        // Botão de limpar
        JPanel botoesUrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoesUrl.setOpaque(false);
        JButton btnLimpar = criarBotaoSecundario("✖  Limpar");
        btnLimpar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                areaUrls.setText("");
                if (campoRTC != null) {
                    campoRTC.setText("");
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
        painel.setBackground(COR_PAINEL_ALT);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Topo: instrução + botão selecionar
        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setOpaque(false);

        JLabel instrucao = criarRotulo("Selecione um arquivo Word (.docx) com as regras de negócio:");
        topo.add(instrucao, BorderLayout.NORTH);

        JPanel seletorPanel = new JPanel(new BorderLayout(10, 0));
        seletorPanel.setOpaque(false);
        seletorPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        labelArquivoWord = new JLabel("Nenhum arquivo selecionado");
        labelArquivoWord.setFont(FONTE_MONO);
        labelArquivoWord.setForeground(COR_TEXTO_SUAVE);
        labelArquivoWord.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                new EmptyBorder(8, 12, 8, 12)));
        labelArquivoWord.setBackground(COR_FUNDO);
        labelArquivoWord.setOpaque(true);

        JButton btnSelecionar = criarBotaoPrimario("📂  Selecionar");
        btnSelecionar.setPreferredSize(new Dimension(140, 38));
        btnSelecionar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selecionarArquivoWord();
            }
        });

        seletorPanel.add(labelArquivoWord, BorderLayout.CENTER);
        seletorPanel.add(btnSelecionar, BorderLayout.EAST);
        topo.add(seletorPanel, BorderLayout.CENTER);
        painel.add(topo, BorderLayout.NORTH);

        // Área de preview do conteúdo Word
        JLabel lblPreview = criarRotulo("Pré-visualização do conteúdo:");
        lblPreview.setBorder(new EmptyBorder(12, 0, 4, 0));

        areaPreviewWord = new JTextArea();
        areaPreviewWord.setFont(FONTE_MONO);
        areaPreviewWord.setBackground(COR_FUNDO);
        areaPreviewWord.setForeground(new Color(180, 190, 210));
        areaPreviewWord.setEditable(false);
        areaPreviewWord.setLineWrap(true);
        areaPreviewWord.setWrapStyleWord(true);
        areaPreviewWord.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaPreviewWord.setText("(selecione um arquivo .docx para visualizar o conteúdo aqui)");

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(lblPreview, BorderLayout.NORTH);
        centro.add(criarScrollPane(areaPreviewWord), BorderLayout.CENTER);
        painel.add(centro, BorderLayout.CENTER);

        return painel;
    }

    /** Aba para exibição do histórico */
    private JPanel criarAbaHistorico() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(COR_PAINEL_ALT);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel instrucao = criarRotulo("Histórico de processamentos (últimos 100):");
        instrucao.setBorder(new EmptyBorder(0, 0, 8, 0));
        painel.add(instrucao, BorderLayout.NORTH);

        areaHistorico = new JTextArea();
        areaHistorico.setFont(FONTE_MONO);
        areaHistorico.setBackground(COR_FUNDO);
        areaHistorico.setForeground(COR_TEXTO_SUAVE);
        areaHistorico.setEditable(false);
        areaHistorico.setLineWrap(true);
        areaHistorico.setWrapStyleWord(true);
        areaHistorico.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaHistorico.setText("(histórico será preenchido após o primeiro processamento)");
        painel.add(criarScrollPane(areaHistorico), BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoes.setOpaque(false);
        JButton btnAtualizarHistorico = criarBotaoSecundario("🔄  Atualizar");
        btnAtualizarHistorico.addActionListener(e -> atualizarAbaHistorico());
        botoes.add(btnAtualizarHistorico);
        painel.add(botoes, BorderLayout.SOUTH);

        return painel;
    }

    private void atualizarAbaHistorico() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                java.util.List<RegistroAuditoria> registros = auditoriaService.obterTodos();
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
        barraProgresso.setFont(FONTE_NORMAL);
        barraProgresso.setBackground(COR_PAINEL);
        barraProgresso.setForeground(COR_DESTAQUE);
        barraProgresso.setBorderPainted(false);
        barraProgresso.setPreferredSize(new Dimension(0, 22));
        topoAcoes.add(barraProgresso, BorderLayout.SOUTH);

        painel.add(topoAcoes, BorderLayout.NORTH);

        btnProcessar = criarBotaoAcao("▶  Gerar Prompt");
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
        painel.setBackground(COR_PAINEL_ALT);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                new EmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblModo = criarRotulo("Modo:");
        lblModo.setFont(FONTE_SUBTITULO);
        lblModo.setIcon(criarIconeIa(14));
        lblModo.setIconTextGap(5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        painel.add(lblModo, gbc);

        comboModoGeracao = new JComboBox<String>(new String[]{"LOCAL", "IA"});
        comboModoGeracao.setSelectedItem(modoGeracaoSelecionado);
        comboModoGeracao.setFont(FONTE_NORMAL);
        final Icon iconIaCombo = criarIconeIa(14);
        final Icon iconLocalCombo = criarIconeLocal(14);
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
                modoGeracaoSelecionado = String.valueOf(comboModoGeracao.getSelectedItem());
                ExpertDevConfig.salvarPreferenciaModoGeracao(modoGeracaoSelecionado);
                ExpertDevConfig.salvarPreferenciaAiHabilitada(isModoIaSelecionado());
                atualizarEstadoOpcoesIa();
                atualizarEstimativaIa();
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        painel.add(comboModoGeracao, gbc);

        JLabel lblPerfil = criarRotulo("Perfil:");
        lblPerfil.setFont(FONTE_SUBTITULO);
        gbc.gridx = 4;
        gbc.weightx = 0;
        painel.add(lblPerfil, gbc);

        comboPerfilPrompt = new JComboBox<String>(new String[]{"tecnico", "negocial"});
        comboPerfilPrompt.setSelectedItem(perfilPromptSelecionado);
        comboPerfilPrompt.setFont(FONTE_NORMAL);
        comboPerfilPrompt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                perfilPromptSelecionado = String.valueOf(comboPerfilPrompt.getSelectedItem());
                ExpertDevConfig.salvarPromptProfile(perfilPromptSelecionado);
                atualizarEstimativaIa();
            }
        });
        gbc.gridx = 5;
        gbc.weightx = 0.2;
        painel.add(comboPerfilPrompt, gbc);

        JLabel lblProvider = criarRotulo("Provider:");
        lblProvider.setFont(FONTE_SUBTITULO);
        lblProvider.setIcon(criarIconeIa(14));
        lblProvider.setIconTextGap(5);
        gbc.gridx = 2;
        gbc.weightx = 0;
        painel.add(lblProvider, gbc);

        comboProviderIa = new JComboBox<String>(new String[]{"openai", "claude"});
        comboProviderIa.setSelectedItem(providerIaSelecionado);
        comboProviderIa.setFont(FONTE_NORMAL);
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
                label.setIcon("claude".equalsIgnoreCase(provider) ? criarIconeClaude(14) : criarIconeIa(14));
                label.setIconTextGap(6);
                return label;
            }
        });
        comboProviderIa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                providerIaSelecionado = normalizarProvider(String.valueOf(comboProviderIa.getSelectedItem()));
                ExpertDevConfig.salvarConfiguracaoAiProvider(providerIaSelecionado);
                atualizarEstimativaIa();
            }
        });
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        painel.add(comboProviderIa, gbc);

        JLabel lblApi = criarRotulo("API Key:");
        lblApi.setFont(FONTE_SUBTITULO);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        painel.add(lblApi, gbc);

        campoApiKey = new JPasswordField();
        campoApiKey.setFont(FONTE_NORMAL);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        painel.add(campoApiKey, gbc);

        btnLimparApiKey = criarBotaoSecundario("Limpar");
        btnLimparApiKey.setFont(FONTE_SUBTITULO);
        btnLimparApiKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                limparApiKey();
            }
        });
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        painel.add(btnLimparApiKey, gbc);

        btnTestarConexaoIa = criarBotaoSecundario("Testar IA");
        btnTestarConexaoIa.setFont(FONTE_SUBTITULO);
        btnTestarConexaoIa.setIcon(criarIconeIa(14));
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
        chkSalvarApiKey.setForeground(COR_TEXTO_SUAVE);
        chkSalvarApiKey.setFont(FONTE_SUBTITULO);
        chkSalvarApiKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                salvarApiKeySelecionada = chkSalvarApiKey.isSelected();
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        painel.add(chkSalvarApiKey, gbc);

        lblAvisoModoEconomicoIa = new JLabel("Modo econômico de IA ativo: contexto reduzido e menos tokens.");
        lblAvisoModoEconomicoIa.setFont(FONTE_SUBTITULO);
        lblAvisoModoEconomicoIa.setForeground(COR_DESTAQUE2);
        lblAvisoModoEconomicoIa.setIcon(criarIconeEconomico(14));
        lblAvisoModoEconomicoIa.setIconTextGap(5);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        painel.add(lblAvisoModoEconomicoIa, gbc);

        lblEstimativaIa = new JLabel("Estimativa IA: aguardando contexto...");
        lblEstimativaIa.setFont(FONTE_SUBTITULO);
        lblEstimativaIa.setForeground(COR_TEXTO_SUAVE);
        lblEstimativaIa.setIcon(criarIconeEconomico(14));
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
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(16, 8, 0, 16));

        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                criarPainelLog(), criarPainelPrompt());
        splitVertical.setDividerLocation(200);
        splitVertical.setDividerSize(6);
        splitVertical.setBackground(COR_FUNDO);
        splitVertical.setBorder(null);

        painel.add(splitVertical, BorderLayout.CENTER);
        return painel;
    }

    /** Painel de log de execução */
    private JPanel criarPainelLog() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBackground(COR_FUNDO);

        JLabel lbl = criarRotulo("📋  Log de Execução");
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        painel.add(lbl, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setFont(FONTE_MONO);
        areaLog.setBackground(temaClaroAtivo ? Color.WHITE : new Color(10, 10, 18));
        areaLog.setForeground(COR_SUCESSO);
        areaLog.setEditable(false);
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);
        areaLog.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaLog.setText("Pronto para processar.\n");

        painel.add(criarScrollPane(areaLog), BorderLayout.CENTER);
        return painel;
    }

    /** Painel com o prompt gerado e botões de ação */
    private JPanel criarPainelPrompt() {
        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Cabeçalho do prompt
        JPanel cabecalhoPrompt = new JPanel(new BorderLayout());
        cabecalhoPrompt.setOpaque(false);

        JLabel lbl = criarRotulo("Prompt Gerado");
        lbl.setIcon(criarIconeIa(14));
        lbl.setIconTextGap(6);
        cabecalhoPrompt.add(lbl, BorderLayout.WEST);

        JPanel botoesSaida = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botoesSaida.setOpaque(false);

        btnCopiarPrompt = criarBotaoSecundario("📋  Copiar");
        btnCopiarPrompt.setEnabled(false);
        btnCopiarPrompt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copiarPromptParaClipboard();
            }
        });

        btnSalvar = criarBotaoSecundario("💾  Salvar Arquivos");
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
        areaPrompt.setFont(FONTE_MONO);
        areaPrompt.setBackground(COR_PAINEL);
        areaPrompt.setForeground(COR_TEXTO);
        areaPrompt.setEditable(false);
        areaPrompt.setLineWrap(true);
        areaPrompt.setWrapStyleWord(true);
        areaPrompt.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaPrompt.setText("O prompt aparecerá aqui após o processamento...");

        painel.add(criarScrollPane(areaPrompt), BorderLayout.CENTER);
        return painel;
    }

    /** Rodapé com status */
    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(COR_PAINEL);
        rodape.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel lblStatus = new JLabel("Expert Dev  •  Java 8 Core  •  Apache POI + JSoup + PDFBox");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(COR_TEXTO_SUAVE);
        rodape.add(lblStatus, BorderLayout.WEST);

        JLabel lblAssinatura = new JLabel("Idealizado e Desenvolvido por Marcus Dimitri");
        lblAssinatura.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAssinatura.setForeground(COR_DESTAQUE2);
        lblAssinatura.setHorizontalAlignment(SwingConstants.RIGHT);
        rodape.add(lblAssinatura, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(COR_BORDA);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COR_PAINEL);
        wrapper.add(sep, BorderLayout.NORTH);
        wrapper.add(rodape, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Lógica de Processamento ───────────────────────────────────────────────

    private void selecionarArquivoWord() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar arquivo Word (.docx)");
        chooser.setFileFilter(new FileNameExtensionFilter("Documentos Word (*.docx)", "docx"));
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            arquivoWordSelecionado = chooser.getSelectedFile();
            labelArquivoWord.setText(arquivoWordSelecionado.getAbsolutePath());
            labelArquivoWord.setForeground(COR_SUCESSO);
            carregarPreviewWord();
        }
    }

    private void carregarPreviewWord() {
        areaPreviewWord.setText("Carregando pré-visualização...");
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            protected String doInBackground() {
                WordDocumentReader reader = new WordDocumentReader();
                ResultadoProcessamento r = reader.ler(arquivoWordSelecionado);
                if (r.isSucesso()) {
                    String texto = r.getTextoExtraido();
                    return texto.length() > 3000
                            ? texto.substring(0, 3000) + "\n\n[... conteúdo truncado para pré-visualização ...]"
                            : texto;
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

    private void iniciarProcessamento() {
        int abaSelecionada = abas.getSelectedIndex();
        boolean viaUrl = abaSelecionada == 0;

        // Validação crítica: RTC obrigatório para rastreabilidade/auditoria
        String rtcInformado = campoRTC != null ? campoRTC.getText().trim() : "";
        if (rtcInformado.isEmpty()) {
            mostrarErro("Informe o número do RTC antes de processar. Este campo é obrigatório.");
            if (campoRTC != null) {
                campoRTC.requestFocusInWindow();
            }
            return;
        }

        // Validações
        if (viaUrl) {
            String textoUrls = areaUrls.getText().trim();
            if (textoUrls.isEmpty()) {
                mostrarErro("Informe pelo menos uma URL para processar.");
                return;
            }
        } else {
            if (arquivoWordSelecionado == null || !arquivoWordSelecionado.exists()) {
                mostrarErro("Selecione um arquivo Word (.docx) antes de processar.");
                return;
            }
        }

        // Desabilita controles durante o processamento
        btnProcessar.setEnabled(false);
        btnCopiarPrompt.setEnabled(false);
        btnSalvar.setEnabled(false);
        areaLog.setText("");
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
                String modoGeracao = modoGeracaoSelecionado;
                String provider = providerIaSelecionado;

                publish("→ RTC: " + (rtcNumero.isEmpty() ? "(não informado)" : rtcNumero));
                publish("→ UC: " + (ucCodigo.isEmpty() ? "(não informado)" : ucCodigo));

                RegistroAuditoria regAuditoria = new RegistroAuditoria(rtcNumero, ucCodigo, ucCodigo,
                        modoGeracao, provider, "");
                ultimoRegistroAuditoriaId = auditoriaService.inserir(regAuditoria);

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
                    publish("→ Arquivo: " + arquivoWordSelecionado.getName());
                    WordDocumentReader reader = new WordDocumentReader();
                    ResultadoProcessamento r = reader.ler(arquivoWordSelecionado);
                    if (!r.isSucesso()) {
                        throw new RuntimeException("Falha ao ler o Word: " + r.getErro());
                    }
                    publish("→ Documento Word lido com sucesso.");
                    publish("→ Caracteres extraídos: " + (r.getTextoExtraido() != null
                            ? r.getTextoExtraido().length() : 0));
                    resultados = Collections.singletonList(r);
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
                                    arquivoWordSaida, arquivoPdf);
                } catch (RuntimeException e) {
                    if (modoIa) {
                        publish("⚠ IA indisponível. Aplicando fallback para modo local...");
                        PromptGenerationService fallbackLocal = new LocalPromptGenerationService(
                                new PromptGenerator(perfilPromptSelecionado));
                        execucao = new ResultConsolidator(fallbackLocal)
                                .consolidar(resultados, urlsReferencia.size(), inicio, Instant.now(),
                                        arquivoWordSaida, arquivoPdf);
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
                if (ultimoRegistroAuditoriaId > 0) {
                    auditoriaService.atualizar(ultimoRegistroAuditoriaId, "CONCLUIDO", promptFinal);
                }

                // Guardar prompt com auditoria em variável de instância para exibir no done()
                promptComAuditoria = promptFinal;

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
                    areaLog.append(msg + "\n");
                }
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
            }

            protected void done() {
                barraProgresso.setIndeterminate(false);
                btnProcessar.setEnabled(true);
                try {
                    ExecucaoConsolidada execucao = get();
                    // Exibe o prompt que ja inclui auditoria (RTC + UC)
                    areaPrompt.setText(promptComAuditoria);
                    areaPrompt.setCaretPosition(0);
                    atualizarAbaHistorico();
                    areaPrompt.setCaretPosition(0);
                    barraProgresso.setValue(100);
                    barraProgresso.setString("Concluído com sucesso!");
                    barraProgresso.setForeground(COR_SUCESSO);
                    btnCopiarPrompt.setEnabled(true);
                    btnSalvar.setEnabled(true);
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    areaLog.append("❌ ERRO: " + msg + "\n");
                    areaPrompt.setText("Ocorreu um erro durante o processamento.\nVerifique o log.");
                    barraProgresso.setValue(0);
                    barraProgresso.setString("Erro!");
                    barraProgresso.setForeground(COR_ERRO);
                    mostrarErro("Erro no processamento:\n" + msg);
                }
            }
        };

        worker.execute();
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
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void alternarTema(boolean claro) {
        ExpertDevConfig.salvarPreferenciaTemaClaro(claro);

        String urlsTexto = areaUrls != null ? areaUrls.getText() : "";
        String previewTexto = areaPreviewWord != null ? areaPreviewWord.getText() : "";
        String logTexto = areaLog != null ? areaLog.getText() : "";
        String promptTexto = areaPrompt != null ? areaPrompt.getText() : "";
        String apiKeyTexto = campoApiKey != null ? new String(campoApiKey.getPassword()) : "";
        boolean salvarKey = chkSalvarApiKey != null && chkSalvarApiKey.isSelected();
        String modoSelecionado = comboModoGeracao != null
                ? String.valueOf(comboModoGeracao.getSelectedItem())
                : modoGeracaoSelecionado;
        int abaSelecionada = abas != null ? abas.getSelectedIndex() : 0;

        aplicarTema(claro);

        getContentPane().removeAll();
        getContentPane().setBackground(COR_FUNDO);
        construirInterface();

        if (areaUrls != null) {
            areaUrls.setText(urlsTexto);
        }
        if (areaPreviewWord != null && previewTexto != null && !previewTexto.trim().isEmpty()) {
            areaPreviewWord.setText(previewTexto);
        }
        if (areaLog != null) {
            areaLog.setText(logTexto);
        }
        if (areaPrompt != null) {
            areaPrompt.setText(promptTexto);
        }
        if (comboModoGeracao != null) {
            comboModoGeracao.setSelectedItem(modoSelecionado);
            modoGeracaoSelecionado = modoSelecionado;
        }
        if (comboProviderIa != null) {
            comboProviderIa.setSelectedItem(providerIaSelecionado);
        }
        if (comboPerfilPrompt != null) {
            comboPerfilPrompt.setSelectedItem(perfilPromptSelecionado);
        }
        if (campoApiKey != null) {
            campoApiKey.setText(apiKeyTexto);
        }
        if (chkSalvarApiKey != null) {
            chkSalvarApiKey.setSelected(salvarKey);
            salvarApiKeySelecionada = salvarKey;
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
            comboModoGeracao.setSelectedItem(modoGeracaoSelecionado);
        }
        if (comboProviderIa != null) {
            comboProviderIa.setSelectedItem(providerIaSelecionado);
        }
        if (comboPerfilPrompt != null) {
            comboPerfilPrompt.setSelectedItem(perfilPromptSelecionado);
        }
        if (campoApiKey != null) {
            campoApiKey.setText(configUi.getAiApiKeyResolvida());
            apiKeyDigitada = new String(campoApiKey.getPassword());
        }
        if (chkSalvarApiKey != null) {
            boolean possuiKeySalva = configUi.getAiApiKeyResolvida() != null
                    && !configUi.getAiApiKeyResolvida().trim().isEmpty();
            chkSalvarApiKey.setSelected(possuiKeySalva);
            salvarApiKeySelecionada = possuiKeySalva;
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
            return "IA".equalsIgnoreCase(modoGeracaoSelecionado);
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
            return new LocalPromptGenerationService(new PromptGenerator(perfilPromptSelecionado));
        }

        String apiKeyInformada = campoApiKey != null ? new String(campoApiKey.getPassword()).trim() : "";
        String apiKey = apiKeyInformada.isEmpty() ? config.getAiApiKeyResolvida() : apiKeyInformada;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Modo IA selecionado, mas nenhuma API Key foi informada.");
        }

        if (salvarApiKeySelecionada && !apiKeyInformada.isEmpty()) {
            ExpertDevConfig.salvarApiKeyIA(apiKeyInformada);
        }

        return new AiPromptGenerationService(
                providerIaSelecionado,
                config.getAiEndpoint(),
                config.getAiModel(),
                apiKey,
                config.getAiTimeoutMs(),
                config.getAiTemperature(),
                config.getAiMaxTokens(),
                config.getAiMaxContextChars(),
                config.isAiModoEconomico(),
                perfilPromptSelecionado
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
        areaLog.append("→ Testando conexão com IA...\n");

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                AiPromptGenerationService ia = new AiPromptGenerationService(
                        providerIaSelecionado,
                        config.getAiEndpoint(),
                        config.getAiModel(),
                        apiKey,
                        config.getAiTimeoutMs(),
                        config.getAiTemperature(),
                        config.getAiMaxTokens(),
                        config.getAiMaxContextChars(),
                        config.isAiModoEconomico(),
                        perfilPromptSelecionado
                );
                ia.testarConexao();
                return null;
            }

            @Override
            protected void done() {
                btnTestarConexaoIa.setEnabled(true);
                try {
                    get();
                    areaLog.append("✓ Conexão com IA validada com sucesso.\n");
                    if (salvarApiKeySelecionada && !apiKeyInformada.isEmpty()) {
                        ExpertDevConfig.salvarApiKeyIA(apiKeyInformada);
                    }
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    areaLog.append("⚠ Falha no teste de IA: " + msg + "\n");
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
        apiKeyDigitada = "";
        salvarApiKeySelecionada = false;
        if (chkSalvarApiKey != null) {
            chkSalvarApiKey.setSelected(false);
        }
        ExpertDevConfig.salvarApiKeyIA("");
        areaLog.append("→ API Key de IA limpa da interface e da configuração local.\n");
    }

    private Icon carregarLogoProjeto(int largura, int altura) {
        BufferedImage logoOriginal = carregarImagemLogoPorTema();
        if (logoOriginal == null) {
            return null;
        }

        BufferedImage logoRecortado = recortarTransparencia(logoOriginal);
        BufferedImage logoAjustado = aplicarContrasteLogo(logoRecortado);

        int margemX = Math.max(8, (int) Math.round(largura * 0.035));
        int margemY = Math.max(6, (int) Math.round(altura * 0.08));
        int areaUtilLargura = Math.max(1, largura - margemX * 2);
        int areaUtilAltura = Math.max(1, altura - margemY * 2);

        int ow = Math.max(1, logoAjustado.getWidth());
        int oh = Math.max(1, logoAjustado.getHeight());
        double escala = Math.min((double) areaUtilLargura / ow, (double) areaUtilAltura / oh);
        int w = Math.max(1, (int) Math.round(ow * escala));
        int h = Math.max(1, (int) Math.round(oh * escala));

        BufferedImage canvas = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = margemX + (areaUtilLargura - w) / 2;
            int y = margemY + (areaUtilAltura - h) / 2;
            g2.drawImage(logoAjustado, x, y, w, h, null);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(canvas);
    }

    private BufferedImage carregarImagemLogoPorTema() {
        String[] candidatosTema = temaClaroAtivo
                ? new String[]{
                "/icons/logo_fundo_branco.png",
                "/icons/logo_transparente.png",
                "/icons/expertdev_logo_light.png",
                "/icons/expertdev_logo_horizontal.png",
                "/icons/expertdev_logo.png"
        }
                : new String[]{
                "/icons/logo_fundo_preto.png",
                "/icons/logo_transparente.png",
                "/icons/expertdev_logo_horizontal_dark.png",
                "/icons/expertdev_logo_dark.png",
                "/icons/expertdev_logo.png"
        };

        String[] fallbackGeral = new String[]{
                "/icons/logo_transparente.png",
                "/icons/logo_fundo_branco.png",
                "/icons/logo_fundo_preto.png",
                "/icons/expertdev_logo_light.png",
                "/icons/expertdev_logo_dark.png",
                "/icons/expertdev_logo_horizontal.png",
                "/icons/expertdev_logo_horizontal_dark.png",
                "/icons/expertdev_logo.png"
        };

        for (String caminho : candidatosTema) {
            BufferedImage imagem = carregarImagemRecurso(caminho);
            if (imagem != null) {
                return imagem;
            }
        }
        for (String caminho : fallbackGeral) {
            BufferedImage imagem = carregarImagemRecurso(caminho);
            if (imagem != null) {
                return imagem;
            }
        }
        return null;
    }

    private BufferedImage carregarImagemRecurso(String caminhoClasspath) {
        try {
            URL recurso = getClass().getResource(caminhoClasspath);
            if (recurso != null) {
                return ImageIO.read(recurso);
            }

            String nomeArquivo = caminhoClasspath.substring(caminhoClasspath.lastIndexOf('/') + 1);
            File[] candidatosLocais = new File[]{
                    new File("icons", nomeArquivo),
                    new File("src/main/resources/icons", nomeArquivo)
            };

            for (File candidato : candidatosLocais) {
                if (candidato.exists() && candidato.isFile()) {
                    return ImageIO.read(candidato);
                }
            }
        } catch (IOException ignored) {
            // Continua fallback para os proximos candidatos.
        }
        return null;
    }

    private BufferedImage recortarTransparencia(BufferedImage origem) {
        Rectangle bounds = encontrarBoundsPorAlpha(origem, 12);
        if (bounds == null || ocupaImagemInteira(bounds, origem)) {
            // Fallback para logos opacas com fundo uniforme (branco, preto, cinza etc.).
            bounds = encontrarBoundsPorCorDeFundo(origem, 30);
        }

        if (bounds == null || ocupaImagemInteira(bounds, origem)) {
            return origem;
        }

        int padX = Math.max(2, (int) Math.round(bounds.width * 0.01));
        int padY = Math.max(2, (int) Math.round(bounds.height * 0.02));
        int sx = Math.max(0, bounds.x - padX);
        int sy = Math.max(0, bounds.y - padY);
        int ex = Math.min(origem.getWidth(), bounds.x + bounds.width + padX);
        int ey = Math.min(origem.getHeight(), bounds.y + bounds.height + padY);

        int w = Math.max(1, ex - sx);
        int h = Math.max(1, ey - sy);
        BufferedImage recorte = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = recorte.createGraphics();
        try {
            g2.drawImage(origem, 0, 0, w, h, sx, sy, ex, ey, null);
        } finally {
            g2.dispose();
        }
        return recorte;
    }

    private Rectangle encontrarBoundsPorAlpha(BufferedImage origem, int alphaMinimo) {
        int largura = origem.getWidth();
        int altura = origem.getHeight();
        int minX = largura;
        int minY = altura;
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int alpha = (origem.getRGB(x, y) >>> 24) & 0xFF;
                if (alpha > alphaMinimo) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return null;
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private Rectangle encontrarBoundsPorCorDeFundo(BufferedImage origem, int tolerancia) {
        int largura = origem.getWidth();
        int altura = origem.getHeight();
        int[] fundo = obterCorMediaDosCantos(origem);

        int minX = largura;
        int minY = altura;
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int argb = origem.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha <= 10) {
                    continue;
                }

                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;
                int distancia = Math.abs(r - fundo[0]) + Math.abs(g - fundo[1]) + Math.abs(b - fundo[2]);
                if (distancia > tolerancia) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return null;
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private int[] obterCorMediaDosCantos(BufferedImage origem) {
        int largura = origem.getWidth();
        int altura = origem.getHeight();
        int[] amostras = new int[]{
                origem.getRGB(0, 0),
                origem.getRGB(largura - 1, 0),
                origem.getRGB(0, altura - 1),
                origem.getRGB(largura - 1, altura - 1)
        };

        int somaR = 0;
        int somaG = 0;
        int somaB = 0;
        int pesoTotal = 0;

        for (int argb : amostras) {
            int a = (argb >>> 24) & 0xFF;
            int r = (argb >>> 16) & 0xFF;
            int g = (argb >>> 8) & 0xFF;
            int b = argb & 0xFF;
            int peso = Math.max(1, a);
            somaR += r * peso;
            somaG += g * peso;
            somaB += b * peso;
            pesoTotal += peso;
        }

        if (pesoTotal == 0) {
            return new int[]{0, 0, 0};
        }
        return new int[]{
                somaR / pesoTotal,
                somaG / pesoTotal,
                somaB / pesoTotal
        };
    }

    private boolean ocupaImagemInteira(Rectangle bounds, BufferedImage origem) {
        return bounds != null
                && bounds.x <= 0
                && bounds.y <= 0
                && bounds.width >= origem.getWidth()
                && bounds.height >= origem.getHeight();
    }

    private BufferedImage aplicarContrasteLogo(BufferedImage origem) {
        // Ajuste suave para preservar identidade da marca e melhorar legibilidade no header.
        float contraste = temaClaroAtivo ? 1.01f : 1.03f;
        int brilho = 0;
        BufferedImage saida = new BufferedImage(origem.getWidth(), origem.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < origem.getHeight(); y++) {
            for (int x = 0; x < origem.getWidth(); x++) {
                int argb = origem.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;

                int novoR = ajustarCanalContraste(r, contraste, brilho);
                int novoG = ajustarCanalContraste(g, contraste, brilho);
                int novoB = ajustarCanalContraste(b, contraste, brilho);

                int ajustado = (a << 24) | (novoR << 16) | (novoG << 8) | novoB;
                saida.setRGB(x, y, ajustado);
            }
        }
        return saida;
    }

    private int ajustarCanalContraste(int valor, float contraste, int brilho) {
        int ajustado = (int) Math.round((valor - 128) * contraste + 128 + brilho);
        return Math.max(0, Math.min(255, ajustado));
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
        if ("claude".equalsIgnoreCase(providerIaSelecionado)) {
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

    // Tenta carregar o icone Java oficial local do projeto; se falhar, usa fallback desenhado.
    private Icon criarIconeJava(int tamanho) {
        URL recurso = getClass().getResource("/icons/java_official.png");
        if (recurso != null) {
            ImageIcon original = new ImageIcon(recurso);
            int larguraOriginal = Math.max(1, original.getIconWidth());
            int alturaOriginal = Math.max(1, original.getIconHeight());

            double escala = Math.min((double) tamanho / larguraOriginal, (double) tamanho / alturaOriginal);
            int larguraFinal = Math.max(1, (int) Math.round(larguraOriginal * escala));
            int alturaFinal = Math.max(1, (int) Math.round(alturaOriginal * escala));
            int x = (tamanho - larguraFinal) / 2;
            int y = (tamanho - alturaFinal) / 2;

            BufferedImage canvas = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(original.getImage(), x, y, larguraFinal, alturaFinal, null);
            } finally {
                g2.dispose();
            }
            return new ImageIcon(canvas);
        }

        // Fallback desenhado caso o recurso local não exista.
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Color azul = temaClaroAtivo ? new Color(37, 99, 235) : new Color(125, 211, 252);
            Color vermelho = temaClaroAtivo ? new Color(220, 38, 38) : new Color(248, 113, 113);

            // Pires
            g2.setColor(azul);
            g2.drawArc(4, tamanho - 7, tamanho - 8, 4, 0, 180);

            // Xicara
            g2.draw(new RoundRectangle2D.Double(6, tamanho - 13, 10, 7, 2, 2));
            g2.drawArc(15, tamanho - 12, 4, 5, -60, 230);

            // Vapor
            g2.setColor(vermelho);
            g2.draw(new QuadCurve2D.Double(9, tamanho - 14, 6, tamanho - 18, 9, tamanho - 21));
            g2.draw(new QuadCurve2D.Double(13, tamanho - 14, 16, tamanho - 18, 13, tamanho - 21));
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    // Icone de IA desenhado localmente para evitar dependencia de emoji/fonte do SO.
    private Icon criarIconeIa(int tamanho) {
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color base = temaClaroAtivo ? new Color(79, 70, 229) : new Color(125, 211, 252);
            Color detalhe = temaClaroAtivo ? new Color(124, 58, 237) : new Color(196, 181, 253);

            // Base "chip"
            g2.setColor(base);
            g2.fill(new RoundRectangle2D.Double(2, 2, tamanho - 4, tamanho - 4, 4, 4));

            // Nucleo interno
            g2.setColor(new Color(255, 255, 255, 235));
            double centroTam = Math.max(4, tamanho * 0.28);
            double centroPos = (tamanho - centroTam) / 2.0;
            g2.fill(new Ellipse2D.Double(centroPos, centroPos, centroTam, centroTam));

            // Trilhas simples
            g2.setColor(detalhe);
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int m = tamanho / 2;
            g2.drawLine(m, 2, m, 5);
            g2.drawLine(m, tamanho - 3, m, tamanho - 6);
            g2.drawLine(2, m, 5, m);
            g2.drawLine(tamanho - 3, m, tamanho - 6, m);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    private Icon criarIconeClaude(int tamanho) {
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = temaClaroAtivo ? new Color(120, 53, 15) : new Color(251, 191, 36);
            Color detalhe = temaClaroAtivo ? new Color(146, 64, 14) : new Color(254, 240, 138);
            g2.setColor(base);
            g2.fill(new RoundRectangle2D.Double(2, 2, tamanho - 4, tamanho - 4, 4, 4));
            g2.setColor(detalhe);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(3, 3, tamanho - 6, tamanho - 6, 25, 290);
            g2.drawLine(tamanho / 2, 4, tamanho / 2, tamanho - 4);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    private Icon criarIconeEconomico(int tamanho) {
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color verde = temaClaroAtivo ? new Color(22, 163, 74) : new Color(74, 222, 128);
            Color detalhe = temaClaroAtivo ? new Color(21, 128, 61) : new Color(187, 247, 208);
            g2.setColor(verde);
            g2.fill(new RoundRectangle2D.Double(2, 2, tamanho - 4, tamanho - 4, 4, 4));
            g2.setColor(detalhe);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(tamanho / 2, 3, tamanho / 2, tamanho - 3);
            g2.drawLine(4, tamanho / 2, tamanho - 4, tamanho / 2);
            g2.drawArc(4, 4, tamanho - 8, tamanho - 8, 40, 100);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    private Icon criarIconeLocal(int tamanho) {
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = temaClaroAtivo ? new Color(15, 23, 42) : new Color(226, 232, 240);
            Color detalhe = temaClaroAtivo ? new Color(71, 85, 105) : new Color(148, 163, 184);

            // Monitor simples para representar processamento local
            g2.setColor(base);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Double(1.5, 1.5, tamanho - 3.0, tamanho - 5.5, 2.5, 2.5));
            g2.drawLine(tamanho / 2, tamanho - 4, tamanho / 2, tamanho - 2);
            g2.setColor(detalhe);
            g2.drawLine(tamanho / 2 - 3, tamanho - 1, tamanho / 2 + 3, tamanho - 1);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    // ─── Helpers de UI ────────────────────────────────────────────────────────

    private JLabel criarRotulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FONTE_ROTULO);
        lbl.setForeground(COR_TEXTO);
        return lbl;
    }

    private JScrollPane criarScrollPane(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(COR_BORDA));
        scroll.getVerticalScrollBar().setBackground(COR_PAINEL);
        scroll.setBackground(COR_PAINEL);
        return scroll;
    }

    private JButton criarBotaoAcao(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        configurarPaletaBotao(
                btn,
                Color.WHITE,
                temaClaroAtivo ? new Color(226, 232, 240) : new Color(203, 213, 225),
                COR_DESTAQUE,
                temaClaroAtivo ? new Color(148, 163, 184) : new Color(71, 85, 105)
        );
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 44));
        return btn;
    }

    private JButton criarBotaoPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FONTE_BOTAO);
        configurarPaletaBotao(
                btn,
                Color.WHITE,
                temaClaroAtivo ? new Color(226, 232, 240) : new Color(203, 213, 225),
                COR_DESTAQUE2,
                temaClaroAtivo ? new Color(148, 163, 184) : new Color(71, 85, 105)
        );
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(FONTE_BOTAO);
        if (temaClaroAtivo) {
            configurarPaletaBotao(
                    btn,
                    COR_TEXTO,
                    new Color(100, 116, 139),
                    COR_PAINEL,
                    new Color(226, 232, 240)
            );
        } else {
            configurarPaletaBotao(
                    btn,
                    new Color(241, 245, 249),
                    new Color(186, 196, 213),
                    new Color(42, 48, 70),
                    new Color(60, 68, 92)
            );
        }
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void configurarPaletaBotao(final JButton btn,
                                       final Color textoHabilitado,
                                       final Color textoDesabilitado,
                                       final Color fundoHabilitado,
                                       final Color fundoDesabilitado) {
        btn.setForeground(textoHabilitado);
        btn.setBackground(fundoHabilitado);
        btn.addPropertyChangeListener("enabled", evt -> {
            boolean habilitado = Boolean.TRUE.equals(evt.getNewValue());
            btn.setForeground(habilitado ? textoHabilitado : textoDesabilitado);
            btn.setBackground(habilitado ? fundoHabilitado : fundoDesabilitado);
        });
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
                new ExpertDevGUI();
            }
        });
    }
}


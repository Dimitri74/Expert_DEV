package br.com.expertdev.ui;

import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.io.DefaultTextFileWriter;
import br.com.expertdev.model.*;
import br.com.expertdev.service.*;
import br.com.expertdev.ui.presentation.PresentationMessageService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

/**
 * Controlador central da GUI: encapsula serviços, estado de aplicação e todos
 * os handlers de eventos. Acessa componentes Swing via referência à janela pai.
 */
class GuiController {

    // ─── Referências injetadas ────────────────────────────────────────────────
    final ExpertDevGUI gui;
    private final AppTheme theme;
    private final UiFactory uiFactory;
    private final AuthSession authSession;
    private final PresentationMessageService presentationMessageService;

    // ─── Serviços ─────────────────────────────────────────────────────────────
    AuditoriaService auditoriaService;
    PerformanceService performanceService;
    ReportService reportService;
    ClipboardMonitor clipboardMonitor;
    TrayNotificationService trayService;
    Timer timerNotificacao;
    JFreeChart chartTendenciaAtual;

    // ─── Estado do autocomplete RTC ───────────────────────────────────────────
    Timer timerSugestoesRtc;
    JPopupMenu popupSugestoesRtc;
    JList<String> listaSugestoesRtc;
    boolean atualizandoRtcProgramaticamente;
    private static final int RTC_AUTOCOMPLETE_DEBOUNCE_MS = 220;

    // ─── Estado de geração / IA ───────────────────────────────────────────────
    String modoGeracaoSelecionado = "LOCAL";
    String providerIaSelecionado = "openai";
    String perfilPromptSelecionado = "tecnico";
    boolean salvarApiKeySelecionada;
    String apiKeyDigitada = "";

    // ─── Estado de processamento ──────────────────────────────────────────────
    long ultimoRegistroAuditoriaId = -1;
    String promptComAuditoria = "";

    // ─────────────────────────────────────────────────────────────────────────

    GuiController(ExpertDevGUI gui, AppTheme theme, UiFactory uiFactory, AuthSession authSession) {
        this.gui = gui;
        this.theme = theme;
        this.uiFactory = uiFactory;
        this.authSession = authSession;
        this.presentationMessageService = new PresentationMessageService(gui);
        this.auditoriaService = new AuditoriaService();
        this.performanceService = new PerformanceService(auditoriaService);
        this.reportService = new ReportService(performanceService);
    }

    /** Chamado após a interface ser construída (componentes Swing já existem). */
    void inicializar() {
        inicializarServicosWorkflow();
    }

    // ─── Inicialização de serviços ────────────────────────────────────────────

    private void inicializarServicosWorkflow() {
        final GuiController self = this;
        clipboardMonitor = new ClipboardMonitor(new ClipboardMonitor.ClipboardListener() {
            public void onDataDetected(String rtc, String titulo) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (gui.campoRTC != null && gui.campoRTC.getText().trim().isEmpty()) {
                            definirCampoRtcProgramaticamente(rtc);
                            verificarEPrefilPerformance();
                            if (titulo != null && !titulo.isEmpty()
                                    && gui.campoUC != null && gui.campoUC.getText().trim().isEmpty()) {
                                gui.campoUC.setText(titulo);
                            }
                            trayService.notificar("Tarefa Detectada",
                                    "RTC " + rtc + " capturado do clipboard.",
                                    TrayIcon.MessageType.INFO);
                        }
                    }
                });
            }
        });
        new Thread(clipboardMonitor, "ClipboardMonitorThread").start();

        Image icone = null;
        try {
            icone = uiFactory.carregarImagemRecurso("/icons/ia_icon.png");
            if (icone == null) {
                java.awt.image.BufferedImage logo = uiFactory.carregarImagemLogoPorTema();
                if (logo != null) {
                    icone = logo.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                }
            }
        } catch (Exception ignored) {}

        trayService = new TrayNotificationService("ExpertDev v2.5.0-BETA", icone,
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        gui.setVisible(true);
                        gui.setExtendedState(JFrame.NORMAL);
                        gui.toFront();
                    }
                });

        timerNotificacao = new Timer(60000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                verificarTempoExcedido();
            }
        });
        timerNotificacao.start();
    }

    private void verificarTempoExcedido() {
        List<MetricaPerformance> metricas = performanceService.listarTodosPorUsuario(obterUsuarioSessao());
        for (MetricaPerformance m : metricas) {
            if (m.getInicioExpertDev() != null && m.getFimExpertDev() == null && m.getEstimativaPoker() > 0) {
                long minutosDecorridos = java.time.Duration.between(m.getInicioExpertDev(), LocalDateTime.now()).toMinutes();
                double horasDecorridas = minutosDecorridos / 60.0;
                if (horasDecorridas > m.getEstimativaPoker()) {
                    trayService.notificar("Tempo Excedido",
                            "A tarefa RTC " + m.getRtcNumero() + " excedeu o tempo estimado no Poker!",
                            TrayIcon.MessageType.WARNING);
                }
            }
        }
    }

    // ─── Sessão ───────────────────────────────────────────────────────────────

    String obterTextoLicenca() {
        if (authSession == null || authSession.isPremium()) return "[PREMIUM]";
        if (authSession.isTrial()) return "[TRIAL " + authSession.getTrialDaysRemaining() + "d]";
        return "[EXPIRADO]";
    }

    Color obterCorLicenca() {
        if (authSession == null || authSession.isPremium()) return theme.corSucesso;
        if (authSession.isTrial()) return new Color(217, 119, 6);
        return theme.corErro;
    }

    String obterUsuarioSessao() {
        if (authSession == null) return "Visitante";
        String username = authSession.getDisplayName();
        if (username == null || username.trim().isEmpty()) return "Visitante";
        return username.trim();
    }

    String obterEmailSessao() {
        if (authSession == null || authSession.getEmail() == null) return "";
        return authSession.getEmail().trim();
    }

    // ─── Autocomplete RTC ─────────────────────────────────────────────────────

    void configurarAutocompleteRtc() {
        timerSugestoesRtc = new Timer(RTC_AUTOCOMPLETE_DEBOUNCE_MS, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                atualizarSugestoesRtc();
            }
        });
        timerSugestoesRtc.setRepeats(false);

        popupSugestoesRtc = new JPopupMenu();
        popupSugestoesRtc.setBorder(BorderFactory.createLineBorder(theme.corBorda));

        listaSugestoesRtc = new JList<String>();
        listaSugestoesRtc.setFont(AppTheme.FONTE_NORMAL);
        listaSugestoesRtc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaSugestoesRtc.setBackground(theme.corPainel);
        listaSugestoesRtc.setForeground(theme.corTexto);
        listaSugestoesRtc.setSelectionBackground(theme.corDestaque);
        listaSugestoesRtc.setSelectionForeground(Color.WHITE);

        listaSugestoesRtc.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String val = value != null ? value.toString() : "";
                if (val.startsWith("[PENDENTE]")) {
                    label.setForeground(new Color(255, 180, 0));
                } else if (val.startsWith("[CONCLUÍDO]")) {
                    label.setForeground(theme.corSucesso);
                }
                if (isSelected) {
                    label.setBackground(theme.corDestaque);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(theme.corPainel);
                }
                label.setBorder(new EmptyBorder(4, 8, 4, 8));
                return label;
            }
        });

        JScrollPane scroll = new JScrollPane(listaSugestoesRtc);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(
                Math.max(260, gui.campoRTC.getPreferredSize().width), 150));
        popupSugestoesRtc.add(scroll);

        listaSugestoesRtc.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                aplicarRtcSelecionado();
            }
        });

        gui.campoRTC.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (popupSugestoesRtc == null || !popupSugestoesRtc.isVisible()) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN
                            && isAbaPerformanceSelecionada()) {
                        solicitarAtualizacaoSugestoesRtc();
                        e.consume();
                    }
                    return;
                }
                int idx = listaSugestoesRtc.getSelectedIndex();
                int tam = listaSugestoesRtc.getModel().getSize();
                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_DOWN:
                        if (tam > 0) {
                            listaSugestoesRtc.setSelectedIndex(Math.min(idx + 1, tam - 1));
                            listaSugestoesRtc.ensureIndexIsVisible(listaSugestoesRtc.getSelectedIndex());
                        }
                        e.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_UP:
                        if (tam > 0) {
                            listaSugestoesRtc.setSelectedIndex(Math.max(idx - 1, 0));
                            listaSugestoesRtc.ensureIndexIsVisible(listaSugestoesRtc.getSelectedIndex());
                        }
                        e.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_ENTER:
                        aplicarRtcSelecionado();
                        e.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_ESCAPE:
                        ocultarPopupSugestoesRtc();
                        e.consume();
                        break;
                    default:
                        break;
                }
            }
        });

        gui.campoRTC.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                solicitarAtualizacaoSugestoesRtc();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                Timer timerFechar = new Timer(200, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        if (popupSugestoesRtc != null
                                && !popupSugestoesRtc.isFocusOwner()
                                && !listaSugestoesRtc.isFocusOwner()) {
                            ocultarPopupSugestoesRtc();
                        }
                    }
                });
                timerFechar.setRepeats(false);
                timerFechar.start();
            }
        });
    }

    private void atualizarSugestoesRtc() {
        if (gui.campoRTC == null || popupSugestoesRtc == null || listaSugestoesRtc == null) return;
        if (!isAbaPerformanceSelecionada() || !gui.campoRTC.isFocusOwner() || atualizandoRtcProgramaticamente) {
            if (popupSugestoesRtc.isVisible()
                    && (popupSugestoesRtc.isFocusOwner() || listaSugestoesRtc.isFocusOwner())) {
                return;
            }
            ocultarPopupSugestoesRtc();
            return;
        }
        String filtro = gui.campoRTC.getText() == null ? "" : gui.campoRTC.getText().trim();
        List<String> sugestoes = performanceService.sugerirRtcsPorUsuario(obterUsuarioSessao(), filtro, 30);
        if (sugestoes.isEmpty()) {
            ocultarPopupSugestoesRtc();
            return;
        }
        listaSugestoesRtc.setListData(sugestoes.toArray(new String[0]));
        listaSugestoesRtc.setSelectedIndex(0);
        if (!popupSugestoesRtc.isVisible()) {
            popupSugestoesRtc.setFocusable(false);
            popupSugestoesRtc.show(gui.campoRTC, 0, gui.campoRTC.getHeight());
        }
    }

    private void aplicarRtcSelecionado() {
        if (listaSugestoesRtc == null) return;
        String rtcSelecionado = listaSugestoesRtc.getSelectedValue();
        if (rtcSelecionado == null || rtcSelecionado.trim().isEmpty()) return;
        String rtcLimpo = rtcSelecionado;
        if (rtcLimpo.startsWith("[PENDENTE] ")) {
            rtcLimpo = rtcLimpo.substring("[PENDENTE] ".length());
        } else if (rtcLimpo.startsWith("[CONCLUÍDO] ")) {
            rtcLimpo = rtcLimpo.substring("[CONCLUÍDO] ".length());
        }
        definirCampoRtcProgramaticamente(rtcLimpo);
        ocultarPopupSugestoesRtc();
        verificarEPrefilPerformance();
        buscarEVincularCasoDeUso(rtcLimpo);
    }

    private void buscarEVincularCasoDeUso(final String rtc) {
        if (rtc == null || rtc.trim().isEmpty()) return;
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                List<RegistroAuditoria> historico = auditoriaService.obterPorRTC(rtc);
                for (RegistroAuditoria reg : historico) {
                    String cod = reg.getUcCodigo();
                    String desc = reg.getUcDescricao();
                    if (cod != null && !cod.trim().isEmpty()) {
                        return (desc != null && !desc.trim().isEmpty()) ? cod + " - " + desc : cod;
                    }
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    String uc = get();
                    if (uc != null && gui.campoUC != null) {
                        gui.campoUC.setText(uc);
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    void solicitarAtualizacaoSugestoesRtc() {
        if (!isAbaPerformanceSelecionada()) {
            ocultarPopupSugestoesRtc();
            return;
        }
        if (timerSugestoesRtc == null) {
            atualizarSugestoesRtc();
            return;
        }
        timerSugestoesRtc.restart();
    }

    void ocultarPopupSugestoesRtc() {
        if (timerSugestoesRtc != null) timerSugestoesRtc.stop();
        if (popupSugestoesRtc != null) popupSugestoesRtc.setVisible(false);
    }

    void definirCampoRtcProgramaticamente(String rtc) {
        atualizandoRtcProgramaticamente = true;
        try {
            gui.campoRTC.setText(rtc == null ? "" : rtc);
        } finally {
            atualizandoRtcProgramaticamente = false;
        }
    }

    boolean isAbaPerformanceSelecionada() {
        return gui.abas != null && gui.abas.getSelectedIndex() == 3;
    }

    // ─── Performance & Gráficos ───────────────────────────────────────────────

    void salvarDadosPerformanceAtuais() {
        String rtc = gui.campoRTC.getText().trim();
        if (rtc.isEmpty()) return;

        String username = obterUsuarioSessao();
        String email = obterEmailSessao();

        MetricaPerformance m = performanceService.obterPorRTCeUsuario(rtc, username);
        if (m == null) m = new MetricaPerformance(rtc, username, email);
        m.setAuthUsername(username);
        m.setAuthEmail(email);

        try {
            String estStr = gui.campoEstimativaPoker.getText().trim();
            if (!estStr.isEmpty()) m.setEstimativaPoker(Double.parseDouble(estStr.replace(",", ".")));
        } catch (NumberFormatException ignored) {}

        try {
            String sprintStr = gui.campoSprint.getText().trim();
            m.setSprint(sprintStr.isEmpty() ? null : Integer.parseInt(sprintStr));
        } catch (NumberFormatException ignored) {}

        m.setComplexidade((String) gui.comboComplexidade.getSelectedItem());
        performanceService.salvarOuAtualizar(m);
        atualizarGraficoPerformance();
    }

    void gerenciarAcaoPerformance(String acao) {
        String rtc = gui.campoRTC.getText().trim();
        if (rtc.isEmpty()) {
            mostrarErro("Informe o RTC no topo antes de gerenciar performance.");
            return;
        }

        String username = obterUsuarioSessao();
        String email = obterEmailSessao();

        MetricaPerformance m = performanceService.obterPorRTCeUsuario(rtc, username);
        if (m == null) m = new MetricaPerformance(rtc, username, email);
        m.setAuthUsername(username);
        m.setAuthEmail(email);

        try {
            String estStr = gui.campoEstimativaPoker.getText().trim();
            if (!estStr.isEmpty()) m.setEstimativaPoker(Double.parseDouble(estStr.replace(",", ".")));
        } catch (NumberFormatException ignored) {}
        m.setComplexidade((String) gui.comboComplexidade.getSelectedItem());

        LocalDateTime agora = LocalDateTime.now();
        switch (acao) {
            case "INICIAR_SCRUM":
                m.setInicioScrum(agora);
                m.setStatus("DESENVOLVIMENTO_SCRUM");
                break;
            case "FINALIZAR_SCRUM":
                if (m.getInicioScrum() == null) { mostrarErro("Inicie o Scrum antes de finalizar."); return; }
                m.setFimScrum(agora);
                break;
            case "FINALIZAR_EXPERTDEV":
                if (m.getInicioExpertDev() == null) {
                    mostrarErro("O tempo ExpertDev deve ter sido iniciado ao processar a tarefa.");
                    return;
                }
                m.setFimExpertDev(agora);
                if (m.getInicioScrum() != null && m.getFimScrum() == null) m.setFimScrum(agora);
                double hExpert = java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes() / 60.0;
                double hEst = m.getEstimativaPoker() != null ? m.getEstimativaPoker() : 0;
                if (hEst > 0 && hExpert < hEst) {
                    double economia = hEst - hExpert;
                    double perc = (economia / hEst) * 100;
                    mostrarMensagem(String.format(
                            "🎉 Excelente!\n\nVocê foi mais rápido que o estimado:\n"
                            + "Estimativa: %.2f h\nRealizado: %.2f h\nEconomia: %.2f h (%.1f%%)",
                            hEst, hExpert, economia, perc));
                }
                m.setStatus("CONCLUIDO");
                break;
            default:
                break;
        }
        performanceService.salvarOuAtualizar(m);
        atualizarGraficoPerformance();
    }

    void verificarEPrefilPerformance() {
        String rtc = gui.campoRTC.getText().trim();
        if (rtc.isEmpty()) {
            if (gui.campoEstimativaPoker != null) gui.campoEstimativaPoker.setText("");
            if (gui.campoSprint != null) gui.campoSprint.setText("");
            if (gui.lblGanhoProdutividade != null) gui.lblGanhoProdutividade.setText("Ganho: 0%");
            return;
        }

        MetricaPerformance m = performanceService.obterPorRTCeUsuario(rtc, obterUsuarioSessao());
        if (m != null) {
            if (gui.campoEstimativaPoker != null)
                gui.campoEstimativaPoker.setText(m.getEstimativaPoker() != null ? String.valueOf(m.getEstimativaPoker()) : "");
            if (gui.campoSprint != null)
                gui.campoSprint.setText(m.getSprint() != null ? String.valueOf(m.getSprint()) : "");
            if (gui.comboComplexidade != null)
                gui.comboComplexidade.setSelectedItem(m.getComplexidade() != null ? m.getComplexidade() : "Média");
        } else {
            if (gui.campoEstimativaPoker != null) gui.campoEstimativaPoker.setText("");
            if (gui.campoSprint != null) gui.campoSprint.setText("");
            if (gui.comboComplexidade != null) gui.comboComplexidade.setSelectedItem("Média");
        }
        if (gui.abas != null && gui.abas.getSelectedIndex() == 3) {
            atualizarGraficoPerformance();
            atualizarDashboardTendencia();
        }
    }

    void exportarRelatorioROI() {
        try {
            List<MetricaPerformance> metricas = performanceService.listarTodosPorUsuario(obterUsuarioSessao());
            if (metricas.isEmpty()) { mostrarAviso("Não há dados de performance para exportar."); return; }
            String path = reportService.exportarRelatorioExecutivo(metricas, chartTendenciaAtual);
            mostrarMensagem("Relatório exportado com sucesso!\nCaminho: " + path);
            try {
                File pdfFile = new File(path);
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(pdfFile.toURI());
            } catch (Exception ex) {
                gui.adicionarCardLog("⚠ Não foi possível abrir o PDF automaticamente: " + ex.getMessage());
            }
        } catch (Exception e) {
            mostrarErro("Erro ao exportar relatório: " + e.getMessage());
        }
    }

    void atualizarDashboardTendencia() {
        List<MetricaPerformance> metricas = performanceService.listarTodosPorUsuario(obterUsuarioSessao());
        if (metricas.isEmpty()) {
            gui.painelTendencia.removeAll();
            gui.painelTendencia.add(new JLabel("Sem dados históricos", SwingConstants.CENTER));
            gui.painelTendencia.revalidate();
            return;
        }

        Map<Date, Double> ganhoPorData = new TreeMap<Date, Double>();
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
                "Curva de Aprendizado", "Data de Conclusão", "Ganho (%)", dataset, false, true, false);

        chart.setBackgroundPaint(theme.corPainel);
        org.jfree.chart.plot.XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(theme.corFundo);
        plot.setDomainGridlinePaint(theme.corBorda);
        plot.setRangeGridlinePaint(theme.corBorda);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, theme.corSucesso);
        renderer.setSeriesStroke(0, new java.awt.BasicStroke(2.0f));
        plot.setRenderer(renderer);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM"));

        chartTendenciaAtual = chart;

        gui.painelTendencia.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(theme.corPainel);
        gui.painelTendencia.add(cp, BorderLayout.CENTER);
        gui.painelTendencia.revalidate();
        gui.painelTendencia.repaint();
    }

    void atualizarGraficoPerformance() {
        String rtc = gui.campoRTC.getText().trim();
        if (rtc.isEmpty()) return;

        MetricaPerformance m = performanceService.obterPorRTCeUsuario(rtc, obterUsuarioSessao());
        if (m == null) {
            gui.painelGrafico.removeAll();
            gui.painelGrafico.add(new JLabel("Sem dados para o RTC " + rtc, SwingConstants.CENTER));
            gui.painelGrafico.revalidate();
            return;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (m.getEstimativaPoker() != null) dataset.addValue(m.getEstimativaPoker(), "Horas", "Estimativa Poker");
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
                "Comparativo de Performance - RTC " + rtc, "Etapa", "Horas",
                dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(theme.corPainel);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(theme.corFundo);
        plot.setRangeGridlinePaint(theme.corBorda);
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(0, theme.corDestaque);

        gui.painelGrafico.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(theme.corPainel);
        gui.painelGrafico.add(cp, BorderLayout.CENTER);
        gui.painelGrafico.revalidate();
        gui.painelGrafico.repaint();

        if (m.getFimScrum() != null && m.getFimExpertDev() != null) {
            double hS = java.time.Duration.between(m.getInicioScrum(), m.getFimScrum()).toMinutes() / 60.0;
            double hE = java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes() / 60.0;
            if (hS > 0) gui.lblGanhoProdutividade.setText(String.format("Ganho: %.1f%%", ((hS - hE) / hS) * 100));
        } else if (m.getEstimativaPoker() != null && m.getEstimativaPoker() > 0 && m.getFimExpertDev() != null) {
            double hE = java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes() / 60.0;
            gui.lblGanhoProdutividade.setText(String.format("Ganho Est.: %.1f%%",
                    ((m.getEstimativaPoker() - hE) / m.getEstimativaPoker()) * 100));
        }
    }

    void atualizarAbaHistorico() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                List<RegistroAuditoria> registros = auditoriaService.obterTodosPorUsuario(obterUsuarioSessao());
                if (registros.isEmpty()) return "(nenhum registro encontrado)";
                StringBuilder sb = new StringBuilder();
                for (RegistroAuditoria reg : registros) sb.append(reg).append("\n");
                return sb.toString();
            }
            @Override
            protected void done() {
                try {
                    gui.areaHistorico.setText(get());
                    gui.areaHistorico.setCaretPosition(0);
                } catch (Exception e) {
                    gui.areaHistorico.setText("Erro ao carregar histórico: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ─── Exit ─────────────────────────────────────────────────────────────────

    void confirmarSaidaExpertDev() {
        Color corPainel  = theme.corPainel;
        Color corTexto   = theme.corTexto;
        Color corVerde   = new Color(34, 197, 94);
        Color corVermelho = new Color(239, 68, 68);

        JPanel painelDialogo = new JPanel(new BorderLayout(15, 15));
        painelDialogo.setBackground(corPainel);
        painelDialogo.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblMensagem = new JLabel("Deseja sair do Expert Dev?");
        lblMensagem.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMensagem.setForeground(corTexto);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        painelBotoes.setBackground(corPainel);

        JButton btnSim = uiFactory.criarBotaoDialogo("Sim", corVerde);
        JButton btnNao = uiFactory.criarBotaoDialogo("Não", corVermelho);

        btnSim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                SwingUtilities.getWindowAncestor(painelDialogo).dispose();
                gui.dispose();
                System.exit(0);
            }
        });
        btnNao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                SwingUtilities.getWindowAncestor(painelDialogo).dispose();
            }
        });

        painelBotoes.add(btnSim);
        painelBotoes.add(btnNao);
        painelDialogo.add(lblMensagem, BorderLayout.CENTER);
        painelDialogo.add(painelBotoes, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(gui, "Confirmação", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(painelDialogo);
        dialog.setSize(450, 200);
        dialog.setLocationRelativeTo(gui);
        dialog.getContentPane().setBackground(corPainel);
        dialog.setVisible(true);
    }

    // ─── Processamento Word ───────────────────────────────────────────────────

    void selecionarArquivoWord() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar arquivos Word (.doc/.docx)");
        chooser.setFileFilter(new FileNameExtensionFilter("Documentos Word (*.doc, *.docx)", "doc", "docx"));
        chooser.setMultiSelectionEnabled(true);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        if (chooser.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {
            File[] selecionados = chooser.getSelectedFiles();
            if (selecionados == null || selecionados.length == 0) {
                File unico = chooser.getSelectedFile();
                if (unico != null) selecionados = new File[]{unico};
            }
            adicionarArquivosWord(selecionados);
            carregarPreviewWord();
        }
    }

    void carregarPreviewWord() {
        List<File> arquivos = obterArquivosWordSelecionados();
        if (arquivos.isEmpty()) {
            gui.areaPreviewWord.setText("(adicione um ou mais arquivos .doc/.docx e selecione um item para pré-visualizar)");
            gui.areaPreviewWord.setCaretPosition(0);
            atualizarEstimativaIa();
            return;
        }

        File selecionadoLista = gui.listaArquivosWord != null ? gui.listaArquivosWord.getSelectedValue() : null;
        if (selecionadoLista == null && gui.listaArquivosWord != null
                && gui.listaArquivosWord.getModel().getSize() > 0) {
            gui.listaArquivosWord.setSelectedIndex(0);
            selecionadoLista = gui.listaArquivosWord.getSelectedValue();
        }
        final File arquivoPreview = selecionadoLista != null ? selecionadoLista : arquivos.get(0);
        gui.areaPreviewWord.setText("Carregando pré-visualização...");

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                WordDocumentReader reader = new WordDocumentReader();
                ResultadoProcessamento r = reader.ler(arquivoPreview);
                if (r.isSucesso()) {
                    String texto = r.getTextoExtraido();
                    if (texto == null) texto = "";
                    texto = texto.trim();
                    if (texto.isEmpty()) {
                        return "Arquivo de prévia: " + arquivoPreview.getName() + "\n"
                                + "Total na fila: " + obterArquivosWordSelecionados().size() + "\n\n"
                                + "[Pré-visualização textual indisponível neste arquivo.]";
                    }
                    String textoPreview = texto.length() > 3000
                            ? texto.substring(0, 3000) + "\n\n[... conteúdo truncado ...]"
                            : texto;
                    List<String> status = extrairMensagensStatusWord(r);
                    if (status.isEmpty()) return textoPreview;
                    StringBuilder sb = new StringBuilder();
                    for (String s : status) sb.append(s).append("\n");
                    sb.append("\n").append(textoPreview);
                    return sb.toString();
                }
                return "Erro ao carregar pré-visualização: " + r.getErro();
            }
            @Override
            protected void done() {
                try {
                    gui.areaPreviewWord.setText(get());
                    gui.areaPreviewWord.setCaretPosition(0);
                    atualizarEstimativaIa();
                } catch (Exception e) {
                    gui.areaPreviewWord.setText("Erro ao carregar pré-visualização.");
                }
            }
        };
        worker.execute();
    }

    void adicionarArquivosWord(File[] arquivos) {
        if (arquivos == null || arquivos.length == 0) return;
        if (gui.modeloArquivosWord == null) gui.modeloArquivosWord = new DefaultListModel<File>();

        int adicionados = 0;
        int ignorados = 0;
        for (File arquivo : arquivos) {
            if (!ehArquivoWordValido(arquivo)) { ignorados++; continue; }
            boolean jaExiste = false;
            for (int i = 0; i < gui.modeloArquivosWord.size(); i++) {
                if (gui.modeloArquivosWord.get(i).getAbsolutePath()
                        .equalsIgnoreCase(arquivo.getAbsolutePath())) {
                    jaExiste = true; break;
                }
            }
            if (!jaExiste) { gui.modeloArquivosWord.addElement(arquivo); adicionados++; }
        }
        if (adicionados > 0) {
            gui.labelArquivoWord.setText("Arquivos carregados para processamento");
            gui.labelArquivoWord.setForeground(theme.corSucesso);
        }
        if (ignorados > 0) gui.adicionarCardLog("⚠ Alguns arquivos foram ignorados por formato inválido (use .doc/.docx).");
        atualizarResumoArquivosWord();
    }

    private boolean ehArquivoWordValido(File arquivo) {
        if (arquivo == null || !arquivo.exists() || !arquivo.isFile()) return false;
        String nome = arquivo.getName().toLowerCase();
        return nome.endsWith(".doc") || nome.endsWith(".docx");
    }

    List<File> obterArquivosWordSelecionados() {
        List<File> arquivos = new ArrayList<File>();
        if (gui.modeloArquivosWord == null) return arquivos;
        for (int i = 0; i < gui.modeloArquivosWord.size(); i++) {
            File f = gui.modeloArquivosWord.get(i);
            if (f != null && f.exists()) arquivos.add(f);
        }
        return arquivos;
    }

    void atualizarResumoArquivosWord() {
        int total = gui.modeloArquivosWord == null ? 0 : gui.modeloArquivosWord.getSize();
        if (gui.labelResumoArquivosWord != null) gui.labelResumoArquivosWord.setText("Arquivos Word: " + total);
        if (gui.labelArquivoWord != null && total == 0) {
            gui.labelArquivoWord.setText("Nenhum arquivo selecionado");
            gui.labelArquivoWord.setForeground(theme.corTextoSuave);
        }
        atualizarVisibilidadePreviewWord(total);
    }

    void atualizarVisibilidadePreviewWord(int totalArquivos) {
        boolean mostrar = totalArquivos <= AppTheme.MAX_ARQUIVOS_PREVIEW_EMBUTIDA;
        if (gui.painelPreviewWord != null) {
            gui.painelPreviewWord.setVisible(mostrar);
            gui.painelPreviewWord.revalidate();
            gui.painelPreviewWord.repaint();
        }
        if (gui.btnAbrirPreviewWord != null) {
            gui.btnAbrirPreviewWord.setEnabled(totalArquivos > 0);
            gui.btnAbrirPreviewWord.setVisible(totalArquivos > 0);
            gui.btnAbrirPreviewWord.setText(totalArquivos > 1 ? "👁 Abrir Prévia (selecionado)" : "👁 Abrir Prévia");
        }
    }

    void abrirDialogoPreviewWordSelecionado() {
        List<File> arquivos = obterArquivosWordSelecionados();
        if (arquivos.isEmpty()) { mostrarErro("Nenhum arquivo Word foi selecionado para pré-visualização."); return; }

        File selecionado = gui.listaArquivosWord != null ? gui.listaArquivosWord.getSelectedValue() : null;
        if (selecionado == null) {
            selecionado = arquivos.get(0);
            if (gui.listaArquivosWord != null && gui.listaArquivosWord.getModel().getSize() > 0) {
                gui.listaArquivosWord.setSelectedIndex(0);
            }
            carregarPreviewWord();
        }

        String textoPreview = gui.areaPreviewWord != null ? gui.areaPreviewWord.getText() : "";
        if (textoPreview == null || textoPreview.trim().isEmpty()) textoPreview = "Carregando...";

        JDialog dialogo = new JDialog(gui, "Pré-visualização - " + selecionado.getName(), true);
        dialogo.setLayout(new BorderLayout(8, 8));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(AppTheme.FONTE_MONO);
        area.setBackground(theme.corFundo);
        area.setForeground(theme.corTexto);
        area.setText(textoPreview);
        area.setCaretPosition(0);

        JLabel lblArquivo = new JLabel("Arquivo: " + selecionado.getAbsolutePath());
        lblArquivo.setBorder(new EmptyBorder(8, 10, 0, 10));
        lblArquivo.setFont(AppTheme.FONTE_SUBTITULO);
        lblArquivo.setForeground(theme.corTextoSuave);

        JButton btnFechar = uiFactory.criarBotaoSecundario("Fechar");
        final JDialog dialogFinal = dialogo;
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { dialogFinal.dispose(); }
        });
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.setOpaque(false);
        rodape.add(btnFechar);

        dialogo.add(lblArquivo, BorderLayout.NORTH);
        dialogo.add(uiFactory.criarScrollPane(area), BorderLayout.CENTER);
        dialogo.add(rodape, BorderLayout.SOUTH);
        dialogo.setSize(900, 620);
        dialogo.setLocationRelativeTo(gui);
        dialogo.setVisible(true);
    }

    void configurarDropArquivosWord(JComponent componente) {
        if (componente == null) return;
        componente.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    @SuppressWarnings("unchecked")
                    List<File> arquivos = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    adicionarArquivosWord(arquivos.toArray(new File[0]));
                    carregarPreviewWord();
                    return true;
                } catch (Exception e) {
                    gui.adicionarCardLog("⚠ Falha no drag-and-drop de Word: " + e.getMessage());
                    return false;
                }
            }
        });
    }

    private List<String> extrairMensagensStatusWord(ResultadoProcessamento resultado) {
        List<String> mensagens = new ArrayList<String>();
        if (resultado == null || resultado.getObservacao() == null) return mensagens;
        String obs = resultado.getObservacao();
        if (obs.contains("Formato DOCX detectado: leitura direta."))
            mensagens.add("✓ Word: ingestão direta de DOCX (sem conversão).");
        if (obs.contains("Conversao concluida com sucesso."))
            mensagens.add("✓ Word: conversão DOC -> DOCX concluída via LibreOffice.");
        if (obs.contains("Conversao DOC desativada por configuracao."))
            mensagens.add("⚠ Word: conversão DOC desativada por configuração, usando parser DOC direto.");
        if (obs.contains("LibreOffice indisponivel") || obs.contains("LibreOffice nao encontrado"))
            mensagens.add("⚠ Word: LibreOffice não detectado; aplicado fallback para parser DOC legado.");
        if (obs.contains("Fallback direto para parser DOC"))
            mensagens.add("⚠ Word: fallback DOC legado acionado.");
        return mensagens;
    }

    private String montarMensagemErroWord(String erroBase) {
        String erro = erroBase == null ? "Erro desconhecido ao processar Word." : erroBase;
        String lower = erro.toLowerCase();
        if (lower.contains("libreoffice") && (lower.contains("nao encontrado") || lower.contains("indisponivel")))
            return "Falha ao ler o Word: " + erro
                    + "\nDica: instale o LibreOffice ou configure 'word.libreoffice.path' em expertdev.properties.";
        if (lower.contains("conversao de doc desativada"))
            return "Falha ao ler o Word: " + erro
                    + "\nDica: habilite 'word.doc.conversion.enabled=true' para tentar DOC -> DOCX automaticamente.";
        return "Falha ao ler o Word: " + erro;
    }

    // ─── Processamento principal ──────────────────────────────────────────────

    void iniciarProcessamento() {
        int abaSelecionada = gui.abas.getSelectedIndex();
        boolean viaUrl;
        if (abaSelecionada == 3) {
            boolean temUrls = gui.areaUrls != null && !gui.areaUrls.getText().trim().isEmpty();
            boolean temWord = !obterArquivosWordSelecionados().isEmpty();
            if (!temUrls && !temWord) {
                mostrarErro("Informe URLs ou selecione um arquivo Word antes de gerar o prompt.");
                return;
            }
            viaUrl = temUrls && !temWord;
        } else {
            viaUrl = abaSelecionada == 0;
        }

        String rtcInformado = gui.campoRTC != null ? gui.campoRTC.getText().trim() : "";
        if (rtcInformado.isEmpty()) {
            mostrarErro("Informe o número do RTC antes de processar. Este campo é obrigatório.");
            if (gui.campoRTC != null) gui.campoRTC.requestFocusInWindow();
            return;
        }

        String estPoker = gui.campoEstimativaPoker != null ? gui.campoEstimativaPoker.getText().trim() : "";
        if (estPoker.isEmpty()) {
            mostrarErro("Informe a Estimativa Poker (Horas) na aba Performance antes de processar.");
            gui.abas.setSelectedIndex(3);
            if (gui.campoEstimativaPoker != null) gui.campoEstimativaPoker.requestFocusInWindow();
            return;
        }

        String sprintInf = gui.campoSprint != null ? gui.campoSprint.getText().trim() : "";
        if (sprintInf.isEmpty()) {
            int resp = JOptionPane.showConfirmDialog(gui,
                    "O número da Sprint não foi informado. Deseja informar agora?",
                    "Sprint não informada", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (resp == JOptionPane.YES_OPTION) {
                gui.abas.setSelectedIndex(3);
                if (gui.campoSprint != null) gui.campoSprint.requestFocusInWindow();
                return;
            }
        }

        if (viaUrl) {
            if (gui.areaUrls.getText().trim().isEmpty()) {
                mostrarErro("Informe pelo menos uma URL para processar.");
                return;
            }
        } else {
            if (obterArquivosWordSelecionados().isEmpty()) {
                mostrarErro("Adicione ao menos um arquivo Word (.doc ou .docx) antes de processar.");
                return;
            }
        }

        gui.btnProcessar.setEnabled(false);
        gui.btnCopiarPrompt.setEnabled(false);
        gui.btnSalvar.setEnabled(false);
        gui.painelLogCards.removeAll();
        gui.adicionarCardLog("Iniciando processamento...");
        gui.areaPrompt.setText("Processando...");
        gui.barraProgresso.setIndeterminate(true);
        gui.barraProgresso.setString("Processando...");

        final boolean modoUrl = viaUrl;

        SwingWorker<ExecucaoConsolidada, String> worker = new SwingWorker<ExecucaoConsolidada, String>() {
            @Override
            protected ExecucaoConsolidada doInBackground() throws Exception {
                ExpertDevConfig config = ExpertDevConfig.carregar();
                java.time.Instant inicio = java.time.Instant.now();
                List<ResultadoProcessamento> resultados;
                final boolean modoIa = isModoIaSelecionado();

                String rtcNumero = gui.campoRTC != null ? gui.campoRTC.getText().trim() : "";
                String ucCodigo = gui.campoUC != null ? gui.campoUC.getText().trim() : "";
                String authUsername = obterUsuarioSessao();
                String authEmail = obterEmailSessao();

                publish("→ RTC: " + (rtcNumero.isEmpty() ? "(não informado)" : rtcNumero));
                publish("→ UC: " + (ucCodigo.isEmpty() ? "(não informado)" : ucCodigo));

                RegistroAuditoria regAuditoria = new RegistroAuditoria(
                        rtcNumero, ucCodigo, ucCodigo, modoGeracaoSelecionado,
                        providerIaSelecionado, "", authUsername, authEmail);
                ultimoRegistroAuditoriaId = auditoriaService.inserir(regAuditoria);

                MetricaPerformance mPerf = performanceService.obterPorRTCeUsuario(rtcNumero, authUsername);
                if (mPerf == null) mPerf = new MetricaPerformance(rtcNumero, authUsername, authEmail);
                mPerf.setAuthUsername(authUsername);
                mPerf.setAuthEmail(authEmail);
                if (mPerf.getInicioExpertDev() == null) mPerf.setInicioExpertDev(LocalDateTime.now());
                performanceService.salvarOuAtualizar(mPerf);

                if (modoUrl) {
                    publish("→ Modo: Via URLs");
                    String textoUrls = gui.areaUrls.getText().trim();
                    UrlParser urlParser = new UrlParser();
                    List<String> urls = urlParser.parsear(textoUrls);
                    publish("→ URLs encontradas: " + urls.size());
                    if (urls.isEmpty()) throw new IllegalArgumentException("Nenhuma URL válida foi detectada.");
                    int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
                    publish("→ Processamento paralelo com " + numThreads + " thread(s)...");
                    resultados = new ParallelUrlProcessor(config, numThreads).processar(urls);
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
                        if (!r.isSucesso()) throw new RuntimeException(montarMensagemErroWord(r.getErro()));
                        publish("→ Documento Word lido com sucesso: " + arquivoWord.getName());
                        for (String s : extrairMensagensStatusWord(r)) publish(s);
                        if (r.getObservacao() != null && !r.getObservacao().trim().isEmpty())
                            publish("→ Rastreamento Word: " + r.getObservacao());
                        publish("→ Caracteres extraídos: " + (r.getTextoExtraido() != null ? r.getTextoExtraido().length() : 0));
                        resultados.add(r);
                    }
                }

                publish("→ Gerando documento Word de saída...");
                String arquivoWordSaida = "";
                try {
                    br.com.expertdev.service.ImageDownloader imgDl = new br.com.expertdev.service.ImageDownloader();
                    br.com.expertdev.service.WordDocumentBuilder wordBuilder = new br.com.expertdev.service.WordDocumentBuilder(imgDl);
                    arquivoWordSaida = wordBuilder.gerar(resultados);
                    publish("✓ Word gerado: " + arquivoWordSaida);
                } catch (Exception e) { publish("⚠ Word de saída não gerado: " + e.getMessage()); }

                publish("→ Gerando PDF de saída...");
                String arquivoPdf = "";
                try {
                    br.com.expertdev.service.ImageDownloader imgDl = new br.com.expertdev.service.ImageDownloader();
                    br.com.expertdev.service.PdfDocumentBuilder pdfBuilder = new br.com.expertdev.service.PdfDocumentBuilder(imgDl);
                    arquivoPdf = pdfBuilder.gerar(resultados);
                    publish("✓ PDF gerado: " + arquivoPdf);
                } catch (Exception e) { publish("⚠ PDF de saída não gerado: " + e.getMessage()); }

                publish("→ Consolidando resultados e gerando prompt...");
                List<String> urlsRef = new ArrayList<String>();
                for (ResultadoProcessamento r : resultados) urlsRef.add(r.getUrl());

                PromptGenerationService servicoPrompt = criarServicoPrompt(config, modoIa);
                ExecucaoConsolidada execucao;
                try {
                    if (modoIa && config.isAiModoEconomico()) publish("→ Modo econômico de IA ativo.");
                    publish("→ Gerador de prompt: " + servicoPrompt.getNomeModo());
                    execucao = new ResultConsolidator(servicoPrompt)
                            .consolidar(resultados, urlsRef.size(), inicio, java.time.Instant.now(),
                                    arquivoWordSaida, arquivoPdf, rtcNumero);
                } catch (RuntimeException e) {
                    if (modoIa) {
                        publish("⚠ IA indisponível. Aplicando fallback para modo local...");
                        PromptGenerationService fallback = new LocalPromptGenerationService(
                                new PromptGenerator(perfilPromptSelecionado));
                        execucao = new ResultConsolidator(fallback)
                                .consolidar(resultados, urlsRef.size(), inicio, java.time.Instant.now(),
                                        arquivoWordSaida, arquivoPdf, rtcNumero);
                        publish("✓ Prompt gerado em fallback local.");
                    } else { throw e; }
                }

                String promptFinal = execucao.getPromptPronto();
                if (!rtcNumero.isEmpty() || !ucCodigo.isEmpty()) {
                    StringBuilder header = new StringBuilder();
                    if (!rtcNumero.isEmpty()) header.append("// RTC: ").append(rtcNumero).append("\n");
                    if (!ucCodigo.isEmpty()) header.append("// UC: ").append(ucCodigo).append("\n");
                    if (header.length() > 0) promptFinal = header.append("\n").toString() + promptFinal;
                }
                if (ultimoRegistroAuditoriaId > 0)
                    auditoriaService.atualizar(ultimoRegistroAuditoriaId, "CONCLUIDO", promptFinal);
                promptComAuditoria = promptFinal;

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
                } catch (Exception e) { publish("⚠ Erro ao salvar arquivos: " + e.getMessage()); }

                publish("✅ Concluído! URLs: " + execucao.getTotalUrls()
                        + " | Sucesso: " + execucao.getUrlsComSucesso()
                        + " | Falhas: " + execucao.getUrlsComFalha()
                        + " | Imagens: " + execucao.getTotalImagens()
                        + " | Tempo: " + execucao.getTempoTotalSegundos() + "s");
                return execucao;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) gui.adicionarCardLog(msg);
            }

            @Override
            protected void done() {
                gui.barraProgresso.setIndeterminate(false);
                gui.btnProcessar.setEnabled(true);
                try {
                    get();
                    gui.areaPrompt.setText(promptComAuditoria);
                    gui.areaPrompt.setCaretPosition(0);
                    atualizarAbaHistorico();
                    gui.barraProgresso.setValue(100);
                    gui.barraProgresso.setString("Concluído com sucesso!");
                    gui.barraProgresso.setForeground(theme.corSucesso);
                    gui.btnCopiarPrompt.setEnabled(true);
                    gui.btnSalvar.setEnabled(true);
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    gui.adicionarCardLog("❌ ERRO: " + msg);
                    gui.areaPrompt.setText("Ocorreu um erro durante o processamento.\nVerifique o log.");
                    gui.barraProgresso.setValue(0);
                    gui.barraProgresso.setString("Erro!");
                    gui.barraProgresso.setForeground(theme.corErro);
                    mostrarErro("Erro no processamento:\n" + msg);
                }
            }
        };
        worker.execute();
    }

    void copiarPromptParaClipboard() {
        String texto = gui.areaPrompt.getText();
        if (texto != null && !texto.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(texto), null);
            JOptionPane.showMessageDialog(gui, "Prompt copiado para a área de transferência!",
                    "Copiado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void abrirDialogoSalvar() {
        JOptionPane.showMessageDialog(gui,
                "Os arquivos já foram salvos automaticamente durante o processamento.\n\n"
                + "Arquivos gerados no diretório de execução:\n"
                + "  • prompt_para_junie_copilot.txt\n"
                + "  • regras_extraidas.txt\n"
                + "  • imagens_encontradas.txt\n"
                + "  • resumo_execucao.txt\n"
                + "  • contexto_com_imagens.docx\n"
                + "  • contexto_com_imagens.pdf",
                "Arquivos Salvos", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── Configuração de IA ───────────────────────────────────────────────────

    void preencherConfigIaInicial(ExpertDevConfig configUi) {
        if (gui.comboModoGeracao != null) gui.comboModoGeracao.setSelectedItem(modoGeracaoSelecionado);
        if (gui.comboProviderIa != null) gui.comboProviderIa.setSelectedItem(providerIaSelecionado);
        if (gui.comboPerfilPrompt != null) gui.comboPerfilPrompt.setSelectedItem(perfilPromptSelecionado);
        if (gui.campoApiKey != null) {
            gui.campoApiKey.setText(configUi.getAiApiKeyResolvida());
            apiKeyDigitada = new String(gui.campoApiKey.getPassword());
        }
        if (gui.chkSalvarApiKey != null) {
            boolean temKey = configUi.getAiApiKeyResolvida() != null
                    && !configUi.getAiApiKeyResolvida().trim().isEmpty();
            gui.chkSalvarApiKey.setSelected(temKey);
            salvarApiKeySelecionada = temKey;
        }
        if (gui.lblAvisoModoEconomicoIa != null) gui.lblAvisoModoEconomicoIa.setVisible(configUi.isAiModoEconomico());
        atualizarEstimativaIa();
    }

    void atualizarEstadoOpcoesIa() {
        boolean iaAtiva = isModoIaSelecionado();
        if (gui.comboProviderIa != null) gui.comboProviderIa.setEnabled(iaAtiva);
        if (gui.comboPerfilPrompt != null) gui.comboPerfilPrompt.setEnabled(true);
        if (gui.campoApiKey != null) gui.campoApiKey.setEnabled(iaAtiva);
        if (gui.btnLimparApiKey != null) gui.btnLimparApiKey.setEnabled(iaAtiva);
        if (gui.chkSalvarApiKey != null) gui.chkSalvarApiKey.setEnabled(iaAtiva);
        if (gui.btnTestarConexaoIa != null) gui.btnTestarConexaoIa.setEnabled(iaAtiva);
        if (gui.lblAvisoModoEconomicoIa != null) gui.lblAvisoModoEconomicoIa.setVisible(iaAtiva);
        if (gui.lblEstimativaIa != null) gui.lblEstimativaIa.setVisible(true);
    }

    boolean isModoIaSelecionado() {
        if (gui.comboModoGeracao == null || gui.comboModoGeracao.getSelectedItem() == null)
            return "IA".equalsIgnoreCase(modoGeracaoSelecionado);
        return "IA".equalsIgnoreCase(String.valueOf(gui.comboModoGeracao.getSelectedItem()));
    }

    String normalizarProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) return "openai";
        String normalized = provider.trim().toLowerCase();
        if ("claude-code".equals(normalized) || "anthropic".equals(normalized)) return "claude";
        return normalized;
    }

    private PromptGenerationService criarServicoPrompt(ExpertDevConfig config, boolean modoIa) {
        if (!modoIa) return new LocalPromptGenerationService(new PromptGenerator(perfilPromptSelecionado));
        String apiKeyInformada = gui.campoApiKey != null ? new String(gui.campoApiKey.getPassword()).trim() : "";
        String apiKey = apiKeyInformada.isEmpty() ? config.getAiApiKeyResolvida() : apiKeyInformada;
        if (apiKey == null || apiKey.trim().isEmpty())
            throw new IllegalStateException("Modo IA selecionado, mas nenhuma API Key foi informada.");
        if (salvarApiKeySelecionada && !apiKeyInformada.isEmpty()) ExpertDevConfig.salvarApiKeyIA(apiKeyInformada);
        return new AiPromptGenerationService(
                providerIaSelecionado, config.getAiEndpoint(), config.getAiModel(), apiKey,
                config.getAiTimeoutMs(), config.getAiTemperature(), config.getAiMaxTokens(),
                config.getAiMaxContextChars(), config.isAiModoEconomico(), perfilPromptSelecionado);
    }

    void testarConexaoIa() {
        ExpertDevConfig config = ExpertDevConfig.carregar();
        final String apiKeyInformada = gui.campoApiKey != null ? new String(gui.campoApiKey.getPassword()).trim() : "";
        final String apiKey = apiKeyInformada.isEmpty() ? config.getAiApiKeyResolvida() : apiKeyInformada;
        if (apiKey == null || apiKey.trim().isEmpty()) { mostrarErro("Informe uma API Key para testar a conexão de IA."); return; }

        gui.btnTestarConexaoIa.setEnabled(false);
        gui.adicionarCardLog("→ Testando conexão com IA...");

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                new AiPromptGenerationService(
                        providerIaSelecionado, config.getAiEndpoint(), config.getAiModel(), apiKey,
                        config.getAiTimeoutMs(), config.getAiTemperature(), config.getAiMaxTokens(),
                        config.getAiMaxContextChars(), config.isAiModoEconomico(), perfilPromptSelecionado
                ).testarConexao();
                return null;
            }
            @Override
            protected void done() {
                gui.btnTestarConexaoIa.setEnabled(true);
                try {
                    get();
                    gui.adicionarCardLog("✓ Conexão com IA validada com sucesso.");
                    if (salvarApiKeySelecionada && !apiKeyInformada.isEmpty())
                        ExpertDevConfig.salvarApiKeyIA(apiKeyInformada);
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    gui.adicionarCardLog("⚠ Falha no teste de IA: " + msg);
                    mostrarErro("Falha ao testar IA:\n" + msg);
                }
            }
        };
        worker.execute();
    }

    void limparApiKey() {
        if (gui.campoApiKey != null) gui.campoApiKey.setText("");
        apiKeyDigitada = "";
        salvarApiKeySelecionada = false;
        if (gui.chkSalvarApiKey != null) gui.chkSalvarApiKey.setSelected(false);
        ExpertDevConfig.salvarApiKeyIA("");
        gui.adicionarCardLog("→ API Key de IA limpa da interface e da configuração local.");
    }

    void atualizarEstimativaIa() {
        if (gui.lblEstimativaIa == null) return;
        ExpertDevConfig cfg = ExpertDevConfig.carregar();
        String contexto = "";
        if (gui.abas != null && gui.abas.getSelectedIndex() == 1 && gui.areaPreviewWord != null) {
            contexto = gui.areaPreviewWord.getText();
        } else if (gui.areaUrls != null) {
            contexto = gui.areaUrls.getText();
        }
        if (contexto == null) contexto = "";

        int chars = contexto.trim().length();
        int inputTokens = Math.max(1, chars / 4 + 280);
        int outputTokens = cfg.getAiMaxTokens();

        double inputRate = "claude".equalsIgnoreCase(providerIaSelecionado) ? 3.0d : 0.15d;
        double outputRate = "claude".equalsIgnoreCase(providerIaSelecionado) ? 15.0d : 0.60d;

        double estimativaCusto = (inputTokens / 1_000_000.0d) * inputRate
                + (outputTokens / 1_000_000.0d) * outputRate;
        DecimalFormat df = new DecimalFormat("0.0000");
        gui.lblEstimativaIa.setText("Estimativa IA: in~" + inputTokens
                + " tok, out max~" + outputTokens
                + " tok, custo~US$" + df.format(estimativaCusto));
    }

    // ─── Mensagens ────────────────────────────────────────────────────────────

    void mostrarAviso(String msg) { presentationMessageService.showWarning(msg); }

    void mostrarMensagem(String msg) { presentationMessageService.showInfo(msg); }

    void mostrarErro(String mensagem) { presentationMessageService.showError(mensagem); }
}

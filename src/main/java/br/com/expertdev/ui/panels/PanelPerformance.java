package br.com.expertdev.ui.panels;

import br.com.expertdev.ui.components.BadgeLabel;
import br.com.expertdev.ui.theme.ExpertDevTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Aba "Performance & ROI" — estimativas Scrum e métricas de ganho.
 */
public class PanelPerformance extends JPanel {

    private final JTextField campoEstimativaPoker;
    private final JTextField campoSprint;
    private final JComboBox<String> comboComplexidade;
    private final JPanel painelGrafico;
    private final JPanel painelTendencia;
    private final BadgeLabel lblGanho;
    private final JButton btnIniciarScrum;
    private final JButton btnFinalizarScrum;
    private final JButton btnFinalizarExpertDev;

    public PanelPerformance() {
        setLayout(new MigLayout("insets 18 20 18 20, fill",
            "[grow][grow][grow]", "[][8!][grow, 120!][grow, 120!][8!][]"));
        setBackground(ExpertDevTheme.BG_PANEL);

        // Header
        JLabel title = buildSectionLabel("PERFORMANCE & ROI");
        add(title, "cell 0 0, spanx 3, wrap");

        // Row de inputs
        add(buildFieldLabel("ESTIMATIVA POKER (h)"), "cell 0 1");
        add(buildFieldLabel("SPRINT"), "cell 1 1");
        add(buildFieldLabel("COMPLEXIDADE"), "cell 2 1, wrap");

        campoEstimativaPoker = buildSmallTextField();
        campoSprint = buildSmallTextField();
        comboComplexidade = new JComboBox<String>(new String[]{"Baixa", "Média", "Alta", "Crítica"});
        styleCombo(comboComplexidade);

        add(campoEstimativaPoker, "cell 0 2, growx, top");
        add(campoSprint, "cell 1 2, growx, top");
        add(comboComplexidade, "cell 2 2, growx, top, wrap");

        // Grid 2 painéis
        painelGrafico = buildInfoPanel("RTC ATUAL", "Aguardando dados...");
        painelTendencia = buildInfoPanel("TENDÊNCIA", "Aguardando dados...");
        add(painelGrafico, "cell 0 3, spanx 1, grow");
        add(painelTendencia, "cell 1 3, spanx 2, grow, wrap");

        // Separador
        add(new JSeparator(), "cell 0 4, spanx 3, growx, wrap");

        // Row de ações
        btnIniciarScrum      = PanelViaUrls.buildGhostSmButton("Iniciar Scrum");
        btnFinalizarScrum    = PanelViaUrls.buildGhostSmButton("Finalizar Scrum");
        btnFinalizarExpertDev = PanelViaUrls.buildGhostSmButton("Finalizar ExpertDev");
        JButton btnExportar  = PanelViaUrls.buildGhostSmButton("Exportar ROI");

        lblGanho = new BadgeLabel("Ganho: 0%",
            ExpertDevTheme.SUCCESS_BG, ExpertDevTheme.SUCCESS,
            new Color(47, 158, 68, 51), ExpertDevTheme.RADIUS_LG);
        lblGanho.setFont(ExpertDevTheme.FONT_BODY.deriveFont(11f));

        JPanel rowAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rowAcoes.setOpaque(false);
        rowAcoes.add(btnIniciarScrum);
        rowAcoes.add(btnFinalizarScrum);
        rowAcoes.add(btnFinalizarExpertDev);
        rowAcoes.add(btnExportar);
        rowAcoes.add(Box.createHorizontalStrut(8));
        rowAcoes.add(lblGanho);

        add(rowAcoes, "cell 0 5, spanx 3, growx");
    }

    // Getters para GuiController
    public JTextField getCampoEstimativaPoker() { return campoEstimativaPoker; }
    public JTextField getCampoSprint()          { return campoSprint; }
    public JComboBox<String> getComboComplexidade() { return comboComplexidade; }
    public JPanel getPainelGrafico()            { return painelGrafico; }
    public JPanel getPainelTendencia()          { return painelTendencia; }
    public JButton getBtnIniciarScrum()         { return btnIniciarScrum; }
    public JButton getBtnFinalizarScrum()       { return btnFinalizarScrum; }
    public JButton getBtnFinalizarExpertDev()   { return btnFinalizarExpertDev; }
    public BadgeLabel getLblGanho()             { return lblGanho; }

    public void setGanho(String texto) {
        lblGanho.setText(texto);
        lblGanho.repaint();
    }

    private static JPanel buildInfoPanel(String headerText, String bodyText) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1));
        panel.setBackground(ExpertDevTheme.BG_PANEL);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        header.setBackground(ExpertDevTheme.SURFACE_ALT);
        JLabel lblH = new JLabel(headerText);
        lblH.setFont(ExpertDevTheme.FONT_LABEL);
        lblH.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        header.add(lblH);
        panel.add(header, BorderLayout.NORTH);

        JLabel body = new JLabel("<html><center>" + bodyText + "</center></html>");
        body.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        body.setForeground(ExpertDevTheme.TEXT_MUTED);
        body.setHorizontalAlignment(SwingConstants.CENTER);
        body.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        panel.add(body, BorderLayout.CENTER);

        return panel;
    }

    private static JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ExpertDevTheme.FONT_LABEL);
        lbl.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        return lbl;
    }

    private static JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ExpertDevTheme.FONT_LABEL.deriveFont(10f));
        lbl.setForeground(ExpertDevTheme.TEXT_MUTED);
        return lbl;
    }

    private static JTextField buildSmallTextField() {
        JTextField f = new JTextField();
        f.setFont(ExpertDevTheme.FONT_BODY);
        f.setBackground(ExpertDevTheme.SURFACE_ALT);
        f.setForeground(ExpertDevTheme.TEXT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        f.setPreferredSize(new Dimension(0, 30));
        return f;
    }

    private static void styleCombo(JComboBox<String> combo) {
        combo.setFont(ExpertDevTheme.FONT_BODY);
        combo.setBackground(ExpertDevTheme.SURFACE_ALT);
        combo.setForeground(ExpertDevTheme.TEXT_BODY);
        combo.setPreferredSize(new Dimension(0, 30));
    }
}

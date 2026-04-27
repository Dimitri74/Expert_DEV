package br.com.expertdev.ui.panels;

import br.com.expertdev.model.AuthSession;
import br.com.expertdev.pro.ui.ProAssistantPanel;
import br.com.expertdev.ui.theme.ExpertDevTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Aba "Assistente Pro" — wrapper do ProAssistantPanel com tokens do novo design.
 */
public class PanelAssistente extends JPanel {

    private final ProAssistantPanel proPanel;

    public PanelAssistente(AuthSession session) {
        setLayout(new MigLayout("insets 0, fill"));
        setBackground(ExpertDevTheme.BG_PANEL);

        // Header
        JPanel headerBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        headerBar.setBackground(ExpertDevTheme.SURFACE_ALT);
        headerBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ExpertDevTheme.BORDER));

        JLabel title = new JLabel("ASSISTENTE PRO");
        title.setFont(ExpertDevTheme.FONT_LABEL);
        title.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        headerBar.add(title);

        if (session != null && !session.isPremium()) {
            BadgeStub badge = new BadgeStub("Trial", ExpertDevTheme.WARNING_BG, ExpertDevTheme.WARNING);
            headerBar.add(badge);
        }

        add(headerBar, "cell 0 0, growx, wrap");

        // Painel Pro (delega tudo ao ProAssistantPanel existente — construtor sem argumentos)
        proPanel = new ProAssistantPanel();
        add(proPanel, "cell 0 1, grow");
    }

    public ProAssistantPanel getProPanel() {
        return proPanel;
    }

    /** Badge simples sem dependência de BadgeLabel para evitar conflito de renderização. */
    private static class BadgeStub extends JLabel {
        private final Color bg;
        BadgeStub(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            setForeground(fg);
            setFont(ExpertDevTheme.FONT_LABEL.deriveFont(10f));
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            } finally { g2.dispose(); }
            super.paintComponent(g);
        }
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 12, d.height + 4);
        }
    }
}

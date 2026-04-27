package br.com.expertdev.ui.components;

import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Componente de cabeçalho de aba para JTabbedPane.
 * Tab ativa: FG=PRIMARY, underline 2px PRIMARY.
 * Tab inativa: FG=TEXT_MUTED, hover FG=TEXT_BODY.
 * Uso: tabbedPane.setTabComponentAt(i, new TabHeaderRenderer(tabbedPane, i, "Texto"));
 */
public class TabHeaderRenderer extends JPanel {

    private final JTabbedPane tabbedPane;
    private final int index;
    private final JLabel label;
    private boolean hovered = false;

    public TabHeaderRenderer(JTabbedPane tabbedPane, int index, String text) {
        this(tabbedPane, index, text, null);
    }

    public TabHeaderRenderer(JTabbedPane tabbedPane, int index, String text, Icon icon) {
        this.tabbedPane = tabbedPane;
        this.index = index;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        label = new JLabel(text);
        label.setIcon(icon);
        label.setIconTextGap(6);
        label.setFont(ExpertDevTheme.FONT_BODY);
        label.setForeground(ExpertDevTheme.TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        add(label, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                refreshState();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                refreshState();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                tabbedPane.setSelectedIndex(index);
            }
        });
    }

    public void refreshState() {
        boolean active = tabbedPane.getSelectedIndex() == index;
        if (active) {
            label.setForeground(ExpertDevTheme.PRIMARY);
            label.setFont(ExpertDevTheme.FONT_BOLD);
        } else if (hovered) {
            label.setForeground(ExpertDevTheme.TEXT_BODY);
            label.setFont(ExpertDevTheme.FONT_BODY);
        } else {
            label.setForeground(ExpertDevTheme.TEXT_MUTED);
            label.setFont(ExpertDevTheme.FONT_BODY);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        boolean active = tabbedPane.getSelectedIndex() == index;
        if (active) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ExpertDevTheme.PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            } finally {
                g2.dispose();
            }
        }
    }
}

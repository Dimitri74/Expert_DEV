package br.com.expertdev.ui.components;

import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JButton com gradiente horizontal entre duas cores.
 * Usado principalmente no botão "Gerar Prompt".
 */
public class GradientButton extends JButton {

    private Color colorStart;
    private Color colorEnd;
    private boolean hovered = false;

    public GradientButton(String text, Color colorStart, Color colorEnd) {
        super(text);
        this.colorStart = colorStart;
        this.colorEnd = colorEnd;

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(ExpertDevTheme.WHITE);
        setFont(ExpertDevTheme.FONT_BOLD.deriveFont(14f));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            Color start = hovered ? colorStart.darker() : colorStart;
            Color end   = hovered ? colorEnd.darker()   : colorEnd;

            GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), 0, end);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);

            // Sombra sutil ao hover
            if (hovered) {
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}

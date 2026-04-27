package br.com.expertdev.ui.components;

import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel com fundo e borda arredondada via paintComponent.
 */
public class RoundedPanel extends JPanel {

    private Color backgroundColor;
    private Color borderColor;
    private int borderWidth;
    private int arcRadius;

    public RoundedPanel(Color backgroundColor, Color borderColor, int borderWidth, int arcRadius) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.arcRadius = arcRadius;
        setOpaque(false);
    }

    public RoundedPanel(Color backgroundColor, int arcRadius) {
        this(backgroundColor, ExpertDevTheme.BORDER, 1, arcRadius);
    }

    public RoundedPanel() {
        this(ExpertDevTheme.BG_PANEL, ExpertDevTheme.BORDER, 1, ExpertDevTheme.RADIUS_MD);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int bw = borderWidth;
            int arc = arcRadius * 2;

            // Fundo
            g2.setColor(backgroundColor);
            g2.fillRoundRect(bw, bw, getWidth() - bw * 2, getHeight() - bw * 2, arc, arc);

            // Borda
            if (borderWidth > 0 && borderColor != null) {
                g2.setStroke(new BasicStroke(borderWidth));
                g2.setColor(borderColor);
                g2.drawRoundRect(bw / 2, bw / 2, getWidth() - bw, getHeight() - bw, arc, arc);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    public void setBackgroundColor(Color c) { this.backgroundColor = c; repaint(); }
    public void setBorderColor(Color c)     { this.borderColor = c; repaint(); }
    public void setArcRadius(int r)         { this.arcRadius = r; repaint(); }
}

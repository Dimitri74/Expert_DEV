package br.com.expertdev.ui.components;

import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.swing.*;
import java.awt.*;

/**
 * JLabel com background colorido, padding e border-radius via paintComponent.
 * Usado para badges de versão, trial, premium, ROI, etc.
 */
public class BadgeLabel extends JLabel {

    private Color bgColor;
    private Color borderColor;
    private int arcRadius;
    private int padH = 8;
    private int padV = 3;

    public BadgeLabel(String text, Color bgColor, Color fgColor, int arcRadius) {
        super(text);
        this.bgColor = bgColor;
        this.borderColor = null;
        this.arcRadius = arcRadius;
        setForeground(fgColor);
        setFont(ExpertDevTheme.FONT_MONO_SM);
        setOpaque(false);
    }

    public BadgeLabel(String text, Color bgColor, Color fgColor, Color borderColor, int arcRadius) {
        this(text, bgColor, fgColor, arcRadius);
        this.borderColor = borderColor;
    }

    public void setPadding(int horizontal, int vertical) {
        this.padH = horizontal;
        this.padV = vertical;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + padH * 2, d.height + padV * 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = arcRadius * 2;

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
        } finally {
            g2.dispose();
        }

        // Centralizar texto com padding
        FontMetrics fm = g.getFontMetrics(getFont());
        String text = getText();
        if (text != null) {
            Graphics2D g2t = (Graphics2D) g.create();
            try {
                g2t.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2t.setFont(getFont());
                g2t.setColor(getForeground());
                int x = padH;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2t.drawString(text, x, y);
            } finally {
                g2t.dispose();
            }
        }
    }
}

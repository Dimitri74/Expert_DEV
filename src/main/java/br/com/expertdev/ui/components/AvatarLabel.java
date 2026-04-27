package br.com.expertdev.ui.components;

import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.swing.*;
import java.awt.*;

/**
 * JLabel circular com iniciais do usuário.
 */
public class AvatarLabel extends JLabel {

    private final String initials;
    private final Color bgColor;
    private final Color fgColor;
    private final int diameter;

    public AvatarLabel(String initials, Color bgColor, Color fgColor, int diameter) {
        this.initials = initials != null ? initials.toUpperCase() : "?";
        this.bgColor  = bgColor;
        this.fgColor  = fgColor;
        this.diameter = diameter;
        setOpaque(false);
        setPreferredSize(new Dimension(diameter, diameter));
        setMinimumSize(new Dimension(diameter, diameter));
        setMaximumSize(new Dimension(diameter, diameter));
    }

    public AvatarLabel(String username, int diameter) {
        this(buildInitials(username), ExpertDevTheme.PRIMARY, ExpertDevTheme.WHITE, diameter);
    }

    private static String buildInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return "" + parts[0].charAt(0) + parts[1].charAt(0);
        }
        return name.length() >= 2 ? name.substring(0, 2) : name.substring(0, 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            g2.setColor(bgColor);
            g2.fillOval(x, y, diameter, diameter);

            g2.setFont(ExpertDevTheme.FONT_BOLD.deriveFont((float) (diameter / 2.5)));
            g2.setColor(fgColor);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(initials);
            int tx = x + (diameter - tw) / 2;
            int ty = y + (diameter - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initials, tx, ty);
        } finally {
            g2.dispose();
        }
    }
}

package br.com.expertdev.ui.components;

import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Card clicável para seleção de modo — Expert Dev v2.6.0-BETA.
 * Pinta todo o conteúdo via paintComponent para suportar shadow, border-radius e animações.
 */
public class ModeCardPanel extends JPanel {

    private static final int CARD_W  = 280;
    private static final int CARD_H  = 360;
    private static final int ARC     = 16;     // border-radius
    private static final int PAD_X   = 24;
    private static final int PAD_TOP = 28;
    private static final int LOGO_BOX_W = 96;
    private static final int LOGO_BOX_H = 80;

    private final ImageIcon cardLogo;
    private final String    title;
    private final String    description;
    private final String[]  features;
    private final String    ctaText;
    private final Color     accentColor;
    private final boolean   cardEnabled;
    private final String    badgeText;
    private final Color     badgeBg;
    private final Color     badgeFg;

    private boolean hovered = false;
    private boolean pressed = false;
    private Runnable clickListener;

    public ModeCardPanel(ImageIcon cardLogo, String title, String description,
                         String[] features, String ctaText, Color accentColor,
                         boolean enabled, String badgeText, Color badgeBg, Color badgeFg) {
        this.cardLogo    = cardLogo;
        this.title       = title;
        this.description = description;
        this.features    = features;
        this.ctaText     = ctaText;
        this.accentColor = accentColor;
        this.cardEnabled = enabled;
        this.badgeText   = badgeText;
        this.badgeBg     = badgeBg;
        this.badgeFg     = badgeFg;

        setOpaque(false);
        setLayout(null);
        setFocusable(cardEnabled);

        if (cardEnabled) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hovered = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (pressed && hovered && clickListener != null) clickListener.run();
                    pressed = false;
                    repaint();
                }
            });
            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    int k = e.getKeyCode();
                    if ((k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) && clickListener != null) {
                        clickListener.run();
                    }
                }
            });
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost  (FocusEvent e) { repaint(); }
            });
        }
    }

    public void setClickListener(Runnable listener) {
        this.clickListener = listener;
    }

    @Override public Dimension getPreferredSize() { return new Dimension(CARD_W, CARD_H); }
    @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
    @Override public Dimension getMaximumSize()   { return getPreferredSize(); }

    // ── Pintura ───────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            // Desabilitado: reduz opacidade do card inteiro
            Composite originalComposite = g2.getComposite();
            if (!cardEnabled) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            }

            int w = getWidth();
            int h = getHeight();

            // Sombra (multi-layer aproximada)
            int shadowLayers = hovered ? 6 : 3;
            int shadowBase   = hovered ? 40 : 20;
            for (int i = shadowLayers; i >= 1; i--) {
                int a = shadowBase / i;
                g2.setColor(new Color(27, 45, 94, a));
                g2.fillRoundRect(i, i + 2, w - i * 2, h - i * 2, ARC * 2, ARC * 2);
            }

            // Fundo branco do card
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, ARC * 2, ARC * 2);

            // Borda (accent no hover, neutra em repouso)
            Color borderColor = hovered ? accentColor : ExpertDevTheme.BORDER;
            float borderWidth = hovered ? 2f : 1.5f;
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRoundRect(0, 0, w - 1, h - 1, ARC * 2, ARC * 2);

            // Foco (outline acessibilidade)
            if (isFocusOwner()) {
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 80));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, ARC * 2, ARC * 2);
            }

            // Restaura composite antes de pintar conteúdo (disabled já aplicou alfa global)
            paintContent(g2, w, h);

            // Badge no canto superior direito (fora do clip do card)
            paintBadge(g2, w);

            // Banner inferior para card desabilitado
            if (!cardEnabled) {
                paintDisabledBanner(g2, w, h);
            }

        } finally {
            g2.dispose();
        }
    }

    private void paintContent(Graphics2D g2, int w, int h) {
        int y = PAD_TOP;

        // ── Área de logo ───────────────────────────────────────────────────
        int iconX = (w - LOGO_BOX_W) / 2;

        if (cardLogo != null) {
            g2.setColor(new Color(0xF8FAFD));
            g2.fillRoundRect(iconX, y, LOGO_BOX_W, LOGO_BOX_H, 22, 22);

            int iw = Math.max(1, cardLogo.getIconWidth());
            int ih = Math.max(1, cardLogo.getIconHeight());
            double scale = Math.min((LOGO_BOX_W - 14) / (double) iw, (LOGO_BOX_H - 14) / (double) ih);
            int drawW = Math.max(1, (int) Math.round(iw * scale));
            int drawH = Math.max(1, (int) Math.round(ih * scale));
            int drawX = iconX + (LOGO_BOX_W - drawW) / 2;
            int drawY = y + (LOGO_BOX_H - drawH) / 2;

            Object interpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(cardLogo.getImage(), drawX, drawY, drawW, drawH, null);
            if (interpolation != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
            }

            g2.setColor(ExpertDevTheme.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(iconX, y, LOGO_BOX_W - 1, LOGO_BOX_H - 1, 22, 22);
        } else {
            // Contêiner gradiente roxo (experimental)
            GradientPaint iconGrad = new GradientPaint(
                iconX, y, new Color(0xF5F0FF),
                iconX + 72, y + 72, new Color(0xDDD6FE)
            );
            g2.setPaint(iconGrad);
            g2.fillRoundRect(iconX + 12, y + 4, 72, 72, 12 * 2, 12 * 2);
            g2.setPaint(null);

            // Emoji foguete
            Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 30);
            g2.setFont(emojiFont);
            g2.setColor(ExpertDevTheme.PREMIUM);
            String rocket = "\uD83D\uDE80";
            FontMetrics efm = g2.getFontMetrics();
            g2.drawString(rocket,
                iconX + 12 + (72 - efm.stringWidth(rocket)) / 2,
                y + 4 + (72 - efm.getHeight()) / 2 + efm.getAscent());
        }
        y += LOGO_BOX_H + 14;

        // ── Título ─────────────────────────────────────────────────────────
        Font titleFont = ExpertDevTheme.FONT_BOLD.deriveFont(Font.BOLD, 17f);
        g2.setFont(titleFont);
        g2.setColor(ExpertDevTheme.BRAND_DARK);
        FontMetrics tfm = g2.getFontMetrics();
        String t = title != null ? title : "";
        g2.drawString(t, (w - tfm.stringWidth(t)) / 2, y + tfm.getAscent());
        y += tfm.getHeight() + 8;

        // ── Descrição (até 2 linhas) ────────────────────────────────────────
        Font descFont = ExpertDevTheme.FONT_BODY.deriveFont(12.5f);
        g2.setFont(descFont);
        g2.setColor(ExpertDevTheme.TEXT_MUTED);
        FontMetrics dfm = g2.getFontMetrics();
        if (description != null) {
            String[] lines = description.split("\n");
            for (int i = 0; i < Math.min(lines.length, 2); i++) {
                String line = lines[i].trim();
                g2.drawString(line, (w - dfm.stringWidth(line)) / 2, y + dfm.getAscent());
                y += dfm.getHeight() + 2;
            }
        }
        y += 12;

        // ── Features ───────────────────────────────────────────────────────
        Font featFont  = ExpertDevTheme.FONT_BODY.deriveFont(12f);
        Font checkFont = ExpertDevTheme.FONT_BOLD.deriveFont(Font.BOLD, 12f);
        FontMetrics ffm = g2.getFontMetrics(featFont);
        int checkW = g2.getFontMetrics(checkFont).stringWidth("\u2713") + 6;

        if (features != null) {
            for (int i = 0; i < Math.min(features.length, 4); i++) {
                int rowY = y + ffm.getAscent();
                // Checkmark
                g2.setFont(checkFont);
                g2.setColor(accentColor);
                g2.drawString("\u2713", PAD_X, rowY);
                // Texto
                g2.setFont(featFont);
                g2.setColor(ExpertDevTheme.TEXT_SECONDARY);
                g2.drawString(features[i], PAD_X + checkW, rowY);
                y += ffm.getHeight() + 5;
            }
        }

        // ── Botão CTA ──────────────────────────────────────────────────────
        int btnH = 42;
        int btnY = h - btnH - 24;
        int btnW = w - PAD_X * 2;

        if (cardEnabled) {
            Color accentDark = accentColor.darker();
            GradientPaint btnGrad = new GradientPaint(
                PAD_X, btnY, accentColor,
                PAD_X + btnW, btnY, accentDark
            );
            g2.setPaint(btnGrad);
            g2.fillRoundRect(PAD_X, btnY, btnW, btnH, ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
            g2.setPaint(null);
        } else {
            g2.setColor(ExpertDevTheme.GRAY_300);
            g2.fillRoundRect(PAD_X, btnY, btnW, btnH, ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
        }

        Font ctaFont = ExpertDevTheme.FONT_BOLD.deriveFont(Font.BOLD, 13f);
        g2.setFont(ctaFont);
        g2.setColor(cardEnabled ? Color.WHITE : ExpertDevTheme.TEXT_MUTED);
        FontMetrics cfm = g2.getFontMetrics();
        String cta = ctaText != null ? ctaText : "";
        g2.drawString(cta,
            PAD_X + (btnW - cfm.stringWidth(cta)) / 2,
            btnY  + (btnH - cfm.getHeight()) / 2 + cfm.getAscent());
    }

    private void paintBadge(Graphics2D g2, int w) {
        if (badgeText == null || badgeText.isEmpty()) return;
        Font badgeFont = ExpertDevTheme.FONT_LABEL.deriveFont(Font.BOLD, 9f);
        g2.setFont(badgeFont);
        FontMetrics bfm = g2.getFontMetrics();
        String upper  = badgeText.toUpperCase();
        int badgeW    = bfm.stringWidth(upper) + 14;
        int badgeH    = 20;
        int badgeX    = w - badgeW - 10;
        int badgeY    = 10;

        g2.setColor(badgeBg);
        g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 20, 20);
        g2.setColor(badgeFg);
        g2.drawString(upper,
            badgeX + (badgeW - bfm.stringWidth(upper)) / 2,
            badgeY + (badgeH - bfm.getHeight()) / 2 + bfm.getAscent());
    }

    private void paintDisabledBanner(Graphics2D g2, int w, int h) {
        int bannerH = 30;
        int bannerY = h - bannerH;

        Shape oldClip = g2.getClip();
        g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, ARC * 2, ARC * 2));

        g2.setColor(ExpertDevTheme.PREMIUM);
        g2.fillRect(0, bannerY, w, bannerH);

        Font bannerFont = ExpertDevTheme.FONT_LABEL.deriveFont(Font.BOLD, 10f);
        g2.setFont(bannerFont);
        g2.setColor(Color.WHITE);
        String bannerText = "\uD83D\uDD12 EM DESENVOLVIMENTO";
        FontMetrics bfm = g2.getFontMetrics();
        g2.drawString(bannerText,
            (w - bfm.stringWidth(bannerText)) / 2,
            bannerY + (bannerH - bfm.getHeight()) / 2 + bfm.getAscent());

        g2.setClip(oldClip);
    }
}
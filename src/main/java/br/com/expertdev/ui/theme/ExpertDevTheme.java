package br.com.expertdev.ui.theme;

import java.awt.*;
import java.io.InputStream;

/**
 * Design Tokens — Expert Dev v2.6.0-BETA
 * Mapeamento 1:1 do protótipo HTML aprovado.
 */
public final class ExpertDevTheme {

    private ExpertDevTheme() {}

    public static final String[] REQUIRED_FONT_RESOURCES = {
        "/fonts/IBMPlexSans-Variable.ttf",
        "/fonts/IBMPlexMono-Regular.ttf",
        "/fonts/IBMPlexMono-Medium.ttf"
    };

    // ── Brand ────────────────────────────────────────────────
    public static final Color BRAND_DARK     = hex("#1B2D5E");
    public static final Color BRAND_BLUE     = hex("#1E56A0");
    public static final Color BRAND_BLUE_MID = hex("#2563C6");
    public static final Color BRAND_AMBER    = hex("#F5A623");
    public static final Color BRAND_LIGHT    = hex("#EEF4FF");

    // ── Primary actions ──────────────────────────────────────
    public static final Color PRIMARY        = hex("#1E56A0");
    public static final Color PRIMARY_DARK   = hex("#163F7A");
    public static final Color PRIMARY_LIGHT  = hex("#EEF4FF");
    public static final Color PRIMARY_FOCUS  = new Color(30, 86, 160, 38);

    // ── Premium ──────────────────────────────────────────────
    public static final Color PREMIUM        = hex("#7C3AED");
    public static final Color PREMIUM_LIGHT  = hex("#F5F0FF");

    // ── Semantic ─────────────────────────────────────────────
    public static final Color SUCCESS        = hex("#2F9E44");
    public static final Color SUCCESS_BG     = hex("#EBFBEE");
    public static final Color WARNING        = hex("#E67700");
    public static final Color WARNING_BG     = hex("#FFF4E6");
    public static final Color DANGER         = hex("#C92A2A");
    public static final Color DANGER_BG      = hex("#FFF5F5");
    public static final Color INFO           = hex("#1971C2");
    public static final Color INFO_BG        = hex("#E7F5FF");

    // ── Neutrals ─────────────────────────────────────────────
    public static final Color WHITE          = hex("#FFFFFF");
    public static final Color GRAY_50        = hex("#F8F9FA");
    public static final Color GRAY_100       = hex("#F1F3F5");
    public static final Color GRAY_200       = hex("#E9ECEF");
    public static final Color GRAY_300       = hex("#DEE2E6");
    public static final Color GRAY_400       = hex("#CED4DA");
    public static final Color GRAY_500       = hex("#ADB5BD");
    public static final Color GRAY_600       = hex("#868E96");
    public static final Color GRAY_700       = hex("#495057");
    public static final Color GRAY_800       = hex("#343A40");
    public static final Color GRAY_900       = hex("#212529");

    // ── Surfaces ─────────────────────────────────────────────
    public static final Color BG_APP         = hex("#F4F6FB");
    public static final Color BG_PANEL       = hex("#FFFFFF");
    public static final Color BG_HEADER      = hex("#FFFFFF");
    public static final Color SURFACE_ALT    = hex("#F8F9FA");

    // ── Text ─────────────────────────────────────────────────
    public static final Color TEXT           = hex("#1B2D5E");
    public static final Color TEXT_BODY      = hex("#343A40");
    public static final Color TEXT_SECONDARY = hex("#495057");
    public static final Color TEXT_MUTED     = hex("#868E96");

    // ── Borders ──────────────────────────────────────────────
    public static final Color BORDER         = hex("#DEE2E6");
    public static final Color BORDER_FOCUS   = hex("#1E56A0");

    // ── Tipografia ───────────────────────────────────────────
    public static final Font FONT_BODY    = new Font("IBM Plex Sans", Font.PLAIN,  13);
    public static final Font FONT_MEDIUM  = new Font("IBM Plex Sans", Font.PLAIN,  13);
    public static final Font FONT_BOLD    = new Font("IBM Plex Sans", Font.BOLD,   13);
    public static final Font FONT_LABEL   = new Font("IBM Plex Sans", Font.BOLD,   11);
    public static final Font FONT_TITLE   = new Font("IBM Plex Sans", Font.BOLD,   15);
    public static final Font FONT_HEADER  = new Font("IBM Plex Sans", Font.BOLD,   16);
    public static final Font FONT_MONO    = new Font("IBM Plex Mono", Font.PLAIN,  12);
    public static final Font FONT_MONO_SM = new Font("IBM Plex Mono", Font.PLAIN,  11);

    // ── Espaçamentos ─────────────────────────────────────────
    public static final int RADIUS_SM     = 4;
    public static final int RADIUS_MD     = 8;
    public static final int RADIUS_LG     = 12;
    public static final int RADIUS_XL     = 16;
    public static final int PAD_XS        = 4;
    public static final int PAD_SM        = 8;
    public static final int PAD_MD        = 12;
    public static final int PAD_LG        = 16;
    public static final int PAD_XL        = 20;
    public static final int PAD_2XL       = 28;
    public static final int HEADER_HEIGHT = 58;
    public static final int CONTEXT_BAR_H = 42;
    public static final int INPUT_HEIGHT  = 36;
    public static final int BTN_HEIGHT    = 36;
    public static final int BTN_SM_HEIGHT = 28;

    // ── Helper ───────────────────────────────────────────────
    public static Color hex(String h) {
        return Color.decode(h);
    }

    /**
     * Registra as fontes IBM Plex do classpath.
     * Chamar uma vez em main() antes de criar qualquer componente.
     * Retorna false se algum recurso obrigatório estiver ausente.
     */
    public static boolean registerFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        boolean allLoaded = true;
        for (String path : REQUIRED_FONT_RESOURCES) {
            InputStream is = null;
            try {
                is = ExpertDevTheme.class.getResourceAsStream(path);
                if (is != null) {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
                } else {
                    allLoaded = false;
                }
            } catch (Exception ignored) {
                allLoaded = false;
            } finally {
                if (is != null) {
                    try { is.close(); } catch (Exception ignored) {}
                }
            }
        }
        return allLoaded;
    }
}

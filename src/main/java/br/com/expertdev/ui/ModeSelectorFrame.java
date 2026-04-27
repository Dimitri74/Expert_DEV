package br.com.expertdev.ui;

import br.com.expertdev.model.AuthSession;
import br.com.expertdev.ui.components.ModeCardPanel;
import br.com.expertdev.ui.theme.ExpertDevTheme;
import br.com.expertdev.ui.util.FadeTransition;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

/**
 * Tela de seleção de modo — Expert Dev v2.6.0-BETA.
 * Aparece após login e antes do MainFrame.
 * Fluxo: LoginFrame → ModeSelectorFrame → [clique GID + fade] → MainFrame
 */
public class ModeSelectorFrame extends JFrame {

    private final AuthSession session;

    public ModeSelectorFrame(AuthSession session) {
        super("Expert Dev — Selecione o modo de trabalho");
        this.session = session;
        construir();
    }

    private void construir() {
        setSize(800, 600);
        setMinimumSize(new Dimension(800, 600));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ícone da janela
        try {
            InputStream is = getClass().getResourceAsStream("/icons/logo_transparente.png");
            if (is != null) setIconImage(ImageIO.read(is));
        } catch (Exception ignored) {}

        // Painel raiz com gradiente diagonal
        JPanel root = new GradientPanel();
        root.setLayout(new MigLayout(
            "fill, insets 36 40 28 40, gap 0",
            "[grow, center]",
            "[]14[]28[]"
        ));
        setContentPane(root);

        root.add(buildHeader(),    "wrap, growx");
        root.add(buildCards(),     "wrap, align center");
        root.add(buildFooter(),    "align center");
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new MigLayout("fillx, insets 0", "[grow, center]", "[]8[]8[]"));
        header.setOpaque(false);

        // Logo Expert Dev
        ImageIcon logo = loadScaledIcon("/icons/logo_transparente.png", -1, 48);
        JLabel logoLabel = logo != null
            ? new JLabel(logo)
            : new JLabel("Expert Dev");
        if (logo == null) {
            logoLabel.setFont(ExpertDevTheme.FONT_HEADER.deriveFont(Font.BOLD, 22f));
            logoLabel.setForeground(ExpertDevTheme.BRAND_DARK);
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Título
        JLabel title = new JLabel("Selecione o modo de trabalho");
        title.setFont(ExpertDevTheme.FONT_TITLE.deriveFont(Font.BOLD, 22f));
        title.setForeground(ExpertDevTheme.BRAND_DARK);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // Subtítulo
        JLabel subtitle = new JLabel("Escolha como deseja utilizar o Expert Dev nesta sess\u00e3o");
        subtitle.setFont(ExpertDevTheme.FONT_BODY.deriveFont(14f));
        subtitle.setForeground(ExpertDevTheme.TEXT_MUTED);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(logoLabel, "wrap, align center");
        header.add(title,     "wrap, align center");
        header.add(subtitle,  "align center");
        return header;
    }

    // ── Cards ─────────────────────────────────────────────────────────────────

    private JPanel buildCards() {
        JPanel panel = new JPanel(new MigLayout("insets 0, gap 24", "[][]", "[]"));
        panel.setOpaque(false);

        // Card IBM-GID (ativo)
        ModeCardPanel ibmCard = new ModeCardPanel(
            loadScaledIcon("/icons/logo_ibm.png", 72, 72),
            "Padr\u00e3o IBM-GID",
            "Extra\u00e7\u00e3o de regras de neg\u00f3cio seguindo\nrigorosamente o padr\u00e3o IBM GID.",
            new String[]{
                "Padr\u00e3o IBM-GID completo",
                "Gera\u00e7\u00e3o de prompts estruturados",
                "Suporte a Confluence e SharePoint",
                "Exporta\u00e7\u00e3o para Word e PDF"
            },
            "Entrar no modo IBM-GID \u2192",
            ExpertDevTheme.BRAND_BLUE,
            true,
            "Est\u00e1vel",
            ExpertDevTheme.SUCCESS_BG,
            ExpertDevTheme.SUCCESS
        );
        ibmCard.setClickListener(this::abrirMainFrame);

        // Card Nova Versão (desabilitado)
        ModeCardPanel expCard = new ModeCardPanel(
            null,
            "Nova Vers\u00e3o",
            "Funcionalidades experimentais e novos\nfluxos de gera\u00e7\u00e3o em desenvolvimento.",
            new String[]{
                "Novos perfis de extra\u00e7\u00e3o",
                "Modelos IA adicionais",
                "Interface aprimorada",
                "Feedback direto ao time"
            },
            "\uD83D\uDD12 Em breve",
            ExpertDevTheme.PREMIUM,
            false,
            "Experimental",
            ExpertDevTheme.PREMIUM_LIGHT,
            ExpertDevTheme.PREMIUM
        );

        panel.add(ibmCard);
        panel.add(expCard);
        return panel;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        footer.setOpaque(false);
        String[] parts = {"Expert Dev v2.6.0-BETA", "\u2022", "Marcus Dimitri", "\u2022", "Enterprise Ready"};
        for (String part : parts) {
            JLabel lbl = new JLabel(part);
            lbl.setFont(ExpertDevTheme.FONT_BODY.deriveFont(11f));
            lbl.setForeground(part.equals("\u2022") ? ExpertDevTheme.GRAY_400 : ExpertDevTheme.TEXT_MUTED);
            footer.add(lbl);
        }
        return footer;
    }

    // ── Transição ─────────────────────────────────────────────────────────────

    private void abrirMainFrame() {
        FadeTransition.fadeTo(this, () -> {
            new MainFrame(session); // MainFrame chama setVisible(true) internamente
            dispose();
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Carrega e escala um ícone do classpath.
     * Se w=-1, calcula proporcional a partir de h; se h=-1, a partir de w.
     */
    public static ImageIcon loadScaledIcon(String path, int w, int h) {
        InputStream is = null;
        try {
            is = ModeSelectorFrame.class.getResourceAsStream(path);
            if (is == null) return null;
            java.awt.image.BufferedImage raw = ImageIO.read(is);
            if (raw == null) return null;
            int fw = w > 0 ? w : (int) (raw.getWidth()  * ((double) h / raw.getHeight()));
            int fh = h > 0 ? h : (int) (raw.getHeight() * ((double) w / raw.getWidth()));
            Image scaled = raw.getScaledInstance(fw, fh, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        } finally {
            if (is != null) try { is.close(); } catch (Exception ignored) {}
        }
    }

    // ── Painel com gradiente diagonal ─────────────────────────────────────────

    static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                    0, 0,             new Color(0xEEF4FF),
                    getWidth(), getHeight(), new Color(0xF4F0FF)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            } finally {
                g2.dispose();
            }
        }
    }
}
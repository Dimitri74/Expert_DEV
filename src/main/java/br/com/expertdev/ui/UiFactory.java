package br.com.expertdev.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.awt.BasicStroke;

/**
 * Fábrica de componentes visuais reutilizáveis da GUI.
 * Cria botões, rótulos, ícones e carrega imagens sem conter lógica de negócio.
 * Todos os métodos dependem de AppTheme para respeitar o tema ativo.
 */
public class UiFactory {

    private final AppTheme theme;

    public UiFactory(AppTheme theme) {
        this.theme = theme;
    }

    // ─── Rótulos e scroll ─────────────────────────────────────────────────────

    public JLabel criarRotulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(AppTheme.FONTE_ROTULO);
        lbl.setForeground(theme.corTexto);
        return lbl;
    }

    public JScrollPane criarScrollPane(JComponent componente) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(BorderFactory.createLineBorder(theme.corBorda));
        scroll.getVerticalScrollBar().setBackground(theme.corPainel);
        scroll.setBackground(theme.corPainel);
        return scroll;
    }

    // ─── Botões ───────────────────────────────────────────────────────────────

    public JButton criarBotaoAcao(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        configurarPaletaBotao(
                btn,
                Color.WHITE,
                theme.isClaroAtivo() ? new Color(226, 232, 240) : new Color(203, 213, 225),
                theme.corDestaque,
                theme.isClaroAtivo() ? new Color(148, 163, 184) : new Color(71, 85, 105)
        );
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 44));
        return btn;
    }

    public JButton criarBotaoPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(AppTheme.FONTE_BOTAO);
        configurarPaletaBotao(
                btn,
                Color.WHITE,
                theme.isClaroAtivo() ? new Color(226, 232, 240) : new Color(203, 213, 225),
                theme.corDestaque2,
                theme.isClaroAtivo() ? new Color(148, 163, 184) : new Color(71, 85, 105)
        );
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(AppTheme.FONTE_BOTAO);
        if (theme.isClaroAtivo()) {
            configurarPaletaBotao(
                    btn,
                    theme.corTexto,
                    new Color(100, 116, 139),
                    theme.corPainel,
                    new Color(226, 232, 240)
            );
        } else {
            configurarPaletaBotao(
                    btn,
                    new Color(241, 245, 249),
                    new Color(186, 196, 213),
                    new Color(42, 48, 70),
                    new Color(60, 68, 92)
            );
        }
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.corBorda),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Botão de diálogo com cor customizada e efeito hover. */
    public JButton criarBotaoDialogo(String texto, Color cor) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setBackground(cor);
        botao.setForeground(new Color(15, 23, 42));
        botao.setBorder(BorderFactory.createLineBorder(cor, 2));
        botao.setFocusPainted(false);
        botao.setContentAreaFilled(true);
        botao.setOpaque(true);
        botao.setPreferredSize(new Dimension(150, 45));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(aumentarBrilho(cor, 1.1f));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(cor);
            }
        });
        return botao;
    }

    public void configurarPaletaBotao(final JButton btn,
                                      final Color textoHabilitado,
                                      final Color textoDesabilitado,
                                      final Color fundoHabilitado,
                                      final Color fundoDesabilitado) {
        btn.setForeground(textoHabilitado);
        btn.setBackground(fundoHabilitado);
        btn.addPropertyChangeListener("enabled", evt -> {
            boolean habilitado = Boolean.TRUE.equals(evt.getNewValue());
            btn.setForeground(habilitado ? textoHabilitado : textoDesabilitado);
            btn.setBackground(habilitado ? fundoHabilitado : fundoDesabilitado);
        });
    }

    public Color aumentarBrilho(Color cor, float fator) {
        int r = Math.min(255, (int) (cor.getRed() * fator));
        int g = Math.min(255, (int) (cor.getGreen() * fator));
        int b = Math.min(255, (int) (cor.getBlue() * fator));
        return new Color(r, g, b);
    }

    // ─── Ícones funcionais (log, status) ─────────────────────────────────────

    public Icon criarIconeInfo(int tam) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(theme.corDestaque);
                g2.fillOval(x, y, tam, tam);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Monospaced", Font.BOLD, tam - 2));
                g2.drawString("i", x + tam / 3 + 1, y + tam - 2);
                g2.dispose();
            }
            public int getIconWidth()  { return tam; }
            public int getIconHeight() { return tam; }
        };
    }

    public Icon criarIconeSucesso(int tam) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(theme.corSucesso);
                g2.fillOval(x, y, tam, tam);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(x + 3, y + tam / 2, x + tam / 2 - 1, y + tam - 4);
                g2.drawLine(x + tam / 2 - 1, y + tam - 4, x + tam - 3, y + 3);
                g2.dispose();
            }
            public int getIconWidth()  { return tam; }
            public int getIconHeight() { return tam; }
        };
    }

    public Icon criarIconeAlerta(int tam) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 180, 0));
                int[] px = {x + tam / 2, x, x + tam};
                int[] py = {y, y + tam, y + tam};
                g2.fillPolygon(px, py, 3);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Monospaced", Font.BOLD, tam - 4));
                g2.drawString("!", x + tam / 2 - 2, y + tam - 2);
                g2.dispose();
            }
            public int getIconWidth()  { return tam; }
            public int getIconHeight() { return tam; }
        };
    }

    public Icon criarIconeErro(int tam) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(theme.corErro);
                g2.fillOval(x, y, tam, tam);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(x + 4, y + 4, x + tam - 4, y + tam - 4);
                g2.drawLine(x + tam - 4, y + 4, x + 4, y + tam - 4);
                g2.dispose();
            }
            public int getIconWidth()  { return tam; }
            public int getIconHeight() { return tam; }
        };
    }

    // ─── Ícones de branding ───────────────────────────────────────────────────

    public ImageIcon criarIconeUsuarioCircular(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.setColor(new Color(30, 120, 240));
        g.fillOval(0, 0, size, size);

        g.setColor(new Color(0, 0, 0, 30));
        g.fillOval(size / 6, size / 4, size * 2 / 3, size * 3 / 4);

        g.setColor(Color.WHITE);
        int headDiam = size * 38 / 100;
        int headX    = (size - headDiam) / 2;
        int headY    = size * 14 / 100;
        g.fillOval(headX, headY, headDiam, headDiam);

        int bodyW = size * 62 / 100;
        int bodyH = size * 38 / 100;
        int bodyX = (size - bodyW) / 2;
        int bodyY = size * 53 / 100;
        g.fillArc(bodyX, bodyY, bodyW, bodyH, 0, 180);

        g.dispose();
        return new ImageIcon(img);
    }

    /** Tenta carregar o ícone Java oficial; se falhar, usa fallback desenhado. */
    public Icon criarIconeJava(int tamanho) {
        URL recurso = UiFactory.class.getResource("/icons/java_official.png");
        if (recurso != null) {
            ImageIcon original     = new ImageIcon(recurso);
            int larguraOriginal    = Math.max(1, original.getIconWidth());
            int alturaOriginal     = Math.max(1, original.getIconHeight());
            double escala          = Math.min((double) tamanho / larguraOriginal, (double) tamanho / alturaOriginal);
            int larguraFinal       = Math.max(1, (int) Math.round(larguraOriginal * escala));
            int alturaFinal        = Math.max(1, (int) Math.round(alturaOriginal * escala));
            int x                  = (tamanho - larguraFinal) / 2;
            int y                  = (tamanho - alturaFinal) / 2;

            BufferedImage canvas = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(original.getImage(), x, y, larguraFinal, alturaFinal, null);
            } finally {
                g2.dispose();
            }
            return new ImageIcon(canvas);
        }

        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color azul    = theme.isClaroAtivo() ? new Color(37, 99, 235)  : new Color(125, 211, 252);
            Color vermelho = theme.isClaroAtivo() ? new Color(220, 38, 38) : new Color(248, 113, 113);
            g2.setColor(azul);
            g2.drawArc(4, tamanho - 7, tamanho - 8, 4, 0, 180);
            g2.draw(new RoundRectangle2D.Double(6, tamanho - 13, 10, 7, 2, 2));
            g2.drawArc(15, tamanho - 12, 4, 5, -60, 230);
            g2.setColor(vermelho);
            g2.draw(new QuadCurve2D.Double(9,  tamanho - 14, 6,  tamanho - 18, 9,  tamanho - 21));
            g2.draw(new QuadCurve2D.Double(13, tamanho - 14, 16, tamanho - 18, 13, tamanho - 21));
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    public Icon criarIconeIa(int tamanho) {
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base    = theme.isClaroAtivo() ? new Color(79, 70, 229)   : new Color(125, 211, 252);
            Color detalhe = theme.isClaroAtivo() ? new Color(124, 58, 237)  : new Color(196, 181, 253);
            g2.setColor(base);
            g2.fill(new RoundRectangle2D.Double(2, 2, tamanho - 4, tamanho - 4, 4, 4));
            g2.setColor(new Color(255, 255, 255, 235));
            double centroTam = Math.max(4, tamanho * 0.28);
            double centroPos = (tamanho - centroTam) / 2.0;
            g2.fill(new Ellipse2D.Double(centroPos, centroPos, centroTam, centroTam));
            g2.setColor(detalhe);
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int m = tamanho / 2;
            g2.drawLine(m, 2, m, 5);
            g2.drawLine(m, tamanho - 3, m, tamanho - 6);
            g2.drawLine(2, m, 5, m);
            g2.drawLine(tamanho - 3, m, tamanho - 6, m);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    public Icon criarIconeClaude(int tamanho) {
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base    = theme.isClaroAtivo() ? new Color(120, 53, 15)  : new Color(251, 191, 36);
            Color detalhe = theme.isClaroAtivo() ? new Color(146, 64, 14)  : new Color(254, 240, 138);
            g2.setColor(base);
            g2.fill(new RoundRectangle2D.Double(2, 2, tamanho - 4, tamanho - 4, 4, 4));
            g2.setColor(detalhe);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(3, 3, tamanho - 6, tamanho - 6, 25, 290);
            g2.drawLine(tamanho / 2, 4, tamanho / 2, tamanho - 4);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    public Icon criarIconeEconomico(int tamanho) {
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color verde   = theme.isClaroAtivo() ? new Color(22, 163, 74)  : new Color(74, 222, 128);
            Color detalhe = theme.isClaroAtivo() ? new Color(21, 128, 61)  : new Color(187, 247, 208);
            g2.setColor(verde);
            g2.fill(new RoundRectangle2D.Double(2, 2, tamanho - 4, tamanho - 4, 4, 4));
            g2.setColor(detalhe);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(tamanho / 2, 3, tamanho / 2, tamanho - 3);
            g2.drawLine(4, tamanho / 2, tamanho - 4, tamanho / 2);
            g2.drawArc(4, 4, tamanho - 8, tamanho - 8, 40, 100);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    public Icon criarIconeLocal(int tamanho) {
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base    = theme.isClaroAtivo() ? new Color(15, 23, 42)   : new Color(226, 232, 240);
            Color detalhe = theme.isClaroAtivo() ? new Color(71, 85, 105)  : new Color(148, 163, 184);
            g2.setColor(base);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Double(1.5, 1.5, tamanho - 3.0, tamanho - 5.5, 2.5, 2.5));
            g2.drawLine(tamanho / 2, tamanho - 4, tamanho / 2, tamanho - 2);
            g2.setColor(detalhe);
            g2.drawLine(tamanho / 2 - 3, tamanho - 1, tamanho / 2 + 3, tamanho - 1);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(img);
    }

    // ─── Pipeline de imagem / logo ────────────────────────────────────────────

    public Icon carregarLogoProjeto(int largura, int altura) {
        BufferedImage logoOriginal = carregarImagemLogoPorTema();
        if (logoOriginal == null) return null;

        BufferedImage logoRecortado = recortarTransparencia(logoOriginal);
        BufferedImage logoAjustado  = aplicarContrasteLogo(logoRecortado);

        int margemX        = Math.max(8, (int) Math.round(largura * 0.035));
        int margemY        = Math.max(6, (int) Math.round(altura  * 0.08));
        int areaUtilLargura = Math.max(1, largura - margemX * 2);
        int areaUtilAltura  = Math.max(1, altura  - margemY * 2);

        int ow    = Math.max(1, logoAjustado.getWidth());
        int oh    = Math.max(1, logoAjustado.getHeight());
        double escala = Math.min((double) areaUtilLargura / ow, (double) areaUtilAltura / oh);
        int w     = Math.max(1, (int) Math.round(ow * escala));
        int h     = Math.max(1, (int) Math.round(oh * escala));

        BufferedImage canvas = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            int x = margemX + (areaUtilLargura - w) / 2;
            int y = margemY + (areaUtilAltura  - h) / 2;
            g2.drawImage(logoAjustado, x, y, w, h, null);
        } finally {
            g2.dispose();
        }
        return new ImageIcon(canvas);
    }

    public BufferedImage carregarImagemLogoPorTema() {
        String[] candidatosTema = theme.isClaroAtivo()
                ? new String[]{"/icons/logo_fundo_branco.png", "/icons/logo_transparente.png",
                               "/icons/expertdev_logo_light.png", "/icons/expertdev_logo_horizontal.png",
                               "/icons/expertdev_logo.png"}
                : new String[]{"/icons/logo_fundo_preto.png", "/icons/logo_transparente.png",
                               "/icons/expertdev_logo_horizontal_dark.png", "/icons/expertdev_logo_dark.png",
                               "/icons/expertdev_logo.png"};

        String[] fallbackGeral = {"/icons/logo_transparente.png", "/icons/logo_fundo_branco.png",
                                  "/icons/logo_fundo_preto.png", "/icons/expertdev_logo_light.png",
                                  "/icons/expertdev_logo_dark.png", "/icons/expertdev_logo_horizontal.png",
                                  "/icons/expertdev_logo_horizontal_dark.png", "/icons/expertdev_logo.png"};

        for (String caminho : candidatosTema) {
            BufferedImage img = carregarImagemRecurso(caminho);
            if (img != null) return img;
        }
        for (String caminho : fallbackGeral) {
            BufferedImage img = carregarImagemRecurso(caminho);
            if (img != null) return img;
        }
        return null;
    }

    public BufferedImage carregarImagemRecurso(String caminhoClasspath) {
        try {
            URL recurso = UiFactory.class.getResource(caminhoClasspath);
            if (recurso != null) return ImageIO.read(recurso);

            String nomeArquivo = caminhoClasspath.substring(caminhoClasspath.lastIndexOf('/') + 1);
            File[] candidatos = {new File("icons", nomeArquivo),
                                 new File("src/main/resources/icons", nomeArquivo)};
            for (File f : candidatos) {
                if (f.exists() && f.isFile()) return ImageIO.read(f);
            }
        } catch (IOException ignored) {}
        return null;
    }

    private BufferedImage recortarTransparencia(BufferedImage origem) {
        Rectangle bounds = encontrarBoundsPorAlpha(origem, 12);
        if (bounds == null || ocupaImagemInteira(bounds, origem)) {
            bounds = encontrarBoundsPorCorDeFundo(origem, 30);
        }
        if (bounds == null || ocupaImagemInteira(bounds, origem)) return origem;

        int padX = Math.max(2, (int) Math.round(bounds.width  * 0.01));
        int padY = Math.max(2, (int) Math.round(bounds.height * 0.02));
        int sx   = Math.max(0, bounds.x - padX);
        int sy   = Math.max(0, bounds.y - padY);
        int ex   = Math.min(origem.getWidth(),  bounds.x + bounds.width  + padX);
        int ey   = Math.min(origem.getHeight(), bounds.y + bounds.height + padY);

        int w = Math.max(1, ex - sx);
        int h = Math.max(1, ey - sy);
        BufferedImage recorte = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = recorte.createGraphics();
        try {
            g2.drawImage(origem, 0, 0, w, h, sx, sy, ex, ey, null);
        } finally {
            g2.dispose();
        }
        return recorte;
    }

    private Rectangle encontrarBoundsPorAlpha(BufferedImage origem, int alphaMinimo) {
        int largura = origem.getWidth();
        int altura  = origem.getHeight();
        int minX = largura, minY = altura, maxX = -1, maxY = -1;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (((origem.getRGB(x, y) >>> 24) & 0xFF) > alphaMinimo) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        return (maxX < minX || maxY < minY) ? null
                : new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private Rectangle encontrarBoundsPorCorDeFundo(BufferedImage origem, int tolerancia) {
        int largura = origem.getWidth();
        int altura  = origem.getHeight();
        int[] fundo = obterCorMediaDosCantos(origem);
        int minX = largura, minY = altura, maxX = -1, maxY = -1;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int argb  = origem.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha <= 10) continue;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8)  & 0xFF;
                int b =  argb         & 0xFF;
                if (Math.abs(r - fundo[0]) + Math.abs(g - fundo[1]) + Math.abs(b - fundo[2]) > tolerancia) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        return (maxX < minX || maxY < minY) ? null
                : new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private int[] obterCorMediaDosCantos(BufferedImage origem) {
        int largura = origem.getWidth();
        int altura  = origem.getHeight();
        int[] amostras = {origem.getRGB(0, 0), origem.getRGB(largura - 1, 0),
                          origem.getRGB(0, altura - 1), origem.getRGB(largura - 1, altura - 1)};
        int somaR = 0, somaG = 0, somaB = 0, pesoTotal = 0;
        for (int argb : amostras) {
            int a   = (argb >>> 24) & 0xFF;
            int peso = Math.max(1, a);
            somaR += ((argb >>> 16) & 0xFF) * peso;
            somaG += ((argb >>> 8)  & 0xFF) * peso;
            somaB += (argb          & 0xFF) * peso;
            pesoTotal += peso;
        }
        if (pesoTotal == 0) return new int[]{0, 0, 0};
        return new int[]{somaR / pesoTotal, somaG / pesoTotal, somaB / pesoTotal};
    }

    private boolean ocupaImagemInteira(Rectangle bounds, BufferedImage origem) {
        return bounds != null
                && bounds.x <= 0 && bounds.y <= 0
                && bounds.width  >= origem.getWidth()
                && bounds.height >= origem.getHeight();
    }

    private BufferedImage aplicarContrasteLogo(BufferedImage origem) {
        float contraste = theme.isClaroAtivo() ? 1.01f : 1.03f;
        BufferedImage saida = new BufferedImage(origem.getWidth(), origem.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < origem.getHeight(); y++) {
            for (int x = 0; x < origem.getWidth(); x++) {
                int argb = origem.getRGB(x, y);
                int a    = (argb >>> 24) & 0xFF;
                int r    = ajustarCanalContraste((argb >>> 16) & 0xFF, contraste);
                int g    = ajustarCanalContraste((argb >>> 8)  & 0xFF, contraste);
                int b    = ajustarCanalContraste(argb          & 0xFF, contraste);
                saida.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return saida;
    }

    private int ajustarCanalContraste(int valor, float contraste) {
        return Math.max(0, Math.min(255, (int) Math.round((valor - 128) * contraste + 128)));
    }
}

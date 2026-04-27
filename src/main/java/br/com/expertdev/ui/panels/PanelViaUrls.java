package br.com.expertdev.ui.panels;

import br.com.expertdev.ui.theme.ExpertDevTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Aba "Via URLs" — campo de entrada de URLs para processar.
 */
public class PanelViaUrls extends JPanel {

    public interface UrlsListener {
        void onUrlsChanged(String urls);
        void onClearUrls();
    }

    private final JTextArea txtUrls;

    public PanelViaUrls(UrlsListener listener) {
        setLayout(new MigLayout("insets 18 20 18 20, fill", "[grow]", "[][4!][grow][]"));
        setBackground(ExpertDevTheme.BG_PANEL);

        // Header
        JLabel title = buildSectionLabel("URLS PARA PROCESSAR");
        add(title, "cell 0 0, split 2");

        JButton btnLimpar = buildGhostSmButton("Limpar");
        add(btnLimpar, "cell 0 0, right");

        // Descricao
        JLabel desc = new JLabel("Uma URL por linha. Suporta Confluence, SharePoint e portais internos.");
        desc.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        desc.setForeground(ExpertDevTheme.TEXT_MUTED);
        add(desc, "cell 0 1");

        // TextArea
        txtUrls = new JTextArea();
        txtUrls.setFont(ExpertDevTheme.FONT_MONO);
        txtUrls.setBackground(ExpertDevTheme.SURFACE_ALT);
        txtUrls.setForeground(ExpertDevTheme.TEXT_BODY);
        txtUrls.setCaretColor(ExpertDevTheme.PRIMARY);
        txtUrls.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        txtUrls.setLineWrap(true);
        txtUrls.setWrapStyleWord(false);

        JScrollPane scroll = new JScrollPane(txtUrls);
        scroll.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true));
        scroll.setBackground(ExpertDevTheme.SURFACE_ALT);
        add(scroll, "cell 0 2, grow");

        // Listeners
        if (listener != null) {
            txtUrls.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e)  { listener.onUrlsChanged(txtUrls.getText()); }
                public void removeUpdate(DocumentEvent e)  { listener.onUrlsChanged(txtUrls.getText()); }
                public void changedUpdate(DocumentEvent e) { listener.onUrlsChanged(txtUrls.getText()); }
            });

            btnLimpar.addActionListener(e -> {
                txtUrls.setText("");
                listener.onClearUrls();
            });
        }
    }

    public JTextArea getTextArea() {
        return txtUrls;
    }

    public String getUrls() {
        return txtUrls.getText();
    }

    public void setUrls(String text) {
        txtUrls.setText(text);
    }

    private static JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ExpertDevTheme.FONT_LABEL);
        lbl.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        return lbl;
    }

    static JButton buildGhostSmButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(ExpertDevTheme.GRAY_100);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ExpertDevTheme.RADIUS_SM * 2, ExpertDevTheme.RADIUS_SM * 2);
                    }
                    g2.setColor(ExpertDevTheme.BORDER);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ExpertDevTheme.RADIUS_SM * 2, ExpertDevTheme.RADIUS_SM * 2);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(ExpertDevTheme.FONT_BODY.deriveFont(11f));
        btn.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 16, ExpertDevTheme.BTN_SM_HEIGHT));
        return btn;
    }
}

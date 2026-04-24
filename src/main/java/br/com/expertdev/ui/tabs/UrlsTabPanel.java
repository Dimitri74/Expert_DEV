package br.com.expertdev.ui.tabs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class UrlsTabPanel {

    public interface Actions {
        void onUrlsChanged();
        void onClearUrls();
    }

    public static class Components {
        private final JPanel panel;
        private final JTextArea areaUrls;

        public Components(JPanel panel, JTextArea areaUrls) {
            this.panel = panel;
            this.areaUrls = areaUrls;
        }

        public JPanel getPanel() {
            return panel;
        }

        public JTextArea getAreaUrls() {
            return areaUrls;
        }
    }

    public Components build(Color corPainelAlt,
                            Color corFundo,
                            Color corTexto,
                            Color corDestaque,
                            Font fonteMono,
                            JLabel instrucao,
                            Actions actions,
                            JButton btnLimparTemplate) {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(corPainelAlt);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));

        painel.add(instrucao, BorderLayout.NORTH);

        JTextArea areaUrls = new JTextArea();
        areaUrls.setFont(fonteMono);
        areaUrls.setBackground(corFundo);
        areaUrls.setForeground(corTexto);
        areaUrls.setCaretColor(corDestaque);
        areaUrls.setLineWrap(true);
        areaUrls.setWrapStyleWord(true);
        areaUrls.setBorder(new EmptyBorder(10, 10, 10, 10));
        areaUrls.setToolTipText("Ex: https://github.com/user/repo/README.md");
        areaUrls.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actions.onUrlsChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actions.onUrlsChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actions.onUrlsChanged();
            }
        });

        JScrollPane scroll = new JScrollPane(areaUrls);
        scroll.setBorder(BorderFactory.createLineBorder(corPainelAlt));
        painel.add(scroll, BorderLayout.CENTER);

        JPanel botoesUrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botoesUrl.setOpaque(false);
        JButton btnLimpar = cloneButton(btnLimparTemplate);
        btnLimpar.addActionListener(e -> actions.onClearUrls());
        botoesUrl.add(btnLimpar);
        painel.add(botoesUrl, BorderLayout.SOUTH);

        return new Components(painel, areaUrls);
    }

    private JButton cloneButton(JButton source) {
        JButton clone = new JButton(source.getText());
        clone.setFont(source.getFont());
        clone.setBackground(source.getBackground());
        clone.setForeground(source.getForeground());
        clone.setBorder(source.getBorder());
        clone.setFocusPainted(source.isFocusPainted());
        clone.setCursor(source.getCursor());
        clone.setOpaque(source.isOpaque());
        clone.setContentAreaFilled(source.isContentAreaFilled());
        return clone;
    }
}
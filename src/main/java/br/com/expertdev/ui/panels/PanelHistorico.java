package br.com.expertdev.ui.panels;

import br.com.expertdev.ui.theme.ExpertDevTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Aba "Histórico" — exibe os últimos 100 processamentos.
 */
public class PanelHistorico extends JPanel {

    private final JTextArea areaHistorico;

    public PanelHistorico() {
        setLayout(new MigLayout("insets 18 20 18 20, fill", "[grow]", "[][grow]"));
        setBackground(ExpertDevTheme.BG_PANEL);

        // Header
        JLabel title = buildSectionLabel("HISTÓRICO DE PROCESSAMENTOS (ÚLTIMOS 100)");
        add(title, "cell 0 0, split 3");

        JButton btnAtualizar = PanelViaUrls.buildGhostSmButton("Atualizar");
        JButton btnLimpar    = PanelViaUrls.buildGhostSmButton("Limpar cache");
        add(btnAtualizar, "right");
        add(btnLimpar, "right, wrap");

        // Area histórico
        areaHistorico = new JTextArea();
        areaHistorico.setFont(ExpertDevTheme.FONT_MONO_SM);
        areaHistorico.setBackground(ExpertDevTheme.SURFACE_ALT);
        areaHistorico.setForeground(ExpertDevTheme.TEXT_BODY);
        areaHistorico.setEditable(false);
        areaHistorico.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        areaHistorico.setLineWrap(true);
        areaHistorico.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(areaHistorico);
        scroll.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true));
        scroll.setBackground(ExpertDevTheme.SURFACE_ALT);

        // Estado vazio
        if (areaHistorico.getText().isEmpty()) {
            areaHistorico.setText("Nenhum processamento encontrado.");
            areaHistorico.setForeground(ExpertDevTheme.TEXT_MUTED);
        }

        add(scroll, "cell 0 1, grow");
    }

    public JTextArea getAreaHistorico() {
        return areaHistorico;
    }

    public void setTexto(String texto) {
        areaHistorico.setForeground(texto == null || texto.isEmpty()
            ? ExpertDevTheme.TEXT_MUTED : ExpertDevTheme.TEXT_BODY);
        areaHistorico.setText(texto == null || texto.isEmpty()
            ? "Nenhum processamento encontrado." : texto);
        areaHistorico.setCaretPosition(0);
    }

    private static JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ExpertDevTheme.FONT_LABEL);
        lbl.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        return lbl;
    }
}

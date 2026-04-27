package br.com.expertdev.ui.panels;

import br.com.expertdev.ui.components.DropZonePanel;
import br.com.expertdev.ui.theme.ExpertDevTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Aba "Upload Word" — drag-and-drop e lista de arquivos .doc/.docx.
 */
public class PanelUploadWord extends JPanel {

    private final DefaultListModel<File> modeloArquivos = new DefaultListModel<File>();
    private final JList<File> listaArquivos;
    private final JTextArea areaPreview;
    private final JLabel lblContador;

    public PanelUploadWord() {
        setLayout(new MigLayout("insets 18 20 18 20, fill", "[grow]", "[][8!][120!][4!][][][4!][grow]"));
        setBackground(ExpertDevTheme.BG_PANEL);

        // Header
        JLabel title = buildSectionLabel("ARQUIVO WORD (.DOC/.DOCX)");
        add(title, "cell 0 0, split 3");

        JButton btnRemover = PanelViaUrls.buildGhostSmButton("Remover");
        JButton btnLimpar  = PanelViaUrls.buildGhostSmButton("Limpar tudo");
        add(btnRemover, "right");
        add(btnLimpar, "right, wrap");

        // Drop zone
        DropZonePanel dropZone = new DropZonePanel(new DropZonePanel.FileDropListener() {
            public void onFilesDrop(List<File> files) {
                for (File f : files) {
                    if (!modeloArquivos.contains(f)) {
                        modeloArquivos.addElement(f);
                    }
                }
                atualizarContador();
                if (!files.isEmpty()) {
                    areaPreview.setText("Arquivo: " + files.get(0).getAbsolutePath());
                }
            }
        });
        dropZone.setPreferredSize(new Dimension(0, 120));
        add(dropZone, "cell 0 1, grow");

        // Lista de arquivos
        listaArquivos = new JList<File>(modeloArquivos);
        listaArquivos.setFont(ExpertDevTheme.FONT_MONO_SM);
        listaArquivos.setBackground(ExpertDevTheme.BG_PANEL);
        listaArquivos.setForeground(ExpertDevTheme.TEXT_BODY);
        listaArquivos.setCellRenderer(new FileListCellRenderer());

        JScrollPane scrollLista = new JScrollPane(listaArquivos);
        scrollLista.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1));
        scrollLista.setPreferredSize(new Dimension(0, 80));
        add(scrollLista, "cell 0 2, grow");

        // Contador
        lblContador = new JLabel("Arquivos Word: 0");
        lblContador.setFont(ExpertDevTheme.FONT_BODY.deriveFont(12f));
        lblContador.setForeground(ExpertDevTheme.TEXT_MUTED);
        add(lblContador, "cell 0 3");

        // Preview label
        JLabel previewTitle = buildSectionLabel("PRÉ-VISUALIZAÇÃO");
        add(previewTitle, "cell 0 4");

        // Preview area
        areaPreview = new JTextArea();
        areaPreview.setFont(ExpertDevTheme.FONT_MONO_SM);
        areaPreview.setBackground(ExpertDevTheme.SURFACE_ALT);
        areaPreview.setForeground(ExpertDevTheme.TEXT_BODY);
        areaPreview.setEditable(false);
        areaPreview.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scrollPreview = new JScrollPane(areaPreview);
        scrollPreview.setBorder(BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true));
        add(scrollPreview, "cell 0 5, grow");

        // Button actions
        btnRemover.addActionListener(e -> {
            int idx = listaArquivos.getSelectedIndex();
            if (idx >= 0) {
                modeloArquivos.remove(idx);
                atualizarContador();
            }
        });

        btnLimpar.addActionListener(e -> {
            modeloArquivos.clear();
            areaPreview.setText("");
            atualizarContador();
        });
    }

    public DefaultListModel<File> getModeloArquivos() {
        return modeloArquivos;
    }

    public JList<File> getListaArquivos() {
        return listaArquivos;
    }

    public JTextArea getAreaPreview() {
        return areaPreview;
    }

    private void atualizarContador() {
        lblContador.setText("Arquivos Word: " + modeloArquivos.size());
    }

    private static JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ExpertDevTheme.FONT_LABEL);
        lbl.setForeground(ExpertDevTheme.TEXT_SECONDARY);
        return lbl;
    }

    private static class FileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                setText(((File) value).getName());
            }
            setFont(ExpertDevTheme.FONT_MONO_SM);
            if (!isSelected) {
                setBackground(index % 2 == 0 ? ExpertDevTheme.BG_PANEL : ExpertDevTheme.SURFACE_ALT);
                setForeground(ExpertDevTheme.TEXT_BODY);
            }
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            return this;
        }
    }
}

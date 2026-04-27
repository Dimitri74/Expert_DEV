package br.com.expertdev.ui.components;

import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * JPanel que aceita drag-and-drop de arquivos .doc/.docx.
 * Borda dashed que muda para PRIMARY no hover/drag.
 */
public class DropZonePanel extends JPanel {

    public interface FileDropListener {
        void onFilesDrop(List<File> files);
    }

    private boolean dragOver = false;
    private FileDropListener listener;
    private final JLabel iconLabel;
    private final JLabel textLabel;
    private final JLabel hintLabel;

    public DropZonePanel(FileDropListener listener) {
        this.listener = listener;
        setOpaque(false);
        setLayout(new GridBagLayout());
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        iconLabel = new JLabel(createFolderIcon(30));

        textLabel = new JLabel("Arraste ou clique para selecionar");
        textLabel.setFont(ExpertDevTheme.FONT_BODY);
        textLabel.setForeground(ExpertDevTheme.TEXT_SECONDARY);

        hintLabel = new JLabel(".doc, .docx - max. 100MB por arquivo / 300MB total");
        hintLabel.setFont(ExpertDevTheme.FONT_MONO_SM);
        hintLabel.setForeground(ExpertDevTheme.TEXT_MUTED);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(iconLabel);
        inner.add(Box.createVerticalStrut(6));
        inner.add(textLabel);
        inner.add(Box.createVerticalStrut(2));
        inner.add(hintLabel);
        add(inner);

        // Hover via mouse
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { dragOver = true; repaint(); }
            @Override
            public void mouseExited(MouseEvent e)  { dragOver = false; repaint(); }
        });

        // Drag-and-drop
        new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent e) {
                dragOver = true;
                repaint();
            }
            @Override
            public void dragExit(DropTargetEvent e) {
                dragOver = false;
                repaint();
            }
            @Override
            public void drop(DropTargetDropEvent e) {
                dragOver = false;
                repaint();
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    List<?> droppedFiles = (List<?>) e.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    List<File> accepted = new ArrayList<File>();
                    for (Object obj : droppedFiles) {
                        if (obj instanceof File) {
                            File f = (File) obj;
                            String name = f.getName().toLowerCase();
                            if (name.endsWith(".doc") || name.endsWith(".docx")) {
                                accepted.add(f);
                            }
                        }
                    }
                    if (!accepted.isEmpty() && listener != null) {
                        listener.onFilesDrop(accepted);
                    }
                    e.dropComplete(true);
                } catch (Exception ex) {
                    e.dropComplete(false);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = dragOver ? ExpertDevTheme.PRIMARY_LIGHT : ExpertDevTheme.SURFACE_ALT;
            Color border = dragOver ? ExpertDevTheme.PRIMARY : ExpertDevTheme.BORDER;

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);

            // Borda dashed
            float[] dash = { 6f, 4f };
            g2.setStroke(new BasicStroke(dragOver ? 2f : 1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.setColor(border);
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, ExpertDevTheme.RADIUS_MD * 2, ExpertDevTheme.RADIUS_MD * 2);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    public void setFileDropListener(FileDropListener l) {
        this.listener = l;
    }

    private static Icon createFolderIcon(final int size) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color body = new Color(245, 191, 54);
                    Color top = new Color(251, 213, 94);
                    Color border = new Color(214, 143, 35);

                    g2.setColor(top);
                    g2.fillRoundRect(x + size / 10, y + size / 5, size * 2 / 5, size / 4, 6, 6);
                    g2.setColor(body);
                    g2.fillRoundRect(x + 1, y + size / 3, size - 2, size * 3 / 5, 8, 8);
                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(x + 1, y + size / 3, size - 2, size * 3 / 5, 8, 8);
                    g2.drawRoundRect(x + size / 10, y + size / 5, size * 2 / 5, size / 4, 6, 6);
                } finally {
                    g2.dispose();
                }
            }
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }
        };
    }
}

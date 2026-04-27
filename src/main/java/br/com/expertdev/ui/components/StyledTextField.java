package br.com.expertdev.ui.components;

import br.com.expertdev.ui.theme.ExpertDevTheme;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JTextField estilizado com estados visual: normal, hover e focus.
 * FlatLaf gerencia o arco via UIManager; esta classe trata cores de borda.
 */
public class StyledTextField extends JTextField {

    private static final Border BORDER_NORMAL = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true),
        BorderFactory.createEmptyBorder(0, 10, 0, 10)
    );
    private static final Border BORDER_HOVER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ExpertDevTheme.GRAY_400, 1, true),
        BorderFactory.createEmptyBorder(0, 10, 0, 10)
    );
    private static final Border BORDER_FOCUS = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ExpertDevTheme.BORDER_FOCUS, 2, true),
        BorderFactory.createEmptyBorder(0, 9, 0, 9)
    );

    private final int fixedHeight;

    public StyledTextField(int fixedHeight) {
        this.fixedHeight = fixedHeight;
        setBackground(ExpertDevTheme.WHITE);
        setForeground(ExpertDevTheme.TEXT_BODY);
        setCaretColor(ExpertDevTheme.PRIMARY);
        setFont(ExpertDevTheme.FONT_BODY);
        setBorder(BORDER_NORMAL);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { setBorder(BORDER_FOCUS); }
            @Override
            public void focusLost(FocusEvent e)   { setBorder(BORDER_NORMAL); }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isFocusOwner()) setBorder(BORDER_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isFocusOwner()) setBorder(BORDER_NORMAL);
            }
        });
    }

    public StyledTextField() {
        this(ExpertDevTheme.INPUT_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, fixedHeight);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}

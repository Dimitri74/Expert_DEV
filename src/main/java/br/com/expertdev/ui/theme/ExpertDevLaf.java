package br.com.expertdev.ui.theme;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import javax.swing.*;
import java.awt.*;

/**
 * Instalação e configuração do FlatLaf para Expert Dev v2.6.0-BETA.
 * Chamar ExpertDevLaf.install() antes de criar qualquer componente Swing.
 */
public final class ExpertDevLaf {

    private ExpertDevLaf() {}

    public static void install() {
        if (!ExpertDevTheme.registerFonts()) {
            throw new IllegalStateException("Fontes IBM Plex obrigatorias nao encontradas no classpath: "
                    + java.util.Arrays.toString(ExpertDevTheme.REQUIRED_FONT_RESOURCES));
        }

        UIManager.put("defaultFont", ExpertDevTheme.FONT_BODY);

        // Cores globais
        UIManager.put("Panel.background",               ExpertDevTheme.BG_APP);
        UIManager.put("Panel.foreground",               ExpertDevTheme.TEXT_BODY);

        // TextField arc (FlatLaf nativo)
        UIManager.put("TextField.arc",                  ExpertDevTheme.RADIUS_MD * 2);
        UIManager.put("PasswordField.arc",              ExpertDevTheme.RADIUS_MD * 2);
        UIManager.put("ComboBox.arc",                   ExpertDevTheme.RADIUS_MD * 2);

        // Inputs
        UIManager.put("TextField.background",           ExpertDevTheme.WHITE);
        UIManager.put("TextField.foreground",           ExpertDevTheme.TEXT_BODY);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ExpertDevTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        UIManager.put("PasswordField.background",       ExpertDevTheme.WHITE);
        UIManager.put("TextArea.background",            ExpertDevTheme.SURFACE_ALT);
        UIManager.put("TextArea.foreground",            ExpertDevTheme.TEXT_BODY);

        // ComboBox
        UIManager.put("ComboBox.background",            ExpertDevTheme.SURFACE_ALT);
        UIManager.put("ComboBox.foreground",            ExpertDevTheme.TEXT_BODY);
        UIManager.put("ComboBox.selectionBackground",   ExpertDevTheme.PRIMARY_LIGHT);
        UIManager.put("ComboBox.selectionForeground",   ExpertDevTheme.PRIMARY);

        // Buttons
        UIManager.put("Button.arc",                     ExpertDevTheme.RADIUS_MD * 2);
        UIManager.put("Button.background",              ExpertDevTheme.GRAY_100);
        UIManager.put("Button.foreground",              ExpertDevTheme.TEXT_SECONDARY);
        UIManager.put("Button.hoverBackground",         ExpertDevTheme.GRAY_200);
        UIManager.put("Button.focusedBackground",       ExpertDevTheme.GRAY_200);

        // TabbedPane
        UIManager.put("TabbedPane.selectedBackground",  ExpertDevTheme.WHITE);
        UIManager.put("TabbedPane.underlineColor",      ExpertDevTheme.PRIMARY);
        UIManager.put("TabbedPane.selectedForeground",  ExpertDevTheme.PRIMARY);
        UIManager.put("TabbedPane.foreground",          ExpertDevTheme.TEXT_MUTED);
        UIManager.put("TabbedPane.tabInsets",           new Insets(8, 12, 8, 12));
        UIManager.put("TabbedPane.background",          ExpertDevTheme.SURFACE_ALT);

        // ScrollPane / ScrollBar
        UIManager.put("ScrollPane.border",              BorderFactory.createLineBorder(ExpertDevTheme.BORDER));
        UIManager.put("ScrollBar.width",                8);
        UIManager.put("ScrollBar.thumbArc",             999);
        UIManager.put("ScrollBar.thumb",                ExpertDevTheme.GRAY_300);
        UIManager.put("ScrollBar.track",                ExpertDevTheme.GRAY_100);

        // CheckBox
        UIManager.put("CheckBox.foreground",            ExpertDevTheme.TEXT_SECONDARY);

        // Label
        UIManager.put("Label.foreground",               ExpertDevTheme.TEXT_BODY);

        // Instala FlatLaf Light
        FlatLightLaf.setup();
    }

    /** Alterna para dark mode em runtime (para implementar futuramente). */
    public static void toggleDark(boolean dark) {
        FlatAnimatedLafChange.showSnapshot();
        try {
            if (dark) {
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            } else {
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }
}

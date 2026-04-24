package br.com.expertdev.ui.presentation;

import javax.swing.*;
import java.awt.*;

public class PresentationMessageService {

    private final Component parent;

    public PresentationMessageService(Component parent) {
        this.parent = parent;
    }

    public void showWarning(String message) {
        JOptionPane.showMessageDialog(parent, message, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(parent, message, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(parent, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
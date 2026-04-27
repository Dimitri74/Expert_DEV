package br.com.expertdev.ui.util;

import javax.swing.*;
import java.awt.*;

/**
 * Fade-out da janela atual → executa callback → permite fade-in da nova janela.
 * Usa Timer Swing (thread-safe). Tem fallback para JVMs sem suporte a translucência.
 */
public final class FadeTransition {

    private FadeTransition() {}

    /**
     * @param source     janela que vai desaparecer (fade-out)
     * @param onComplete callback executado no meio do fade (momento de trocar de tela)
     */
    public static void fadeTo(Window source, Runnable onComplete) {
        // Frames decorados (com barra de título nativa) lançam IllegalComponentStateException
        // ao chamar setOpacity(). Nesses casos, pula a animação e executa o callback direto.
        boolean decorated = (source instanceof Frame) && !((Frame) source).isUndecorated();
        if (decorated || !source.isDisplayable()) {
            SwingUtilities.invokeLater(onComplete);
            return;
        }

        boolean supported = false;
        try {
            supported = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
        } catch (Exception ignored) {}

        if (!supported) {
            SwingUtilities.invokeLater(onComplete);
            return;
        }

        final float[] alpha = {1.0f};
        final boolean[] switched = {false};

        Timer timer = new Timer(16, null); // ~60fps
        timer.addActionListener(e -> {
            alpha[0] -= 0.07f;

            // No meio do fade, abre a próxima tela
            if (alpha[0] <= 0.5f && !switched[0]) {
                switched[0] = true;
                SwingUtilities.invokeLater(onComplete);
            }

            if (!source.isDisplayable()) {
                // Janela já foi descartada — encerra o timer
                ((Timer) e.getSource()).stop();
                return;
            }

            if (alpha[0] <= 0f) {
                ((Timer) e.getSource()).stop();
            } else {
                try {
                    source.setOpacity(Math.max(0f, alpha[0]));
                } catch (UnsupportedOperationException | IllegalComponentStateException ex) {
                    ((Timer) e.getSource()).stop();
                    if (!switched[0]) {
                        switched[0] = true;
                        SwingUtilities.invokeLater(onComplete);
                    }
                }
            }
        });

        timer.start();
    }
}
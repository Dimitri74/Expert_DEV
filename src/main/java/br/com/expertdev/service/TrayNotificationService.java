package br.com.expertdev.service;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class TrayNotificationService {

    private SystemTray tray;
    private TrayIcon trayIcon;

    public TrayNotificationService(String tooltip, Image image, ActionListener openListener) {
        if (!SystemTray.isSupported()) {
            return;
        }

        tray = SystemTray.getSystemTray();
        
        PopupMenu menu = new PopupMenu();
        MenuItem itemAbrir = new MenuItem("Abrir ExpertDev");
        itemAbrir.addActionListener(openListener);
        
        MenuItem itemSair = new MenuItem("Sair");
        itemSair.addActionListener(e -> System.exit(0));
        
        menu.add(itemAbrir);
        menu.addSeparator();
        menu.add(itemSair);

        // Se não tiver imagem, cria uma simples
        if (image == null) {
            image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, 16, 16);
            g.dispose();
        }

        trayIcon = new TrayIcon(image, tooltip, menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(openListener);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Erro ao adicionar ícone ao Tray: " + e.getMessage());
        }
    }

    public void notificar(String titulo, String mensagem, TrayIcon.MessageType tipo) {
        if (trayIcon != null) {
            trayIcon.displayMessage(titulo, mensagem, tipo);
        }
    }
}

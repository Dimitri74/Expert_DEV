package br.com.expertdev.service;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClipboardMonitor implements Runnable {

    public interface ClipboardListener {
        void onDataDetected(String rtc, String titulo);
    }

    private final ClipboardListener listener;
    private boolean running = true;
    private String lastContent = "";

    // Regex para identificar padrões comuns de Jira/RTC
    // Exemplo Jira: PROJECT-1234
    // Exemplo RTC: RTC 123456
    // Exemplo Link: https://jira.company.com/browse/PROJ-123
    private static final Pattern PATTERN_JIRA = Pattern.compile("([A-Z]+-\\d+)");
    private static final Pattern PATTERN_RTC = Pattern.compile("(?:RTC\\s*|#)(\\d+)");
    private static final Pattern PATTERN_URL_RTC = Pattern.compile("id=(\\d+)");

    public ClipboardMonitor(ClipboardListener listener) {
        this.listener = listener;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1500); // Verifica a cada 1.5s
                String currentContent = getClipboardText();
                if (currentContent != null && !currentContent.equals(lastContent)) {
                    lastContent = currentContent;
                    processContent(currentContent);
                }
            } catch (Exception e) {
                // Silencioso para não poluir logs de sistema se o clipboard estiver ocupado
            }
        }
    }

    private String getClipboardText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            // Pode falhar se outra app estiver usando o clipboard
        }
        return null;
    }

    private void processContent(String content) {
        String rtc = null;
        String titulo = null;

        // Tenta encontrar RTC/Jira ID
        Matcher mRtc = PATTERN_RTC.matcher(content);
        if (mRtc.find()) {
            rtc = mRtc.group(1);
        } else {
            Matcher mUrl = PATTERN_URL_RTC.matcher(content);
            if (mUrl.find()) {
                rtc = mUrl.group(1);
            } else {
                Matcher mJira = PATTERN_JIRA.matcher(content);
                if (mJira.find()) {
                    rtc = mJira.group(1);
                }
            }
        }

        // Se encontrou um ID, tenta extrair algo que pareça um título
        // Geralmente se copiar do navegador vem "ID: Titulo" ou similar
        if (rtc != null) {
            String clean = content.replace("\n", " ").replace("\r", " ").trim();
            // Tenta pegar o que vem depois do ID se houver
            int idx = clean.indexOf(rtc);
            if (idx != -1) {
                String potentialTitle = clean.substring(idx + rtc.length()).trim();
                if (potentialTitle.startsWith(":") || potentialTitle.startsWith("-")) {
                    potentialTitle = potentialTitle.substring(1).trim();
                }
                if (!potentialTitle.isEmpty() && potentialTitle.length() > 5) {
                    // Limita tamanho para não pegar texto demais
                    titulo = potentialTitle.length() > 100 ? potentialTitle.substring(0, 97) + "..." : potentialTitle;
                }
            }
        }

        if (rtc != null) {
            listener.onDataDetected(rtc, titulo);
        }
    }
}

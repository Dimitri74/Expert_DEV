package br.com.expertdev.ui.tabs;

import javax.swing.*;

public class ExpertDevTabsBuilder {

    public interface TabFactory {
        JPanel create();
    }

    public void addCoreTabs(JTabbedPane abas,
                            TabFactory urlsTab,
                            TabFactory uploadTab,
                            TabFactory historicoTab,
                            TabFactory performanceTab) {
        abas.addTab("Via URLs", urlsTab.create());
        abas.addTab("Upload Word", uploadTab.create());
        abas.addTab("Historico", historicoTab.create());
        abas.addTab("Performance & ROI", performanceTab.create());
    }
}
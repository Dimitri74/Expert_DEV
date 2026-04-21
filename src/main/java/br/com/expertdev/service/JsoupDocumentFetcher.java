package br.com.expertdev.service;

import br.com.expertdev.config.ExpertDevConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class JsoupDocumentFetcher implements DocumentFetcher {

    private final ExpertDevConfig config;

    public JsoupDocumentFetcher(ExpertDevConfig config) {
        this.config = config;
    }

    public Document fetch(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(config.getUserAgent())
                .timeout(config.getTimeoutMs())
                .get();
    }
}


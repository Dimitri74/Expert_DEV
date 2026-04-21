package br.com.expertdev.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UrlParser {

    public List<String> parsear(String input) {
        Set<String> urlsValidas = new LinkedHashSet<>();
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] partes = input.split(",");
        for (String parte : partes) {
            String url = parte.trim();
            if (url.isEmpty()) {
                continue;
            }

            if (validarUrlHttp(url)) {
                urlsValidas.add(url);
            } else {
                System.err.println("⚠ URL ignorada (inválida): " + url);
            }
        }

        return new ArrayList<>(urlsValidas);
    }

    private boolean validarUrlHttp(String valor) {
        try {
            URI uri = new URI(valor);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            return host != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (URISyntaxException e) {
            return false;
        }
    }
}


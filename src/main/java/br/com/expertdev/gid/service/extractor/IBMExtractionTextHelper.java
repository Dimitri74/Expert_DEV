package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.enumtype.IBMTipoURLIntegracao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class IBMExtractionTextHelper {

    private static final Pattern PATTERN_RTC = Pattern.compile("(?i)rtc\\s*([0-9]{6,10})");
    private static final Pattern PATTERN_UC = Pattern.compile("(?i)\\b(uc[-_ ]?[a-z0-9]{2,})\\b");
    private static final Pattern PATTERN_URL = Pattern.compile("https?://[^\\s)]+", Pattern.CASE_INSENSITIVE);

    private IBMExtractionTextHelper() {
    }

    static String extrairRtc(String texto) {
        if (texto == null) {
            return null;
        }
        Matcher m = PATTERN_RTC.matcher(texto);
        return m.find() ? m.group(1) : null;
    }

    static String extrairUcCodigo(String texto) {
        if (texto == null) {
            return null;
        }
        Matcher m = PATTERN_UC.matcher(texto);
        return m.find() ? m.group(1).toUpperCase(Locale.ROOT).replace(" ", "") : null;
    }

    static List<String> linhas(String texto) {
        List<String> out = new ArrayList<String>();
        if (texto == null) {
            return out;
        }
        String[] parts = texto.replace("\r", "").split("\n");
        for (String p : parts) {
            String v = p == null ? "" : p.trim();
            if (!v.isEmpty()) {
                out.add(v);
            }
        }
        return out;
    }

    static List<String> extrairUrls(String texto) {
        List<String> urls = new ArrayList<String>();
        if (texto == null) {
            return urls;
        }
        Matcher m = PATTERN_URL.matcher(texto);
        while (m.find()) {
            urls.add(m.group());
        }
        return urls;
    }

    static IBMTipoURLIntegracao classificarUrl(String url) {
        if (url == null) {
            return IBMTipoURLIntegracao.OUTRA;
        }
        String v = url.toLowerCase(Locale.ROOT);
        if (v.contains("login") || v.contains("auth") || v.contains("openid")) {
            return IBMTipoURLIntegracao.AUTENTICACAO;
        }
        if (v.contains("api.") || v.contains("/v1/") || v.contains("/v2/") || v.contains("/v3/")) {
            return IBMTipoURLIntegracao.API_INTERNA;
        }
        if (v.contains("sigms") || v.contains("dataprev") || v.contains("mastercard") || v.contains("correios")) {
            return IBMTipoURLIntegracao.SISTEMA_EXTERNO;
        }
        return IBMTipoURLIntegracao.OUTRA;
    }

    static String aposSeparador(String linha) {
        if (linha == null) {
            return "";
        }
        int idx = linha.indexOf(':');
        if (idx < 0) {
            idx = linha.indexOf('-');
        }
        if (idx >= 0 && idx + 1 < linha.length()) {
            return linha.substring(idx + 1).trim();
        }
        return linha.trim();
    }
}


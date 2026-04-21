package br.com.expertdev.service;

import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.model.ImagemInfo;
import br.com.expertdev.model.ResultadoProcessamento;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageProcessor {

    private static final String TEXTO_TRUNCADO = "\n\n[Texto truncado]";
    private static final String OBSERVACAO_TEXTO_CURTO = "Página processada com pouco conteúdo textual útil.";
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)\\s]+)(?:\\s+\"[^\"]*\")?\\)");

    private final ExpertDevConfig config;
    private final DocumentFetcher documentFetcher;

    public PageProcessor(ExpertDevConfig config, DocumentFetcher documentFetcher) {
        this.config = config;
        this.documentFetcher = documentFetcher;
    }

    public ResultadoProcessamento processar(String url) {
        ResultadoProcessamento resultado = new ResultadoProcessamento(url);

        try {
            if (isGitHubBlobMarkdownUrl(url)) {
                System.out.println("   -> Modo GitHub README detectado (raw)");
                String markdown = obterMarkdownRawGitHub(url);
                String textoMarkdown = extrairTextoMarkdown(markdown);
                List<ImagemInfo> imagensMarkdown = extrairImagensMarkdown(markdown, url);

                resultado.setTextoExtraido(textoMarkdown);
                resultado.setImagens(imagensMarkdown);
                resultado.setSucesso(true);

                if (textoMarkdown.trim().length() < config.getTamanhoMinimoTextoUtil()) {
                    resultado.setObservacao(OBSERVACAO_TEXTO_CURTO);
                }
                return resultado;
            }

            Document doc = documentFetcher.fetch(url);
            limparDomRuido(doc);

            String texto = extrairTextoPrincipal(doc);
            List<ImagemInfo> imagens = extrairImagens(doc, url);

            resultado.setTextoExtraido(texto);
            resultado.setImagens(imagens);
            resultado.setSucesso(true);

            if (texto.trim().length() < config.getTamanhoMinimoTextoUtil()) {
                resultado.setObservacao(OBSERVACAO_TEXTO_CURTO);
            }
        } catch (Exception e) {
            resultado.setSucesso(false);
            resultado.setErro(e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        return resultado;
    }

    private boolean isGitHubBlobMarkdownUrl(String url) {
        if (url == null) {
            return false;
        }
        String normalized = url.toLowerCase();
        return normalized.contains("github.com/")
                && normalized.contains("/blob/")
                && (normalized.endsWith(".md") || normalized.contains(".md?"));
    }

    private String obterMarkdownRawGitHub(String blobUrl) throws Exception {
        String rawUrl = converterBlobParaRaw(blobUrl);
        return org.jsoup.Jsoup.connect(rawUrl)
                .userAgent(config.getUserAgent())
                .timeout(config.getTimeoutMs())
                .ignoreContentType(true)
                .execute()
                .body();
    }

    private String converterBlobParaRaw(String blobUrl) {
        String semQuery = blobUrl.split("\\?")[0].split("#")[0];
        String raw = semQuery.replace("https://github.com/", "https://raw.githubusercontent.com/");
        return raw.replace("/blob/", "/");
    }

    private String extrairTextoMarkdown(String markdown) {
        if (markdown == null) {
            return "";
        }

        String texto = markdown.replace("\r", "");
        texto = texto.replaceAll("<[^>]+>", " ");
        texto = texto.replaceAll("[ \t]+", " ");
        texto = texto.replaceAll("\n{3,}", "\n\n").trim();
        if (texto.length() > config.getLimiteTexto()) {
            return texto.substring(0, config.getLimiteTexto()) + TEXTO_TRUNCADO;
        }
        return texto;
    }

    private List<ImagemInfo> extrairImagensMarkdown(String markdown, String paginaUrl) {
        List<ImagemInfo> imagens = new ArrayList<>();
        if (markdown == null || markdown.trim().isEmpty()) {
            return imagens;
        }

        Set<String> srcsVistos = new LinkedHashSet<>();
        Matcher matcher = MARKDOWN_IMAGE_PATTERN.matcher(markdown);

        while (matcher.find()) {
            String alt = matcher.group(1) == null ? "" : matcher.group(1).trim();
            String srcOriginal = matcher.group(2) == null ? "" : matcher.group(2).trim();
            String srcResolvido = resolverSrcImagemMarkdown(srcOriginal, paginaUrl);

            if (srcResolvido.isEmpty() || srcResolvido.startsWith("data:image/")) {
                continue;
            }

            if (!srcsVistos.add(srcResolvido)) {
                continue;
            }

            imagens.add(new ImagemInfo(paginaUrl, srcResolvido, alt));
        }

        return imagens;
    }

    // ...existing code...

    private String resolverSrcImagemMarkdown(String src, String paginaUrl) {
        if (src == null || src.trim().isEmpty()) {
            return "";
        }

        String trimmed = src.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("//")) {
            return "https:" + trimmed;
        }

        try {
            String rawUrl = converterBlobParaRaw(paginaUrl);
            int lastSlash = rawUrl.lastIndexOf('/');
            if (lastSlash < 0) {
                return "";
            }

            String rawDir = rawUrl.substring(0, lastSlash + 1);
            URI base = new URI(rawDir);

            if (trimmed.startsWith("/")) {
                URI repoRoot = construirRaizRepoRaw(paginaUrl);
                if (repoRoot == null) {
                    return "";
                }
                return repoRoot.resolve(trimmed.substring(1)).toString();
            }

            return base.resolve(trimmed).toString();
        } catch (Exception e) {
            return "";
        }
    }

    private URI construirRaizRepoRaw(String paginaUrl) {
        try {
            URI uri = new URI(paginaUrl.split("\\?")[0].split("#")[0]);
            String[] partes = uri.getPath().split("/");
            if (partes.length < 5) {
                return null;
            }

            String owner = partes[1];
            String repo = partes[2];
            String branch = partes[4];

            return new URI("https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + "/");
        } catch (Exception e) {
            return null;
        }
    }

    private void limparDomRuido(Document doc) {
        doc.select(config.getSeletorRuido()).remove();
    }

    private String extrairTextoPrincipal(Document doc) {
        String texto = doc.select(config.getSeletorConteudoPrincipal()).text();
        if (texto.length() < 500) {
            texto = doc.body().text();
        }

        texto = normalizarTexto(texto);
        if (texto.length() > config.getLimiteTexto()) {
            return texto.substring(0, config.getLimiteTexto()) + TEXTO_TRUNCADO;
        }
        return texto;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replaceAll("\\s+", " ").trim();
    }

    private List<ImagemInfo> extrairImagens(Document doc, String paginaUrl) {
        Elements imagensExtraidas = doc.select("img");
        List<ImagemInfo> imagens = new ArrayList<>();
        Set<String> srcsVistos = new LinkedHashSet<>();

        for (Element imagem : imagensExtraidas) {
            String src = imagem.attr("abs:src").trim();
            String alt = imagem.attr("alt").trim();

            if (src.isEmpty() || src.startsWith("data:image/")) {
                continue;
            }

            if (!srcsVistos.add(src)) {
                continue;
            }

            imagens.add(new ImagemInfo(paginaUrl, src, alt));
        }

        return imagens;
    }
}


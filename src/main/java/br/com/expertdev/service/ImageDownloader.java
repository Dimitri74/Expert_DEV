package br.com.expertdev.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloader {

    private static final String CACHE_DIR = "cache_imagens";
    private static final int TIMEOUT_MS = 15000;
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10 MB

    public ImageDownloader() {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
    }

    public byte[] downloadImage(String urlString) {
        try {
            String downloadUrl = prepararUrlDownload(urlString);

            File cached = getCachedFile(downloadUrl);
            if (cached.exists() && cached.length() > 0) {
                return readFileBytes(cached);
            }

            byte[] imageBytes = fetchImageBytes(downloadUrl);
            if (imageBytes != null && imageBytes.length > 0 && imageBytes.length <= MAX_IMAGE_SIZE) {
                saveToCache(cached, imageBytes);
                return imageBytes;
            }
        } catch (Exception e) {
            System.err.println("⚠ Erro ao baixar imagem " + urlString + ": " + e.getMessage());
        }
        return null;
    }

    private byte[] fetchImageBytes(String urlString) throws IOException {
        URL url = new URL(normalizarUrl(urlString));
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        try (InputStream in = conn.getInputStream()) {
            return readAllBytes(in);
        }
    }

    private String prepararUrlDownload(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            return originalUrl;
        }

        try {
            URL url = new URL(originalUrl);
            String host = url.getHost() == null ? "" : url.getHost().toLowerCase();
            if (!host.contains("img.shields.io")) {
                return originalUrl;
            }

            String path = url.getPath();
            String pathLower = path.toLowerCase();
            if (pathLower.endsWith(".png")) {
                return originalUrl;
            }

            String novoPath;
            if (pathLower.endsWith(".svg")) {
                novoPath = path.substring(0, path.length() - 4) + ".png";
            } else {
                novoPath = path + ".png";
            }

            URI uri = new URI(
                    url.getProtocol(),
                    url.getUserInfo(),
                    url.getHost(),
                    url.getPort(),
                    novoPath,
                    url.getQuery(),
                    url.getRef()
            );
            return uri.toASCIIString();
        } catch (Exception e) {
            return originalUrl;
        }
    }

    private String normalizarUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            URI uri = new URI(
                    url.getProtocol(),
                    url.getUserInfo(),
                    url.getHost(),
                    url.getPort(),
                    url.getPath(),
                    url.getQuery(),
                    url.getRef()
            );
            return uri.toASCIIString();
        } catch (IOException | URISyntaxException e) {
            return urlString;
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private byte[] readFileBytes(File file) throws IOException {
        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        try {
            return readAllBytes(fis);
        } finally {
            fis.close();
        }
    }

    private void saveToCache(File file, byte[] bytes) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
        } catch (IOException e) {
            System.err.println("⚠ Erro ao cachear imagem: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                    // sem ação
                }
            }
        }
    }

    private File getCachedFile(String urlString) {
        String hash = Integer.toHexString(urlString.hashCode());
        String extension = extractExtension(urlString);
        return new File(CACHE_DIR, hash + extension);
    }

    private String extractExtension(String urlString) {
        try {
            String path = new URL(urlString).getPath();
            int lastDot = path.lastIndexOf(".");
            if (lastDot > 0) {
                String ext = path.substring(lastDot);
                if (ext.length() < 10) {
                    return ext;
                }
            }
        } catch (Exception ignored) {
            // sem ação
        }
        return ".jpg";
    }
}


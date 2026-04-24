package br.com.expertdev.service;

import br.com.expertdev.model.ImagemInfo;
import br.com.expertdev.model.ResultadoProcessamento;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfDocumentBuilder {

    private static final String ARQUIVO_PDF = "contexto_com_imagens.pdf";
    private static final float IMAGEM_WIDTH = 400;
    private static final float IMAGEM_HEIGHT = 300;

    private final ImageDownloader imageDownloader;

    public PdfDocumentBuilder(ImageDownloader imageDownloader) {
        this.imageDownloader = imageDownloader;
    }

    public String gerar(List<ResultadoProcessamento> resultados) throws IOException {
        PDDocument document = new PDDocument();

        try {
            adicionarTitulo(document);

            for (ResultadoProcessamento resultado : resultados) {
                if (!resultado.isSucesso()) {
                    continue;
                }

                adicionarSecaoUrl(document, resultado);
            }

            try {
                document.save(ARQUIVO_PDF);
                System.out.println("   ✓ Salvo: " + ARQUIVO_PDF);
                return ARQUIVO_PDF;
            } catch (IOException e) {
                String fallback = gerarNomeAlternativo(ARQUIVO_PDF);
                document.save(fallback);
                System.out.println("   ✓ Salvo: " + fallback + " (arquivo original estava em uso)");
                return fallback;
            }
        } finally {
            document.close();
        }
    }

    private void adicionarTitulo(PDDocument document) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);

        float width = page.getMediaBox().getWidth();
        float height = page.getMediaBox().getHeight();

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            PDFont font = PDType1Font.HELVETICA_BOLD;
            contentStream.setFont(font, 24);
            float titleX = (width - 200) / 2;
            contentStream.beginText();
            contentStream.newLineAtOffset(titleX, height - 50);
            contentStream.showText(sanitizarTextoPdf("Contexto Extraído com Imagens"));
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, height - 100);
            contentStream.showText(sanitizarTextoPdf("Gerado em: " + new java.util.Date()));
            contentStream.endText();
        }
    }

    private void adicionarSecaoUrl(PDDocument document, ResultadoProcessamento resultado) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);
        List<ImagemInfo> imagens = resultado.getImagens();
        boolean modoDiVisual = isModoDiVisual(resultado, imagens);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float yPosition = page.getMediaBox().getHeight() - 50;

            // URL como título
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(sanitizarTextoPdf("URL: " + resultado.getUrl()));
            contentStream.endText();
            yPosition -= 30;

            // Observação
            if (resultado.getObservacao() != null && !resultado.getObservacao().trim().isEmpty()) {
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(sanitizarTextoPdf("Observação: " + resultado.getObservacao()));
                contentStream.endText();
                yPosition -= 20;
            }

            // Texto extraído
            String texto = resultado.getTextoExtraido();
            if (texto != null && !texto.trim().isEmpty() && !modoDiVisual) {
                String textoTruncado = texto.length() > 500 ? texto.substring(0, 500) + "..." : texto;
                yPosition = escreverTextoMultilinha(contentStream, textoTruncado, page, yPosition);
            } else if (modoDiVisual) {
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(sanitizarTextoPdf("Modo DI: priorizando imagens e conteúdo visual relevante ao front."));
                contentStream.endText();
                yPosition -= 20;
            }

            // Imagens
            if (imagens != null && !imagens.isEmpty()) {
                yPosition -= 20;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(sanitizarTextoPdf("Imagens encontradas:"));
                contentStream.endText();
                yPosition -= 25;

                for (ImagemInfo imagem : imagens) {
                    yPosition = adicionarImagem(document, contentStream, imagem, page, yPosition);
                }
            }
        }
    }

    private float escreverTextoMultilinha(PDPageContentStream contentStream, String texto, PDPage page, float yPosition) throws IOException {
        String textoSeguro = texto == null ? "" : texto.replace("\r", " ").replace("\n", " ");
        textoSeguro = sanitizarTextoPdf(textoSeguro);
        contentStream.setFont(PDType1Font.HELVETICA, 11);
        float pageWidth = page.getMediaBox().getWidth();
        float margin = 50;
        float maxWidth = pageWidth - (2 * margin);

        String[] palavras = textoSeguro.split(" ");
        StringBuilder linha = new StringBuilder();

        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.setLeading(15);

        for (String palavra : palavras) {
            String candidata = linha.length() == 0 ? palavra : linha.toString() + " " + palavra;
            if (estimarLarguraTexto(candidata) > maxWidth) {
                contentStream.showText(linha.toString());
                contentStream.newLine();
                linha = new StringBuilder(palavra);
                yPosition -= 15;

                if (yPosition < margin) {
                    contentStream.endText();
                    return yPosition;
                }
            } else {
                if (linha.length() > 0) {
                    linha.append(" ");
                }
                linha.append(palavra);
            }
        }

        if (linha.length() > 0) {
            contentStream.showText(linha.toString());
        }
        contentStream.endText();

        return yPosition - 15;
    }

    private float estimarLarguraTexto(String texto) {
        if (texto == null) {
            return 0;
        }
        return texto.length() * 5.5f;
    }

    private float adicionarImagem(PDDocument document, PDPageContentStream contentStream, ImagemInfo imagem, PDPage page, float yPosition) throws IOException {
        String urlImagem = imagem.getSrc();
        String alt = imagem.getAlt();

        byte[] imageBytes = imageDownloader.downloadImage(urlImagem);
        if (imageBytes == null || imageBytes.length == 0) {
            return yPosition - 15;
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (bufferedImage == null) {
                return yPosition - 15;
            }

            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "imagem");

            if (yPosition - IMAGEM_HEIGHT < 50) {
                return yPosition;
            }

            contentStream.drawImage(pdImage, 75, yPosition - IMAGEM_HEIGHT, IMAGEM_WIDTH, IMAGEM_HEIGHT);
            yPosition -= (IMAGEM_HEIGHT + 10);

            if (alt != null && !alt.trim().isEmpty()) {
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                contentStream.beginText();
                contentStream.newLineAtOffset(75, yPosition);
                contentStream.showText(sanitizarTextoPdf("Descrição: " + alt));
                contentStream.endText();
                yPosition -= 15;
            }

            contentStream.setFont(PDType1Font.HELVETICA, 8);
            contentStream.beginText();
            contentStream.newLineAtOffset(75, yPosition);
            if (urlImagem != null && !urlImagem.startsWith("embedded://")) {
                String urlTruncada = urlImagem.length() > 60 ? urlImagem.substring(0, 60) + "..." : urlImagem;
                contentStream.showText(sanitizarTextoPdf(urlTruncada));
            }
            contentStream.endText();
            yPosition -= 15;

        } catch (Exception e) {
            System.err.println("⚠ Erro ao adicionar imagem ao PDF: " + e.getMessage());
        }

        return yPosition;
    }

    private boolean isModoDiVisual(ResultadoProcessamento resultado, List<ImagemInfo> imagens) {
        if (resultado == null || imagens == null || imagens.isEmpty()) {
            return false;
        }
        String url = resultado.getUrl() == null ? "" : resultado.getUrl().toLowerCase();
        String obs = resultado.getObservacao() == null ? "" : resultado.getObservacao().toLowerCase();
        String texto = resultado.getTextoExtraido() == null ? "" : resultado.getTextoExtraido().toLowerCase();
        return url.contains("integracao_di")
                || url.contains("_di_")
                || texto.contains("documento de interface")
                || obs.contains("integracao_di");
    }

    private String sanitizarTextoPdf(String texto) {
        if (texto == null) {
            return "";
        }

        StringBuilder limpo = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c >= 32 && c <= 255) {
                limpo.append(c);
            } else {
                limpo.append(' ');
            }
        }
        return limpo.toString().replaceAll("\\s+", " ").trim();
    }

    private String gerarNomeAlternativo(String nomeArquivo) {
        String sufixo = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if (nomeArquivo.toLowerCase().endsWith(".pdf")) {
            return nomeArquivo.substring(0, nomeArquivo.length() - 4) + "_" + sufixo + ".pdf";
        }
        return nomeArquivo + "_" + sufixo;
    }
}

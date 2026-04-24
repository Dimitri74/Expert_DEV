package br.com.expertdev.service;

import br.com.expertdev.model.ImagemInfo;
import br.com.expertdev.model.ResultadoProcessamento;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WordDocumentBuilder {

    private static final String ARQUIVO_PALAVRA = "contexto_com_imagens.docx";
    private static final int IMAGEM_WIDTH_EMU = 4572000; // 4 polegadas em EMU
    private static final int IMAGEM_HEIGHT_EMU = 3429000; // 3 polegadas em EMU

    private final ImageDownloader imageDownloader;

    public WordDocumentBuilder(ImageDownloader imageDownloader) {
        this.imageDownloader = imageDownloader;
    }

    public String gerar(List<ResultadoProcessamento> resultados) throws IOException {
        XWPFDocument document = new XWPFDocument();

        try {
            adicionarTitulo(document);

            for (ResultadoProcessamento resultado : resultados) {
                if (!resultado.isSucesso()) {
                    continue;
                }

                adicionarSecaoUrl(document, resultado);
            }

            salvarDocumento(document, ARQUIVO_PALAVRA);
            return ARQUIVO_PALAVRA;
        } finally {
            document.close();
        }
    }

    private void adicionarTitulo(XWPFDocument document) {
        XWPFParagraph titulo = document.createParagraph();
        titulo.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun tituloRun = titulo.createRun();
        tituloRun.setText("Contexto Extraído com Imagens");
        tituloRun.setBold(true);
        tituloRun.setFontSize(18);

        XWPFParagraph dataPara = document.createParagraph();
        XWPFRun dataRun = dataPara.createRun();
        dataRun.setText("Gerado em: " + new java.util.Date());
        dataRun.setItalic(true);
        dataRun.setFontSize(10);

        document.createParagraph(); // linha em branco
    }

    private void adicionarSecaoUrl(XWPFDocument document, ResultadoProcessamento resultado) {
        String url = resultado.getUrl();
        String texto = resultado.getTextoExtraido();
        List<ImagemInfo> imagens = resultado.getImagens();
        boolean modoDiVisual = isModoDiVisual(resultado, imagens);

        // URL como seção
        XWPFParagraph urlPara = document.createParagraph();
        XWPFRun urlRun = urlPara.createRun();
        urlRun.setText("URL: " + url);
        urlRun.setBold(true);
        urlRun.setFontSize(12);
        urlRun.setColor("0066CC");

        // Observação se houver
        if (resultado.getObservacao() != null && !resultado.getObservacao().trim().isEmpty()) {
            XWPFParagraph obsPara = document.createParagraph();
            XWPFRun obsRun = obsPara.createRun();
            obsRun.setText("Observação: " + resultado.getObservacao());
            obsRun.setItalic(true);
            obsRun.setFontSize(10);
            obsRun.setColor("FF6600");
        }

        // Texto extraído
        if (texto != null && !texto.trim().isEmpty() && !modoDiVisual) {
            XWPFParagraph textoPara = document.createParagraph();
            XWPFRun textoRun = textoPara.createRun();
            String textoTruncado = texto.length() > 500 ? texto.substring(0, 500) + "..." : texto;
            textoRun.setText(textoTruncado);
            textoRun.setFontSize(11);
        } else if (modoDiVisual) {
            XWPFParagraph modoPara = document.createParagraph();
            XWPFRun modoRun = modoPara.createRun();
            modoRun.setText("Modo DI: priorizando imagens e conteúdo visual relevante ao front.");
            modoRun.setItalic(true);
            modoRun.setFontSize(10);
            modoRun.setColor("666666");
        }

        // Imagens
        if (imagens != null && !imagens.isEmpty()) {
            XWPFParagraph imagensPara = document.createParagraph();
            XWPFRun imagensRun = imagensPara.createRun();
            imagensRun.setText("Imagens encontradas:");
            imagensRun.setBold(true);
            imagensRun.setFontSize(11);

            for (ImagemInfo imagem : imagens) {
                adicionarImagem(document, imagem);
            }
        }

        // Separador
        document.createParagraph(); // linha em branco
        XWPFParagraph separador = document.createParagraph();
        separador.setPageBreak(false);
    }

    private void adicionarImagem(XWPFDocument document, ImagemInfo imagem) {
        String urlImagem = imagem.getSrc();
        String alt = imagem.getAlt();

        byte[] imageBytes = imageDownloader.downloadImage(urlImagem);
        if (imageBytes == null || imageBytes.length == 0) {
            XWPFParagraph erropara = document.createParagraph();
            XWPFRun erroRun = erropara.createRun();
            erroRun.setText("[Erro ao baixar imagem: " + urlImagem + "]");
            erroRun.setItalic(true);
            erroRun.setColor("FF0000");
            return;
        }

        try {
            XWPFParagraph imagemPara = document.createParagraph();
            imagemPara.setIndentationLeft(720);
            XWPFRun imagemRun = imagemPara.createRun();

            String tipoMime = detectarTipoMime(imageBytes);
            int tipo = Document.PICTURE_TYPE_PNG;
            if (tipoMime.contains("jpeg") || tipoMime.contains("jpg")) {
                tipo = Document.PICTURE_TYPE_JPEG;
            } else if (tipoMime.contains("gif")) {
                tipo = Document.PICTURE_TYPE_GIF;
            }

            imagemRun.addPicture(
                    new java.io.ByteArrayInputStream(imageBytes),
                    tipo,
                    "imagem",
                    IMAGEM_WIDTH_EMU,
                    IMAGEM_HEIGHT_EMU
            );

            if (alt != null && !alt.trim().isEmpty()) {
                XWPFParagraph altPara = document.createParagraph();
                altPara.setIndentationLeft(720);
                XWPFRun altRun = altPara.createRun();
                altRun.setText("Descrição: " + alt);
                altRun.setItalic(true);
                altRun.setFontSize(9);
                altRun.setColor("666666");
            }

            XWPFParagraph urlImgPara = document.createParagraph();
            urlImgPara.setIndentationLeft(720);
            XWPFRun urlImgRun = urlImgPara.createRun();
            if (urlImagem != null && !urlImagem.startsWith("embedded://")) {
                urlImgRun.setText(urlImagem);
                urlImgRun.setFontSize(8);
                urlImgRun.setColor("999999");
            }

            document.createParagraph();
        } catch (Exception e) {
            System.err.println("⚠ Erro ao adicionar imagem ao documento: " + e.getMessage());
        }
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

    private String detectarTipoMime(byte[] bytes) {
        if (bytes.length < 12) {
            return "image/jpeg";
        }

        // PNG
        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "image/png";
        }

        // JPEG
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        // GIF
        if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46) {
            return "image/gif";
        }

        return "image/jpeg"; // padrão
    }

    private void salvarDocumento(XWPFDocument document, String nomeArquivo) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(nomeArquivo)) {
            document.write(fos);
            System.out.println("   ✓ Salvo: " + nomeArquivo);
            return;
        } catch (IOException e) {
            String fallback = gerarNomeAlternativo(nomeArquivo);
            try (FileOutputStream fos = new FileOutputStream(fallback)) {
                document.write(fos);
            }
            System.out.println("   ✓ Salvo: " + fallback + " (arquivo original estava em uso)");
            return;
        }
    }

    private String gerarNomeAlternativo(String nomeArquivo) {
        String sufixo = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if (nomeArquivo.toLowerCase().endsWith(".docx")) {
            return nomeArquivo.substring(0, nomeArquivo.length() - 5) + "_" + sufixo + ".docx";
        }
        return nomeArquivo + "_" + sufixo;
    }
}


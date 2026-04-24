package br.com.expertdev.service;

import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.model.ImagemInfo;
import br.com.expertdev.model.ResultadoProcessamento;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lê arquivo Word (.docx e .doc) e extrai texto e referências de imagens,
 * retornando um ResultadoProcessamento para ser usado no pipeline normal.
 */
public class WordDocumentReader {

    private static final Map<String, byte[]> IMAGENS_EMBEDDADAS = new ConcurrentHashMap<>();

    private final ExpertDevConfig config;
    private final WordLegacyDocConverter converter;

    public WordDocumentReader() {
        this(ExpertDevConfig.carregar(), new WordLegacyDocConverter());
    }

    WordDocumentReader(ExpertDevConfig config, WordLegacyDocConverter converter) {
        this.config = config;
        this.converter = converter;
    }

    public ResultadoProcessamento ler(File arquivo) {
        String identificador = "upload://" + arquivo.getName();
        ResultadoProcessamento resultado = new ResultadoProcessamento(identificador);

        WordLegacyDocConverter.ConversionResult conversao;
        try {
            conversao = prepararArquivoComFallback(arquivo);
        } catch (IOException e) {
            resultado.setSucesso(false);
            resultado.setErro("Erro ao preparar arquivo Word: " + e.getMessage());
            return resultado;
        }

        File arquivoParaLeitura = conversao.getArquivoParaLeitura();
        String nomeLeitura = arquivoParaLeitura.getName().toLowerCase(Locale.ROOT);
        try {
            if (nomeLeitura.endsWith(".docx")) {
                lerDocx(arquivoParaLeitura, identificador, resultado);
            } else if (nomeLeitura.endsWith(".doc")) {
                lerDocLegado(arquivoParaLeitura, resultado);
            } else {
                throw new IOException("Formato Word nao suportado: " + arquivoParaLeitura.getName());
            }

            if (resultado.isSucesso()) {
                String observacaoBase = "Importado do arquivo Word: " + arquivo.getName();
                if (conversao.getLogDetalhado() != null && !conversao.getLogDetalhado().trim().isEmpty()) {
                    observacaoBase += " | " + conversao.getLogDetalhado();
                }
                resultado.setObservacao(observacaoBase);
            }
        } catch (IOException e) {
            resultado.setSucesso(false);
            resultado.setErro("Erro ao ler arquivo Word: " + e.getMessage());
        }

        return resultado;
    }

    public static byte[] getEmbeddedImageBytes(String src) {
        if (src == null || !src.startsWith("embedded://")) {
            return null;
        }
        byte[] bytes = IMAGENS_EMBEDDADAS.get(src);
        return bytes == null ? null : Arrays.copyOf(bytes, bytes.length);
    }

    private WordLegacyDocConverter.ConversionResult prepararArquivoComFallback(File arquivoOriginal) throws IOException {
        try {
            return converter.prepararArquivo(arquivoOriginal, config);
        } catch (IOException conversaoFalhou) {
            if (arquivoOriginal.getName().toLowerCase(Locale.ROOT).endsWith(".doc")
                    && config != null
                    && config.isWordDocFallbackToDirectRead()) {
                return new WordLegacyDocConverter.ConversionResult(
                        arquivoOriginal,
                        false,
                        "Conversao DOC->DOCX indisponivel; aplicando parser DOC direto.",
                        java.util.Arrays.asList(
                                "Falha de conversao capturada no reader.",
                                "Fallback direto para parser DOC acionado."
                        )
                );
            }
            throw conversaoFalhou;
        }
    }

    private void lerDocx(File arquivo, String identificador, ResultadoProcessamento resultado) throws IOException {

        try (FileInputStream fis = new FileInputStream(arquivo);
             XWPFDocument doc = new XWPFDocument(fis)) {

            StringBuilder texto = new StringBuilder();
            List<ImagemInfo> imagens = new ArrayList<>();

            for (XWPFParagraph paragrafo : doc.getParagraphs()) {
                anexarTextoParagrafo(paragrafo, texto);
                extrairImagensParagrafo(paragrafo, identificador, imagens);
            }

            // Muitos documentos funcionais usam tabelas para estruturar campos de tela.
            for (XWPFTable tabela : doc.getTables()) {
                for (XWPFTableRow row : tabela.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragrafoCelula : cell.getParagraphs()) {
                            anexarTextoParagrafo(paragrafoCelula, texto);
                            extrairImagensParagrafo(paragrafoCelula, identificador, imagens);
                        }
                    }
                }
            }

            String textoFiltrado = normalizarTextoWord(texto.toString(), arquivo.getName(), !imagens.isEmpty());
            resultado.setTextoExtraido(textoFiltrado);
            resultado.setImagens(imagens);
            resultado.setSucesso(true);
        }
    }

    private void lerDocLegado(File arquivo, ResultadoProcessamento resultado) throws IOException {
        try (FileInputStream fis = new FileInputStream(arquivo);
             HWPFDocument doc = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(doc)) {

            String texto = extractor.getText();
            List<ImagemInfo> imagens = extrairImagensDocLegado(doc, "upload://" + arquivo.getName());
            String textoFiltrado = normalizarTextoWord(texto == null ? "" : texto, arquivo.getName(), !imagens.isEmpty());
            resultado.setTextoExtraido(textoFiltrado);
            resultado.setImagens(imagens);
            resultado.setSucesso(true);
        }
    }

    private void anexarTextoParagrafo(XWPFParagraph paragrafo, StringBuilder texto) {
        if (paragrafo == null) {
            return;
        }
        String linha = paragrafo.getText();
        if (linha != null && !linha.trim().isEmpty()) {
            texto.append(linha.trim()).append("\n");
        }
    }

    private void extrairImagensParagrafo(XWPFParagraph paragrafo, String identificador, List<ImagemInfo> imagens) {
        if (paragrafo == null) {
            return;
        }
        for (XWPFRun run : paragrafo.getRuns()) {
            List<XWPFPicture> fotos = run.getEmbeddedPictures();
            if (fotos == null) {
                continue;
            }
            for (XWPFPicture foto : fotos) {
                String descricao = foto.getDescription();
                String nomeArquivo = foto.getPictureData() != null
                        ? foto.getPictureData().getFileName()
                        : "imagem_embutida";
                byte[] dados = foto.getPictureData() == null ? null : foto.getPictureData().getData();
                if (!isImagemPertinente(dados)) {
                    continue;
                }
                String chaveImagem = gerarChaveEmbedded(identificador, nomeArquivo, imagens.size() + 1);
                IMAGENS_EMBEDDADAS.put(chaveImagem, Arrays.copyOf(dados, dados.length));
                ImagemInfo info = new ImagemInfo(
                        identificador,
                        chaveImagem,
                        descricao != null ? descricao : nomeArquivo
                );
                imagens.add(info);
            }
        }
    }

    private List<ImagemInfo> extrairImagensDocLegado(HWPFDocument doc, String identificador) {
        List<ImagemInfo> imagens = new ArrayList<>();
        if (doc == null || doc.getPicturesTable() == null) {
            return imagens;
        }
        List<Picture> pictures = doc.getPicturesTable().getAllPictures();
        if (pictures == null || pictures.isEmpty()) {
            return imagens;
        }

        int indice = 0;
        for (Picture picture : pictures) {
            if (picture == null) {
                continue;
            }
            byte[] dados = picture.getContent();
            if (!isImagemPertinente(dados)) {
                continue;
            }
            indice++;
            String nomeArquivo = picture.suggestFullFileName();
            if (nomeArquivo.trim().isEmpty()) {
                nomeArquivo = "imagem_doc_" + indice;
            }
            String chaveImagem = gerarChaveEmbedded(identificador, nomeArquivo, indice);
            IMAGENS_EMBEDDADAS.put(chaveImagem, Arrays.copyOf(dados, dados.length));
            imagens.add(new ImagemInfo(identificador, chaveImagem, nomeArquivo));
        }
        return imagens;
    }

    private boolean isImagemPertinente(byte[] dados) {
        return dados != null && dados.length >= 1024;
    }

    private String gerarChaveEmbedded(String identificador, String nomeArquivo, int indice) {
        String base = (identificador == null ? "upload" : identificador)
                .replace("upload://", "")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String nome = (nomeArquivo == null ? "imagem" : nomeArquivo).replaceAll("[^a-zA-Z0-9._-]", "_");
        return "embedded://" + base + "/" + indice + "-" + nome;
    }

    private String normalizarTextoWord(String textoBruto, String nomeArquivo, boolean possuiImagens) {
        if (textoBruto == null || textoBruto.trim().isEmpty()) {
            return "";
        }
        String[] linhas = textoBruto.replace("\r", "").split("\n");
        StringBuilder saida = new StringBuilder();
        boolean di = isDocumentoDi(nomeArquivo, textoBruto);

        for (String original : linhas) {
            String linha = original == null ? "" : original.trim();
            if (linha.isEmpty() || deveDescartarLinha(linha)) {
                continue;
            }
            if (di && !isLinhaRelevanteDi(linha)) {
                continue;
            }
            if (saida.length() > 0) {
                saida.append("\n");
            }
            saida.append(linha);
        }

        if (di && possuiImagens && saida.length() > 1800) {
            return saida.substring(0, 1800) + "...";
        }
        return saida.toString();
    }

    private boolean isDocumentoDi(String nomeArquivo, String textoBruto) {
        String nome = nomeArquivo == null ? "" : nomeArquivo.toLowerCase(Locale.ROOT);
        String texto = textoBruto == null ? "" : textoBruto.toLowerCase(Locale.ROOT);
        return nome.contains("integracao_di")
                || nome.contains("_di_")
                || texto.contains("documento de interface")
                || texto.contains("integração di")
                || texto.contains("integracao di");
    }

    private boolean deveDescartarLinha(String linha) {
        String lower = linha.toLowerCase(Locale.ROOT);
        return lower.contains("_toc")
                || lower.contains("msodatastore")
                || lower.contains("_pid_hlinks")
                || lower.contains("summaryinformation")
                || lower.contains("documentsummaryinformation")
                || lower.contains("historico de revisao")
                || lower.contains("confidencial")
                || lower.contains("rodape")
                || lower.contains("cabecalho")
                || lower.contains("footer-odd")
                || lower.contains("bjbjh!h!");
    }

    private boolean isLinhaRelevanteDi(String linha) {
        String lower = linha.toLowerCase(Locale.ROOT);
        return lower.contains("tela")
                || lower.contains("campo")
                || lower.contains("label")
                || lower.contains("botao")
                || lower.contains("botão")
                || lower.contains("front")
                || lower.contains("parametro")
                || lower.contains("parâmetro")
                || lower.contains("pre-cond")
                || lower.contains("pos-cond")
                || lower.contains("servico")
                || lower.contains("serviço")
                || lower.contains("json")
                || lower.contains("endpoint")
                || lower.contains("request")
                || lower.contains("response");
    }
}



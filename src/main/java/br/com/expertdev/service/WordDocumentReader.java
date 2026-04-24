package br.com.expertdev.service;

import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.model.ImagemInfo;
import br.com.expertdev.model.ResultadoProcessamento;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
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
import java.util.List;
import java.util.Locale;

/**
 * Lê arquivo Word (.docx e .doc) e extrai texto e referências de imagens,
 * retornando um ResultadoProcessamento para ser usado no pipeline normal.
 */
public class WordDocumentReader {

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

            resultado.setTextoExtraido(texto.toString().trim());
            resultado.setImagens(imagens);
            resultado.setSucesso(true);
        }
    }

    private void lerDocLegado(File arquivo, ResultadoProcessamento resultado) throws IOException {
        try (FileInputStream fis = new FileInputStream(arquivo);
             HWPFDocument doc = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(doc)) {

            String texto = extractor.getText();
            resultado.setTextoExtraido(texto == null ? "" : texto.trim());
            resultado.setImagens(new ArrayList<ImagemInfo>());
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
                ImagemInfo info = new ImagemInfo(
                        identificador,
                        "embedded://" + nomeArquivo,
                        descricao != null ? descricao : nomeArquivo
                );
                imagens.add(info);
            }
        }
    }
}



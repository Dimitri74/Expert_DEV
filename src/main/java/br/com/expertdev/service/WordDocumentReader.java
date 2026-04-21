package br.com.expertdev.service;

import br.com.expertdev.model.ImagemInfo;
import br.com.expertdev.model.ResultadoProcessamento;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê um arquivo .docx e extrai texto e referências de imagens,
 * retornando um ResultadoProcessamento para ser usado no pipeline normal.
 */
public class WordDocumentReader {

    public ResultadoProcessamento ler(File arquivo) {
        String identificador = "upload://" + arquivo.getName();
        ResultadoProcessamento resultado = new ResultadoProcessamento(identificador);

        try (FileInputStream fis = new FileInputStream(arquivo);
             XWPFDocument doc = new XWPFDocument(fis)) {

            StringBuilder texto = new StringBuilder();
            List<ImagemInfo> imagens = new ArrayList<>();

            for (XWPFParagraph paragrafo : doc.getParagraphs()) {
                String linha = paragrafo.getText();
                if (linha != null && !linha.trim().isEmpty()) {
                    texto.append(linha).append("\n");
                }

                // Extrai imagens embutidas nos runs do parágrafo
                for (XWPFRun run : paragrafo.getRuns()) {
                    List<XWPFPicture> fotos = run.getEmbeddedPictures();
                    if (fotos != null) {
                        for (XWPFPicture foto : fotos) {
                            String descricao = foto.getDescription();
                            String nomeArquivo = foto.getPictureData() != null
                                    ? foto.getPictureData().getFileName()
                                    : "imagem_embutida";
                            // Cria referência simbólica (imagem embutida, sem URL externa)
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

            resultado.setTextoExtraido(texto.toString().trim());
            resultado.setImagens(imagens);
            resultado.setSucesso(true);
            resultado.setObservacao("Importado do arquivo Word: " + arquivo.getName());

        } catch (IOException e) {
            resultado.setSucesso(false);
            resultado.setErro("Erro ao ler arquivo Word: " + e.getMessage());
        }

        return resultado;
    }
}


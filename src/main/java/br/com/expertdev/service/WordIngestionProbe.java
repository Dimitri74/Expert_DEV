package br.com.expertdev.service;

import br.com.expertdev.model.ResultadoProcessamento;

import java.io.File;

/**
 * Runner simples para validar a ingestao de arquivo Word no contexto da Etapa B2.
 */
public class WordIngestionProbe {

    private static final String STATUS_DOCX_DIRETO = "DOCX_DIRETO";
    private static final String STATUS_DOC_CONVERTIDO = "DOC_CONVERTIDO";
    private static final String STATUS_DOC_FALLBACK = "DOC_FALLBACK";
    private static final String STATUS_WORD_GENERICO = "WORD_OK";

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Uso: java br.com.expertdev.service.WordIngestionProbe <arquivo.doc|arquivo.docx>");
            System.exit(2);
            return;
        }

        File arquivo = new File(args[0]);
        WordDocumentReader reader = new WordDocumentReader();
        ResultadoProcessamento r = reader.ler(arquivo);

        if (!r.isSucesso()) {
            System.out.println("ERRO: " + r.getErro());
            System.exit(1);
            return;
        }

        int tamanho = r.getTextoExtraido() == null ? 0 : r.getTextoExtraido().length();
        int imagens = r.getImagens() == null ? 0 : r.getImagens().size();
        String status = detectarStatus(r.getObservacao());
        System.out.println("OK");
        System.out.println("Status: " + status);
        System.out.println("Observacao: " + r.getObservacao());
        System.out.println("Caracteres: " + tamanho);
        System.out.println("Imagens: " + imagens);
        System.exit(0);
    }

    private static String detectarStatus(String observacao) {
        if (observacao == null || observacao.trim().isEmpty()) {
            return STATUS_WORD_GENERICO;
        }

        String texto = observacao.toLowerCase();
        if (texto.contains("formato docx detectado: leitura direta")) {
            return STATUS_DOCX_DIRETO;
        }
        if (texto.contains("conversao concluida com sucesso")) {
            return STATUS_DOC_CONVERTIDO;
        }
        if (texto.contains("fallback direto para parser doc") || texto.contains("parser doc direto")) {
            return STATUS_DOC_FALLBACK;
        }
        return STATUS_WORD_GENERICO;
    }
}


package br.com.expertdev.pro.workflow;

import br.com.expertdev.pro.model.PromptBundle;
import br.com.expertdev.pro.model.IssueContext;

/**
 * Serviço para entregar prompts (clipboard, arquivo, etc).
 */
public class PromptDeliveryService {

    /**
     * Copia prompt para área de transferência do SO.
     */
    public void copiarParaClipboard(String conteudo) throws Exception {
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        java.awt.datatransfer.StringSelection selection =
                new java.awt.datatransfer.StringSelection(conteudo);
        toolkit.getSystemClipboard().setContents(selection, null);
    }

    /**
     * Salva prompt em arquivo .md para referência futura.
     */
    public String salvarPrompt(PromptBundle bundle) throws Exception {
        String nomeArquivo = gerarNomeArquivo(bundle);
        String caminhoPrompts = System.getProperty("user.home") + "/.expertdev/prompts";

        java.nio.file.Path dir = java.nio.file.Paths.get(caminhoPrompts);
        java.nio.file.Files.createDirectories(dir);

        java.nio.file.Path arquivo = dir.resolve(nomeArquivo);
        String conteudo = formatarPromptParaSalvar(bundle);
        java.nio.file.Files.write(arquivo, conteudo.getBytes());

        return arquivo.toString();
    }

    private String gerarNomeArquivo(PromptBundle bundle) {
        long timestamp = bundle.getTimestampCriacao();
        String tipo = bundle.getTipoPrompt() != null ? bundle.getTipoPrompt() : "general";
        return String.format("prompt_%s_%d.md", tipo, timestamp);
    }

    private String formatarPromptParaSalvar(PromptBundle bundle) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Prompt ExpertDev ").append(bundle.getVersaoExpertDev()).append("\n");
        sb.append("**Tipo:** ").append(bundle.getTipoPrompt()).append("\n");
        sb.append("**Data:** ").append(new java.util.Date(bundle.getTimestampCriacao())).append("\n");
        sb.append("**Arquivo:** ").append(bundle.getContexto().getArquivoAlvo()).append("\n\n");
        sb.append("## Prompt\n\n");
        sb.append(bundle.getPromptGerado());
        return sb.toString();
    }
}


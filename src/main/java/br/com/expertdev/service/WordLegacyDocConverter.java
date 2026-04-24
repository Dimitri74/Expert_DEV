package br.com.expertdev.service;

import br.com.expertdev.config.ExpertDevConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Converte documentos .doc para .docx usando LibreOffice headless quando disponivel.
 */
public class WordLegacyDocConverter {

    public static class ConversionResult {
        private final File arquivoParaLeitura;
        private final boolean convertido;
        private final String detalhe;
        private final List<String> etapas;

        public ConversionResult(File arquivoParaLeitura, boolean convertido, String detalhe, List<String> etapas) {
            this.arquivoParaLeitura = arquivoParaLeitura;
            this.convertido = convertido;
            this.detalhe = detalhe;
            this.etapas = etapas == null
                    ? Collections.<String>emptyList()
                    : Collections.unmodifiableList(new ArrayList<String>(etapas));
        }

        public File getArquivoParaLeitura() {
            return arquivoParaLeitura;
        }

        public boolean isConvertido() {
            return convertido;
        }

        public String getDetalhe() {
            return detalhe;
        }

        public List<String> getEtapas() {
            return etapas;
        }

        public String getLogDetalhado() {
            if (etapas == null || etapas.isEmpty()) {
                return detalhe == null ? "" : detalhe;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < etapas.size(); i++) {
                if (i > 0) {
                    sb.append(" | ");
                }
                sb.append(etapas.get(i));
            }
            return sb.toString();
        }
    }

    private static class LibreOfficeDetection {
        private final String executavel;
        private final List<String> etapas;

        private LibreOfficeDetection(String executavel, List<String> etapas) {
            this.executavel = executavel;
            this.etapas = etapas;
        }
    }

    public ConversionResult prepararArquivo(File arquivoOriginal, ExpertDevConfig config) throws IOException {
        List<String> etapas = new ArrayList<String>();
        if (arquivoOriginal == null || !arquivoOriginal.exists()) {
            throw new IOException("Arquivo nao encontrado para leitura Word.");
        }
        etapas.add("Arquivo recebido: " + arquivoOriginal.getName());

        String nome = arquivoOriginal.getName().toLowerCase(Locale.ROOT);
        if (nome.endsWith(".docx")) {
            etapas.add("Formato DOCX detectado: leitura direta.");
            return new ConversionResult(arquivoOriginal, false, "Arquivo DOCX lido diretamente.", etapas);
        }
        if (!nome.endsWith(".doc")) {
            throw new IOException("Formato Word nao suportado: " + arquivoOriginal.getName());
        }
        etapas.add("Formato DOC legado detectado.");

        if (config == null || !config.isWordDocConversionEnabled()) {
            etapas.add("Conversao DOC desativada por configuracao.");
            if (config != null && config.isWordDocFallbackToDirectRead()) {
                etapas.add("Fallback direto para parser DOC habilitado.");
                return new ConversionResult(arquivoOriginal, false,
                        "Conversao DOC desativada; aplicando leitura direta de DOC.", etapas);
            }
            throw new IOException("Conversao de DOC desativada em configuracao.");
        }

        LibreOfficeDetection detection = detectarExecutavelLibreOffice(config);
        etapas.addAll(detection.etapas);
        String executavel = detection.executavel;
        if (executavel == null) {
            if (config.isWordDocFallbackToDirectRead()) {
                etapas.add("Fallback direto para parser DOC habilitado.");
                return new ConversionResult(arquivoOriginal, false,
                        "LibreOffice nao encontrado; aplicando leitura direta de DOC.", etapas);
            }
            throw new IOException("LibreOffice nao encontrado para converter DOC em DOCX.");
        }

        File pastaSaida = Files.createTempDirectory("expertdev-doc-conv-").toFile();
        pastaSaida.deleteOnExit();
        etapas.add("Pasta temporaria de conversao: " + pastaSaida.getAbsolutePath());

        File convertido = new File(pastaSaida, removerExtensao(arquivoOriginal.getName()) + ".docx");
        List<String> comando = new ArrayList<String>();
        comando.add(executavel);
        comando.add("--headless");
        comando.add("--convert-to");
        comando.add("docx");
        comando.add("--outdir");
        comando.add(pastaSaida.getAbsolutePath());
        comando.add(arquivoOriginal.getAbsolutePath());
        etapas.add("Executando conversao DOC->DOCX via LibreOffice.");

        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            process = pb.start();
            int timeout = Math.max(10, config.getWordDocConversionTimeoutSec());
            boolean finalizado = process.waitFor(timeout, TimeUnit.SECONDS);
            if (!finalizado) {
                process.destroyForcibly();
                etapas.add("Conversao interrompida por timeout de " + timeout + "s.");
                throw new IOException("Timeout ao converter DOC para DOCX.");
            }
            if (process.exitValue() != 0) {
                etapas.add("Conversao retornou codigo " + process.exitValue() + ".");
                throw new IOException("Conversao DOC->DOCX falhou (codigo " + process.exitValue() + ").");
            }
            if (!convertido.exists()) {
                etapas.add("Conversao finalizou sem arquivo DOCX de saida.");
                throw new IOException("Conversao concluida sem gerar arquivo DOCX.");
            }

            convertido.deleteOnExit();
            etapas.add("Conversao concluida com sucesso.");
            return new ConversionResult(convertido, true,
                    "Arquivo DOC convertido para DOCX com LibreOffice.", etapas);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            etapas.add("Conversao interrompida por sinal de thread.");
            throw new IOException("Conversao DOC interrompida.", e);
        }
    }

    private LibreOfficeDetection detectarExecutavelLibreOffice(ExpertDevConfig config) {
        List<String> etapas = new ArrayList<String>();
        if (config != null && config.getWordLibreOfficePath() != null
                && !config.getWordLibreOfficePath().trim().isEmpty()) {
            String configurado = config.getWordLibreOfficePath().trim();
            etapas.add("Tentando LibreOffice configurado: " + configurado);
            if (testarExecutavelLibreOffice(configurado)) {
                etapas.add("LibreOffice detectado no caminho configurado.");
                return new LibreOfficeDetection(configurado, etapas);
            }
            etapas.add("Caminho configurado nao esta executavel.");
        }

        String[] candidatos = new String[] {
                "soffice",
                "C:\\Program Files\\LibreOffice\\program\\soffice.exe",
                "C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe"
        };

        for (String candidato : candidatos) {
            etapas.add("Tentando detectar LibreOffice em: " + candidato);
            if (testarExecutavelLibreOffice(candidato)) {
                etapas.add("LibreOffice detectado em: " + candidato);
                return new LibreOfficeDetection(candidato, etapas);
            }
        }
        etapas.add("LibreOffice indisponivel nos caminhos avaliados.");
        return new LibreOfficeDetection(null, etapas);
    }

    private boolean testarExecutavelLibreOffice(String executavel) {
        try {
            ProcessBuilder pb = new ProcessBuilder(executavel, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finalizado = process.waitFor(5, TimeUnit.SECONDS);
            if (!finalizado) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String removerExtensao(String nomeArquivo) {
        int idx = nomeArquivo.lastIndexOf('.');
        return idx <= 0 ? nomeArquivo : nomeArquivo.substring(0, idx);
    }
}


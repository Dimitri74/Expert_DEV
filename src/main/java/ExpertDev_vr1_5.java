import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.io.DefaultTextFileWriter;
import br.com.expertdev.io.TextFileWriter;
import br.com.expertdev.model.ExecucaoConsolidada;
import br.com.expertdev.model.ResultadoProcessamento;
import br.com.expertdev.service.ImageDownloader;
import br.com.expertdev.service.ParallelUrlProcessor;
import br.com.expertdev.service.PdfDocumentBuilder;
import br.com.expertdev.service.PromptGenerator;
import br.com.expertdev.service.ResultConsolidator;
import br.com.expertdev.service.UrlParser;
import br.com.expertdev.service.WordDocumentBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;

public class ExpertDev_vr1_5 {

    public static void main(String[] args) {
        Instant inicio = Instant.now();
        ExpertDevConfig config = ExpertDevConfig.carregar();

        System.out.println("=== Expert Dev 2.4.0-BETA (com Paralelismo, PDF e IA opcional na UI) ===\n");

        String input = obterInput(args);
        UrlParser urlParser = new UrlParser();
        List<String> urls = urlParser.parsear(input);

        if (urls.isEmpty()) {
            System.err.println("❌ Nenhuma URL válida foi informada. Encerrando.");
            return;
        }

        // Processamento paralelo com número de threads baseado em cores disponíveis
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        System.out.println("Iniciando processamento paralelo com " + numThreads + " thread(s)...\n");

        ParallelUrlProcessor parallelProcessor = new ParallelUrlProcessor(config, numThreads);
        List<ResultadoProcessamento> resultados = parallelProcessor.processar(urls);

        // Gerar Word
        String arquivoWord = gerarDocumentoWord(resultados);

        // Gerar PDF
        String arquivoPdf = gerarDocumentoPdf(resultados);

        // Consolidar e gerar demais outputs
        ExecucaoConsolidada execucaoFinal = new ResultConsolidator(new PromptGenerator())
                .consolidar(resultados, urls.size(), inicio, Instant.now(), arquivoWord, arquivoPdf, "");

        salvarSaidas(config, execucaoFinal, new DefaultTextFileWriter());
        imprimirResumoFinal(config, execucaoFinal);
    }

    private static String obterInput(String[] args) {
        String input = (args.length > 0) ? args[0] : null;
        if (input != null && !input.trim().isEmpty()) {
            return input.trim();
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Cole as URL(s) do site (separadas por vírgula): ");
        return scanner.nextLine().trim();
    }

    private static String gerarDocumentoWord(List<ResultadoProcessamento> resultados) {
        try {
            ImageDownloader imageDownloader = new ImageDownloader();
            WordDocumentBuilder builder = new WordDocumentBuilder(imageDownloader);
            return builder.gerar(resultados);
        } catch (Exception e) {
            System.err.println("⚠ Erro ao gerar documento Word: " + e.getMessage());
            return "";
        }
    }

    private static String gerarDocumentoPdf(List<ResultadoProcessamento> resultados) {
        try {
            ImageDownloader imageDownloader = new ImageDownloader();
            PdfDocumentBuilder builder = new PdfDocumentBuilder(imageDownloader);
            return builder.gerar(resultados);
        } catch (Exception e) {
            System.err.println("⚠ Erro ao gerar documento PDF: " + e.getMessage());
            return "";
        }
    }

    private static void salvarSaidas(ExpertDevConfig config,
                                     ExecucaoConsolidada execucao,
                                     TextFileWriter textFileWriter) {
        try {
            textFileWriter.write(config.getArquivoResumo(), execucao.getResumoExecucao());
            System.out.println("   ✓ Salvo: " + config.getArquivoResumo());

            if (execucao.possuiSucesso()) {
                textFileWriter.write(config.getArquivoRegras(), execucao.getRegrasExtraidas());
                System.out.println("   ✓ Salvo: " + config.getArquivoRegras());

                textFileWriter.write(config.getArquivoImagens(), execucao.getImagensEncontradas());
                System.out.println("   ✓ Salvo: " + config.getArquivoImagens());

                textFileWriter.write(config.getArquivoPrompt(), execucao.getPromptPronto());
                System.out.println("   ✓ Salvo: " + config.getArquivoPrompt());
            }

            if (execucao.possuiErros()) {
                textFileWriter.write(config.getArquivoErros(), execucao.getErrosProcessamento());
                System.out.println("   ✓ Salvo: " + config.getArquivoErros());
            }
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar arquivos de saída: "
                    + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private static void imprimirResumoFinal(ExpertDevConfig config, ExecucaoConsolidada execucao) {
        System.out.println();
        System.out.println(repeat("=", 90));

        if (execucao.possuiSucesso()) {
            System.out.println("✅ Expert Dev finalizado!");
            System.out.println("Arquivos gerados:");
            System.out.println("   • " + config.getArquivoRegras());
            System.out.println("   • " + config.getArquivoImagens());
            System.out.println("   • " + config.getArquivoPrompt());
            if (!execucao.getArquivoWord().isEmpty()) {
                System.out.println("   • " + execucao.getArquivoWord() + " (Word com imagens embarcadas)");
            }
            if (!execucao.getArquivoPdf().isEmpty()) {
                System.out.println("   • " + execucao.getArquivoPdf() + " (PDF com imagens embarcadas)");
            }
        } else {
            System.out.println("⚠ Processamento finalizado sem URLs processadas com sucesso.");
        }

        System.out.println("   • " + config.getArquivoResumo());
        if (execucao.possuiErros()) {
            System.out.println("   • " + config.getArquivoErros());
        }

        System.out.println("\nResumo da execução:");
        System.out.println("   • URLs recebidas: " + execucao.getTotalUrls());
        System.out.println("   • Processadas com sucesso: " + execucao.getUrlsComSucesso());
        System.out.println("   • Com falha: " + execucao.getUrlsComFalha());
        System.out.println("   • Imagens coletadas (únicas): " + execucao.getTotalImagens());
        System.out.println("   • Tempo total: " + execucao.getTempoTotalSegundos() + "s");
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}

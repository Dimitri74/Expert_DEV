package br.com.expertdev.service;

import br.com.expertdev.model.ExecucaoConsolidada;
import br.com.expertdev.model.ImagemInfo;
import br.com.expertdev.model.ResultadoProcessamento;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class ResultConsolidator {

    private static final DateTimeFormatter RESUMO_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final String DELIMITADOR_REGRAS = "\n\n" + repeat("=", 80) + "\n\n";

    private final PromptGenerationService promptGenerationService;
    private final MsgSistemaRtcFilterService msgSistemaRtcFilterService;

    public ResultConsolidator(PromptGenerator promptGenerator) {
        this(new LocalPromptGenerationService(promptGenerator));
    }

    public ResultConsolidator(PromptGenerationService promptGenerationService) {
        this.promptGenerationService = promptGenerationService;
        this.msgSistemaRtcFilterService = new MsgSistemaRtcFilterService();
    }

    public ExecucaoConsolidada consolidar(List<ResultadoProcessamento> resultados,
                                          int totalUrls,
                                          Instant inicio,
                                          Instant fim,
                                          String arquivoWord,
                                          String arquivoPdf,
                                          String rtcNumero) {
        StringJoiner regrasExtraidas = new StringJoiner(DELIMITADOR_REGRAS);
        StringJoiner imagensEncontradas = new StringJoiner("\n");
        List<String> erros = new ArrayList<>();
        List<String> urlsSucesso = new ArrayList<>();
        List<String> urlsFalha = new ArrayList<>();
        Set<String> imagensGlobais = new LinkedHashSet<>();

        int sucesso = 0;
        int falhas = 0;
        int totalImagens = 0;

        for (ResultadoProcessamento resultado : resultados) {
            if (resultado.isSucesso()) {
                sucesso++;
                urlsSucesso.add(resultado.getUrl());
                regrasExtraidas.add(criarBlocoTexto(resultado, rtcNumero));
                totalImagens += adicionarImagens(resultado, imagensGlobais, imagensEncontradas);
            } else {
                falhas++;
                urlsFalha.add(resultado.getUrl());
                erros.add("URL: " + resultado.getUrl() + " | Erro: " + resultado.getErro());
            }
        }

        long tempoTotalSegundos = Duration.between(inicio, fim).getSeconds();
        String regras = regrasExtraidas.toString();
        String imagens = imagensEncontradas.toString();
        String resumo = gerarResumoExecucao(totalUrls, sucesso, falhas, totalImagens, tempoTotalSegundos, inicio, fim, urlsSucesso, urlsFalha);
        String prompt;
        try {
            prompt = promptGenerationService.gerarPrompt(regras, imagens);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar prompt no modo "
                    + promptGenerationService.getNomeModo() + ": " + e.getMessage(), e);
        }
        String errosTexto = erros.isEmpty() ? "" : String.join("\n", erros);

        return new ExecucaoConsolidada(
                regras,
                imagens,
                prompt,
                resumo,
                errosTexto,
                arquivoWord,
                arquivoPdf,
                totalUrls,
                sucesso,
                falhas,
                totalImagens,
                tempoTotalSegundos
        );
    }

    private String criarBlocoTexto(ResultadoProcessamento resultado, String rtcNumero) {
        StringBuilder blocoTexto = new StringBuilder();
        blocoTexto.append("URL: ").append(resultado.getUrl()).append("\n\n");

        if (resultado.getObservacao() != null && !resultado.getObservacao().trim().isEmpty()) {
            blocoTexto.append("Observação: ").append(resultado.getObservacao()).append("\n\n");
        }

        String textoBase = resultado.getTextoExtraido() == null ? "" : resultado.getTextoExtraido();
        String textoFiltrado = msgSistemaRtcFilterService.reduzirRuidoSeAplicavel(
                resultado.getUrl(), textoBase, rtcNumero);
        blocoTexto.append(textoFiltrado);
        return blocoTexto.toString();
    }

    private int adicionarImagens(ResultadoProcessamento resultado,
                                 Set<String> imagensGlobais,
                                 StringJoiner imagensEncontradas) {
        List<ImagemInfo> imagens = resultado.getImagens();
        if (imagens == null || imagens.isEmpty()) {
            return 0;
        }

        boolean cabecalhoAdicionado = false;
        int adicionadas = 0;

        for (ImagemInfo imagem : imagens) {
            if (!imagensGlobais.add(imagem.getSrc())) {
                continue;
            }

            if (!cabecalhoAdicionado) {
                imagensEncontradas.add("\n--- Imagens encontradas na página: " + resultado.getUrl() + " ---");
                cabecalhoAdicionado = true;
            }

            imagensEncontradas.add("Imagem: " + imagem.getSrc());
            if (imagem.getAlt() != null && !imagem.getAlt().trim().isEmpty()) {
                imagensEncontradas.add("   Descrição: " + imagem.getAlt());
            }
            adicionadas++;
        }

        return adicionadas;
    }

    private String gerarResumoExecucao(int totalUrls,
                                       int sucesso,
                                       int falhas,
                                       int totalImagens,
                                       long tempoTotalSegundos,
                                       Instant inicio,
                                       Instant fim,
                                       List<String> urlsSucesso,
                                       List<String> urlsFalha) {
        StringBuilder resumo = new StringBuilder();
        resumo.append("Resumo da execução - Expert Dev 2.5.0-BETA\n");
        resumo.append(repeat("=", 50)).append("\n\n");
        resumo.append("Início: ").append(RESUMO_DATE_FORMAT.format(inicio)).append("\n");
        resumo.append("Fim: ").append(RESUMO_DATE_FORMAT.format(fim)).append("\n\n");

        resumo.append("Totais:\n");
        resumo.append(" - URLs recebidas: ").append(totalUrls).append("\n");
        resumo.append(" - URLs com sucesso: ").append(sucesso).append("\n");
        resumo.append(" - URLs com falha: ").append(falhas).append("\n");
        resumo.append(" - Imagens únicas coletadas: ").append(totalImagens).append("\n");
        resumo.append(" - Tempo total (s): ").append(tempoTotalSegundos).append("\n\n");

        resumo.append("URLs processadas com sucesso:\n");
        appendUrls(resumo, urlsSucesso);
        resumo.append("\nURLs com falha:\n");
        appendUrls(resumo, urlsFalha);

        return resumo.toString();
    }

    private void appendUrls(StringBuilder resumo, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            resumo.append(" - Nenhuma\n");
            return;
        }

        for (String url : urls) {
            resumo.append(" - ").append(url).append("\n");
        }
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}


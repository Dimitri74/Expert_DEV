package br.com.expertdev.service;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class PromptContextRefiner {

    public RefinedPromptContext refinar(String regras, String imagens) {
        String regrasLimpas = limparBloco(regras, "[Sem regras extraidas]");
        String imagensLimpas = limparBloco(imagens, "[Sem imagens detectadas]");

        String objetivo = detectarObjetivo(regrasLimpas);
        String restricoes = detectarRestricoes(regrasLimpas);
        String lacunas = detectarLacunas(regrasLimpas, imagensLimpas);
        String blocosRelevantes = extrairBlocosRelevantes(regrasLimpas);
        String resumoIntermediario = gerarResumoIntermediario(regrasLimpas, imagensLimpas, blocosRelevantes);

        return new RefinedPromptContext(
                regrasLimpas,
                imagensLimpas,
                objetivo,
                restricoes,
                lacunas,
                blocosRelevantes,
                resumoIntermediario
        );
    }

    private String limparBloco(String texto, String fallback) {
        if (texto == null || texto.trim().isEmpty()) {
            return fallback;
        }

        String[] linhas = texto.replace("\r", "").split("\n");
        Set<String> unicas = new LinkedHashSet<String>();
        StringBuilder sb = new StringBuilder();
        boolean ultimoFoiVazio = false;

        for (String linhaOriginal : linhas) {
            String linha = linhaOriginal == null ? "" : linhaOriginal.trim();
            if (deveIgnorarLinha(linha)) {
                continue;
            }

            if (linha.isEmpty()) {
                if (!ultimoFoiVazio && sb.length() > 0) {
                    sb.append("\n\n");
                }
                ultimoFoiVazio = true;
                continue;
            }

            String chave = linha.toLowerCase();
            if (!permitirDuplicidade(linha) && !unicas.add(chave)) {
                continue;
            }

            if (sb.length() > 0 && !ultimoFoiVazio) {
                sb.append("\n");
            }
            sb.append(linha);
            ultimoFoiVazio = false;
        }

        String resultado = sb.toString().trim();
        return resultado.isEmpty() ? fallback : resultado;
    }

    private boolean deveIgnorarLinha(String linha) {
        if (linha == null || linha.isEmpty()) {
            return false;
        }
        return linha.startsWith("![")
                || linha.startsWith("[![")
                || linha.startsWith("http://img.shields.io")
                || linha.startsWith("https://img.shields.io")
                || linha.matches("^[=-]{3,}$");
    }

    private boolean permitirDuplicidade(String linha) {
        return linha.startsWith("URL:") || linha.startsWith("Imagem:");
    }

    private String detectarObjetivo(String regras) {
        String texto = regras.toLowerCase();
        if (texto.contains("food delivery")) {
            return "Evoluir ou implementar uma plataforma de food delivery com foco em requisitos de negocio e arquitetura descritos no contexto.";
        }
        if (texto.contains("microsservi") || texto.contains("microservi")) {
            return "Planejar implementacao orientada a modulos, servicos e contratos a partir do contexto extraido.";
        }
        return "Analisar o contexto extraido e transformar os requisitos em um plano de implementacao tecnico acionavel.";
    }

    private String detectarRestricoes(String regras) {
        StringBuilder sb = new StringBuilder();
        sb.append("- Manter aderencia ao contexto extraido.\n");
        sb.append("- Evitar assumir requisitos nao citados sem sinalizar lacuna.\n");
        sb.append("- Priorizar objetividade, ordem de implementacao e testes.\n");
        if (regras.toLowerCase().contains("java 8")) {
            sb.append("- Considerar compatibilidade tecnológica mencionada no contexto.\n");
        }
        return sb.toString().trim();
    }

    private String detectarLacunas(String regras, String imagens) {
        StringBuilder sb = new StringBuilder();
        if (!regras.toLowerCase().contains("autentic") && !regras.toLowerCase().contains("login")) {
            sb.append("- Confirmar requisitos de autenticacao/autorizacao.\n");
        }
        if (!regras.toLowerCase().contains("banco") && !regras.toLowerCase().contains("mysql")
                && !regras.toLowerCase().contains("postgres")) {
            sb.append("- Confirmar persistencia e banco de dados esperados.\n");
        }
        if (imagens == null || imagens.contains("[Sem imagens detectadas]")) {
            sb.append("- Validar referencias visuais ausentes ou insuficientes.\n");
        }
        String texto = sb.toString().trim();
        return texto.isEmpty() ? "- Nenhuma lacuna critica detectada automaticamente." : texto;
    }

    private String extrairBlocosRelevantes(String regras) {
        String[] linhas = regras.replace("\r", "").split("\n");
        List<BlocoRelevancia> blocos = new ArrayList<BlocoRelevancia>();
        for (String linhaOriginal : linhas) {
            String linha = linhaOriginal == null ? "" : linhaOriginal.trim();
            if (linha.isEmpty() || linha.length() < 8) {
                continue;
            }
            int score = calcularScore(linha.toLowerCase());
            if (score > 0) {
                blocos.add(new BlocoRelevancia(linha, score));
            }
        }

        if (blocos.isEmpty()) {
            return "- Sem blocos de alta relevancia detectados automaticamente.";
        }

        Collections.sort(blocos, new Comparator<BlocoRelevancia>() {
            @Override
            public int compare(BlocoRelevancia a, BlocoRelevancia b) {
                return Integer.compare(b.score, a.score);
            }
        });

        StringBuilder sb = new StringBuilder();
        int limite = Math.min(6, blocos.size());
        for (int i = 0; i < limite; i++) {
            BlocoRelevancia bloco = blocos.get(i);
            sb.append("- [score ").append(bloco.score).append("] ").append(bloco.texto).append("\n");
        }
        return sb.toString().trim();
    }

    private int calcularScore(String linha) {
        int score = 0;
        if (linha.contains("arquitet") || linha.contains("microserv")) score += 5;
        if (linha.contains("endpoint") || linha.contains("/v1/")) score += 4;
        if (linha.contains("regra") || linha.contains("requisito")) score += 4;
        if (linha.contains("teste") || linha.contains("junit") || linha.contains("mockito")) score += 3;
        if (linha.contains("kafka") || linha.contains("saga")) score += 3;
        if (linha.contains("database") || linha.contains("mysql") || linha.contains("postgres")) score += 3;
        if (linha.contains("seguran") || linha.contains("auth") || linha.contains("jwt")) score += 3;
        if (linha.startsWith("##") || linha.startsWith("###") || linha.startsWith("- ")) score += 1;
        return score;
    }

    private String gerarResumoIntermediario(String regras, String imagens, String blocosRelevantes) {
        StringBuilder sb = new StringBuilder();
        sb.append("- Caracteres de regras refinadas: ").append(regras.length()).append("\n");
        sb.append("- Caracteres de referencias visuais: ").append(imagens.length()).append("\n");
        sb.append("- Principais blocos por relevancia:\n").append(blocosRelevantes).append("\n");
        sb.append("- Objetivo: converter contexto bruto em plano tecnico executavel com perguntas objetivas para lacunas.");
        return sb.toString().trim();
    }

    private static class BlocoRelevancia {
        private final String texto;
        private final int score;

        private BlocoRelevancia(String texto, int score) {
            this.texto = texto;
            this.score = score;
        }
    }

    public static class RefinedPromptContext {
        private final String regras;
        private final String imagens;
        private final String objetivo;
        private final String restricoes;
        private final String lacunas;
        private final String blocosRelevantes;
        private final String resumoIntermediario;

        public RefinedPromptContext(String regras,
                                    String imagens,
                                    String objetivo,
                                    String restricoes,
                                    String lacunas,
                                    String blocosRelevantes,
                                    String resumoIntermediario) {
            this.regras = regras;
            this.imagens = imagens;
            this.objetivo = objetivo;
            this.restricoes = restricoes;
            this.lacunas = lacunas;
            this.blocosRelevantes = blocosRelevantes;
            this.resumoIntermediario = resumoIntermediario;
        }

        public String getRegras() {
            return regras;
        }

        public String getImagens() {
            return imagens;
        }

        public String getObjetivo() {
            return objetivo;
        }

        public String getRestricoes() {
            return restricoes;
        }

        public String getLacunas() {
            return lacunas;
        }

        public String getBlocosRelevantes() {
            return blocosRelevantes;
        }

        public String getResumoIntermediario() {
            return resumoIntermediario;
        }
    }
}


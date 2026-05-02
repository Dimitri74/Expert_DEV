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
        String t = regras.toLowerCase();
        // UC/DI: presença de fluxos e condições indica especificação funcional
        if (t.contains("caso de uso") || t.contains("fluxo b") || t.contains("pré-cond") || t.contains("pre-cond")) {
            return "Implementar ou evoluir o caso de uso descrito no contexto, respeitando fluxos, regras de negócio e interfaces especificados.";
        }
        // API/serviço: presença de contrato HTTP
        if (t.contains("endpoint") || t.contains("request") || t.contains("response") || t.contains("get ") || t.contains("post ")) {
            return "Implementar ou ajustar o serviço/endpoint descrito no contexto, respeitando contrato, validações e códigos de retorno.";
        }
        // Requisitos não-funcionais
        if (t.contains("nao-funcional") || t.contains("não-funcional") || t.contains(" rnf")) {
            return "Atender aos requisitos não-funcionais descritos no contexto, garantindo aderência às restrições de qualidade, segurança e desempenho.";
        }
        return "Analisar o contexto extraído e transformar os requisitos em um plano de implementação técnico acionável.";
    }

    private String detectarRestricoes(String regras) {
        StringBuilder sb = new StringBuilder();
        String t = regras.toLowerCase();

        sb.append("- Usar exclusivamente a stack, versões e padrões detectados no contexto.\n");
        sb.append("- Não assumir requisitos não citados sem sinalizar como lacuna.\n");
        sb.append("- Preservar organização de camadas e contratos existentes no projeto.\n");

        // Versão Java — do mais específico para o mais genérico
        if (t.contains("java 7")) {
            sb.append("- Java 7 detectado: evitar APIs de versões posteriores (sem lambda, sem streams nativos).\n");
        } else if (t.contains("java 8")) {
            sb.append("- Java 8 detectado: lambda e streams OK; evitar APIs Java 9+.\n");
        } else if (t.contains("java 11")) {
            sb.append("- Java 11 detectado: APIs LTS disponíveis até Java 11.\n");
        } else if (t.contains("java 17") || t.contains("java 21")) {
            sb.append("- Java moderno detectado: records, sealed classes e switch expressions disponíveis.\n");
        }

        // Framework back-end
        if (t.contains("quarkus")) {
            sb.append("- Framework: Quarkus — usar CDI, MicroProfile e extensões Quarkus. Evitar APIs não suportadas no modo nativo.\n");
        } else if (t.contains("spring boot") || t.contains("spring-boot")) {
            sb.append("- Framework: Spring Boot — seguir convenções Spring (IoC, AOP, Data).\n");
        } else if (t.contains("java ee") || t.contains("jee") || t.contains("jakarta ee")) {
            sb.append("- Framework: JEE/Jakarta EE — usar EJB, CDI e JPA conforme padrão do projeto.\n");
        }

        // Front-end
        if (t.contains("jsf") || t.contains("primefaces") || t.contains("facelets")) {
            sb.append("- Front-end: JSF — usar lifecycle e componentes JSF; não misturar com SPA frameworks.\n");
        } else if (t.contains("angular")) {
            sb.append("- Front-end: Angular — seguir módulos, serviços e componentes conforme estrutura existente.\n");
        } else if (t.contains("backbone")) {
            sb.append("- Front-end: Backbone.js — usar models, views e routers conforme padrão existente.\n");
        }

        // Banco de dados
        if (t.contains("db2")) {
            sb.append("- Banco: DB2 — atentar ao dialeto SQL DB2 e paginação (FETCH FIRST n ROWS ONLY).\n");
        } else if (t.contains("oracle")) {
            sb.append("- Banco: Oracle — atentar ao dialeto Oracle e uso de sequences/ROWNUM.\n");
        }

        return sb.toString().trim();
    }

    private String detectarLacunas(String regras, String imagens) {
        StringBuilder sb = new StringBuilder();
        String t = regras.toLowerCase();

        // Autenticação/autorização ausente
        if (!t.contains("autentic") && !t.contains("login") && !t.contains("autoriza")
                && !t.contains("jwt") && !t.contains("oauth") && !t.contains("session")) {
            sb.append("- Confirmar se há requisito de autenticação/autorização para este fluxo.\n");
        }

        // Persistência ausente
        if (!t.contains("banco") && !t.contains("db2") && !t.contains("oracle")
                && !t.contains("mysql") && !t.contains("postgres") && !t.contains("persist")
                && !t.contains("reposit") && !t.contains("dao") && !t.contains("jpa")) {
            sb.append("- Confirmar estratégia de persistência (banco e camada de acesso a dados).\n");
        }

        // Pré/pós-condições ausentes — indica especificação incompleta
        if (!t.contains("pré-cond") && !t.contains("pre-cond")
                && !t.contains("pós-cond") && !t.contains("pos-cond")) {
            sb.append("- Pré e pós-condições não identificadas — confirmar comportamento de entrada e saída esperados.\n");
        }

        String texto = sb.toString().trim();
        return texto.isEmpty() ? "- Nenhuma lacuna crítica detectada automaticamente." : texto;
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
        if (linha.contains("fluxo") || linha.contains("parametro") || linha.contains("parâmetro")) score += 4;
        if (linha.contains("pre-cond") || linha.contains("pré-cond") || linha.contains("pos-cond") || linha.contains("pós-cond")) score += 4;
        if (linha.contains("caso de uso") || linha.contains("objetivo") || linha.contains("ator")) score += 3;
        if (linha.contains("ma") && linha.matches(".*\\b(ma|mn|mh)\\d{2,4}\\b.*")) score += 4;
        if (linha.contains("teste") || linha.contains("junit") || linha.contains("mockito")) score += 3;
        if (linha.contains("kafka") || linha.contains("saga")) score += 3;
        if (linha.contains("database") || linha.contains("mysql") || linha.contains("postgres") || linha.contains("db2")) score += 3;
        if (linha.contains("seguran") || linha.contains("auth") || linha.contains("jwt")) score += 3;
        if (linha.startsWith("##") || linha.startsWith("###") || linha.startsWith("- ")) score += 1;
        return score;
    }

    private String gerarResumoIntermediario(String regras, String imagens, String blocosRelevantes) {
        StringBuilder sb = new StringBuilder();
        String t = regras.toLowerCase();

        // Monta descrição de stack detectada no texto do contexto
        StringBuilder stack = new StringBuilder();
        detectarJava(t, stack);
        detectarFrameworkBackend(t, stack);
        detectarFrontend(t, stack);
        detectarBanco(t, stack);

        sb.append("- Stack detectada: ")
                .append(stack.length() > 0 ? stack.toString() : "não identificada no contexto")
                .append("\n");

        sb.append("- Principais pontos extraídos:\n").append(blocosRelevantes);
        return sb.toString().trim();
    }

    private static void detectarJava(String t, StringBuilder stack) {
        if (t.contains("java 7")) { appendToken(stack, "Java 7"); }
        else if (t.contains("java 8")) { appendToken(stack, "Java 8"); }
        else if (t.contains("java 11")) { appendToken(stack, "Java 11"); }
        else if (t.contains("java 17") || t.contains("java 21")) { appendToken(stack, "Java 17+"); }
        else if (t.contains("java")) { appendToken(stack, "Java"); }
    }

    private static void detectarFrameworkBackend(String t, StringBuilder stack) {
        if (t.contains("quarkus")) { appendToken(stack, "Quarkus"); }
        else if (t.contains("spring boot") || t.contains("spring-boot")) { appendToken(stack, "Spring Boot"); }
        else if (t.contains("java ee") || t.contains("jee") || t.contains("jakarta ee")) { appendToken(stack, "JEE"); }
    }

    private static void detectarFrontend(String t, StringBuilder stack) {
        if (t.contains("jsf") || t.contains("primefaces")) { appendToken(stack, "JSF"); }
        else if (t.contains("angular")) { appendToken(stack, "Angular"); }
        else if (t.contains("backbone")) { appendToken(stack, "Backbone.js"); }
        else if (t.contains("react")) { appendToken(stack, "React"); }
    }

    private static void detectarBanco(String t, StringBuilder stack) {
        if (t.contains("db2")) { appendToken(stack, "DB2"); }
        else if (t.contains("oracle")) { appendToken(stack, "Oracle"); }
        else if (t.contains("mysql")) { appendToken(stack, "MySQL"); }
        else if (t.contains("postgres")) { appendToken(stack, "PostgreSQL"); }
        else if (t.contains("mongodb")) { appendToken(stack, "MongoDB"); }
    }

    private static void appendToken(StringBuilder sb, String token) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(token);
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


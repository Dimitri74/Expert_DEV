package br.com.expertdev.service;

public class PromptBlueprintBuilder {

    // -- prompt de uso local (sem IA) ------------------------------------------

    public String buildLocalPrompt(PromptContextRefiner.RefinedPromptContext context, String perfilPrompt) {
        String perfil = normalizarPerfil(perfilPrompt);
        StringBuilder sb = new StringBuilder();

        sb.append("Você é um desenvolvedor sênior. Com base no contexto abaixo, entregue um plano de implementação técnico e acionável.\n\n");

        sb.append("Perfil: ").append("negocial".equals(perfil)
                ? "Negocial — visão de negócio, priorização, impacto e riscos."
                : "Técnico — detalhamento de implementação, contratos e testes.")
                .append("\n\n");

        sb.append("Objetivo:\n").append(context.getObjetivo()).append("\n\n");

        sb.append("Stack e contexto detectados:\n").append(context.getResumoIntermediario()).append("\n\n");

        sb.append("Contexto consolidado (requisitos e regras):\n").append(context.getRegras()).append("\n\n");

        if (!context.getImagens().contains("[Sem imagens")) {
            sb.append("Referências visuais:\n").append(context.getImagens()).append("\n\n");
        }

        sb.append("Restrições detectadas no contexto:\n").append(context.getRestricoes()).append("\n\n");

        sb.append("Lacunas identificadas:\n").append(context.getLacunas()).append("\n\n");

        appendDiretrizes(sb);
        appendFormatoResposta(sb, perfil);

        return sb.toString();
    }

    // -- prompt de sistema para IA ---------------------------------------------

    public String buildAiSystemPrompt() {
        return "Você é um especialista em engenharia de prompt e planejamento técnico de software. "
                + "Sua saída deve ser estruturada, objetiva, acionável e pronta para uso em assistentes de código.";
    }

    // -- prompt de usuário enviado à IA para gerar o prompt final --------------

    public String buildAiUserPrompt(PromptContextRefiner.RefinedPromptContext context,
                                    String observacaoEconomica,
                                    String perfilPrompt) {
        String perfil = normalizarPerfil(perfilPrompt);
        StringBuilder sb = new StringBuilder();

        sb.append("Gere um PROMPT FINAL conciso e acionável para um desenvolvedor sênior.\n\n");
        sb.append("Perfil desejado: ").append(perfil).append("\n");
        sb.append("Observação de processamento: ").append(observacaoEconomica).append("\n\n");

        sb.append("Stack detectada no contexto:\n").append(context.getResumoIntermediario()).append("\n\n");
        sb.append("Contexto (requisitos e regras):\n").append(context.getRegras()).append("\n\n");

        if (!context.getImagens().contains("[Sem imagens")) {
            sb.append("Referências visuais:\n").append(context.getImagens()).append("\n\n");
        }

        sb.append("Lacunas:\n").append(context.getLacunas()).append("\n\n");

        sb.append("O prompt gerado deve conter obrigatoriamente:\n");
        sb.append("1. Objetivo claro derivado do contexto.\n");
        sb.append("2. Stack detectada — nunca inventar tecnologias ausentes no contexto.\n");
        sb.append("3. Plano de implementação (Backend / Frontend / Integrações — somente os aplicáveis).\n");
        sb.append("4. Diretriz de indentação parcial: apenas o trecho implementado, não a classe inteira.\n");
        sb.append("5. Diretriz de qualidade Sonar: sem magic numbers, sem variáveis não utilizadas, sem catch vazio.\n");
        sb.append("6. Rastreabilidade // RTC: e // UC: quando presentes no contexto.\n");
        sb.append("7. Lacunas com perguntas objetivas — sem criar dúvidas desnecessárias.\n\n");

        sb.append("Retorne apenas o prompt final, sem comentários extras.");
        return sb.toString();
    }

    // -- helpers privados -------------------------------------------------------

    /**
     * Diretrizes de implementação comuns a todos os perfis e modos.
     * Tecnologia-agnósticas — baseadas no que o contexto revelar.
     */
    private void appendDiretrizes(StringBuilder sb) {
        sb.append("Diretrizes de implementação:\n");
        sb.append("1. Use EXCLUSIVAMENTE a stack, versões e padrões detectados no contexto. Não assuma nem invente tecnologias ausentes.\n");
        sb.append("2. Preserve a organização de camadas, convenções de nomes e contratos do projeto existente.\n");
        sb.append("3. Indente apenas o trecho implementado — não reformate a classe inteira.\n");
        sb.append("4. Inclua comentários onde a lógica não for autoexplicativa; evite comentários óbvios.\n");
        sb.append("5. Escreva código sem bloqueios Sonar: sem magic numbers expostos, sem variáveis não utilizadas, sem catch vazio, sem métodos acima do limite de complexidade ciclomática.\n");
        sb.append("6. Quando RTC e/ou UC estiverem presentes no contexto, inclua no topo do trecho implementado:\n");
        sb.append("   // RTC: <numero>\n");
        sb.append("   // UC: <codigo> — <descricao>\n\n");
    }

    /**
     * Contrato de formato da resposta esperada do desenvolvedor/IA.
     */
    private void appendFormatoResposta(StringBuilder sb, String perfil) {
        sb.append("Formato da resposta:\n");
        sb.append("1. Plano de implementação por prioridade (Backend / Frontend / Integrações — somente os aplicáveis ao contexto).\n");
        sb.append("2. Impactos, dependências e efeitos colaterais.\n");
        sb.append("3. Testes unitários aderentes à versão e framework detectados.\n");
        sb.append("4. Perguntas objetivas apenas para lacunas reais — não crie dúvidas desnecessárias.\n");
        sb.append("5. Use linguagem ").append("negocial".equals(perfil)
                ? "estratégica e orientada ao negócio"
                : "objetiva e técnica")
                .append(".\n");
    }

    private String normalizarPerfil(String perfilPrompt) {
        if (perfilPrompt == null) {
            return "tecnico";
        }
        String perfil = perfilPrompt.trim().toLowerCase();
        if ("executivo".equals(perfil) || "negocial".equals(perfil)) {
            return "negocial";
        }
        return "tecnico";
    }
}


package br.com.expertdev.service;

public class PromptBlueprintBuilder {

    public String buildLocalPrompt(PromptContextRefiner.RefinedPromptContext context, String perfilPrompt) {
        String perfil = normalizarPerfil(perfilPrompt);
        StringBuilder sb = new StringBuilder();
        sb.append("Você é um desenvolvedor sênior com foco em Java 8, Angular e JavaScript.\n\n");
        sb.append("Perfil de resposta:\n");
        sb.append("- ").append("negocial".equals(perfil)
                ? "Negocial (visao de negocio, priorizacao, impacto e riscos)."
                : "Tecnico (detalhamento de implementacao, contratos e testes).")
                .append("\n\n");
        sb.append("Objetivo:\n");
        sb.append(context.getObjetivo()).append("\n\n");
        sb.append("Resumo intermediario do contexto:\n");
        sb.append(context.getResumoIntermediario()).append("\n\n");
        sb.append("Contexto consolidado (regras e requisitos):\n\n");
        sb.append(context.getRegras()).append("\n\n");
        sb.append("Referências visuais encontradas:\n\n");
        sb.append(context.getImagens()).append("\n\n");
        sb.append("Restrições obrigatórias:\n");
        sb.append(context.getRestricoes()).append("\n\n");
        sb.append("Lacunas e pontos para confirmação:\n");
        sb.append(context.getLacunas()).append("\n\n");
        sb.append("Diretrizes obrigatórias de implementação:\n");
        sb.append("- O desenvolvimento deve seguir as tecnologias, padrões e arquitetura já adotados no projeto existente.\n");
        sb.append("- Preserve a organização de camadas, convenções de nomes, contratos públicos e estilo predominante da base atual.\n");
        sb.append("- Adote boas práticas compatíveis com Java 8.\n");
        sb.append("- Use streams e expressões lambda quando fizer sentido para clareza, legibilidade e manutenção, sem uso excessivo ou desnecessário.\n");
        sb.append("- Priorize código coeso, legível, testável e aderente às limitações reais de Java 8.\n\n");
        sb.append("Regra obrigatória de rastreabilidade no código:\n");
        sb.append("- Ao implementar ou alterar código, inclua os comentários de auditoria no topo do arquivo ou do trecho principal entregue.\n");
        sb.append("- Use exatamente os identificadores informados no contexto, por exemplo:\n");
        sb.append("  // RTC: <numero>\n");
        sb.append("  // UC: <codigo e descricao>\n");
        sb.append("- Não omita esses comentários na implementação final quando RTC/UC estiverem presentes no prompt.\n\n");
        sb.append("Formato obrigatório da resposta:\n");
        sb.append("1. Comece com um plano detalhado por módulos e ordem de prioridade.\n");
        sb.append("2. Liste impactos, dependências e possíveis efeitos colaterais.\n");
        sb.append("3. Proponha testes unitários aderentes às tecnologias e versões descritas.\n");
        sb.append("4. Faça perguntas objetivas apenas onde houver lacunas reais.\n");
        sb.append("5. Não invente stack, dependências ou decisões ausentes no contexto.\n");
        sb.append("6. Use linguagem ").append("negocial".equals(perfil) ? "estrategica e orientada ao negocio" : "objetiva e tecnica")
                .append(", pronta para execução.\n");
        return sb.toString();
    }

    public String buildAiSystemPrompt() {
        return "Voce e um especialista em engenharia de prompt e planejamento tecnico de software. "
                + "Sua saida deve ser estruturada, objetiva, acionavel e pronta para uso em assistentes de codigo.";
    }

    public String buildAiUserPrompt(PromptContextRefiner.RefinedPromptContext context,
                                    String observacaoEconomica,
                                    String perfilPrompt) {
        String perfil = normalizarPerfil(perfilPrompt);
        StringBuilder sb = new StringBuilder();
        sb.append("Gere um PROMPT FINAL de alta qualidade para um desenvolvedor senior com foco em Java 8, Angular e JavaScript.\n\n");
        sb.append("Perfil de resposta desejado: ").append(perfil).append(".\n\n");
        sb.append("Objetivo do prompt final:\n");
        sb.append(context.getObjetivo()).append("\n\n");
        sb.append("Observacao de processamento:\n");
        sb.append(observacaoEconomica).append("\n\n");
        sb.append("Resumo intermediario:\n");
        sb.append(context.getResumoIntermediario()).append("\n\n");
        sb.append("Contexto refinado:\n");
        sb.append(context.getRegras()).append("\n\n");
        sb.append("Referencias visuais refinadas:\n");
        sb.append(context.getImagens()).append("\n\n");
        sb.append("Restricoes:\n");
        sb.append(context.getRestricoes()).append("\n\n");
        sb.append("Lacunas detectadas:\n");
        sb.append(context.getLacunas()).append("\n\n");
        sb.append("Diretrizes obrigatórias de implementação:\n");
        sb.append("- O desenvolvimento deve seguir as tecnologias, padrões e arquitetura já adotados no projeto existente.\n");
        sb.append("- Preserve a organização de camadas, convenções de nomes, contratos públicos e estilo predominante da base atual.\n");
        sb.append("- Adote boas práticas compatíveis com Java 8.\n");
        sb.append("- Use streams e expressões lambda quando fizer sentido para clareza, legibilidade e manutenção, sem uso excessivo ou desnecessário.\n");
        sb.append("- Priorize código coeso, legível, testável e aderente às limitações reais de Java 8.\n\n");
        sb.append("Regra obrigatória de rastreabilidade no código:\n");
        sb.append("- Ao implementar ou alterar código, inclua os comentários de auditoria no topo do arquivo ou do trecho principal entregue.\n");
        sb.append("- Use exatamente os identificadores informados no contexto, por exemplo:\n");
        sb.append("  // RTC: <numero>\n");
        sb.append("  // UC: <codigo e descricao>\n");
        sb.append("- Não omita esses comentários na implementação final quando RTC/UC estiverem presentes no prompt.\n\n");
        sb.append("Contrato obrigatorio de saida:\n");
        sb.append("1) Comecar com plano detalhado por modulos e ordem de prioridade.\n");
        sb.append("2) Listar impactos, dependencias e efeitos colaterais.\n");
        sb.append("3) Propor testes unitarios aderentes.\n");
        sb.append("4) Fazer perguntas objetivas se houver lacunas reais.\n");
        sb.append("5) Garantir que a implementação proposta preserve os comentários // RTC: e // UC: quando eles estiverem presentes no contexto.\n");
        sb.append("6) Nao incluir explicacoes sobre como voce pensou.\n\n");
        sb.append("Retorne apenas o prompt final, sem comentarios extras.");
        return sb.toString();
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


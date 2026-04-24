package br.com.expertdev.service;

public class PromptBlueprintBuilder {

    public String buildLocalPrompt(PromptContextRefiner.RefinedPromptContext context, String perfilPrompt) {
        String perfil = normalizarPerfil(perfilPrompt);
        StringBuilder sb = new StringBuilder();
        sb.append("Você é um desenvolvedor sênior capaz de atuar com Java, Angular, JavaScript e outras tecnologias modernas.\n\n");
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
        sb.append("- O desenvolvimento deve seguir rigorosamente as tecnologias, padrões e arquitetura já adotados no projeto existente.\n");
        sb.append("- Siga a versão da linguagem (ex: Java 7, 8, 11, 17+) e do ecossistema (Node, Angular, etc) detectada no contexto.\n");
        sb.append("- Preserve a organização de camadas, convenções de nomes, contratos públicos e estilo predominante da base atual.\n");
        sb.append("- Priorize código de alto nível, coeso, legível, testável e aderente às melhores práticas da stack identificada.\n");
        sb.append("- Stack/arquitetura obrigatória do SIPCS quando aplicável: Quarkus 3.x, Java 17/21, Maven, Clean Architecture, DB2 e deploy em OKD 4.\n");
        sb.append("- Não violar fronteiras da Clean Architecture (domain, application/use cases, adapters, infrastructure) e não acoplar regra de negócio a framework.\n");
        sb.append("- Explicitar impactos em DB2 (schema, query, transação, paginação) e em plataforma OKD 4 (configuração, probes, requests/limits, observabilidade).\n\n");
        sb.append("NFRs e qualidade obrigatórios:\n");
        sb.append("- Defina metas mensuráveis de performance, disponibilidade, resiliência e segurança para o plano proposto.\n");
        sb.append("- Incluir estratégia de rollout/rollback no OKD 4 (health checks, monitoração e plano de reversão).\n");
        sb.append("- Incluir cobertura de qualidade ligada ao Sonar (ex.: cobertura mínima de testes e ausência de novos blockers/critical no escopo alterado).\n\n");
        sb.append("Regra obrigatória de rastreabilidade no código:\n");
        sb.append("- Ao implementar ou alterar código, inclua os comentários de auditoria no topo do arquivo ou do trecho principal entregue.\n");
        sb.append("- Use exatamente os identificadores informados no contexto, por exemplo:\n");
        sb.append("  // RTC: <numero>\n");
        sb.append("  // UC: <codigo e descricao>\n");
        sb.append("- Não omita esses comentários na implementação final quando RTC/UC estiverem presentes no prompt.\n\n");
        sb.append("Formato obrigatório da resposta:\n");
        sb.append("1. Comece com um plano detalhado por ordem de prioridade, separado em Backend, Frontend e Integrações/Contratos (quando aplicável).\n");
        sb.append("2. Liste impactos, dependências e possíveis efeitos colaterais.\n");
        sb.append("3. Proponha testes unitários aderentes às tecnologias e versões detectadas.\n");
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
        sb.append("Gere um PROMPT FINAL de alta qualidade para um desenvolvedor sênior capaz de atuar com Java, Angular, JavaScript e outras tecnologias modernas.\n\n");
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
        sb.append("- O desenvolvimento deve seguir rigorosamente as tecnologias, padrões e arquitetura já adotados no projeto existente.\n");
        sb.append("- Siga a versão da linguagem (ex: Java 7, 8, 11, 17+) e do ecossistema (Node, Angular, etc) detectada no contexto.\n");
        sb.append("- Preserve a organização de camadas, convenções de nomes, contratos públicos e estilo predominante da base atual.\n");
        sb.append("- Priorize código de alto nível, coeso, legível, testável e aderente às melhores práticas da stack identificada.\n");
        sb.append("- Stack/arquitetura obrigatória do SIPCS quando aplicável: Quarkus 3.x, Java 17/21, Maven, Clean Architecture, DB2 e deploy em OKD 4.\n");
        sb.append("- Não violar fronteiras da Clean Architecture (domain, application/use cases, adapters, infrastructure) e não acoplar regra de negócio a framework.\n");
        sb.append("- Explicitar impactos em DB2 (schema, query, transação, paginação) e em plataforma OKD 4 (configuração, probes, requests/limits, observabilidade).\n\n");
        sb.append("NFRs e qualidade obrigatórios:\n");
        sb.append("- Defina metas mensuráveis de performance, disponibilidade, resiliência e segurança para o plano proposto.\n");
        sb.append("- Incluir estratégia de rollout/rollback no OKD 4 (health checks, monitoração e plano de reversão).\n");
        sb.append("- Incluir cobertura de qualidade ligada ao Sonar (ex.: cobertura mínima de testes e ausência de novos blockers/critical no escopo alterado).\n\n");
        sb.append("Regra obrigatória de rastreabilidade no código:\n");
        sb.append("- Ao implementar ou alterar código, inclua os comentários de auditoria no topo do arquivo ou do trecho principal entregue.\n");
        sb.append("- Use exatamente os identificadores informados no contexto, por exemplo:\n");
        sb.append("  // RTC: <numero>\n");
        sb.append("  // UC: <codigo e descricao>\n");
        sb.append("- Não omita esses comentários na implementação final quando RTC/UC estiverem presentes no prompt.\n\n");
        sb.append("Contrato obrigatorio de saida:\n");
        sb.append("1) Comecar com plano detalhado por ordem de prioridade, separado em Backend, Frontend e Integracoes/Contratos (quando aplicavel).\n");
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


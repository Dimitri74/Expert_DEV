package br.com.expertdev.pro.workflow;

import br.com.expertdev.pro.model.IssueContext;
import br.com.expertdev.pro.model.PromptBundle;
import br.com.expertdev.pro.model.AiAssistantTarget;

/**
 * Serviço para gerar prompts contextualizados com dados técnicos.
 * Reutiliza contexto existente e cria prompts padronizados.
 */
public class PromptContextService {

    /**
     * Gera prompt estruturado baseado em contexto técnico.
     */
    public PromptBundle gerarPrompt(IssueContext contexto, String tipoPrompt) {
        return gerarPrompt(contexto, tipoPrompt, AiAssistantTarget.COPILOT);
    }

    /**
     * Gera prompt estruturado com personalização por assistente IA.
     */
    public PromptBundle gerarPrompt(IssueContext contexto, String tipoPrompt, AiAssistantTarget assistenteAlvo) {
        String prompt = construirPrompt(contexto, tipoPrompt, assistenteAlvo);

        PromptBundle bundle = new PromptBundle(prompt, contexto);
        bundle.setTipoPrompt(tipoPrompt);
        bundle.setAssistenteAlvo(assistenteAlvo);

        return bundle;
    }

    private String construirPrompt(IssueContext contexto, String tipoPrompt) {
        return construirPrompt(contexto, tipoPrompt, AiAssistantTarget.COPILOT);
    }

    private String construirPrompt(IssueContext contexto, String tipoPrompt, AiAssistantTarget assistenteAlvo) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Solicitação ExpertDev\n\n");
        sb.append("**Tipo:** ").append(tipoPrompt).append("\n");
        sb.append("**Assistente IA:** ").append(assistenteAlvo.getRotulo()).append("\n");
        sb.append("**Arquivo:** ").append(contexto.getArquivoAlvo()).append("\n");
        if (contexto.getLinhaAlvo() > 0) {
            sb.append("**Linha:** ").append(contexto.getLinhaAlvo()).append("\n");
        }
        sb.append("**Versão:** ExpertDev 2.5.0-BETA\n\n");

        sb.append("## Problema\n\n");
        sb.append(contexto.getDescricaoProblema()).append("\n\n");

        if (contexto.getStackTrace() != null && !contexto.getStackTrace().isEmpty()) {
            sb.append("## Stack Trace\n\n");
            sb.append("```\n");
            sb.append(contexto.getStackTrace()).append("\n");
            sb.append("```\n\n");
        }

        if (contexto.getObjetivoTarefa() != null && !contexto.getObjetivoTarefa().isEmpty()) {
            sb.append("## Objetivo\n\n");
            sb.append(contexto.getObjetivoTarefa()).append("\n\n");
        }

        if (contexto.getCriteriosAceite() != null && !contexto.getCriteriosAceite().isEmpty()) {
            sb.append("## Critérios de Aceite\n\n");
            sb.append(contexto.getCriteriosAceite()).append("\n\n");
        }

        sb.append("## Instruções\n\n");
        sb.append("1. Analise o código no arquivo mencionado\n");
        sb.append("2. Identifique a causa raiz do problema\n");
        sb.append("3. Forneça solução com código exemplo\n");
        sb.append("4. Liste passos de teste\n");
        sb.append("5. Entregue a resposta no formato: diagnóstico -> correção -> validação\n\n");

        sb.append("## Entrega no Assistente\n\n");
        sb.append("- Destino: ").append(assistenteAlvo.getRotulo()).append("\n");
        sb.append("- Ação: ").append(assistenteAlvo.getInstrucaoUso()).append("\n");

        return sb.toString();
    }

    /**
     * Template de prompt para bug UI/Layout.
     */
    public PromptBundle gerarPromptUI(IssueContext contexto) {
        if (contexto.getDescricaoProblema() == null) {
            contexto.setDescricaoProblema("Ajuste visual em componentes Swing");
        }
        contexto.setCategoria("UI");
        return gerarPrompt(contexto, "UI_LAYOUT_FIX");
    }

    /**
     * Template de prompt para bug de Autenticação.
     */
    public PromptBundle gerarPromptAuth(IssueContext contexto) {
        if (contexto.getDescricaoProblema() == null) {
            contexto.setDescricaoProblema("Issue em fluxo de autenticação ou recuperação");
        }
        contexto.setCategoria("Auth");
        return gerarPrompt(contexto, "AUTH_ISSUE");
    }

    /**
     * Template de prompt para erro de compilação/build.
     */
    public PromptBundle gerarPromptBuild(IssueContext contexto) {
        if (contexto.getDescricaoProblema() == null) {
            contexto.setDescricaoProblema("Erro de compilação Maven ou build failure");
        }
        contexto.setCategoria("Build");
        return gerarPrompt(contexto, "BUILD_ERROR");
    }

    /**
     * Template de prompt para erros de banco de dados.
     */
    public PromptBundle gerarPromptDatabaseError(IssueContext contexto) {
        return gerarPromptDatabaseError(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptDatabaseError(IssueContext contexto, AiAssistantTarget assistenteAlvo) {
        if (contexto.getDescricaoProblema() == null) {
            contexto.setDescricaoProblema("Erro de banco de dados: query falhando, constraint violation ou conexão perdida");
        }
        if (contexto.getCriteriosAceite() == null) {
            contexto.setCriteriosAceite(
                "1. Query executar sem erro\n" +
                "2. Dados persistidos corretamente\n" +
                "3. Transações com rollback em caso de falha\n" +
                "4. Connection pool não esgotado"
            );
        }
        contexto.setCategoria("Database");
        return gerarPrompt(contexto, "DATABASE_ERROR", assistenteAlvo);
    }

    /**
     * Template de prompt para problemas de performance.
     */
    public PromptBundle gerarPromptPerformance(IssueContext contexto) {
        return gerarPromptPerformance(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptPerformance(IssueContext contexto, AiAssistantTarget assistenteAlvo) {
        if (contexto.getDescricaoProblema() == null) {
            contexto.setDescricaoProblema("Problema de performance: resposta lenta, alto uso de memória ou CPU excessiva");
        }
        if (contexto.getCriteriosAceite() == null) {
            contexto.setCriteriosAceite(
                "1. Tempo de resposta < 2s para operações normais\n" +
                "2. Uso de memória heap estável (sem leak)\n" +
                "3. Queries com índices adequados\n" +
                "4. Sem N+1 queries identificadas"
            );
        }
        contexto.setCategoria("Performance");
        return gerarPrompt(contexto, "PERFORMANCE_ISSUE", assistenteAlvo);
    }

    /**
     * Template de prompt para vulnerabilidades de segurança.
     */
    public PromptBundle gerarPromptSecurity(IssueContext contexto) {
        return gerarPromptSecurity(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptSecurity(IssueContext contexto, AiAssistantTarget assistenteAlvo) {
        if (contexto.getDescricaoProblema() == null) {
            contexto.setDescricaoProblema("Vulnerabilidade de segurança: possível SQL injection, XSS, CSRF ou exposição de dados");
        }
        if (contexto.getCriteriosAceite() == null) {
            contexto.setCriteriosAceite(
                "1. Inputs sanitizados e validados\n" +
                "2. Sem dados sensíveis expostos em logs\n" +
                "3. Prepared statements em todas as queries\n" +
                "4. OWASP Top 10 verificado no trecho alterado"
            );
        }
        contexto.setCategoria("Security");
        return gerarPrompt(contexto, "SECURITY_VULNERABILITY", assistenteAlvo);
    }

    /**
     * Template de prompt para refatoração de código.
     */
    public PromptBundle gerarPromptRefactoring(IssueContext contexto) {
        return gerarPromptRefactoring(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptRefactoring(IssueContext contexto, AiAssistantTarget assistenteAlvo) {
        if (contexto.getDescricaoProblema() == null) {
            contexto.setDescricaoProblema("Refatoração necessária: código duplicado, alta complexidade ciclomática ou violações SOLID");
        }
        if (contexto.getCriteriosAceite() == null) {
            contexto.setCriteriosAceite(
                "1. Comportamento externo preservado (sem quebra de testes)\n" +
                "2. Complexidade ciclomática reduzida\n" +
                "3. Duplicação eliminada (DRY)\n" +
                "4. Cobertura de testes mantida ou ampliada"
            );
        }
        contexto.setCategoria("Refactoring");
        return gerarPrompt(contexto, "REFACTORING", assistenteAlvo);
    }

    /**
     * Template de prompt para criação/correção de testes.
     */
    public PromptBundle gerarPromptTestCoverage(IssueContext contexto) {
        return gerarPromptTestCoverage(contexto, AiAssistantTarget.COPILOT);
    }

    public PromptBundle gerarPromptTestCoverage(IssueContext contexto, AiAssistantTarget assistenteAlvo) {
        if (contexto.getDescricaoProblema() == null) {
            contexto.setDescricaoProblema("Testes insuficientes ou ausentes para o componente indicado");
        }
        if (contexto.getCriteriosAceite() == null) {
            contexto.setCriteriosAceite(
                "1. Cobertura de linha >= 80% no componente\n" +
                "2. Casos de sucesso e falha cobertos\n" +
                "3. Testes isolados (sem dependências externas reais)\n" +
                "4. Execução < 5s no total"
            );
        }
        contexto.setCategoria("Tests");
        return gerarPrompt(contexto, "TEST_CREATION", assistenteAlvo);
    }

    /**
     * Compatibilidade retroativa com chamadas antigas.
     */
    public PromptBundle gerarPromptTests(IssueContext contexto) {
        return gerarPromptTestCoverage(contexto);
    }
}


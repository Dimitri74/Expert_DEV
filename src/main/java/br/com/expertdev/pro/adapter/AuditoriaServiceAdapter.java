package br.com.expertdev.pro.adapter;

import br.com.expertdev.model.RegistroAuditoria;
import br.com.expertdev.service.AuditoriaService;
import br.com.expertdev.pro.model.IssueContext;
import br.com.expertdev.pro.model.PromptBundle;

import java.util.List;

/**
 * Adapter que expõe AuditoriaService legado para o módulo Pro.
 * Registra automaticamente prompts gerados na auditoria do sistema.
 */
public class AuditoriaServiceAdapter {

    private final AuditoriaService auditoriaService;

    public AuditoriaServiceAdapter() {
        this.auditoriaService = new AuditoriaService();
    }

    public AuditoriaServiceAdapter(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // -------------------------------------------------------------------------
    // Integração com fluxo Pro
    // -------------------------------------------------------------------------

    /**
     * Registra a geração de um prompt na auditoria legada.
     * Reutiliza RegistroAuditoria para compatibilidade total.
     * @return id do registro criado, ou -1 em caso de erro.
     */
    public long registrarGeracaoPrompt(PromptBundle bundle, String rtcNumero, String ucCodigo) {
        RegistroAuditoria reg = new RegistroAuditoria();
        reg.setRtcNumero(rtcNumero != null ? rtcNumero : "PRO-PROMPT");
        reg.setUcCodigo(ucCodigo != null ? ucCodigo : "N/A");
        reg.setUcDescricao(bundle.getTipoPrompt() != null ? bundle.getTipoPrompt() : "Prompt Pro");
        reg.setModoGeracao("PRO");
        reg.setProvider(bundle.getAssistenteAlvo() != null ? bundle.getAssistenteAlvo().name() : "COPILOT");
        reg.setStatus("GERADO");
        reg.setPromptGerado(bundle.getPromptGerado());

        if (bundle.getContexto() != null) {
            reg.setUrlsOuArquivo(bundle.getContexto().getArquivoAlvo());
        }

        return auditoriaService.inserir(reg);
    }

    /**
     * Registra geração de prompt com contexto completo do IssueContext.
     */
    public long registrarGeracaoPrompt(PromptBundle bundle, IssueContext contexto) {
        String rtc = contexto.getCategoria() != null ? "PRO-" + contexto.getCategoria().toUpperCase() : "PRO-GERAL";
        return registrarGeracaoPrompt(bundle, rtc, contexto.getArquivoAlvo());
    }

    /**
     * Atualiza o status de um registro existente.
     */
    public void atualizarStatus(long id, String status) {
        auditoriaService.atualizar(id, status, null);
    }

    /**
     * Retorna os últimos N registros de auditoria.
     */
    public List<RegistroAuditoria> listarUltimos() {
        return auditoriaService.obterTodos();
    }

    /**
     * Retorna registros por usuário autenticado.
     */
    public List<RegistroAuditoria> listarPorUsuario(String username) {
        return auditoriaService.obterTodosPorUsuario(username);
    }

    /**
     * Retorna o último registro criado.
     */
    public RegistroAuditoria obterUltimo() {
        return auditoriaService.obterUltimo();
    }

    /**
     * Acesso ao serviço legado bruto para operações avançadas.
     */
    public AuditoriaService getAuditoriaServiceLegado() {
        return auditoriaService;
    }
}


package br.com.expertdev.pro.adapter;

import br.com.expertdev.model.AuthSession;
import br.com.expertdev.model.LicenseStatus;
import br.com.expertdev.service.AuthService;
import br.com.expertdev.pro.model.IssueContext;

/**
 * Adapter que expõe o AuthService legado para o módulo Pro.
 * Centraliza operações de autenticação sem duplicar código.
 */
public class AuthServiceAdapter {

    private final AuthService authService;
    private AuthSession sessaoAtual;

    public AuthServiceAdapter() {
        this.authService = new AuthService();
    }

    public AuthServiceAdapter(AuthService authService) {
        this.authService = authService;
    }

    // -------------------------------------------------------------------------
    // Autenticação
    // -------------------------------------------------------------------------

    /**
     * Realiza login e armazena a sessão corrente.
     * @return true se autenticado com sucesso.
     */
    public boolean login(String username, String senha) {
        AuthSession session = authService.autenticar(username, senha);
        if (session != null && session.getLicenseStatus() != LicenseStatus.EXPIRED) {
            this.sessaoAtual = session;
            return true;
        }
        return false;
    }

    /**
     * Verifica se há sessão ativa.
     */
    public boolean isSessaoAtiva() {
        return sessaoAtual != null && sessaoAtual.getLicenseStatus() != LicenseStatus.EXPIRED;
    }

    /**
     * Retorna o username da sessão corrente, ou null.
     */
    public String getUsuarioAtual() {
        return sessaoAtual != null ? sessaoAtual.getUsername() : null;
    }

    /**
     * Invalida a sessão corrente.
     */
    public void logout() {
        this.sessaoAtual = null;
    }

    /**
     * Enriquece IssueContext com dados do usuário autenticado.
     */
    public void enriquecerContexto(IssueContext contexto) {
        if (isSessaoAtiva()) {
            String usuario = getUsuarioAtual();
            if (contexto.getObjetivoTarefa() == null) {
                contexto.setObjetivoTarefa("Tarefa executada por: " + usuario);
            }
        }
    }

    /**
     * Acesso ao serviço legado bruto para operações avançadas.
     */
    public AuthService getAuthServiceLegado() {
        return authService;
    }

    public AuthSession getSessaoAtual() {
        return sessaoAtual;
    }
}


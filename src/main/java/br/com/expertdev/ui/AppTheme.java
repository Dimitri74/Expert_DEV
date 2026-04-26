package br.com.expertdev.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Centraliza paletas de cores, fontes e constantes de layout da GUI.
 * Mantém o estado do tema ativo (claro/escuro) e expõe as cores correntes
 * como campos públicos para evitar verbosidade nos construtores de painel.
 */
public class AppTheme {

    // ─── Paleta escura ────────────────────────────────────────────────────────
    public static final Color COR_FUNDO_ESCURO       = new Color(18, 18, 30);
    public static final Color COR_PAINEL_ESCURO      = new Color(28, 28, 45);
    public static final Color COR_PAINEL_ALT_ESCURO  = new Color(35, 35, 55);
    public static final Color COR_DESTAQUE_ESCURO    = new Color(99, 102, 241);
    public static final Color COR_DESTAQUE2_ESCURO   = new Color(139, 92, 246);
    public static final Color COR_SUCESSO_ESCURO     = new Color(52, 211, 153);
    public static final Color COR_ERRO_ESCURO        = new Color(248, 113, 113);
    public static final Color COR_TEXTO_ESCURO       = new Color(226, 232, 240);
    public static final Color COR_TEXTO_SUAVE_ESCURO = new Color(148, 163, 184);
    public static final Color COR_BORDA_ESCURO       = new Color(51, 65, 85);

    // ─── Paleta clara ─────────────────────────────────────────────────────────
    public static final Color COR_FUNDO_CLARO       = new Color(245, 247, 251);
    public static final Color COR_PAINEL_CLARO      = new Color(255, 255, 255);
    public static final Color COR_PAINEL_ALT_CLARO  = new Color(250, 251, 255);
    public static final Color COR_DESTAQUE_CLARO    = new Color(79, 70, 229);
    public static final Color COR_DESTAQUE2_CLARO   = new Color(124, 58, 237);
    public static final Color COR_SUCESSO_CLARO     = new Color(22, 163, 74);
    public static final Color COR_ERRO_CLARO        = new Color(220, 38, 38);
    public static final Color COR_TEXTO_CLARO       = new Color(15, 23, 42);
    public static final Color COR_TEXTO_SUAVE_CLARO = new Color(71, 85, 105);
    public static final Color COR_BORDA_CLARO       = new Color(203, 213, 225);

    // ─── Fontes (não variam com o tema) ───────────────────────────────────────
    public static final Font FONTE_TITULO    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONTE_ROTULO    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONTE_NORMAL    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONTE_MONO      = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONTE_BOTAO     = new Font("Segoe UI", Font.BOLD, 13);

    // ─── Constantes de layout ─────────────────────────────────────────────────
    public static final int HEADER_LOGO_LARGURA          = 320;
    public static final int HEADER_LOGO_ALTURA           = 74;
    public static final int MAX_ARQUIVOS_PREVIEW_EMBUTIDA = 1;

    // ─── Cores ativas (mudam ao trocar de tema) ───────────────────────────────
    public Color corFundo;
    public Color corPainel;
    public Color corPainelAlt;
    public Color corDestaque;
    public Color corDestaque2;
    public Color corSucesso;
    public Color corErro;
    public Color corTexto;
    public Color corTextoSuave;
    public Color corBorda;

    private boolean claroAtivo;

    public AppTheme(boolean claro) {
        aplicar(claro);
    }

    /** Troca o tema e atualiza todas as cores ativas. */
    public void aplicar(boolean claro) {
        this.claroAtivo = claro;
        if (claro) {
            corFundo      = COR_FUNDO_CLARO;
            corPainel     = COR_PAINEL_CLARO;
            corPainelAlt  = COR_PAINEL_ALT_CLARO;
            corDestaque   = COR_DESTAQUE_CLARO;
            corDestaque2  = COR_DESTAQUE2_CLARO;
            corSucesso    = COR_SUCESSO_CLARO;
            corErro       = COR_ERRO_CLARO;
            corTexto      = COR_TEXTO_CLARO;
            corTextoSuave = COR_TEXTO_SUAVE_CLARO;
            corBorda      = COR_BORDA_CLARO;
        } else {
            corFundo      = COR_FUNDO_ESCURO;
            corPainel     = COR_PAINEL_ESCURO;
            corPainelAlt  = COR_PAINEL_ALT_ESCURO;
            corDestaque   = COR_DESTAQUE_ESCURO;
            corDestaque2  = COR_DESTAQUE2_ESCURO;
            corSucesso    = COR_SUCESSO_ESCURO;
            corErro       = COR_ERRO_ESCURO;
            corTexto      = COR_TEXTO_ESCURO;
            corTextoSuave = COR_TEXTO_SUAVE_ESCURO;
            corBorda      = COR_BORDA_ESCURO;
        }
    }

    public boolean isClaroAtivo() {
        return claroAtivo;
    }
}

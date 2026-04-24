package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.IBMExtracaoMensagem;
import br.com.expertdev.gid.model.IBMMensagemSistema;
import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Estratégia de extração para catálogo de mensagens do sistema (MSG_SISTEMA).
 *
 * Filtro por RTC: ao invés de despejar todas as MA/MN do catálogo,
 * extrai apenas as mensagens relacionadas ao RTC detectado no documento.
 * A estratégia é em dois passos:
 *  1. Varre o histórico de revisões buscando entradas do RTC → coleta códigos citados.
 *  2. Extrai definições completas (código + texto) apenas dessas mensagens.
 * Se nenhum RTC for detectado, cai no comportamento original (extrai tudo).
 */
public class IBMMensagemExtractorStrategy extends AbstractIBMExtractorStrategy {

    // Captura qualquer código MA/MN/MH no texto
    private static final Pattern CODIGO_MSG = Pattern.compile("\\b(MA|MN|MH)\\d{2,4}\\b", Pattern.CASE_INSENSITIVE);

    // Detecta linha do histórico com RTC: "DD/MM/AAAA  XX.X  ...RTC NNNNN..."
    private static final Pattern LINHA_HISTORICO_RTC = Pattern.compile(
            "(?i)\\d{2}/\\d{2}/\\d{4}.*?rtc\\s*([0-9]{6,10})", Pattern.DOTALL);

    // Detecta linha de definição de mensagem: "MA755 – texto" ou "MN001 – texto"
    private static final Pattern LINHA_DEFINICAO_MSG = Pattern.compile(
            "^(MA|MN|MH)(\\d{2,4})\\s*[–\\-]\\s*(.+)$", Pattern.CASE_INSENSITIVE);

    @Override
    public IBMTipoArtefato getTipo() {
        return IBMTipoArtefato.MSG_SISTEMA;
    }

    @Override
    public IBMArtefatoExtracao extrair(File arquivoOrigem, String textoLimpo, int confiancaDeteccao) {
        IBMExtracaoMensagem out = new IBMExtracaoMensagem();
        preencherBase(out, arquivoOrigem, textoLimpo, confiancaDeteccao);

        String rtcDetectado = out.getRtcNumero();

        // Passo 1: Coletar catálogo completo (código → texto)
        Map<String, IBMMensagemSistema> catalogo = construirCatalogo(textoLimpo);

        // Passo 2: Determinar quais mensagens pertencem ao RTC
        Set<String> codigosDoRTC = (rtcDetectado != null && !rtcDetectado.isEmpty())
                ? codigosReferenciadosPeloRTC(textoLimpo, rtcDetectado)
                : new LinkedHashSet<String>(catalogo.keySet()); // fallback: tudo

        // Passo 3: Montar lista final filtrada
        List<IBMMensagemSistema> mensagens = new ArrayList<IBMMensagemSistema>();
        for (String codigo : codigosDoRTC) {
            IBMMensagemSistema msg = catalogo.get(codigo.toUpperCase());
            if (msg != null) {
                mensagens.add(msg);
            }
        }

        out.setMensagens(mensagens);

        if (mensagens.isEmpty()) {
            out.getAvisos().add("Nenhuma mensagem MA/MN identificada para o RTC " + rtcDetectado + ".");
        } else {
            out.getAvisos().add("MSG_SISTEMA: " + mensagens.size() + " mensagem(ns) extraída(s) para RTC " + rtcDetectado
                    + " (catálogo total: " + catalogo.size() + " mensagens).");
        }

        return out;
    }

    /**
     * Constrói um mapa código → IBMMensagemSistema varrendo as linhas de definição do catálogo.
     * Linha de definição: "MA755 – O valor do campo..."
     */
    private Map<String, IBMMensagemSistema> construirCatalogo(String textoLimpo) {
        Map<String, IBMMensagemSistema> mapa = new LinkedHashMap<String, IBMMensagemSistema>();
        List<String> linhas = IBMExtractionTextHelper.linhas(textoLimpo);
        IBMMensagemSistema atual = null;

        for (String linha : linhas) {
            Matcher m = LINHA_DEFINICAO_MSG.matcher(linha);
            if (m.matches()) {
                atual = new IBMMensagemSistema();
                String codigo = (m.group(1) + m.group(2)).toUpperCase();
                atual.setCodigo(codigo);
                String descricao = m.group(3).trim();
                atual.setDescricao(descricao);
                atual.setVariaveis(descricao.contains("<") && descricao.contains(">") ? descricao : "");
                mapa.put(codigo, atual);
            } else if (atual != null && !linha.isEmpty()
                    && !LINHA_DEFINICAO_MSG.matcher(linha).matches()
                    && !linha.matches("^\\d{2}/\\d{2}/\\d{4}.*")
                    && !linha.startsWith("4.")) {
                // Continuação multi-linha da mesma mensagem
                String descAtual = atual.getDescricao();
                if (descAtual != null && descAtual.length() < 400) {
                    atual.setDescricao(descAtual + " " + linha);
                }
            }
        }
        return mapa;
    }

    /**
     * Varre o histórico de revisões buscando entradas que mencionam o RTC informado
     * e coleta todos os códigos MA/MN/MH citados nessas entradas.
     *
     * Formato típico do histórico:
     *   "22/04/2026  29.2  Alteração...RTC 24823240...:Inclusão das mensagens: MA755 e MA756."
     */
    private Set<String> codigosReferenciadosPeloRTC(String textoLimpo, String rtcNumero) {
        Set<String> codigos = new LinkedHashSet<String>();
        // Divide por linhas do histórico (cada entrada começa com data DD/MM/AAAA)
        String[] blocos = textoLimpo.split("(?=\\d{2}/\\d{2}/\\d{4})");
        for (String bloco : blocos) {
            if (bloco.toLowerCase().contains("rtc") && bloco.contains(rtcNumero)) {
                Matcher mc = CODIGO_MSG.matcher(bloco);
                while (mc.find()) {
                    codigos.add(mc.group().toUpperCase());
                }
            }
        }
        return codigos;
    }
}


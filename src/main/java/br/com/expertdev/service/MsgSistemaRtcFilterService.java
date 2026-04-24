package br.com.expertdev.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reduz ruido de catalogos MSG_SISTEMA no prompt final.
 *
 * Quando detecta arquivo de catalogo de mensagens e recebe RTC, tenta:
 * 1) localizar codigos MA/MN/MH citados no historico do RTC;
 * 2) trazer apenas as definicoes desses codigos.
 */
public class MsgSistemaRtcFilterService {

    private static final Pattern CODIGO_MSG = Pattern.compile("\\b(?:MA|MN|MH)\\d{2,4}\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern LINHA_DEFINICAO = Pattern.compile("^\\s*((?:MA|MN|MH)\\d{2,4})\\s*[\\u2013\\-]\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LINHA_DATA = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}.*$");

    public String reduzirRuidoSeAplicavel(String origem, String textoOriginal, String rtcNumero) {
        if (textoOriginal == null || textoOriginal.trim().isEmpty()) {
            return "";
        }
        if (!ehCatalogoMsgSistema(origem, textoOriginal)) {
            return textoOriginal;
        }
        if (rtcNumero == null || rtcNumero.trim().isEmpty()) {
            return textoOriginal;
        }

        String rtcLimpo = rtcNumero.trim();
        Map<String, String> catalogo = construirCatalogo(textoOriginal);
        Set<String> codigosDoRtc = codigosReferenciadosNoHistorico(textoOriginal, rtcLimpo);

        if (codigosDoRtc.isEmpty()) {
            return montarFallbackSemCodigo(rtcLimpo, textoOriginal);
        }

        StringBuilder out = new StringBuilder();
        out.append("[MSG_SISTEMA filtrado por RTC]\n");
        out.append("RTC: ").append(rtcLimpo).append("\n");
        out.append("Codigos referenciados no historico: ").append(String.join(", ", codigosDoRtc)).append("\n\n");

        int encontrados = 0;
        for (String codigo : codigosDoRtc) {
            String descricao = catalogo.get(codigo);
            if (descricao == null) {
                out.append(codigo).append(" - [definicao nao localizada no catalogo]\n");
                continue;
            }
            out.append(codigo).append(" - ").append(descricao).append("\n");
            encontrados++;
        }

        out.append("\nResumo filtro: ").append(encontrados)
                .append(" definicao(oes) localizada(s) de ")
                .append(codigosDoRtc.size())
                .append(" codigo(s) do RTC.");

        return out.toString();
    }

    private boolean ehCatalogoMsgSistema(String origem, String textoOriginal) {
        String origemLc = origem == null ? "" : origem.toLowerCase();
        String textoLc = textoOriginal.toLowerCase();

        if (origemLc.contains("msg_sistema") || origemLc.contains("msg sistema")) {
            return true;
        }

        return textoLc.contains("mensagens do sistema")
                && textoLc.contains("4.1")
                && (textoLc.contains("ma001") || textoLc.contains("mn001"));
    }

    private Set<String> codigosReferenciadosNoHistorico(String texto, String rtcNumero) {
        Set<String> codigos = new LinkedHashSet<String>();
        String[] blocos = texto.split("(?=\\d{2}/\\d{2}/\\d{4})");
        for (String bloco : blocos) {
            String blocoLc = bloco.toLowerCase();
            if (!blocoLc.contains("rtc") || !bloco.contains(rtcNumero)) {
                continue;
            }
            Matcher m = CODIGO_MSG.matcher(bloco);
            while (m.find()) {
                codigos.add(m.group().toUpperCase());
            }
        }
        return codigos;
    }

    private Map<String, String> construirCatalogo(String texto) {
        Map<String, String> mapa = new LinkedHashMap<String, String>();
        String[] linhas = texto.split("\\r?\\n");
        String codigoAtual = null;
        StringBuilder descricaoAtual = null;

        for (String linhaBruta : linhas) {
            String linha = linhaBruta == null ? "" : linhaBruta.trim();
            Matcher def = LINHA_DEFINICAO.matcher(linha);
            if (def.matches()) {
                if (codigoAtual != null && descricaoAtual != null) {
                    mapa.put(codigoAtual, descricaoAtual.toString().trim());
                }
                codigoAtual = def.group(1).toUpperCase();
                descricaoAtual = new StringBuilder(def.group(2).trim());
                continue;
            }

            if (codigoAtual == null || descricaoAtual == null || linha.isEmpty()) {
                continue;
            }
            if (linha.startsWith("4.") || LINHA_DATA.matcher(linha).matches()) {
                mapa.put(codigoAtual, descricaoAtual.toString().trim());
                codigoAtual = null;
                descricaoAtual = null;
                continue;
            }

            // Continua descricao da mensagem em linha quebrada.
            if (descricaoAtual.length() < 2000) {
                descricaoAtual.append(' ').append(linha);
            }
        }

        if (codigoAtual != null && descricaoAtual != null) {
            mapa.put(codigoAtual, descricaoAtual.toString().trim());
        }

        return mapa;
    }

    private String montarFallbackSemCodigo(String rtcNumero, String textoOriginal) {
        int limite = Math.min(5000, textoOriginal.length());
        String trecho = textoOriginal.substring(0, limite);

        StringBuilder out = new StringBuilder();
        out.append("[MSG_SISTEMA detectado]\n");
        out.append("RTC: ").append(rtcNumero).append("\n");
        out.append("Nenhum codigo MA/MN/MH foi encontrado no historico para esse RTC.\n");
        out.append("Trecho inicial (fallback compacto):\n\n");
        out.append(trecho);
        if (limite < textoOriginal.length()) {
            out.append("\n\n[... conteudo truncado para reduzir ruido ...]");
        }
        return out.toString();
    }
}


package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.IBMRuidoFilterResult;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Remove ruido comum de documentos IBM exportados em Word legado.
 */
public class IBMRuidoFilter {

    private static final Pattern[] PADROES_DESCARTE = new Pattern[] {
            Pattern.compile("(?i).*_toc\\d+.*"),
            Pattern.compile("(?i).*(msodatastore|_pid_hlinks).*"),
            Pattern.compile("(?i).*(taxkeyword|compliancepolicy|sensitivity|dlpmanual).*"),
            Pattern.compile("(?i).*msip_label_.*"),
            Pattern.compile("(?i).*(summaryinformation|documentsummaryinformation).*"),
            Pattern.compile("(?i).*(cabecalho|rodape|footer-odd|numero de pagina).*"),
            Pattern.compile("(?i).*(historico de revisao|confidencial).*"),
            Pattern.compile("(?i).*(oj qj|\\bcj\\b|\\bmh\\b|\\bsh\\b|\\bth\\b|\\baj\\b).*"),
            Pattern.compile("(?i).*bjbjh!h!.*")
    };

    private static final String[] MARCADORES = new String[] {
            "_toc", "msodatastore", "_pid_hlinks", "taxkeyword", "compliancepolicy",
            "msip_label_", "dlpmanual", "summaryinformation", "documentsummaryinformation",
            "cabecalho", "rodape", "footer-odd", "historico de revisao", "confidencial",
            "oj qj", "bjbjh!h!"
    };

    public IBMRuidoFilterResult filtrar(String textoBruto) {
        IBMRuidoFilterResult result = new IBMRuidoFilterResult();
        if (textoBruto == null || textoBruto.trim().isEmpty()) {
            result.setTextoLimpo("");
            result.setLinhasDescartadas(0);
            return result;
        }

        String[] linhas = textoBruto.replace("\r", "").split("\n");
        StringBuilder limpo = new StringBuilder();
        int descartadas = 0;
        Set<String> marcadoresEncontrados = new LinkedHashSet<String>();

        for (String original : linhas) {
            String linha = original == null ? "" : original.trim();
            if (linha.isEmpty()) {
                continue;
            }

            String linhaLower = linha.toLowerCase();
            capturarMarcadores(linhaLower, marcadoresEncontrados);

            if (deveDescartar(linha)) {
                descartadas++;
                continue;
            }

            if (limpo.length() > 0) {
                limpo.append("\n");
            }
            limpo.append(linha);
        }

        result.setTextoLimpo(limpo.toString());
        result.setLinhasDescartadas(descartadas);
        result.setMarcadoresRuido(new ArrayList<String>(marcadoresEncontrados));
        return result;
    }

    private boolean deveDescartar(String linha) {
        for (Pattern pattern : PADROES_DESCARTE) {
            if (pattern.matcher(linha).matches()) {
                return true;
            }
        }
        return false;
    }

    private void capturarMarcadores(String linhaLower, Set<String> marcadores) {
        for (String marcador : MARCADORES) {
            if (linhaLower.contains(marcador)) {
                marcadores.add(marcador);
            }
        }
    }
}


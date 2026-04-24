package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Classifica o tipo do artefato IBM usando sinais de nome de arquivo e conteudo.
 */
public class IBMLayoutDetector {

    public static class DetectionResult {
        private final IBMTipoArtefato tipo;
        private final int confianca;
        private final List<String> sinais;

        public DetectionResult(IBMTipoArtefato tipo, int confianca, List<String> sinais) {
            this.tipo = tipo;
            this.confianca = confianca;
            this.sinais = sinais;
        }

        public IBMTipoArtefato getTipo() {
            return tipo;
        }

        public int getConfianca() {
            return confianca;
        }

        public List<String> getSinais() {
            return sinais;
        }
    }

    public DetectionResult detectar(String nomeArquivo, String textoLimpo) {
        String nome = nomeArquivo == null ? "" : nomeArquivo.toLowerCase(Locale.ROOT);
        String texto = textoLimpo == null ? "" : textoLimpo.toLowerCase(Locale.ROOT);

        int scoreMsg = 0;
        int scoreUc = 0;
        int scoreDi = 0;
        int scoreApi = 0;
        int scoreCanal = 0;
        int scoreSup = 0;
        List<String> sinais = new ArrayList<String>();

        if (nome.contains("msg_sistema")) {
            scoreMsg += 70;
            sinais.add("nome: msg_sistema");
        }
        if (nome.contains("integracao_uc") || nome.contains("_uc_")) {
            scoreUc += 55;
            sinais.add("nome: integracao_uc");
        }
        if (nome.contains("integracao_di") || nome.contains("_di_")) {
            scoreDi += 55;
            sinais.add("nome: integracao_di");
        }
        if (nome.contains("api_quarkus") || nome.contains("catalogo_servico")) {
            scoreApi += 70;
            sinais.add("nome: api_quarkus/catalogo_servico");
        }
        if (nome.contains("canais_uc")) {
            scoreCanal += 70;
            sinais.add("nome: canais_uc");
        }
        if (nome.contains("especificacao_suplementar")) {
            scoreSup += 70;
            sinais.add("nome: especificacao_suplementar");
        }

        scoreMsg += pontuar(texto, sinais, "mensagens", 8, "conteudo: secao mensagens");
        scoreMsg += pontuarRegexMaMn(texto, sinais);

        scoreUc += pontuar(texto, sinais, "regra de negocio", 10, "conteudo: regra de negocio");
        scoreUc += pontuar(texto, sinais, "fluxo basico", 8, "conteudo: fluxo basico");
        scoreUc += pontuar(texto, sinais, "fluxo alternativo", 6, "conteudo: fluxo alternativo");

        scoreDi += pontuar(texto, sinais, "pre-cond", 7, "conteudo: pre-condicao");
        scoreDi += pontuar(texto, sinais, "pos-cond", 7, "conteudo: pos-condicao");
        scoreDi += pontuar(texto, sinais, "parametro", 6, "conteudo: parametros");

        scoreApi += pontuar(texto, sinais, "endpoint", 10, "conteudo: endpoint");
        scoreApi += pontuar(texto, sinais, "request", 6, "conteudo: request");
        scoreApi += pontuar(texto, sinais, "response", 6, "conteudo: response");
        scoreApi += pontuar(texto, sinais, "http", 5, "conteudo: http");

        scoreCanal += pontuar(texto, sinais, "whatsapp", 8, "conteudo: canal whatsapp");
        scoreCanal += pontuar(texto, sinais, "canal", 5, "conteudo: canal");

        scoreSup += pontuar(texto, sinais, "requisitos nao-funcionais", 8, "conteudo: rnf");
        scoreSup += pontuar(texto, sinais, "glossario", 5, "conteudo: glossario");
        scoreSup += pontuar(texto, sinais, "sigms", 8, "conteudo: sistema externo sigms");

        int max = scoreMsg;
        IBMTipoArtefato tipo = IBMTipoArtefato.MSG_SISTEMA;

        if (scoreUc > max) {
            max = scoreUc;
            tipo = IBMTipoArtefato.INTEGRACAO_UC;
        }
        if (scoreDi > max) {
            max = scoreDi;
            tipo = IBMTipoArtefato.INTEGRACAO_DI;
        }
        if (scoreApi > max) {
            max = scoreApi;
            tipo = IBMTipoArtefato.API_QUARKUS;
        }
        if (scoreCanal > max) {
            max = scoreCanal;
            tipo = IBMTipoArtefato.CANAIS_UC;
        }
        if (scoreSup > max) {
            max = scoreSup;
            tipo = IBMTipoArtefato.ESPECIFICACAO_SUPLEMENTAR;
        }

        if (max <= 0) {
            return new DetectionResult(IBMTipoArtefato.DESCONHECIDO, 0, sinais);
        }
        return new DetectionResult(tipo, Math.min(100, max), sinais);
    }

    private int pontuar(String texto, List<String> sinais, String token, int pontos, String sinal) {
        if (texto.contains(token)) {
            sinais.add(sinal);
            return pontos;
        }
        return 0;
    }

    private int pontuarRegexMaMn(String texto, List<String> sinais) {
        int indexMa = texto.indexOf("ma");
        int indexMn = texto.indexOf("mn");
        if (indexMa >= 0 || indexMn >= 0) {
            sinais.add("conteudo: codigos MA/MN");
            return 8;
        }
        return 0;
    }
}


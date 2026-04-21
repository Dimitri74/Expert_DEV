package br.com.expertdev.service;

public class AiContextReducer {

    public ReducedContext reduzir(String regras, String imagens, int maxChars) {
        int limite = Math.max(2000, maxChars);
        String regrasSafe = normalizar(regras, "[Sem regras extraidas]");
        String imagensSafe = normalizar(imagens, "[Sem imagens detectadas]");

        int limiteRegras = (int) (limite * 0.80);
        int limiteImagens = limite - limiteRegras;

        String regrasReduzidas = truncar(regrasSafe, limiteRegras);
        String imagensReduzidas = truncar(imagensSafe, limiteImagens);

        boolean truncou = !regrasSafe.equals(regrasReduzidas) || !imagensSafe.equals(imagensReduzidas);
        String observacao = truncou
                ? "[Modo economico: contexto reduzido automaticamente para economizar tokens.]"
                : "[Modo economico: contexto dentro do limite configurado.]";

        return new ReducedContext(regrasReduzidas, imagensReduzidas, observacao, truncou);
    }

    private String normalizar(String valor, String fallback) {
        if (valor == null) {
            return fallback;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? fallback : texto;
    }

    private String truncar(String texto, int limite) {
        if (texto == null || texto.length() <= limite) {
            return texto;
        }
        int corte = Math.max(0, limite - 80);
        String prefixo = texto.substring(0, corte);
        return prefixo + "\n\n[... contexto truncado no modo economico ...]";
    }

    public static class ReducedContext {
        private final String regras;
        private final String imagens;
        private final String observacao;
        private final boolean truncou;

        public ReducedContext(String regras, String imagens, String observacao, boolean truncou) {
            this.regras = regras;
            this.imagens = imagens;
            this.observacao = observacao;
            this.truncou = truncou;
        }

        public String getRegras() {
            return regras;
        }

        public String getImagens() {
            return imagens;
        }

        public String getObservacao() {
            return observacao;
        }

        public boolean isTruncou() {
            return truncou;
        }
    }
}


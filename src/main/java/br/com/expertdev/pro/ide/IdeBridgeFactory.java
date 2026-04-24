package br.com.expertdev.pro.ide;

/**
 * Factory para criar instâncias corretas de IdeBridge baseado no SO/IDE detectada.
 */
public class IdeBridgeFactory {

    public static IdeBridge criarBridge() {
        String os = System.getProperty("os.name").toLowerCase();

        // Prioridade: tentar JetBrains em Windows
        if (os.contains("win")) {
            JetBrainsBridge jetbrains = new JetBrainsBridge();
            if (jetbrains.isIdeInstalada()) {
                return jetbrains;
            }
        }

        // Fallback: retorna bridge nulo (não faz nada)
        return new NullIdeBridge();
    }

    /**
     * Bridge que faz nada (quando nenhuma IDE é detectada).
     */
    private static class NullIdeBridge implements IdeBridge {
        @Override
        public void abrirNaIde(String caminhoArquivo, int numeroLinha) throws Exception {
            System.out.println("[Pro] Nenhuma IDE detectada. Abrir manual: " + caminhoArquivo + ":" + numeroLinha);
        }

        @Override
        public boolean isIdeInstalada() {
            return false;
        }

        @Override
        public String getNomeIde() {
            return "Nenhuma IDE detectada";
        }
    }
}


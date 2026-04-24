package br.com.expertdev.pro.ide;

/**
 * Interface para integração com IDEs (JetBrains, VS Code, etc).
 * Responsável por abrir arquivo/contexto na IDE configurada.
 */
public interface IdeBridge {

    /**
     * Abre um arquivo na IDE no caminho e linha especificados.
     * @param caminhoArquivo caminho relativo ou absoluto do arquivo
     * @param numeroLinha número da linha (1-based)
     * @throws Exception se falhar ao abrir
     */
    void abrirNaIde(String caminhoArquivo, int numeroLinha) throws Exception;

    /**
     * Verifica se a IDE está instalada e disponível.
     * @return true se IDE detectada
     */
    boolean isIdeInstalada();

    /**
     * Retorna o nome da IDE.
     * @return nome (ex: "JetBrains", "VS Code")
     */
    String getNomeIde();
}


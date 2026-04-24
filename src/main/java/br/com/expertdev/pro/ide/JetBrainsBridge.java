package br.com.expertdev.pro.ide;

import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementação de IdeBridge para JetBrains IDEs (IntelliJ, PyCharm, etc).
 */
public class JetBrainsBridge implements IdeBridge {

    private static final String IDEAUI_COMMAND = "idea";
    private static final String[] IDEA_COMMANDS_WIN = {
            "idea64.exe", "idea.exe", "idea64", "idea", "idea.bat", "idea.cmd"
    };

    private String comandoIdeResolvido;

    @Override
    public void abrirNaIde(String caminhoArquivo, int numeroLinha) throws Exception {
        String comando = resolverComandoIde();
        if (comando == null) {
            throw new RuntimeException(
                    "IntelliJ IDEA nao foi encontrada. Configure o launcher 'idea' no PATH " +
                    "ou verifique a instalacao do IntelliJ/Toolbox."
            );
        }

        ProcessBuilder pb = criarProcessBuilderAbertura(comando, Math.max(1, numeroLinha), normalizarCaminho(caminhoArquivo));

        pb.start();
    }

    @Override
    public boolean isIdeInstalada() {
        return resolverComandoIde() != null;
    }

    @Override
    public String getNomeIde() {
        return "JetBrains IntelliJ IDEA";
    }

    private String normalizarCaminho(String caminho) {
        File f = new File(caminho);
        if (!f.isAbsolute()) {
            f = new File(System.getProperty("user.dir"), caminho);
        }
        return f.getAbsolutePath();
    }

    private synchronized String resolverComandoIde() {
        if (comandoIdeResolvido != null) {
            return comandoIdeResolvido;
        }

        String sistema = System.getProperty("os.name", "").toLowerCase();
        if (!sistema.contains("win")) {
            comandoIdeResolvido = IDEAUI_COMMAND;
            return comandoIdeResolvido;
        }

        for (String candidato : IDEA_COMMANDS_WIN) {
            String encontrado = localizarNoPath(candidato);
            if (encontrado != null) {
                comandoIdeResolvido = encontrado;
                return comandoIdeResolvido;
            }
        }

        for (String caminho : listarPossiveisExecutaveisWindows()) {
            if (new File(caminho).isFile()) {
                comandoIdeResolvido = caminho;
                return comandoIdeResolvido;
            }
        }

        return null;
    }

    private ProcessBuilder criarProcessBuilderAbertura(String comando, int numeroLinha, String arquivo) {
        String lower = comando.toLowerCase();
        if (lower.endsWith(".bat") || lower.endsWith(".cmd")) {
            String linha = "\"" + comando + "\" --line " + numeroLinha + " \"" + arquivo + "\"";
            return new ProcessBuilder("cmd.exe", "/c", linha);
        }
        return new ProcessBuilder(comando, "--line", String.valueOf(numeroLinha), arquivo);
    }

    private String localizarNoPath(String comando) {
        try {
            ProcessBuilder pb = new ProcessBuilder("where", comando);
            Process p = pb.start();
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                return null;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String linha;
                while ((linha = br.readLine()) != null) {
                    String candidato = linha.trim();
                    if (!candidato.isEmpty()) {
                        return candidato;
                    }
                }
            }
        } catch (Exception ignored) {
            // fallback por paths locais
        }
        return null;
    }

    private List<String> listarPossiveisExecutaveisWindows() {
        List<String> candidatos = new ArrayList<>();

        String ideaHome = System.getenv("IDEA_HOME");
        adicionarExecutaveisDeBin(candidatos, ideaHome);

        String programFiles = System.getenv("ProgramFiles");
        adicionarExecutaveisDeDiretorioJetBrains(candidatos, programFiles);

        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        adicionarExecutaveisDeDiretorioJetBrains(candidatos, programFilesX86);

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.trim().isEmpty()) {
            adicionarExecutaveisDeDiretorioJetBrains(candidatos, localAppData + "\\Programs");
            adicionarExecutaveisToolbox(candidatos, localAppData + "\\JetBrains\\Toolbox\\apps");
        }

        return candidatos;
    }

    private void adicionarExecutaveisDeDiretorioJetBrains(List<String> destino, String base) {
        if (base == null || base.trim().isEmpty()) {
            return;
        }

        File jetbrainsRoot = new File(base, "JetBrains");
        if (!jetbrainsRoot.isDirectory()) {
            return;
        }

        File[] pastas = jetbrainsRoot.listFiles(File::isDirectory);
        if (pastas == null) {
            return;
        }

        for (File pasta : pastas) {
            String nome = pasta.getName().toLowerCase();
            if (nome.contains("intellij") || nome.contains("idea")) {
                adicionarExecutaveisDeBin(destino, pasta.getAbsolutePath());
            }
        }
    }

    private void adicionarExecutaveisToolbox(List<String> destino, String toolboxAppsPath) {
        File toolboxApps = new File(toolboxAppsPath);
        if (!toolboxApps.isDirectory()) {
            return;
        }

        File[] canais = toolboxApps.listFiles(File::isDirectory);
        if (canais == null) {
            return;
        }

        for (File canal : canais) {
            if (!canal.getName().toLowerCase().contains("idea")) {
                continue;
            }

            File[] variacoes = canal.listFiles(File::isDirectory);
            if (variacoes == null) {
                continue;
            }

            for (File variacao : variacoes) {
                File[] versoes = variacao.listFiles(File::isDirectory);
                if (versoes == null) {
                    continue;
                }
                for (File versao : versoes) {
                    adicionarExecutaveisDeBin(destino, versao.getAbsolutePath());
                }
            }
        }
    }

    private void adicionarExecutaveisDeBin(List<String> destino, String raizIde) {
        if (raizIde == null || raizIde.trim().isEmpty()) {
            return;
        }

        File bin = new File(raizIde, "bin");
        List<String> nomes = Arrays.asList("idea64.exe", "idea.exe", "idea64", "idea", "idea.bat", "idea.cmd");
        for (String nome : nomes) {
            File exe = new File(bin, nome);
            if (exe.isFile()) {
                destino.add(exe.getAbsolutePath());
            }
        }
    }
}


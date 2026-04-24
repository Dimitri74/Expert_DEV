package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.*;
import java.io.File;
import java.util.*;

/**
 * Probe de validação para Etapa G2 (Consolidação).
 * Executa pipeline completo B3->B4->G2 e exibe resultados estruturados.
 *
 * Uso: mvn exec:java -Dexec.mainClass=br.com.expertdev.gid.service.IBMG2Probe -Dexec.args="arquivo1.doc arquivo2.doc"
 */
public class IBMG2Probe {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("ERRO: Forneça caminho(s) de arquivo(s) Word.");
            System.out.println("Uso: IBMG2Probe <arquivo1> [arquivo2] [arquivo3]...");
            System.exit(1);
        }

        List<File> arquivos = new ArrayList<>();
        for (String arg : args) {
            File file = new File(arg);
            if (!file.exists()) {
                System.out.println("ERRO: Arquivo não encontrado: " + arg);
                continue;
            }
            arquivos.add(file);
        }

        if (arquivos.isEmpty()) {
            System.out.println("ERRO: Nenhum arquivo válido fornecido.");
            System.exit(1);
        }

        // Executar orquestração completa
        IBMG2OrchestrationService orquestracao = new IBMG2OrchestrationService();
        Map<String, IBMContextoRTC> consolidacao = orquestracao.processarCompleto(arquivos);

        // Exibir resultados
        exibirResultados(orquestracao, consolidacao);
    }

    private static void exibirResultados(IBMG2OrchestrationService orquestracao,
                                         Map<String, IBMContextoRTC> consolidacao) {
        String separator = new String(new char[80]).replace('\0', '=');
        System.out.println("\n" + separator);
        System.out.println("RESULTADO DA ETAPA G2 - CONSOLIDAÇÃO");
        System.out.println(separator);

        // Estatísticas gerais
        Map<String, Object> stats = orquestracao.obterEstatisticas();
        System.out.println("\n📊 ESTATÍSTICAS GERAIS:");
        System.out.println("  RTCs Consolidados: " + stats.get("rtcsConsolidados"));
        System.out.println("  Artefatos Extraídos: " + stats.get("artefatosExtraidos"));
        System.out.println("  Total Relações: " + stats.getOrDefault("totalRelacoes", 0));
        System.out.println("  Avisos: " + stats.get("totalAvisos"));

        // RTCs e seus contextos
        System.out.println("\n📋 RTCS CONSOLIDADOS:");
        if (consolidacao.isEmpty()) {
            System.out.println("  (nenhum RTC consolidado)");
        } else {
            for (String rtc : consolidacao.keySet()) {
                IBMContextoRTC contexto = consolidacao.get(rtc);
                IBMConsolidacaoDependencias deps = orquestracao.obterDependenciasRTC(rtc);

                System.out.println("\n  ▶ RTC: " + rtc);
                if (contexto.getUcCodigo() != null && !contexto.getUcCodigo().isEmpty()) {
                    System.out.println("    └─ UC: " + contexto.getUcCodigo());
                }

                int qtdArtefatos = contexto.getArtefatosComplementares() != null ?
                                  contexto.getArtefatosComplementares().size() : 0;
                System.out.println("    └─ Artefatos: " + qtdArtefatos);

                if (deps != null) {
                    System.out.println("    └─ Relações: " + deps.getTotalRelacoes());

                    // Listar relações
                    if (deps.getTotalRelacoes() > 0) {
                        for (IBMRelacaoDependencia rel : deps.getRelacoes()) {
                            System.out.println("       • " + rel.toString());
                        }
                    }
                }
            }
        }

        // Avisos
        List<String> avisos = orquestracao.obterAvisos();
        if (!avisos.isEmpty()) {
            System.out.println("\n⚠️  AVISOS:");
            for (String aviso : avisos) {
                System.out.println("  • " + aviso);
            }
        }

        // Artefatos extraídos
        List<IBMArtefatoExtracao> artefatos = orquestracao.obterUltimosArtefatos();
        System.out.println("\n📄 ARTEFATOS EXTRAÍDOS (" + artefatos.size() + "):");
        for (int i = 0; i < Math.min(5, artefatos.size()); i++) {
            IBMArtefatoExtracao artefato = artefatos.get(i);
            System.out.println("  " + (i + 1) + ". Tipo: " + artefato.getTipoArtefato() +
                    " | RTC: " + artefato.getRtcNumero() + " | Confiança: " +
                    artefato.getConfiancaDeteccao() + "%");
        }
        if (artefatos.size() > 5) {
            System.out.println("  ... e mais " + (artefatos.size() - 5) + " artefatos");
        }

        System.out.println("\n" + separator);
        System.out.println("FIM DA EXECUÇÃO - G2 Probe");
        System.out.println(separator + "\n");
    }
}


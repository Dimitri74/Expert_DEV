package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.IBMClassificacaoDocumento;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Runner simples para validar B3 (classificacao + filtro de ruido).
 */
public class IBMB3Probe {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Uso: java br.com.expertdev.gid.service.IBMB3Probe <arquivo1.doc|docx> [arquivo2 ...]");
            System.exit(2);
            return;
        }

        List<File> arquivos = new ArrayList<File>();
        for (String a : args) {
            arquivos.add(new File(a));
        }

        IBMB3ClassificationService service = new IBMB3ClassificationService();
        List<IBMClassificacaoDocumento> docs = service.classificarArquivos(arquivos);

        for (IBMClassificacaoDocumento d : docs) {
            System.out.println("---");
            System.out.println("Arquivo: " + d.getNomeArquivo());
            System.out.println("Tipo: " + d.getTipoDetectado());
            System.out.println("Confianca: " + d.getConfianca());
            System.out.println("Chars bruto: " + d.getTextoBrutoCaracteres());
            System.out.println("Chars limpo: " + d.getTextoLimpoCaracteres());
            System.out.println("Linhas ruido descartadas: " + d.getLinhasRuidoDescartadas());
            System.out.println("Sinais: " + String.join(" | ", d.getSinaisDeteccao()));
            System.out.println("Leitura: " + (d.getObservacaoLeitura() == null ? "" : d.getObservacaoLeitura()));
        }

        System.exit(0);
    }
}


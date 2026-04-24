package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.IBMClassificacaoDocumento;
import br.com.expertdev.gid.model.IBMRuidoFilterResult;
import br.com.expertdev.model.ResultadoProcessamento;
import br.com.expertdev.service.WordDocumentReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Etapa B3: classifica layout IBM e remove ruido do conteudo bruto.
 */
public class IBMB3ClassificationService {

    private final WordDocumentReader wordReader;
    private final IBMRuidoFilter ruidoFilter;
    private final IBMLayoutDetector layoutDetector;

    public IBMB3ClassificationService() {
        this(new WordDocumentReader(), new IBMRuidoFilter(), new IBMLayoutDetector());
    }

    IBMB3ClassificationService(WordDocumentReader wordReader,
                               IBMRuidoFilter ruidoFilter,
                               IBMLayoutDetector layoutDetector) {
        this.wordReader = wordReader;
        this.ruidoFilter = ruidoFilter;
        this.layoutDetector = layoutDetector;
    }

    public List<IBMClassificacaoDocumento> classificarArquivos(List<File> arquivos) {
        if (arquivos == null || arquivos.isEmpty()) {
            return Collections.emptyList();
        }

        List<IBMClassificacaoDocumento> out = new ArrayList<IBMClassificacaoDocumento>();
        for (File arquivo : arquivos) {
            out.add(classificarArquivo(arquivo));
        }
        return out;
    }

    public IBMClassificacaoDocumento classificarArquivo(File arquivo) {
        IBMClassificacaoDocumento doc = new IBMClassificacaoDocumento();
        doc.setNomeArquivo(arquivo == null ? "" : arquivo.getName());

        if (arquivo == null || !arquivo.exists()) {
            doc.setSinaisDeteccao(Collections.singletonList("arquivo inexistente"));
            return doc;
        }

        ResultadoProcessamento leitura = wordReader.ler(arquivo);
        doc.setObservacaoLeitura(leitura.getObservacao());

        if (!leitura.isSucesso()) {
            List<String> sinais = new ArrayList<String>();
            sinais.add("falha leitura: " + leitura.getErro());
            doc.setSinaisDeteccao(sinais);
            return doc;
        }

        String bruto = leitura.getTextoExtraido() == null ? "" : leitura.getTextoExtraido();
        doc.setTextoBrutoCaracteres(bruto.length());

        IBMRuidoFilterResult filtro = ruidoFilter.filtrar(bruto);
        String limpo = filtro.getTextoLimpo() == null ? "" : filtro.getTextoLimpo();
        doc.setTextoLimpoCaracteres(limpo.length());
        doc.setLinhasRuidoDescartadas(filtro.getLinhasDescartadas());

        IBMLayoutDetector.DetectionResult deteccao = layoutDetector.detectar(arquivo.getName(), limpo);
        doc.setTipoDetectado(deteccao.getTipo());
        doc.setConfianca(deteccao.getConfianca());

        List<String> sinais = new ArrayList<String>();
        sinais.addAll(deteccao.getSinais());
        if (filtro.getMarcadoresRuido() != null && !filtro.getMarcadoresRuido().isEmpty()) {
            sinais.add("ruido identificado: " + String.join(",", filtro.getMarcadoresRuido()));
        }
        doc.setSinaisDeteccao(sinais);

        int limitePreview = Math.min(350, limpo.length());
        doc.setPreviewTextoLimpo(limpo.substring(0, limitePreview));
        return doc;
    }
}


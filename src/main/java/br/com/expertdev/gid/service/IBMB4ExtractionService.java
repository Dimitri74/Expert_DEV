package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.IBMClassificacaoDocumento;
import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;
import br.com.expertdev.gid.service.extractor.IBMExtractorFactory;
import br.com.expertdev.gid.service.extractor.IBMExtractorStrategy;
import br.com.expertdev.model.ResultadoProcessamento;
import br.com.expertdev.service.WordDocumentReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Etapa B4: extracao semantica por tipo classificado no B3.
 */
public class IBMB4ExtractionService {

    private final IBMB3ClassificationService b3ClassificationService;
    private final WordDocumentReader wordReader;
    private final IBMRuidoFilter ruidoFilter;
    private final IBMExtractorFactory extractorFactory;

    public IBMB4ExtractionService() {
        this(new IBMB3ClassificationService(), new WordDocumentReader(), new IBMRuidoFilter(), new IBMExtractorFactory());
    }

    IBMB4ExtractionService(IBMB3ClassificationService b3ClassificationService,
                           WordDocumentReader wordReader,
                           IBMRuidoFilter ruidoFilter,
                           IBMExtractorFactory extractorFactory) {
        this.b3ClassificationService = b3ClassificationService;
        this.wordReader = wordReader;
        this.ruidoFilter = ruidoFilter;
        this.extractorFactory = extractorFactory;
    }

    public List<IBMArtefatoExtracao> extrairArquivos(List<File> arquivos) {
        if (arquivos == null || arquivos.isEmpty()) {
            return Collections.emptyList();
        }

        List<IBMArtefatoExtracao> out = new ArrayList<IBMArtefatoExtracao>();
        List<IBMClassificacaoDocumento> classes = b3ClassificationService.classificarArquivos(arquivos);
        for (int i = 0; i < arquivos.size(); i++) {
            File arquivo = arquivos.get(i);
            IBMClassificacaoDocumento c = i < classes.size() ? classes.get(i) : null;
            out.add(extrairArquivo(arquivo, c));
        }
        return out;
    }

    public IBMArtefatoExtracao extrairArquivo(File arquivo, IBMClassificacaoDocumento classificacao) {
        if (arquivo == null || !arquivo.exists()) {
            IBMArtefatoExtracao vazio = new IBMArtefatoExtracao();
            vazio.getAvisos().add("Arquivo inexistente para extracao B4.");
            return vazio;
        }

        ResultadoProcessamento leitura = wordReader.ler(arquivo);
        if (!leitura.isSucesso()) {
            IBMArtefatoExtracao falha = new IBMArtefatoExtracao();
            falha.setNomeArquivoOrigem(arquivo.getName());
            falha.getAvisos().add("Falha na leitura Word: " + leitura.getErro());
            return falha;
        }

        String textoBruto = leitura.getTextoExtraido() == null ? "" : leitura.getTextoExtraido();
        String textoLimpo = ruidoFilter.filtrar(textoBruto).getTextoLimpo();

        IBMTipoArtefato tipo = classificacao == null ? IBMTipoArtefato.DESCONHECIDO : classificacao.getTipoDetectado();
        int confianca = classificacao == null ? 0 : classificacao.getConfianca();

        IBMExtractorStrategy strategy = extractorFactory.get(tipo);
        if (strategy == null) {
            IBMArtefatoExtracao fallback = new IBMArtefatoExtracao();
            fallback.setTipoArtefato(tipo);
            fallback.setNomeArquivoOrigem(arquivo.getName());
            fallback.setConfiancaDeteccao(confianca);
            fallback.getAvisos().add("Sem estrategia B4 para tipo " + tipo + ".");
            return fallback;
        }

        IBMArtefatoExtracao artefato = strategy.extrair(arquivo, textoLimpo, confianca);
        if (leitura.getObservacao() != null && !leitura.getObservacao().trim().isEmpty()) {
            artefato.getAvisos().add("Leitura Word: " + leitura.getObservacao());
        }
        return artefato;
    }
}


package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.IBMClassificacaoDocumento;
import br.com.expertdev.gid.model.IBMContextoRTC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Servico base da Etapa B1 para consolidar artefatos por RTC.
 * A extracao semantica e os parsers entram nas etapas seguintes.
 */
public class IBMGIDExtractionService {

    private final IBMB3ClassificationService b3ClassificationService = new IBMB3ClassificationService();
    private final IBMB4ExtractionService b4ExtractionService = new IBMB4ExtractionService();

    public IBMContextoRTC iniciarContexto(String rtcNumero, String ucCodigo) {
        IBMContextoRTC contexto = new IBMContextoRTC();
        contexto.setRtcNumero(rtcNumero);
        contexto.setUcCodigo(ucCodigo);
        return contexto;
    }

    public void anexarArtefatoComplementar(IBMContextoRTC contexto, IBMArtefatoExtracao artefato) {
        if (contexto == null || artefato == null) {
            return;
        }
        List<IBMArtefatoExtracao> complementares = contexto.getArtefatosComplementares();
        if (complementares == null) {
            complementares = new ArrayList<IBMArtefatoExtracao>();
            contexto.setArtefatosComplementares(complementares);
        }
        complementares.add(artefato);
    }

    public List<IBMClassificacaoDocumento> classificarDocumentos(List<File> arquivos) {
        return b3ClassificationService.classificarArquivos(arquivos);
    }

    public List<IBMArtefatoExtracao> extrairSemanticaDocumentos(List<File> arquivos) {
        return b4ExtractionService.extrairArquivos(arquivos);
    }
}


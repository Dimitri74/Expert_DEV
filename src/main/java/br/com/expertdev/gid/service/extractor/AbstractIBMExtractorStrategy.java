package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;

import java.io.File;

abstract class AbstractIBMExtractorStrategy implements IBMExtractorStrategy {

    protected void preencherBase(IBMArtefatoExtracao artefato, File arquivoOrigem, String textoLimpo, int confiancaDeteccao) {
        if (arquivoOrigem != null) {
            artefato.setNomeArquivoOrigem(arquivoOrigem.getName());
        }
        artefato.setConfiancaDeteccao(confiancaDeteccao);
        artefato.setRtcNumero(IBMExtractionTextHelper.extrairRtc(textoLimpo));
        artefato.setUcCodigo(IBMExtractionTextHelper.extrairUcCodigo(textoLimpo));
    }
}


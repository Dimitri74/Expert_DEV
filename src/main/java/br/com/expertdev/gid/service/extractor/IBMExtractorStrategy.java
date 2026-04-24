package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.io.File;

public interface IBMExtractorStrategy {

    IBMTipoArtefato getTipo();

    IBMArtefatoExtracao extrair(File arquivoOrigem, String textoLimpo, int confiancaDeteccao);
}


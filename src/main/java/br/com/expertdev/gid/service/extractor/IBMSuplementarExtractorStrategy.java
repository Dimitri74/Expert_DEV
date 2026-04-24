package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.IBMExtracaoSuplementar;
import br.com.expertdev.gid.model.IBMURLIntegracao;
import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IBMSuplementarExtractorStrategy extends AbstractIBMExtractorStrategy {

    @Override
    public IBMTipoArtefato getTipo() {
        return IBMTipoArtefato.ESPECIFICACAO_SUPLEMENTAR;
    }

    @Override
    public IBMArtefatoExtracao extrair(File arquivoOrigem, String textoLimpo, int confiancaDeteccao) {
        IBMExtracaoSuplementar out = new IBMExtracaoSuplementar();
        preencherBase(out, arquivoOrigem, textoLimpo, confiancaDeteccao);

        List<String> linhas = IBMExtractionTextHelper.linhas(textoLimpo);
        List<String> rnf = new ArrayList<String>();
        List<String> sistemas = new ArrayList<String>();
        List<String> glossario = new ArrayList<String>();
        List<IBMURLIntegracao> urls = new ArrayList<IBMURLIntegracao>();

        for (String linha : linhas) {
            String l = linha.toLowerCase(Locale.ROOT);
            if (l.contains("nao-funcional") || l.contains("não-funcional") || l.contains("performance")
                    || l.contains("disponibilidade") || l.contains("seguranca") || l.contains("segurança")) {
                rnf.add(linha);
            }

            if (l.contains("sigms") || l.contains("sinaf") || l.contains("dataprev")
                    || l.contains("correios") || l.contains("mastercard")) {
                sistemas.add(linha);
            }

            if (l.contains("glossario") || l.contains("definicao") || l.contains("definição")) {
                glossario.add(linha);
            }

            List<String> urlsLinha = IBMExtractionTextHelper.extrairUrls(linha);
            for (String u : urlsLinha) {
                IBMURLIntegracao item = new IBMURLIntegracao();
                item.setUrl(u);
                item.setTipo(IBMExtractionTextHelper.classificarUrl(u));
                item.setOrigem("SUPLEMENTAR");
                urls.add(item);
            }
        }

        out.setRequisitosNaoFuncionais(rnf);
        out.setSistemasExternos(sistemas);
        out.setGlossario(glossario);
        out.setUrlsIntegracao(urls);
        return out;
    }
}


package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.IBMExtracaoUC;
import br.com.expertdev.gid.model.IBMFluxo;
import br.com.expertdev.gid.model.IBMRegraNegocio;
import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;
import br.com.expertdev.gid.model.enumtype.IBMTipoFluxo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IBMUCExtractorStrategy extends AbstractIBMExtractorStrategy {

    @Override
    public IBMTipoArtefato getTipo() {
        return IBMTipoArtefato.INTEGRACAO_UC;
    }

    @Override
    public IBMArtefatoExtracao extrair(File arquivoOrigem, String textoLimpo, int confiancaDeteccao) {
        IBMExtracaoUC out = new IBMExtracaoUC();
        preencherBase(out, arquivoOrigem, textoLimpo, confiancaDeteccao);

        List<String> linhas = IBMExtractionTextHelper.linhas(textoLimpo);
        List<String> atores = new ArrayList<String>();
        List<IBMRegraNegocio> regras = new ArrayList<IBMRegraNegocio>();
        List<IBMFluxo> fluxos = new ArrayList<IBMFluxo>();
        IBMFluxo fluxoAtual = null;

        for (String linha : linhas) {
            String l = linha.toLowerCase(Locale.ROOT);

            if (l.contains("objetivo") && (out.getObjetivo() == null || out.getObjetivo().isEmpty())) {
                out.setObjetivo(IBMExtractionTextHelper.aposSeparador(linha));
            }
            if ((l.contains("pre-cond") || l.contains("pré-cond") || l.contains("pre condic") || l.contains("pré condic"))
                    && (out.getPreCondicao() == null || out.getPreCondicao().isEmpty())) {
                out.setPreCondicao(IBMExtractionTextHelper.aposSeparador(linha));
            }
            if ((l.contains("pos-cond") || l.contains("pós-cond") || l.contains("pos condic") || l.contains("pós condic"))
                    && (out.getPosCondicao() == null || out.getPosCondicao().isEmpty())) {
                out.setPosCondicao(IBMExtractionTextHelper.aposSeparador(linha));
            }
            if (l.contains("ator")) {
                atores.add(IBMExtractionTextHelper.aposSeparador(linha));
            }

            if (l.contains("regra de negocio") || l.matches(".*\\brn\\d*.*")) {
                IBMRegraNegocio regra = new IBMRegraNegocio();
                regra.setDescricao(IBMExtractionTextHelper.aposSeparador(linha));
                regra.setOrigemSecao("REGRA_NEGOCIO");
                regras.add(regra);
            }

            if (l.contains("fluxo basico") || l.contains("fluxo b\u00e1sico") || (l.contains("fluxo") && l.contains("fb "))) {
                fluxoAtual = novoFluxo("Fluxo Basico", IBMTipoFluxo.FB, fluxos);
                continue;
            }
            if (l.contains("fluxo alternativo") || (l.contains("fluxo") && l.contains("fa "))) {
                fluxoAtual = novoFluxo("Fluxo Alternativo", IBMTipoFluxo.FA, fluxos);
                continue;
            }
            if (l.contains("fluxo de exce") || l.contains("fluxo exce") || (l.contains("fluxo") && l.contains("fe "))) {
                fluxoAtual = novoFluxo("Fluxo Excecao", IBMTipoFluxo.FE, fluxos);
                continue;
            }
            if (fluxoAtual != null && l.matches("^([0-9]+[.)-].*|[a-z][.)-].*)")) {
                fluxoAtual.getPassos().add(linha);
            }
        }

        out.setAtores(atores);
        out.setRegrasNegocio(regras);
        out.setFluxos(fluxos);
        if (out.getObjetivo() == null || out.getObjetivo().isEmpty()) {
            out.getAvisos().add("Objetivo nao identificado automaticamente.");
        }
        return out;
    }

    private IBMFluxo novoFluxo(String titulo, IBMTipoFluxo tipo, List<IBMFluxo> fluxos) {
        IBMFluxo f = new IBMFluxo();
        f.setTitulo(titulo);
        f.setTipoFluxo(tipo);
        fluxos.add(f);
        return f;
    }
}


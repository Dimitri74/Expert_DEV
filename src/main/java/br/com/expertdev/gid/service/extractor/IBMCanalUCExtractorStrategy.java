package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.IBMExtracaoCanalUC;
import br.com.expertdev.gid.model.IBMFluxo;
import br.com.expertdev.gid.model.IBMRegraNegocio;
import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;
import br.com.expertdev.gid.model.enumtype.IBMTipoFluxo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IBMCanalUCExtractorStrategy extends AbstractIBMExtractorStrategy {

    @Override
    public IBMTipoArtefato getTipo() {
        return IBMTipoArtefato.CANAIS_UC;
    }

    @Override
    public IBMArtefatoExtracao extrair(File arquivoOrigem, String textoLimpo, int confiancaDeteccao) {
        IBMExtracaoCanalUC out = new IBMExtracaoCanalUC();
        preencherBase(out, arquivoOrigem, textoLimpo, confiancaDeteccao);

        List<String> linhas = IBMExtractionTextHelper.linhas(textoLimpo);
        List<IBMRegraNegocio> regras = new ArrayList<IBMRegraNegocio>();
        List<IBMFluxo> fluxos = new ArrayList<IBMFluxo>();
        IBMFluxo fluxoAtual = null;

        for (String linha : linhas) {
            String l = linha.toLowerCase(Locale.ROOT);
            if ((out.getNomeCasoUso() == null || out.getNomeCasoUso().isEmpty())
                    && (l.contains("caso de uso") || l.contains("uc"))) {
                out.setNomeCasoUso(IBMExtractionTextHelper.aposSeparador(linha));
            }
            if (out.getCanal() == null || out.getCanal().isEmpty()) {
                if (l.contains("whatsapp")) {
                    out.setCanal("WHATSAPP");
                } else if (l.contains("sms")) {
                    out.setCanal("SMS");
                } else if (l.contains("e-mail") || l.contains("email")) {
                    out.setCanal("EMAIL");
                } else if (l.contains("push")) {
                    out.setCanal("PUSH");
                }
            }

            if (l.contains("regra") || l.matches(".*\\brn\\d*.*")) {
                IBMRegraNegocio regra = new IBMRegraNegocio();
                regra.setDescricao(IBMExtractionTextHelper.aposSeparador(linha));
                regra.setOrigemSecao("CANAIS_UC");
                regras.add(regra);
            }

            if (l.contains("fluxo basico")) {
                fluxoAtual = novoFluxo("Fluxo Basico", IBMTipoFluxo.FB, fluxos);
                continue;
            }
            if (l.contains("fluxo alternativo")) {
                fluxoAtual = novoFluxo("Fluxo Alternativo", IBMTipoFluxo.FA, fluxos);
                continue;
            }
            if (l.contains("fluxo de exce") || l.contains("fluxo exce")) {
                fluxoAtual = novoFluxo("Fluxo Excecao", IBMTipoFluxo.FE, fluxos);
                continue;
            }
            if (fluxoAtual != null && l.matches("^([0-9]+[.)-].*|[a-z][.)-].*)")) {
                fluxoAtual.getPassos().add(linha);
            }
        }

        out.setRegrasNegocio(regras);
        out.setFluxos(fluxos);
        if (out.getCanal() == null || out.getCanal().isEmpty()) {
            out.getAvisos().add("Canal nao identificado automaticamente.");
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


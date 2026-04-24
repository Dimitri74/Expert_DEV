package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.IBMExtracaoAPI;
import br.com.expertdev.gid.model.IBMParametro;
import br.com.expertdev.gid.model.IBMURLIntegracao;
import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IBMAPIExtractorStrategy extends AbstractIBMExtractorStrategy {

    private static final Pattern HTTP_CODES = Pattern.compile("\\b(200|201|202|204|400|401|403|404|409|422|500|502|503)\\b");
    private static final Pattern METODO = Pattern.compile("\\b(GET|POST|PUT|PATCH|DELETE)\\b");

    @Override
    public IBMTipoArtefato getTipo() {
        return IBMTipoArtefato.API_QUARKUS;
    }

    @Override
    public IBMArtefatoExtracao extrair(File arquivoOrigem, String textoLimpo, int confiancaDeteccao) {
        IBMExtracaoAPI out = new IBMExtracaoAPI();
        preencherBase(out, arquivoOrigem, textoLimpo, confiancaDeteccao);

        List<String> linhas = IBMExtractionTextHelper.linhas(textoLimpo);
        Set<String> codigos = new LinkedHashSet<String>();
        List<IBMParametro> params = new ArrayList<IBMParametro>();
        List<IBMURLIntegracao> urls = new ArrayList<IBMURLIntegracao>();

        for (String linha : linhas) {
            String l = linha.toLowerCase(Locale.ROOT);
            if ((out.getNomeServico() == null || out.getNomeServico().isEmpty())
                    && (l.contains("servico") || l.contains("service") || l.contains("endpoint"))) {
                out.setNomeServico(IBMExtractionTextHelper.aposSeparador(linha));
            }

            if (out.getMetodoHttp() == null || out.getMetodoHttp().isEmpty()) {
                Matcher m = METODO.matcher(linha.toUpperCase(Locale.ROOT));
                if (m.find()) {
                    out.setMetodoHttp(m.group(1));
                }
            }

            List<String> urlsLinha = IBMExtractionTextHelper.extrairUrls(linha);
            for (String url : urlsLinha) {
                IBMURLIntegracao u = new IBMURLIntegracao();
                u.setUrl(url);
                u.setTipo(IBMExtractionTextHelper.classificarUrl(url));
                u.setOrigem("API");
                urls.add(u);
                if (out.getEndpoint() == null || out.getEndpoint().isEmpty()) {
                    out.setEndpoint(url);
                }
            }

            Matcher c = HTTP_CODES.matcher(linha);
            while (c.find()) {
                codigos.add(c.group(1));
            }

            if (l.contains("param") || l.contains("query") || l.contains("path")) {
                IBMParametro p = new IBMParametro();
                p.setNome(IBMExtractionTextHelper.aposSeparador(linha));
                p.setDescricao(linha);
                p.setObrigatorio(l.contains("obrigat") || l.contains("required"));
                p.setTipo(l.contains("int") || l.contains("number") ? "number" : "string");
                params.add(p);
            }
        }

        out.setParametros(params);
        out.setCodigosHttpRetorno(new ArrayList<String>(codigos));
        out.setUrlsRelacionadas(urls);
        return out;
    }
}


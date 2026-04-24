package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.IBMExtracaoDI;
import br.com.expertdev.gid.model.IBMFluxo;
import br.com.expertdev.gid.model.IBMParametro;
import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;
import br.com.expertdev.gid.model.enumtype.IBMTipoFluxo;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Estratégia de extração para documentos DI (Descrição de Interface).
 *
 * Suporta dois perfis:
 *
 * 1. DI de INTERFACE (telas) — identificado pela presença de seções "2.X. Interface ..."
 *    Extrai por seção: campos (nome, formato, máscara, tamanho, obrigatoriedade),
 *    opções/botões e regras de apresentação.
 *    Isso gera visão completa para o desenvolvedor Frontend.
 *
 * 2. DI de SERVIÇO/INTEGRAÇÃO — identificado por seções de parâmetros de entrada/saída.
 *    Comportamento original preservado.
 *
 * NOTA sobre imagens: documentos .doc legados lidos via fallback parser não preservam
 * imagens embutidas (OLE/ESCHER). O aviso "Sem imagens detectadas" é esperado nesse
 * cenário. A visão das telas é reconstruída a partir dos campos textuais do DI.
 */
public class IBMDIExtractorStrategy extends AbstractIBMExtractorStrategy {

    // Detecta início de seção de tela: "2.1.  Interface Consulta Parâmetro..."
    private static final Pattern SECAO_INTERFACE = Pattern.compile(
            "^(\\d+\\.\\d+\\.?)\\s+(Interface\\s+.+)$", Pattern.CASE_INSENSITIVE);

    // Detecta início de seção de campos, opções ou regras dentro de uma tela
    private static final Pattern SUBSECAO = Pattern.compile(
            "^\\d+\\.\\d+\\.\\d+\\.?\\s+(.+)$");

    // Detecta propriedade de campo: "- Descrição:", "- Formato:", "- Tamanho:", etc.
    private static final Pattern PROP_CAMPO = Pattern.compile(
            "^[-\\u2013\\u2022]\\s*(Descrição|Formato|Máscara|Tamanho|Nome do campo|Valores possíveis|Regras de interface|Obrigatório|Campo obrigatório)\\s*:(.*)$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    @Override
    public IBMTipoArtefato getTipo() {
        return IBMTipoArtefato.INTEGRACAO_DI;
    }

    @Override
    public IBMArtefatoExtracao extrair(File arquivoOrigem, String textoLimpo, int confiancaDeteccao) {
        IBMExtracaoDI out = new IBMExtracaoDI();
        preencherBase(out, arquivoOrigem, textoLimpo, confiancaDeteccao);

        List<String> linhas = IBMExtractionTextHelper.linhas(textoLimpo);
        boolean ehInterface = detectarTipoInterface(linhas);
        out.setTipoInterface(ehInterface);

        if (ehInterface) {
            extrairTelasInterface(linhas, out);
        } else {
            extrairServico(linhas, out);
        }

        boolean temImagens = textoLimpo.toLowerCase(Locale.ROOT).contains(".png")
                || textoLimpo.toLowerCase(Locale.ROOT).contains(".jpg")
                || textoLimpo.toLowerCase(Locale.ROOT).contains("imagem");
        if (!temImagens) {
            out.getAvisos().add(
                "AVISO: Imagens das telas nao extraidas. Arquivo .doc legado processado via "
                + "fallback parser sem suporte a OLE/imagens embutidas. "
                + "A visao das telas foi reconstruida a partir dos campos textuais do DI.");
        }

        if (ehInterface) {
            int totalCampos = 0;
            for (List<IBMParametro> campos : out.getTelasPorSecao().values()) {
                totalCampos += campos.size();
            }
            out.getAvisos().add(String.format(
                "DI_INTERFACE: %d tela(s) extraida(s), %d campo(s) total.",
                out.getTelasPorSecao().size(), totalCampos));
        }

        return out;
    }

    // -------------------------------------------------------------------------
    // Detecção de perfil
    // -------------------------------------------------------------------------

    private boolean detectarTipoInterface(List<String> linhas) {
        for (String linha : linhas) {
            if (SECAO_INTERFACE.matcher(linha).matches()) return true;
            String l = linha.toLowerCase(Locale.ROOT);
            if (l.contains("interface") && (l.contains("consulta") || l.contains("inclui")
                    || l.contains("altera") || l.contains("exclui") || l.contains("detalha"))) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Extração: DI de Interface (telas)
    // -------------------------------------------------------------------------

    private void extrairTelasInterface(List<String> linhas, IBMExtracaoDI out) {
        Map<String, List<IBMParametro>> telasCampos = out.getTelasPorSecao();
        Map<String, List<String>> telasOpcoes = out.getOpcoesPorSecao();
        Map<String, List<String>> telasRegras = out.getRegrasPorSecao();

        String secaoAtual = null;
        String subsecaoAtual = null; // "Campos", "Opções", "Regras de apresentação"
        IBMParametro campoAtual = null;

        for (String linha : linhas) {
            // Nova seção de tela (2.X Interface ...)
            Matcher mSecao = SECAO_INTERFACE.matcher(linha);
            if (mSecao.matches()) {
                secaoAtual = mSecao.group(1).trim() + " " + mSecao.group(2).trim();
                subsecaoAtual = null;
                campoAtual = null;
                telasCampos.put(secaoAtual, new ArrayList<IBMParametro>());
                telasOpcoes.put(secaoAtual, new ArrayList<String>());
                telasRegras.put(secaoAtual, new ArrayList<String>());
                continue;
            }

            if (secaoAtual == null) {
                // Antes da primeira tela — captura nome do caso de uso
                String l = linha.toLowerCase(Locale.ROOT);
                if ((l.contains("caso de uso") || l.contains("objetivo"))
                        && out.getNomeCasoUso() == null) {
                    out.setNomeCasoUso(IBMExtractionTextHelper.aposSeparador(linha));
                }
                continue;
            }

            // Detecta subsecao (2.X.1 Campos / 2.X.2 Opções / 2.X.3 Regras)
            String lLower = linha.toLowerCase(Locale.ROOT);
            if (SUBSECAO.matcher(linha).matches()) {
                if (lLower.contains("campo")) {
                    subsecaoAtual = "campos";
                    campoAtual = null;
                } else if (lLower.contains("op") && (lLower.contains("ção") || lLower.contains("cao"))) {
                    subsecaoAtual = "opcoes";
                    campoAtual = null;
                } else if (lLower.contains("regra") || lLower.contains("apresenta")) {
                    subsecaoAtual = "regras";
                    campoAtual = null;
                }
                continue;
            }

            if (subsecaoAtual == null) {
                continue;
            }

            List<IBMParametro> campos = telasCampos.get(secaoAtual);
            List<String> opcoes = telasOpcoes.get(secaoAtual);
            List<String> regras = telasRegras.get(secaoAtual);

            if ("campos".equals(subsecaoAtual)) {
                // Início de novo campo: linha que começa com bullet/tab e não é propriedade
                if ((linha.startsWith("\t") || linha.startsWith("  ") || linha.startsWith("-")
                        || linha.startsWith("\u2013") || linha.startsWith("\u2022"))
                        && !PROP_CAMPO.matcher(linha).matches()
                        && !lLower.startsWith("- descrição")
                        && !lLower.startsWith("- formato")
                        && !lLower.startsWith("- máscara")
                        && !lLower.startsWith("- tamanho")
                        && !lLower.startsWith("- nome do campo")
                        && !lLower.startsWith("- valores")
                        && campos != null) {
                    String nomeCampo = linha.replaceAll("^[-\\t\\s\u2013\u2022]+", "").trim();
                    if (!nomeCampo.isEmpty() && nomeCampo.length() > 1) {
                        campoAtual = new IBMParametro();
                        campoAtual.setNome(nomeCampo);
                        campoAtual.setDescricao("");
                        campos.add(campoAtual);
                    }
                    continue;
                }

                // Propriedade de campo
                Matcher mProp = PROP_CAMPO.matcher(linha);
                if (mProp.matches() && campoAtual != null) {
                    String chave = mProp.group(1).trim().toLowerCase(Locale.ROOT);
                    String valor = mProp.group(2).trim();
                    aplicarPropriedadeCampo(campoAtual, chave, valor);
                    continue;
                }

                // Continuação da propriedade anterior
                if (campoAtual != null && !linha.isEmpty()
                        && (linha.startsWith(" ") || linha.startsWith("\t"))) {
                    String desc = campoAtual.getDescricao();
                    if (desc != null && desc.length() < 500) {
                        campoAtual.setDescricao(desc + " " + linha.trim());
                    }
                }

            } else if ("opcoes".equals(subsecaoAtual) && opcoes != null) {
                // Botões/links: linhas com bullet ou indentação
                if (linha.startsWith("\t") || linha.startsWith("  ")
                        || linha.startsWith("-") || linha.startsWith("\u2013")) {
                    String opcao = linha.replaceAll("^[-\\t\\s\u2013\u2022]+", "").trim();
                    if (!opcao.isEmpty() && opcao.length() > 1) {
                        opcoes.add(opcao);
                    }
                }

            } else if ("regras".equals(subsecaoAtual) && regras != null) {
                if (!linha.isEmpty()) {
                    regras.add(linha);
                }
            }
        }
    }

    private void aplicarPropriedadeCampo(IBMParametro campo, String chave, String valor) {
        if (chave.contains("descri")) {
            campo.setDescricao(valor);
        } else if (chave.contains("formato")) {
            campo.setTipo(valor);
        } else if (chave.contains("máscara") || chave.contains("mascara")) {
            // Armazena máscara no tipo se não tiver formato
            if (campo.getTipo() == null || campo.getTipo().isEmpty()) {
                campo.setTipo("máscara: " + valor);
            }
        } else if (chave.contains("tamanho")) {
            // Armazena tamanho como sufixo do tipo
            String tipo = campo.getTipo() == null ? "" : campo.getTipo();
            campo.setTipo(tipo + (tipo.isEmpty() ? "" : " ") + "[tamanho: " + valor + "]");
        } else if (chave.contains("nome do campo")) {
            // Nome do campo na interface é mais preciso que o título da seção
            if (valor != null && !valor.isEmpty()) {
                campo.setNome(valor);
            }
        } else if (chave.contains("obrigat") || chave.contains("required")) {
            campo.setObrigatorio(true);
        } else if (chave.contains("valores")) {
            String desc = campo.getDescricao() == null ? "" : campo.getDescricao();
            campo.setDescricao(desc + " | Valores: " + valor);
        }
    }

    // -------------------------------------------------------------------------
    // Extração: DI de Serviço/Integração (comportamento original preservado)
    // -------------------------------------------------------------------------

    private void extrairServico(List<String> linhas, IBMExtracaoDI out) {
        List<IBMParametro> entrada = new ArrayList<IBMParametro>();
        List<IBMParametro> saida = new ArrayList<IBMParametro>();
        List<IBMFluxo> fluxos = new ArrayList<IBMFluxo>();
        IBMFluxo fluxoAtual = null;
        boolean emEntrada = false;
        boolean emSaida = false;

        for (String linha : linhas) {
            String l = linha.toLowerCase(Locale.ROOT);

            if ((l.contains("caso de uso") || l.contains("nome do caso"))
                    && (out.getNomeCasoUso() == null || out.getNomeCasoUso().isEmpty())) {
                out.setNomeCasoUso(IBMExtractionTextHelper.aposSeparador(linha));
            }
            if (l.contains("pre-cond") && (out.getPreCondicao() == null || out.getPreCondicao().isEmpty())) {
                out.setPreCondicao(IBMExtractionTextHelper.aposSeparador(linha));
            }
            if (l.contains("pos-cond") && (out.getPosCondicao() == null || out.getPosCondicao().isEmpty())) {
                out.setPosCondicao(IBMExtractionTextHelper.aposSeparador(linha));
            }

            if (l.contains("parametro") && l.contains("entrada")) { emEntrada = true; emSaida = false; continue; }
            if (l.contains("parametro") && (l.contains("saida") || l.contains("saída"))) { emEntrada = false; emSaida = true; continue; }
            if (l.contains("fluxo ")) { emEntrada = false; emSaida = false; }

            if (emEntrada || emSaida) {
                IBMParametro p = tentarParametro(linha);
                if (p != null) {
                    if (emEntrada) entrada.add(p); else saida.add(p);
                }
            }

            if (l.contains("fluxo basico")) { fluxoAtual = novoFluxo("Fluxo Basico", IBMTipoFluxo.FB, fluxos); continue; }
            if (l.contains("fluxo alternativo")) { fluxoAtual = novoFluxo("Fluxo Alternativo", IBMTipoFluxo.FA, fluxos); continue; }
            if (l.contains("fluxo de exce") || l.contains("fluxo exce")) { fluxoAtual = novoFluxo("Fluxo Excecao", IBMTipoFluxo.FE, fluxos); continue; }
            if (fluxoAtual != null && l.matches("^([0-9]+[.)-].*|[a-z][.)-].*)")) {
                fluxoAtual.getPassos().add(linha);
            }

            if (l.contains(".png") || l.contains(".jpg") || l.contains("imagem")) {
                out.getImagensReferencia().add(linha);
            }
        }

        out.setParametrosEntrada(entrada);
        out.setParametrosSaida(saida);
        out.setFluxos(fluxos);
    }

    private IBMParametro tentarParametro(String linha) {
        if (linha == null || linha.length() < 2) return null;
        IBMParametro p = new IBMParametro();
        p.setNome(IBMExtractionTextHelper.aposSeparador(linha));
        p.setDescricao(linha);
        String lower = linha.toLowerCase(Locale.ROOT);
        p.setObrigatorio(lower.contains("obrigat") || lower.contains("required"));
        if (lower.contains("string")) p.setTipo("string");
        else if (lower.contains("int") || lower.contains("numero") || lower.contains("number")) p.setTipo("number");
        else if (lower.contains("date") || lower.contains("data")) p.setTipo("date");
        else p.setTipo("desconhecido");
        return p;
    }

    private IBMFluxo novoFluxo(String titulo, IBMTipoFluxo tipo, List<IBMFluxo> fluxos) {
        IBMFluxo f = new IBMFluxo();
        f.setTitulo(titulo);
        f.setTipoFluxo(tipo);
        fluxos.add(f);
        return f;
    }
}


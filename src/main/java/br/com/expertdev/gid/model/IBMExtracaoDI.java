package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracao tipada para documento DI de interface (telas) ou integração (serviços).
 *
 * Quando o DI é do tipo "Interface" (Descrição de Interface de Caso de Uso),
 * as telas são extraídas em {@link #telasPorSecao} — mapa de "2.1 Interface X" →
 * lista de campos com nome, descrição, formato, máscara, tamanho, obrigatoriedade
 * e regras de apresentação.
 *
 * Quando é DI de serviço/integração, usa {@link #parametrosEntrada} e {@link #parametrosSaida}.
 */
public class IBMExtracaoDI extends IBMArtefatoExtracao {

    private String nomeCasoUso;
    private String preCondicao;
    private String posCondicao;

    // Para DI de serviço/integração
    private List<IBMFluxo> fluxos = new ArrayList<IBMFluxo>();
    private List<IBMParametro> parametrosEntrada = new ArrayList<IBMParametro>();
    private List<IBMParametro> parametrosSaida = new ArrayList<IBMParametro>();

    /**
     * Para DI de Interface (telas).
     * Chave: título da seção (ex: "2.1 - Interface Consulta Parâmetro Mensagem Digital")
     * Valor: lista de campos/propriedades extraídos dessa tela.
     */
    private Map<String, List<IBMParametro>> telasPorSecao = new LinkedHashMap<String, List<IBMParametro>>();

    /**
     * Opções/botões por tela.
     * Chave: título da seção. Valor: lista de rótulos dos botões/links.
     */
    private Map<String, List<String>> opcoesPorSecao = new LinkedHashMap<String, List<String>>();

    /**
     * Regras de apresentação por tela.
     * Chave: título da seção. Valor: lista de textos das regras.
     */
    private Map<String, List<String>> regrasPorSecao = new LinkedHashMap<String, List<String>>();

    /** Imagens/screenshots referenciados no DI (para DI legado com nome de arquivo). */
    private List<String> imagensReferencia = new ArrayList<String>();

    /** Indica se este DI é de interface (telas) ou de serviço/integração. */
    private boolean tipoInterface = false;

    public IBMExtracaoDI() {
        setTipoArtefato(IBMTipoArtefato.INTEGRACAO_DI);
    }

    public String getNomeCasoUso() { return nomeCasoUso; }
    public void setNomeCasoUso(String nomeCasoUso) { this.nomeCasoUso = nomeCasoUso; }

    public String getPreCondicao() { return preCondicao; }
    public void setPreCondicao(String preCondicao) { this.preCondicao = preCondicao; }

    public String getPosCondicao() { return posCondicao; }
    public void setPosCondicao(String posCondicao) { this.posCondicao = posCondicao; }

    public List<IBMFluxo> getFluxos() { return fluxos; }
    public void setFluxos(List<IBMFluxo> fluxos) { this.fluxos = fluxos; }

    public List<IBMParametro> getParametrosEntrada() { return parametrosEntrada; }
    public void setParametrosEntrada(List<IBMParametro> parametrosEntrada) { this.parametrosEntrada = parametrosEntrada; }

    public List<IBMParametro> getParametrosSaida() { return parametrosSaida; }
    public void setParametrosSaida(List<IBMParametro> parametrosSaida) { this.parametrosSaida = parametrosSaida; }

    public Map<String, List<IBMParametro>> getTelasPorSecao() { return telasPorSecao; }
    public void setTelasPorSecao(Map<String, List<IBMParametro>> telasPorSecao) { this.telasPorSecao = telasPorSecao; }

    public Map<String, List<String>> getOpcoesPorSecao() { return opcoesPorSecao; }
    public void setOpcoesPorSecao(Map<String, List<String>> opcoesPorSecao) { this.opcoesPorSecao = opcoesPorSecao; }

    public Map<String, List<String>> getRegrasPorSecao() { return regrasPorSecao; }
    public void setRegrasPorSecao(Map<String, List<String>> regrasPorSecao) { this.regrasPorSecao = regrasPorSecao; }

    public List<String> getImagensReferencia() { return imagensReferencia; }
    public void setImagensReferencia(List<String> imagensReferencia) { this.imagensReferencia = imagensReferencia; }

    public boolean isTipoInterface() { return tipoInterface; }
    public void setTipoInterface(boolean tipoInterface) { this.tipoInterface = tipoInterface; }
}


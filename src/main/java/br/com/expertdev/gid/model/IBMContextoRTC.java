package br.com.expertdev.gid.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contexto consolidado por RTC com todos os artefatos extraidos.
 */
public class IBMContextoRTC {

    private String rtcNumero;
    private String ucCodigo;
    private IBMExtracaoMensagem extracaoMensagem;
    private IBMExtracaoUC extracaoUC;
    private IBMExtracaoDI extracaoDI;
    private IBMExtracaoAPI extracaoAPI;
    private IBMExtracaoCanalUC extracaoCanalUC;
    private IBMExtracaoSuplementar extracaoSuplementar;
    private List<IBMArtefatoExtracao> artefatosComplementares = new ArrayList<IBMArtefatoExtracao>();
    private LocalDateTime dataConsolidacao = LocalDateTime.now();

    public String getRtcNumero() {
        return rtcNumero;
    }

    public void setRtcNumero(String rtcNumero) {
        this.rtcNumero = rtcNumero;
    }

    public String getUcCodigo() {
        return ucCodigo;
    }

    public void setUcCodigo(String ucCodigo) {
        this.ucCodigo = ucCodigo;
    }

    public IBMExtracaoMensagem getExtracaoMensagem() {
        return extracaoMensagem;
    }

    public void setExtracaoMensagem(IBMExtracaoMensagem extracaoMensagem) {
        this.extracaoMensagem = extracaoMensagem;
    }

    public IBMExtracaoUC getExtracaoUC() {
        return extracaoUC;
    }

    public void setExtracaoUC(IBMExtracaoUC extracaoUC) {
        this.extracaoUC = extracaoUC;
    }

    public IBMExtracaoDI getExtracaoDI() {
        return extracaoDI;
    }

    public void setExtracaoDI(IBMExtracaoDI extracaoDI) {
        this.extracaoDI = extracaoDI;
    }

    public IBMExtracaoAPI getExtracaoAPI() {
        return extracaoAPI;
    }

    public void setExtracaoAPI(IBMExtracaoAPI extracaoAPI) {
        this.extracaoAPI = extracaoAPI;
    }

    public IBMExtracaoCanalUC getExtracaoCanalUC() {
        return extracaoCanalUC;
    }

    public void setExtracaoCanalUC(IBMExtracaoCanalUC extracaoCanalUC) {
        this.extracaoCanalUC = extracaoCanalUC;
    }

    public IBMExtracaoSuplementar getExtracaoSuplementar() {
        return extracaoSuplementar;
    }

    public void setExtracaoSuplementar(IBMExtracaoSuplementar extracaoSuplementar) {
        this.extracaoSuplementar = extracaoSuplementar;
    }

    public List<IBMArtefatoExtracao> getArtefatosComplementares() {
        return artefatosComplementares;
    }

    public void setArtefatosComplementares(List<IBMArtefatoExtracao> artefatosComplementares) {
        this.artefatosComplementares = artefatosComplementares;
    }

    public LocalDateTime getDataConsolidacao() {
        return dataConsolidacao;
    }

    public void setDataConsolidacao(LocalDateTime dataConsolidacao) {
        this.dataConsolidacao = dataConsolidacao;
    }
}


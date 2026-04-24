package br.com.expertdev.gid.service;

import br.com.expertdev.gid.model.IBMArtefatoExtracao;
import br.com.expertdev.gid.model.IBMExtracaoAPI;
import br.com.expertdev.gid.model.IBMExtracaoCanalUC;
import br.com.expertdev.gid.model.IBMExtracaoDI;
import br.com.expertdev.gid.model.IBMExtracaoMensagem;
import br.com.expertdev.gid.model.IBMExtracaoSuplementar;
import br.com.expertdev.gid.model.IBMExtracaoUC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Runner simples para validar B4: extracao semantica por tipo.
 */
public class IBMB4Probe {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Uso: java br.com.expertdev.gid.service.IBMB4Probe <arquivo1.doc|docx> [arquivo2 ...]");
            System.exit(2);
            return;
        }

        List<File> arquivos = new ArrayList<File>();
        for (String a : args) {
            arquivos.add(new File(a));
        }

        IBMB4ExtractionService service = new IBMB4ExtractionService();
        List<IBMArtefatoExtracao> itens = service.extrairArquivos(arquivos);

        for (IBMArtefatoExtracao item : itens) {
            System.out.println("---");
            System.out.println("Arquivo: " + item.getNomeArquivoOrigem());
            System.out.println("Tipo: " + item.getTipoArtefato());
            System.out.println("Confianca: " + item.getConfiancaDeteccao());
            System.out.println("RTC: " + item.getRtcNumero());
            System.out.println("UC: " + item.getUcCodigo());
            if (item instanceof IBMExtracaoMensagem) {
                IBMExtracaoMensagem m = (IBMExtracaoMensagem) item;
                System.out.println("Mensagens extraidas: " + m.getMensagens().size());
            } else if (item instanceof IBMExtracaoUC) {
                IBMExtracaoUC uc = (IBMExtracaoUC) item;
                System.out.println("Regras UC: " + uc.getRegrasNegocio().size() + " | Fluxos: " + uc.getFluxos().size());
            } else if (item instanceof IBMExtracaoDI) {
                IBMExtracaoDI di = (IBMExtracaoDI) item;
                System.out.println("Parametros E/S: " + di.getParametrosEntrada().size() + "/" + di.getParametrosSaida().size());
            } else if (item instanceof IBMExtracaoAPI) {
                IBMExtracaoAPI api = (IBMExtracaoAPI) item;
                System.out.println("Endpoint: " + api.getEndpoint() + " | HTTP codes: " + api.getCodigosHttpRetorno().size());
            } else if (item instanceof IBMExtracaoCanalUC) {
                IBMExtracaoCanalUC c = (IBMExtracaoCanalUC) item;
                System.out.println("Canal: " + c.getCanal() + " | Regras: " + c.getRegrasNegocio().size());
            } else if (item instanceof IBMExtracaoSuplementar) {
                IBMExtracaoSuplementar s = (IBMExtracaoSuplementar) item;
                System.out.println("RNF: " + s.getRequisitosNaoFuncionais().size() + " | URLs: " + s.getUrlsIntegracao().size());
            }
            if (item.getAvisos() != null && !item.getAvisos().isEmpty()) {
                System.out.println("Avisos: " + String.join(" | ", item.getAvisos()));
            }
        }

        System.exit(0);
    }
}


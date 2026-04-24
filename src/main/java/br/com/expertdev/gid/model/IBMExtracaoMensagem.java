package br.com.expertdev.gid.model;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracao tipada para catalogo de mensagens do sistema.
 */
public class IBMExtracaoMensagem extends IBMArtefatoExtracao {

    private List<IBMMensagemSistema> mensagens = new ArrayList<IBMMensagemSistema>();

    public IBMExtracaoMensagem() {
        setTipoArtefato(IBMTipoArtefato.MSG_SISTEMA);
    }

    public List<IBMMensagemSistema> getMensagens() {
        return mensagens;
    }

    public void setMensagens(List<IBMMensagemSistema> mensagens) {
        this.mensagens = mensagens;
    }
}


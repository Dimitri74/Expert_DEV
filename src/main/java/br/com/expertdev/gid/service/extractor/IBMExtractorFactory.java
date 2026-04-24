package br.com.expertdev.gid.service.extractor;

import br.com.expertdev.gid.model.enumtype.IBMTipoArtefato;

import java.util.EnumMap;
import java.util.Map;

public class IBMExtractorFactory {

    private final Map<IBMTipoArtefato, IBMExtractorStrategy> strategies = new EnumMap<IBMTipoArtefato, IBMExtractorStrategy>(IBMTipoArtefato.class);

    public IBMExtractorFactory() {
        registrar(new IBMMensagemExtractorStrategy());
        registrar(new IBMUCExtractorStrategy());
        registrar(new IBMDIExtractorStrategy());
        registrar(new IBMAPIExtractorStrategy());
        registrar(new IBMCanalUCExtractorStrategy());
        registrar(new IBMSuplementarExtractorStrategy());
    }

    private void registrar(IBMExtractorStrategy strategy) {
        strategies.put(strategy.getTipo(), strategy);
    }

    public IBMExtractorStrategy get(IBMTipoArtefato tipo) {
        return strategies.get(tipo);
    }
}


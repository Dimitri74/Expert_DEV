package br.com.expertdev.service;

import br.com.expertdev.config.ExpertDevConfig;
import br.com.expertdev.model.ResultadoProcessamento;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelUrlProcessor {

    private final int numThreads;
    private final PageProcessor pageProcessor;

    public ParallelUrlProcessor(ExpertDevConfig config, int numThreads) {
        this.numThreads = numThreads > 0 ? numThreads : 1;
        this.pageProcessor = new PageProcessor(config, new JsoupDocumentFetcher(config));
    }

    public List<ResultadoProcessamento> processar(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return new ArrayList<>();
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<ResultadoProcessamento>> futures = new ArrayList<>();

        for (String url : urls) {
            futures.add(executor.submit(new ProcessadorUrl(url)));
        }

        List<ResultadoProcessamento> resultados = new ArrayList<>();
        for (Future<ResultadoProcessamento> future : futures) {
            try {
                resultados.add(future.get());
            } catch (Exception e) {
                System.err.println("⚠ Erro ao aguardar resultado de processamento: " + e.getMessage());
            }
        }

        executor.shutdown();
        return resultados;
    }

    private class ProcessadorUrl implements Callable<ResultadoProcessamento> {
        private final String url;

        ProcessadorUrl(String url) {
            this.url = url;
        }

        public ResultadoProcessamento call() {
            System.out.println("Processando: " + url + " [thread: " + Thread.currentThread().getName() + "]");
            ResultadoProcessamento resultado = pageProcessor.processar(url);

            if (resultado.isSucesso()) {
                System.out.println("   ✓ Sucesso: " + url);
            } else {
                System.err.println("   ⚠ Falha: " + url + " - " + resultado.getErro());
            }

            return resultado;
        }
    }
}


package br.com.expertdev.service;

public interface PromptGenerationService {

    String gerarPrompt(String regras, String imagensInfo) throws Exception;

    String getNomeModo();
}


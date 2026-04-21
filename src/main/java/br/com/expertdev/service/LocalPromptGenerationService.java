package br.com.expertdev.service;

public class LocalPromptGenerationService implements PromptGenerationService {

    private final PromptGenerator promptGenerator;

    public LocalPromptGenerationService(PromptGenerator promptGenerator) {
        this.promptGenerator = promptGenerator;
    }

    @Override
    public String gerarPrompt(String regras, String imagensInfo) {
        return promptGenerator.gerar(regras, imagensInfo);
    }

    @Override
    public String getNomeModo() {
        return "LOCAL";
    }
}


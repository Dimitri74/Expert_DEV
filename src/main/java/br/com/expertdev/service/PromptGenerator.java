package br.com.expertdev.service;

public class PromptGenerator {

    private final PromptContextRefiner contextRefiner = new PromptContextRefiner();
    private final PromptBlueprintBuilder blueprintBuilder = new PromptBlueprintBuilder();
    private final String perfilPrompt;

    public PromptGenerator() {
        this("tecnico");
    }

    public PromptGenerator(String perfilPrompt) {
        this.perfilPrompt = perfilPrompt;
    }

    public String gerar(String regras, String imagensInfo) {
        PromptContextRefiner.RefinedPromptContext context = contextRefiner.refinar(regras, imagensInfo);
        return blueprintBuilder.buildLocalPrompt(context, perfilPrompt);
    }
}


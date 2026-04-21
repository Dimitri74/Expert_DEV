package br.com.expertdev.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class ExpertDevConfig {

    private static final String CONFIG_FILE_NAME = "expertdev.properties";

    private final int timeoutMs;
    private final int limiteTexto;
    private final int tamanhoMinimoTextoUtil;
    private final String userAgent;
    private final String seletorConteudoPrincipal;
    private final String seletorRuido;
    private final String arquivoRegras;
    private final String arquivoImagens;
    private final String arquivoPrompt;
    private final String arquivoResumo;
    private final String arquivoErros;
    private final boolean temaClaroPadrao;
    private final boolean aiHabilitada;
    private final String aiProvider;
    private final String aiEndpoint;
    private final String aiModel;
    private final int aiTimeoutMs;
    private final int aiMaxTokens;
    private final int aiMaxContextChars;
    private final double aiTemperature;
    private final boolean aiModoEconomico;
    private final String aiApiKey;
    private final String uiModoGeracao;
    private final String promptProfile;

    private ExpertDevConfig(Properties properties) {
        this.timeoutMs = parseInteger(properties, "timeout.ms", 30000);
        this.limiteTexto = parseInteger(properties, "texto.limite", 120000);
        this.tamanhoMinimoTextoUtil = parseInteger(properties, "texto.minimo.util", 100);
        this.userAgent = properties.getProperty("http.userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        this.seletorConteudoPrincipal = properties.getProperty(
                "selector.primary",
                "article, main, .content, .wiki-content, .document-content, .rtc-content"
        );
        this.seletorRuido = properties.getProperty(
                "selector.noise",
                "script, style, noscript, nav, footer, header, aside, form, iframe, .menu, .breadcrumb, .breadcrumbs, .navbar, .sidebar, .ads, .advertisement"
        );
        this.arquivoRegras = properties.getProperty("output.rules.file", "regras_extraidas.txt");
        this.arquivoImagens = properties.getProperty("output.images.file", "imagens_encontradas.txt");
        this.arquivoPrompt = properties.getProperty("output.prompt.file", "prompt_para_junie_copilot.txt");
        this.arquivoResumo = properties.getProperty("output.summary.file", "resumo_execucao.txt");
        this.arquivoErros = properties.getProperty("output.errors.file", "erros_processamento.txt");
        this.temaClaroPadrao = parseBoolean(properties, "ui.theme.light.default", true);
        this.aiHabilitada = parseBoolean(properties, "ai.enabled", false);
        this.aiProvider = properties.getProperty("ai.provider", "openai").trim();
        this.aiEndpoint = properties.getProperty("ai.endpoint", getEndpointPadrao(this.aiProvider)).trim();
        this.aiModel = properties.getProperty("ai.model", getModeloPadrao(this.aiProvider)).trim();
        this.aiTimeoutMs = parseInteger(properties, "ai.timeout.ms", 30000);
        this.aiMaxTokens = parseInteger(properties, "ai.max.tokens", 700);
        this.aiMaxContextChars = parseInteger(properties, "ai.max.context.chars", 12000);
        this.aiTemperature = parseDouble(properties, "ai.temperature", 0.1d);
        this.aiModoEconomico = parseBoolean(properties, "ai.economy.mode", true);
        this.aiApiKey = properties.getProperty("ai.api.key", "").trim();
        this.uiModoGeracao = properties.getProperty("ui.generation.mode", "LOCAL").trim();
        this.promptProfile = properties.getProperty("prompt.profile", "tecnico").trim().toLowerCase();
    }

    public static ExpertDevConfig carregar() {
        Properties properties = criarPropriedadesPadrao();
        carregarDoClasspath(properties);
        carregarDoArquivoExterno(properties);
        return new ExpertDevConfig(properties);
    }

    private static Properties criarPropriedadesPadrao() {
        Properties properties = new Properties();
        properties.setProperty("timeout.ms", "30000");
        properties.setProperty("texto.limite", "120000");
        properties.setProperty("texto.minimo.util", "100");
        properties.setProperty("http.userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        properties.setProperty("selector.primary", "article, main, .content, .wiki-content, .document-content, .rtc-content");
        properties.setProperty("selector.noise", "script, style, noscript, nav, footer, header, aside, form, iframe, .menu, .breadcrumb, .breadcrumbs, .navbar, .sidebar, .ads, .advertisement");
        properties.setProperty("output.rules.file", "regras_extraidas.txt");
        properties.setProperty("output.images.file", "imagens_encontradas.txt");
        properties.setProperty("output.prompt.file", "prompt_para_junie_copilot.txt");
        properties.setProperty("output.summary.file", "resumo_execucao.txt");
        properties.setProperty("output.errors.file", "erros_processamento.txt");
        properties.setProperty("ui.theme.light.default", "true");
        properties.setProperty("ai.enabled", "false");
        properties.setProperty("ai.provider", "openai");
        properties.setProperty("ai.endpoint", getEndpointPadrao("openai"));
        properties.setProperty("ai.model", getModeloPadrao("openai"));
        properties.setProperty("ai.timeout.ms", "30000");
        properties.setProperty("ai.max.tokens", "700");
        properties.setProperty("ai.max.context.chars", "12000");
        properties.setProperty("ai.temperature", "0.1");
        properties.setProperty("ai.economy.mode", "true");
        properties.setProperty("ai.api.key", "");
        properties.setProperty("ui.generation.mode", "LOCAL");
        properties.setProperty("prompt.profile", "tecnico");
        return properties;
    }

    private static String getEndpointPadrao(String provider) {
        if (provider == null) {
            return "https://api.openai.com/v1/chat/completions";
        }
        String normalized = provider.trim().toLowerCase();
        if ("claude".equals(normalized) || "anthropic".equals(normalized) || "claude-code".equals(normalized)) {
            return "https://api.anthropic.com/v1/messages";
        }
        return "https://api.openai.com/v1/chat/completions";
    }

    private static String getModeloPadrao(String provider) {
        if (provider == null) {
            return "gpt-4o-mini";
        }
        String normalized = provider.trim().toLowerCase();
        if ("claude".equals(normalized) || "anthropic".equals(normalized) || "claude-code".equals(normalized)) {
            return "claude-3-5-sonnet-latest";
        }
        return "gpt-4o-mini";
    }

    private static void carregarDoClasspath(Properties properties) {
        InputStream inputStream = ExpertDevConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
        if (inputStream == null) {
            return;
        }

        try (InputStream in = inputStream) {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("⚠ Não foi possível carregar configuração do classpath: " + e.getMessage());
        }
    }

    private static void carregarDoArquivoExterno(Properties properties) {
        File arquivo = new File(CONFIG_FILE_NAME);
        if (!arquivo.exists() || !arquivo.isFile()) {
            return;
        }

        try (InputStream inputStream = new FileInputStream(arquivo)) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("⚠ Não foi possível carregar configuração externa: " + e.getMessage());
        }
    }

    private static int parseInteger(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("⚠ Valor inválido para '" + key + "': " + value + ". Usando padrão " + defaultValue + ".");
            return defaultValue;
        }
    }

    private static boolean parseBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        String normalized = value.trim().toLowerCase();
        if ("true".equals(normalized) || "1".equals(normalized) || "sim".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "nao".equals(normalized)) {
            return false;
        }

        System.err.println("⚠ Valor inválido para '" + key + "': " + value + ". Usando padrão " + defaultValue + ".");
        return defaultValue;
    }

    private static double parseDouble(Properties properties, String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("⚠ Valor inválido para '" + key + "': " + value + ". Usando padrão " + defaultValue + ".");
            return defaultValue;
        }
    }

    private static Properties carregarPropriedadesCompletas() {
        Properties properties = criarPropriedadesPadrao();
        carregarDoClasspath(properties);
        carregarDoArquivoExterno(properties);
        return properties;
    }

    public static void salvarPreferenciaTemaClaro(boolean temaClaro) {
        Properties properties = carregarPropriedadesCompletas();
        properties.setProperty("ui.theme.light.default", String.valueOf(temaClaro));

        try (OutputStream outputStream = new FileOutputStream(CONFIG_FILE_NAME)) {
            properties.store(outputStream, "Expert Dev configuration");
        } catch (IOException e) {
            System.err.println("⚠ Não foi possível salvar preferência de tema: " + e.getMessage());
        }
    }

    public static void salvarPreferenciaModoGeracao(String modo) {
        Properties properties = carregarPropriedadesCompletas();
        properties.setProperty("ui.generation.mode", modo == null ? "LOCAL" : modo.trim().toUpperCase());
        salvarPropriedades(properties, "Expert Dev configuration");
    }

    public static void salvarPreferenciaAiHabilitada(boolean habilitada) {
        Properties properties = carregarPropriedadesCompletas();
        properties.setProperty("ai.enabled", String.valueOf(habilitada));
        salvarPropriedades(properties, "Expert Dev configuration");
    }

    public static void salvarApiKeyIA(String apiKey) {
        Properties properties = carregarPropriedadesCompletas();
        properties.setProperty("ai.api.key", apiKey == null ? "" : apiKey.trim());
        salvarPropriedades(properties, "Expert Dev configuration");
    }

    public static void salvarConfiguracaoAiProvider(String provider) {
        String providerNormalizado = provider == null ? "openai" : provider.trim().toLowerCase();
        Properties properties = carregarPropriedadesCompletas();
        properties.setProperty("ai.provider", providerNormalizado);
        properties.setProperty("ai.endpoint", getEndpointPadrao(providerNormalizado));
        properties.setProperty("ai.model", getModeloPadrao(providerNormalizado));
        salvarPropriedades(properties, "Expert Dev configuration");
    }

    public static void salvarPromptProfile(String profile) {
        String normalizado = ("negocial".equalsIgnoreCase(profile) || "executivo".equalsIgnoreCase(profile))
                ? "negocial"
                : "tecnico";
        Properties properties = carregarPropriedadesCompletas();
        properties.setProperty("prompt.profile", normalizado);
        salvarPropriedades(properties, "Expert Dev configuration");
    }

    private static void salvarPropriedades(Properties properties, String comentario) {
        try (OutputStream outputStream = new FileOutputStream(CONFIG_FILE_NAME)) {
            properties.store(outputStream, comentario);
        } catch (IOException e) {
            System.err.println("⚠ Não foi possível salvar configuração: " + e.getMessage());
        }
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getLimiteTexto() {
        return limiteTexto;
    }

    public int getTamanhoMinimoTextoUtil() {
        return tamanhoMinimoTextoUtil;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getSeletorConteudoPrincipal() {
        return seletorConteudoPrincipal;
    }

    public String getSeletorRuido() {
        return seletorRuido;
    }

    public String getArquivoRegras() {
        return arquivoRegras;
    }

    public String getArquivoImagens() {
        return arquivoImagens;
    }

    public String getArquivoPrompt() {
        return arquivoPrompt;
    }

    public String getArquivoResumo() {
        return arquivoResumo;
    }

    public String getArquivoErros() {
        return arquivoErros;
    }

    public boolean isTemaClaroPadrao() {
        return temaClaroPadrao;
    }

    public boolean isAiHabilitada() {
        return aiHabilitada;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public String getAiEndpoint() {
        return aiEndpoint;
    }

    public String getAiModel() {
        return aiModel;
    }

    public int getAiTimeoutMs() {
        return aiTimeoutMs;
    }

    public int getAiMaxTokens() {
        return aiMaxTokens;
    }

    public int getAiMaxContextChars() {
        return aiMaxContextChars;
    }

    public double getAiTemperature() {
        return aiTemperature;
    }

    public boolean isAiModoEconomico() {
        return aiModoEconomico;
    }

    public String getUiModoGeracao() {
        return uiModoGeracao;
    }

    public String getAiApiKeyResolvida() {
        if (aiApiKey != null && !aiApiKey.trim().isEmpty()) {
            return aiApiKey.trim();
        }
        String envKey = System.getenv("EXPERTDEV_AI_API_KEY");
        return envKey == null ? "" : envKey.trim();
    }

    public String getPromptProfile() {
        return ("negocial".equals(promptProfile) || "executivo".equals(promptProfile)) ? "negocial" : "tecnico";
    }
}


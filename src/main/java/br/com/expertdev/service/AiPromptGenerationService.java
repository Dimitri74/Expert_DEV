package br.com.expertdev.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AiPromptGenerationService implements PromptGenerationService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String provider;
    private final String endpoint;
    private final String model;
    private final String apiKey;
    private final int timeoutMs;
    private final double temperature;
    private final int maxTokens;
    private final int maxContextChars;
    private final boolean modoEconomico;
    private final String perfilPrompt;
    private final AiContextReducer contextReducer;
    private final PromptContextRefiner promptContextRefiner;
    private final PromptBlueprintBuilder blueprintBuilder;

    public AiPromptGenerationService(String provider,
                                     String endpoint,
                                     String model,
                                     String apiKey,
                                     int timeoutMs,
                                     double temperature,
                                     int maxTokens,
                                     int maxContextChars,
                                     boolean modoEconomico,
                                     String perfilPrompt) {
        this.provider = provider;
        this.endpoint = endpoint;
        this.model = model;
        this.apiKey = apiKey;
        this.timeoutMs = timeoutMs;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.maxContextChars = maxContextChars;
        this.modoEconomico = modoEconomico;
        this.perfilPrompt = perfilPrompt;
        this.contextReducer = new AiContextReducer();
        this.promptContextRefiner = new PromptContextRefiner();
        this.blueprintBuilder = new PromptBlueprintBuilder();
    }

    @Override
    public String gerarPrompt(String regras, String imagensInfo) throws Exception {
        AiContextReducer.ReducedContext contexto = modoEconomico
                ? contextReducer.reduzir(regras, imagensInfo, maxContextChars)
                : new AiContextReducer.ReducedContext(
                        (regras == null || regras.trim().isEmpty()) ? "[Sem regras extraidas]" : regras,
                        (imagensInfo == null || imagensInfo.trim().isEmpty()) ? "[Sem imagens detectadas]" : imagensInfo,
                        "[Modo de IA padrao sem reducao economica de contexto.]",
                        false
                );

        PromptContextRefiner.RefinedPromptContext promptContext = promptContextRefiner.refinar(
                contexto.getRegras(), contexto.getImagens());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(criarMensagem("system", blueprintBuilder.buildAiSystemPrompt()));
        messages.add(criarMensagem("user", blueprintBuilder.buildAiUserPrompt(
                promptContext,
                contexto.getObservacao(),
                perfilPrompt
        )));

        return executarChat(messages);
    }

    public void testarConexao() throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(criarMensagem("system", "Responda de forma curta para validacao de conectividade."));
        messages.add(criarMensagem("user", "Responda exatamente: OK"));
        String retorno = executarChat(messages);
        if (retorno.trim().isEmpty()) {
            throw new IOException("Resposta vazia da IA.");
        }
    }

    @Override
    public String getNomeModo() {
        return "IA-" + provider.toUpperCase() + (modoEconomico ? " (economico)" : "");
    }

    private String executarChat(List<Map<String, String>> messages) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            configurarHeaders(conn);

            String payload = montarPayload(messages);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream respostaStream = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();
            String corpo = lerTexto(respostaStream);

            if (code < 200 || code >= 300) {
                throw new IOException("Falha HTTP " + code + " ao chamar IA: " + corpo);
            }

            JsonNode root = MAPPER.readTree(corpo);
            String conteudo = extrairConteudo(root).trim();
            if (conteudo.isEmpty()) {
                throw new IOException("IA respondeu sem conteudo util.");
            }
            return conteudo;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String montarPayload(List<Map<String, String>> messages) throws IOException {
        if (isClaudeProvider()) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("max_tokens", maxTokens);
            payload.put("temperature", temperature);
            payload.put("messages", messages);
            return MAPPER.writeValueAsString(payload);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);
        payload.put("temperature", temperature);
        payload.put("max_tokens", maxTokens);
        return MAPPER.writeValueAsString(payload);
    }

    private Map<String, String> criarMensagem(String role, String content) {
        Map<String, String> msg = new HashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }

    private void configurarHeaders(HttpURLConnection conn) {
        if (isClaudeProvider()) {
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            return;
        }
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
    }

    private String extrairConteudo(JsonNode root) {
        if (isClaudeProvider()) {
            JsonNode content = root.path("content");
            if (content.isArray() && content.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode node : content) {
                    String text = node.path("text").asText("");
                    if (!text.trim().isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(text.trim());
                    }
                }
                return sb.toString();
            }
            return "";
        }
        return root.path("choices").path(0).path("message").path("content").asText("");
    }

    private boolean isClaudeProvider() {
        if (provider == null) {
            return false;
        }
        String normalized = provider.trim().toLowerCase();
        return "claude".equals(normalized) || "anthropic".equals(normalized) || "claude-code".equals(normalized);
    }

    private String lerTexto(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int n;
        while ((n = in.read(data)) != -1) {
            buffer.write(data, 0, n);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}


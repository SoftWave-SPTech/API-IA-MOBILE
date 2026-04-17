package softwave.api_finance_ia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import softwave.api_finance_ia.config.GeminiConfig;
import softwave.api_finance_ia.exception.BadRequestException;
import softwave.api_finance_ia.exception.ServiceUnavailableException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;

    public GeminiService(GeminiConfig geminiConfig, ObjectMapper objectMapper) {
        this.geminiConfig = geminiConfig;
        this.objectMapper = objectMapper;
    }

    public InsightEstruturado gerarInsightEstruturado(String prompt) {
        if (geminiConfig.getApiKey() == null || geminiConfig.getApiKey().isBlank()) {
            throw new BadRequestException("GEMINI_API_KEY nao configurada.");
        }

        try {
            String endpoint = "%s/v1beta/models/%s:generateContent?key=%s"
                    .formatted(geminiConfig.getBaseUrl(), geminiConfig.getModel(), geminiConfig.getApiKey());

            String requestBody = """
                    {
                      "contents": [{
                        "parts": [{"text": %s}]
                      }],
                      "generationConfig": {
                        "temperature": %s,
                        "maxOutputTokens": %s
                      }
                    }
                    """.formatted(
                    objectMapper.writeValueAsString(prompt),
                    geminiConfig.getTemperature(),
                    geminiConfig.getMaxOutputTokens()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(geminiConfig.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if (root.has("error")) {
                String errorMessage = root.path("error").path("message").asText("Falha no provedor de IA.");
                throw new ServiceUnavailableException(errorMessage);
            }

            String rawText = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText("")
                    .trim();
            return parseStructuredOutput(rawText);
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceUnavailableException("Erro ao consultar Gemini: " + e.getMessage());
        }
    }

    private InsightEstruturado parseStructuredOutput(String rawText) {
        try {
            String jsonPayload = rawText;
            int start = rawText.indexOf('{');
            int end = rawText.lastIndexOf('}');
            if (start >= 0 && end > start) {
                jsonPayload = rawText.substring(start, end + 1);
            }

            JsonNode parsed = objectMapper.readTree(jsonPayload);
            String resumo = parsed.path("resumo").asText("").trim();
            List<String> bullets = readStringArray(parsed.path("bullets"));
            List<String> riscos = readStringArray(parsed.path("riscos"));
            List<String> oportunidades = readStringArray(parsed.path("oportunidades"));
            int scoreConfianca = parsed.path("scoreConfianca").asInt(75);

            if (resumo.isBlank()) {
                resumo = rawText;
            }
            return new InsightEstruturado(resumo, bullets, riscos, oportunidades, scoreConfianca, geminiConfig.getModel());
        } catch (Exception ex) {
            return new InsightEstruturado(
                    rawText,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    70,
                    geminiConfig.getModel()
            );
        }
    }

    private List<String> readStringArray(JsonNode node) {
        if (!node.isArray()) {
            return Collections.emptyList();
        }
        return java.util.stream.StreamSupport.stream(node.spliterator(), false)
                .map(JsonNode::asText)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    public record InsightEstruturado(
            String resumo,
            List<String> bullets,
            List<String> riscos,
            List<String> oportunidades,
            Integer scoreConfianca,
            String modeloIA
    ) {
    }
}

package softwave.api_finance_ia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import softwave.api_finance_ia.config.OpenRouterConfig;
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
public class OpenRouterService {

    private final OpenRouterConfig openRouterConfig;
    private final ObjectMapper objectMapper;

    public OpenRouterService(OpenRouterConfig openRouterConfig, ObjectMapper objectMapper) {
        this.openRouterConfig = openRouterConfig;
        this.objectMapper = objectMapper;
    }

    public InsightEstruturado gerarInsightEstruturado(String prompt) {
        if (openRouterConfig.getApiKey() == null || openRouterConfig.getApiKey().isBlank()) {
            throw new BadRequestException("OPENROUTER_API_KEY nao configurada.");
        }

        try {
            String endpoint = "%s/chat/completions".formatted(openRouterConfig.getBaseUrl());

            // -----------------------------------------------------------------
            // CORREÇÃO PRINCIPAL:
            // O prompt completo (construído pelo PromptBuilderService) vai no
            // role "system" — que tem maior peso de instrução no modelo.
            // O role "user" é apenas o gatilho de execução, sem conteúdo extra
            // que possa conflitar com as regras do system prompt.
            //
            // ANTES (errado): system genérico + user com o prompt completo
            //   → o system genérico sobrescrevia as regras de formato,
            //     causando retorno com chaves erradas ou markdown indevido.
            //
            // AGORA (correto): system com o prompt completo + user como gatilho
            //   → o modelo respeita todas as regras de JSON, chaves exatas
            //     e proibições definidas no PromptBuilderService.
            // -----------------------------------------------------------------
            String requestBody = """
                    {
                      "model": %s,
                      "messages": [
                        {
                          "role": "system",
                          "content": %s
                        },
                        {
                          "role": "user",
                          "content": "Gere o insight financeiro conforme as instrucoes."
                        }
                      ],
                      "temperature": %s,
                      "max_tokens": %s
                    }
                    """.formatted(
                    objectMapper.writeValueAsString(openRouterConfig.getModel()),
                    objectMapper.writeValueAsString(prompt),
                    openRouterConfig.getTemperature(),
                    openRouterConfig.getMaxOutputTokens()
            );

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(openRouterConfig.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openRouterConfig.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            if (openRouterConfig.getAppUrl() != null && !openRouterConfig.getAppUrl().isBlank()) {
                requestBuilder.header("HTTP-Referer", openRouterConfig.getAppUrl());
            }
            if (openRouterConfig.getAppName() != null && !openRouterConfig.getAppName().isBlank()) {
                requestBuilder.header("X-Title", openRouterConfig.getAppName());
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonNode root = objectMapper.readTree(response.body());

            if (response.statusCode() >= 400 || root.has("error")) {
                String errorMessage = root.path("error").path("message")
                        .asText("Falha no OpenRouter.");
                throw new ServiceUnavailableException(errorMessage);
            }

            String rawText = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText("")
                    .trim();

            return parseStructuredOutput(rawText);

        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceUnavailableException("Erro ao consultar OpenRouter: " + e.getMessage());
        }
    }

    private InsightEstruturado parseStructuredOutput(String rawText) {
        try {
            // Extrai o bloco JSON mesmo que venha com markdown ```json ... ```
            String jsonPayload = rawText;
            int start = rawText.indexOf('{');
            int end   = rawText.lastIndexOf('}');
            if (start >= 0 && end > start) {
                jsonPayload = rawText.substring(start, end + 1);
            }

            JsonNode parsed = objectMapper.readTree(jsonPayload);

            String resumo       = parsed.path("resumo").asText("").trim();
            List<String> bullets      = readStringArray(parsed.path("bullets"));
            List<String> riscos       = readStringArray(parsed.path("riscos"));
            List<String> oportunidades = readStringArray(parsed.path("oportunidades"));
            int scoreConfianca  = parsed.path("scoreConfianca").asInt(75);

            // Se o resumo vier vazio (parse falhou), usa o texto bruto
            if (resumo.isBlank()) {
                resumo = rawText;
            }

            return new InsightEstruturado(
                    resumo, bullets, riscos, oportunidades,
                    scoreConfianca, openRouterConfig.getModel()
            );

        } catch (Exception ex) {
            // Fallback: devolve o texto cru no resumo para não perder a resposta
            return new InsightEstruturado(
                    rawText,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    70,
                    openRouterConfig.getModel()
            );
        }
    }

    private List<String> readStringArray(JsonNode node) {
        if (!node.isArray()) {
            return Collections.emptyList();
        }
        return java.util.stream.StreamSupport
                .stream(node.spliterator(), false)
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
    ) {}
}
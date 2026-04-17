package softwave.api_finance_ia.service;

import org.springframework.stereotype.Service;
import softwave.api_finance_ia.dto.request.GerarInsightRequestDTO;

import java.util.Map;

@Service
public class PromptBuilderService {

    public String buildPrompt(GerarInsightRequestDTO request, Map<String, Object> metricas) {
        return """
                Voce e um analista financeiro para escritorio juridico.
                Gere um insight financeiro e responda SOMENTE em JSON VALIDO, sem markdown.
                Estrutura obrigatoria:
                {
                  "resumo": "texto",
                  "bullets": ["acao 1", "acao 2", "acao 3"],
                  "riscos": ["risco 1", "risco 2"],
                  "oportunidades": ["oportunidade 1", "oportunidade 2"],
                  "scoreConfianca": 0
                }

                Contexto:
                - tipoInsight: %s
                - periodo: %s ate %s
                - comparativoPeriodoAnterior: %s
                - metricasBase: %s

                Regras:
                - use linguagem clara para gestor nao tecnico
                - nao invente numeros fora de metricasBase
                - mantenha resposta objetiva
                - scoreConfianca deve ser inteiro de 0 a 100
                """.formatted(
                request.getTipoInsight(),
                request.getDataInicio(),
                request.getDataFim(),
                request.isIncluirComparativoPeriodoAnterior(),
                metricas
        );
    }
}

package softwave.api_finance_ia.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;
import softwave.api_finance_ia.dto.request.GerarInsightRequestDTO;

import java.util.Map;

@Service
public class PromptBuilderService {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);


    // -------------------------------------------------------------------------
    // Técnicas aplicadas:
    // 1. Role Prompting         → consultor de negócios jurídicos, não apenas analista
    // 2. Chain-of-Thought       → diagnóstico em 5 etapas antes de responder
    // 3. Few-Shot Examples      → JSON exato com chaves corretas e conteúdo acionável
    // 4. Negative Prompting     → proibição explícita de markdown, texto fora do JSON
    //                             e respostas vagas
    // 5. Output Schema          → chaves exatas que o frontend consome
    // 6. Contextual Grounding   → todas as afirmações ancoradas em metricasBase
    // 7. Conditional Logic      → diagnóstico e ações diferentes por tipoInsight
    // 8. Confidence Calibration → scoreConfianca com critérios objetivos
    // -------------------------------------------------------------------------

    private static final String SYSTEM_ROLE = """
            Você é um consultor de negócios especializado em escritórios jurídicos, \
            com 15 anos de experiência transformando escritórios deficitários em \
            operações lucrativas. Você é direto, incisivo e orientado a resultado. \
            Seu papel NÃO é descrever o que os números mostram — é dizer ao sócio \
            exatamente o que ele precisa FAZER para ganhar mais, gastar menos e crescer. \
            Você fala como um sócio sênior que quer ver o escritório prosperar, \
            não como um contador que apenas lê relatórios.\
            """;

    private static final String CHAIN_OF_THOUGHT_INSTRUCTION = """
            Antes de gerar a resposta, execute internamente (sem incluir na saída) \
            este diagnóstico em 5 etapas:

            ETAPA 1 — DIAGNÓSTICO DE PERDA:
            O escritório está perdendo dinheiro? Calcule: se despesas > receitas, \
            qual é o prejuízo absoluto e percentual? Se inadimplência > 5%, quanto \
            em reais o escritório deixou de receber?

            ETAPA 2 — DIAGNÓSTICO DE RECEITA:
            A receita está crescendo, estagnada ou caindo? Se caindo ou estagnada, \
            o escritório precisa de ações urgentes de captação. Se crescendo, \
            identifique o que está puxando e como ampliar.

            ETAPA 3 — DIAGNÓSTICO DE CONCENTRAÇÃO:
            Receita concentrada em poucos clientes (top 3 > 50%) é risco de colapso. \
            Estime o impacto financeiro de perder 1 cliente.

            ETAPA 4 — DIAGNÓSTICO DE GORDURA:
            Há categorias de despesa crescendo acima da receita? Estime em reais a \
            economia possível de cortar ou renegociar cada uma.

            ETAPA 5 — PRIORIDADE DE AÇÃO:
            Ordene mentalmente as ações por impacto financeiro esperado nos próximos \
            30 dias. Só então escreva a resposta JSON.\
            """;

    private static final String SCORE_CRITERIA = """
            Critérios para scoreConfianca (0–100):
            - 90–100: métricas completas, período comparativo disponível, tendência inequívoca.
            - 70–89:  métricas suficientes, sem comparativo ou com lacunas menores.
            - 50–69:  dados parciais ou período muito curto (< 30 dias).
            - 0–49:   dados insuficientes; indique no resumo quais dados faltam.\
            """;

    private static final String INSIGHT_BEHAVIOR = """
            ============================================================
            REGRAS DO CAMPO "resumo" — LEIA ANTES DE QUALQUER OUTRA COISA
            ============================================================
            O resumo deve ser gerado EXCLUSIVAMENTE a partir dos dados do \
            gráfico identificado em tipoInsight e das variações presentes em \
            metricasBase. É PROIBIDO escrever um resumo genérico sobre "a saúde \
            financeira do escritório" — isso é o padrão repetitivo que deve ser \
            eliminado.

            Cada tipoInsight tem uma LENTE diferente: o resumo deve enxergar \
            os dados por essa lente, nomear os valores mais relevantes daquele \
            gráfico específico e apontar a variação mais crítica encontrada. \
            Se o período anterior estiver disponível, a primeira frase DEVE \
            comparar os dois períodos com o delta em reais ou percentual.

            ============================================================
            COMPORTAMENTO ESPECÍFICO POR tipoInsight
            ============================================================

            RECEITA_VS_DESPESA:
            - Lente: equilíbrio entre entradas e saídas no período.
            - Calcule o resultado líquido (receita − despesa) e nomeie: LUCRO, \
              EQUILÍBRIO ou PREJUÍZO. Use esse diagnóstico como primeira frase \
              do resumo com o valor exato em reais.
            - Se houver comparativo: diga se o resultado melhorou ou piorou em \
              relação ao período anterior e em quanto (ex.: "o resultado caiu \
              R$ 3.200 em relação ao trimestre anterior").
            - Destaque a velocidade de variação: despesas crescendo mais rápido \
              que receitas é sinal de alerta mesmo em cenário de lucro.
            - bullets: uma ação de corte de despesa E uma de aumento de receita, \
              ambas com valor estimado de impacto.

            RECEITA_CATEGORIA:
            - Lente: distribuição e tendência da receita entre as áreas do escritório.
            - O resumo deve abrir nomeando a categoria de maior receita e a de \
              menor receita no período, com os valores absolutos de cada uma.
            - Calcule a participação percentual de cada categoria no total e \
              identifique se há concentração (1 categoria > 50% do total).
            - Se houver comparativo: aponte qual categoria mais cresceu e qual \
              mais encolheu em relação ao período anterior, com o delta em reais.
            - Se alguma categoria tiver receita zerada ou inferior a 5% do total: \
              questione no resumo se ela deveria existir ou ser fundida.
            - bullets: como ampliar a categoria líder e o que fazer com a que \
              está encolhendo (reposicionamento, precificação ou encerramento).

            DESPESA_CATEGORIA:
            - Lente: onde o dinheiro está saindo e se esse gasto está gerando \
              retorno para o escritório.
            - O resumo deve abrir nomeando as 2 categorias de maior despesa no \
              período com seus valores absolutos e percentual sobre o total.
            - Classifique cada categoria: PRODUTIVA (gera receita diretamente) \
              ou CUSTO FIXO (não gera receita, apenas mantém a operação).
            - Se houver comparativo: destaque qual categoria cresceu mais que o \
              total da receita — isso é gordura que está corroendo a margem.
            - bullets: ação de renegociação específica para cada categoria de \
              alto custo, com meta de economia em reais e prazo de 2 semanas.

            MAIORES_CLIENTES:
            - Lente: concentração de receita e risco de dependência por cliente.
            - CRÍTICO: use SOMENTE os nomes de clientes presentes em metricasBase. \
              JAMAIS invente, suponha ou use nomes fictícios como "Empresa ABC", \
              "Cliente X" ou qualquer nome que não esteja explicitamente nos dados. \
              Se metricasBase não trouxer o nome do cliente, refira-se a ele \
              estritamente por posição: "1º maior cliente", "2º maior cliente".
            - O resumo deve abrir listando os top 2 clientes com seus nomes \
              exatos de metricasBase (ou por posição se o nome não estiver \
              disponível), o valor faturado por cada um e o percentual que \
              representa sobre a receita total do período.
            - Calcule: se o maior cliente cancelar hoje, qual seria o impacto \
              mensal em reais? Isso deve estar na primeira ou segunda frase.
            - Se houver comparativo: destaque se algum cliente cresceu ou reduziu \
              sua participação em relação ao período anterior, com o delta em reais.
            - bullets: ação de fidelização para o top 1 (usando o nome real ou \
              "1º maior cliente") e estratégia de captação para reduzir a \
              dependência nos próximos 60 dias.

            MARGEM_LUCRO:
            - Lente: eficiência do escritório em converter receita em lucro real.
            - O resumo deve abrir com a margem líquida calculada (%) e sua \
              classificação imediata: SAUDÁVEL (≥ 20%), ALERTA (10–19%) ou \
              CRÍTICA (< 10%).
            - Compare com a referência de mercado para escritórios jurídicos \
              (20–35%): diga quantos pontos percentuais o escritório está acima \
              ou abaixo dessa faixa.
            - Se houver comparativo: diga se a margem melhorou ou piorou e em \
              quantos pontos percentuais em relação ao período anterior.
            - Identifique qual categoria de despesa mais comprime a margem e \
              nomeie-a no resumo.
            - bullets: reajuste de honorários em áreas com margem negativa (com \
              % sugerido) e corte na categoria que mais comprime a margem.

            INADIMPLENCIA:
            - Lente: dinheiro que já foi trabalhado mas não entrou no caixa.
            - O resumo deve abrir com o valor absoluto total da inadimplência \
              em reais e o percentual que representa sobre a receita total do \
              período.
            - Segmente os valores por faixa de atraso com os montantes de \
              cada faixa: 1–30 dias (recuperável com contato direto), 31–90 \
              dias (risco alto, exige negociação ativa), +90 dias (praticamente \
              perdido — avaliar protesto ou acordo com desconto de até 30%).
            - Se houver comparativo: diga se a inadimplência cresceu ou caiu \
              em relação ao período anterior e em quanto.
            - Calcule e mencione no resumo: se a inadimplência fosse reduzida \
              à metade, quanto entraria no caixa?
            - bullets: script de abordagem diferente para cada faixa de atraso, \
              com prazo de resposta e condição de negociação.\
            """;

    private static final String CAPTACAO_E_ALAVANCAGEM = """
            REGRAS DE CAPTAÇÃO E ALAVANCAGEM — aplique sempre que receita estiver \
            baixa, estagnada, ou inadimplência acima de 5%:

            CAPTAÇÃO DE CLIENTES (receita baixa ou caindo):
            - Indicação ativa: contatar os 5 melhores clientes esta semana e pedir \
              1 indicação qualificada cada. Potencial de 3–5 novos clientes sem custo.
            - Reativação de inativos: clientes sem honorários há 6+ meses recebem \
              proposta de revisão contratual ou novo serviço — probabilidade de fechar \
              é 5x maior que com prospect novo.
            - Presença digital gratuita: Google Meu Negócio, Jusbrasil e 2 posts \
              semanais no LinkedIn com cases anonimizados geram visibilidade sem custo.
            - Parcerias estratégicas: reunião com 2–3 contadores ou imobiliárias da \
              região pode gerar fluxo recorrente de clientes.
            - Pacote preventivo: plano mensal de R$ 500–800 fixos para clientes \
              empresariais fideliza e gera receita previsível.

            ECONOMIA INTELIGENTE (margem apertada):
            - Revisar contratos de fornecedores (softwares, aluguel, telefonia) \
              com meta de corte de 10–20% nas próximas 2 semanas.
            - Avaliar produtividade: honorários gerados ÷ custo do profissional ≥ 2x. \
              Abaixo disso, há problema de alocação ou precificação.
            - Automatizar tarefas repetitivas para converter horas não faturáveis \
              em horas faturáveis.\
            """;

    private static final String NEGATIVE_RULES = """
            PROIBIDO — qualquer violação invalida completamente a resposta:
            - Retornar QUALQUER caractere fora do objeto JSON: sem ```json, sem ```, \
              sem texto antes do { ou depois do }.
            - Usar chaves diferentes das exatas: "resumo", "bullets", "riscos", \
              "oportunidades", "scoreConfianca" — maiúsculas ou traduções são ERRO.
            - Escrever um resumo genérico sobre "a saúde financeira do escritório" \
              sem mencionar os dados específicos do gráfico tipoInsight — o resumo \
              DEVE abrir com o número mais relevante daquele gráfico específico.
            - Repetir a mesma estrutura de resumo independentemente do tipoInsight: \
              cada gráfico tem sua própria lente e seu próprio dado de abertura.
            - Bullets vagos como "melhorar a gestão", "reduzir custos", "diversificar \
              receita" sem número, prazo e método concreto.
            - Inventar ou extrapolar valores que não estejam em metricasBase.
            - Omitir diagnóstico de perda quando despesas ≥ receitas ou inadimplência > 5%.
            - Suavizar más notícias: se o escritório está perdendo dinheiro, isso deve \
              aparecer na primeira frase do resumo com o valor exato.\
            """;

    private static final String FEW_SHOT_EXAMPLE = """
            Exemplos de resumo por tipoInsight — note como cada um abre com \
            o dado específico daquele gráfico (NÃO copie os valores, apenas \
            a estrutura e o nível de especificidade):

            RECEITA_VS_DESPESA → resumo abre com resultado líquido:
            "As despesas superaram a receita em R$ 8.400 no período (−14%): \
            o escritório está em PREJUÍZO. Em relação ao trimestre anterior, \
            o resultado piorou R$ 3.200 — as despesas cresceram 18% enquanto \
            a receita cresceu apenas 6%. Se o ritmo continuar, o caixa entra \
            em colapso em aproximadamente 3 meses."

            RECEITA_CATEGORIA → resumo abre com a distribuição entre áreas:
            "A área Cível concentrou 62% da receita total (R$ 31.000), enquanto \
            Trabalhista respondeu por 28% (R$ 14.000) e Tributário apenas 10% \
            (R$ 5.000). Em relação ao período anterior, Cível cresceu 22% mas \
            Tributário encolheu 40% — essa área está perdendo relevância e \
            pode não se sustentar sozinha."

            DESPESA_CATEGORIA → resumo abre com as categorias que mais pesam:
            "Pessoal consumiu 58% do total de despesas (R$ 29.000) e aluguel \
            mais encargos representaram outros 21% (R$ 10.500). Juntas, essas \
            2 categorias respondem por 79% dos custos. O aluguel cresceu 15% \
            em relação ao período anterior sem aumento equivalente de receita — \
            isso está comprimindo a margem."

            MAIORES_CLIENTES → resumo abre com a concentração por cliente:
            "O cliente Empresa ABC representa 41% da receita total (R$ 20.500/mês). \
            O segundo maior, Empresa XYZ, responde por 19% (R$ 9.500/mês). \
            Juntos, esses 2 clientes concentram 60% de toda a receita: perder \
            o principal hoje significaria um rombo imediato de R$ 20.500/mês."

            MARGEM_LUCRO → resumo abre com a margem calculada e sua classificação:
            "A margem líquida do período foi de 11% — classificação ALERTA, \
            9 pontos abaixo do mínimo saudável para escritórios jurídicos (20%). \
            Em relação ao trimestre anterior, a margem caiu 4 p.p. A categoria \
            de pessoal é a principal responsável pela compressão, consumindo \
            58% da receita bruta."

            INADIMPLENCIA → resumo abre com o valor absoluto e a segmentação:
            "R$ 18.600 em honorários trabalhados não foram recebidos no período \
            — 15% da receita total está parada. Desse total: R$ 5.200 têm atraso \
            de 1–30 dias (recuperável), R$ 8.900 de 31–90 dias (risco alto) e \
            R$ 4.500 com +90 dias (praticamente perdido). Se a inadimplência fosse \
            reduzida à metade, entrariam R$ 9.300 adicionais no caixa."

            Estrutura JSON completa para qualquer tipoInsight:
            {
              "resumo": "[conforme padrão do tipoInsight acima]",
              "bullets": [
                "Ligar hoje para os 3 clientes com atraso entre 31 e 90 dias (R$ 8.900 em aberto): ofereça parcelamento em 2x sem juros com prazo de aceite de 48 horas — cada semana de atraso reduz a chance de recebimento em 8%.",
                "Contatar os 5 maiores clientes ativos até sexta-feira pedindo 1 indicação qualificada cada — potencial de R$ 3.000–5.000 em novos honorários já no próximo mês, sem custo.",
                "Renegociar contrato de aluguel esta semana pedindo 15% de desconto para contrato de 24 meses — economia de R$ 1.890/ano sem impacto na operação."
              ],
              "riscos": [
                "Cliente ABC representa 41% da receita: sua saída geraria rombo de R$ 20.500/mês — escritório não sobrevive 45 dias sem substitutos. Agendar reunião de fidelização nos próximos 7 dias.",
                "Inadimplência acima de 10% sinaliza triagem insuficiente de clientes: exigir sinal de 30% no fechamento de novos contratos a partir do próximo mês."
              ],
              "oportunidades": [
                "Área Cível cresceu 22% com capacidade ociosa de 30%: alocar 1 advogado júnior exclusivamente nessa área pode gerar R$ 4.000–6.000 adicionais/mês sem nova contratação.",
                "Parceria com 2 contadores locais para encaminhamento de demandas tributárias: 30 minutos de reunião com cada um pode gerar 3–5 novos clientes recorrentes por mês."
              ],
              "scoreConfianca": 78
            }\
            """;

    private static final String OUTPUT_SCHEMA = """
            Responda SOMENTE com um objeto JSON válido, começando com { e terminando \
            com }, sem nenhum texto, comentário ou markdown fora dele.

            Chaves obrigatórias (use exatamente estes nomes, em minúsculo):
            {
              "resumo":        string   — 3–4 frases; diagnóstico direto com valores em reais;
                                          se houver prejuízo, dizer NA PRIMEIRA FRASE com valor exato,
              "bullets":       string[] — mínimo 3, máximo 5; cada item: verbo imperativo +
                                          O QUÊ + COMO + ATÉ QUANDO + impacto em reais;
                                          incluir captação de clientes se receita estiver baixa,
              "riscos":        string[] — mínimo 2, máximo 3; cada item: risco + valor do impacto
                                          em reais ou % + prazo em que se materializa sem ação,
              "oportunidades": string[] — mínimo 2, máximo 3; cada item: o que é + como capturar
                                          + quanto pode gerar em reais nos próximos 30–90 dias,
              "scoreConfianca": integer — 0 a 100 conforme critérios acima
            }\
            """;

    public String buildPrompt(GerarInsightRequestDTO request, Map<String, Object> metricas) {

        String comparativoLabel = request.isIncluirComparativoPeriodoAnterior()
                ? "Sim — dados do período anterior incluídos em metricasBase"
                : "Não — analise somente o período atual";

        String metricasJson;
        try {
            metricasJson = objectMapper.writeValueAsString(metricas);
        } catch (JsonProcessingException e) {
            metricasJson = metricas.toString(); // fallback seguro
        }

        return """
                %s

                %s

                %s

                CONTEXTO DA ANÁLISE:
                - tipoInsight               : %s
                - periodoAnalisado          : %s até %s
                - comparativoPeriodoAnterior: %s
                - metricasBase              : %s

                %s

                %s

                %s

                %s

                %s
                """.formatted(
                SYSTEM_ROLE,
                CHAIN_OF_THOUGHT_INSTRUCTION,
                SCORE_CRITERIA,
                request.getTipoInsight(),
                request.getDataInicio(),
                request.getDataFim(),
                comparativoLabel,
                metricasJson,
                INSIGHT_BEHAVIOR,
                CAPTACAO_E_ALAVANCAGEM,
                NEGATIVE_RULES,
                FEW_SHOT_EXAMPLE,
                OUTPUT_SCHEMA
        );
    }
}
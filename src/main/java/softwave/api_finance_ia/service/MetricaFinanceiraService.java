package softwave.api_finance_ia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import softwave.api_finance_ia.dto.integracao.ClienteRankingReceitaResponse;
import softwave.api_finance_ia.dto.integracao.CobrancaResumoResponse;
import softwave.api_finance_ia.dto.integracao.TransacaoResumoResponse;
import softwave.api_finance_ia.dto.request.GerarInsightRequestDTO;
import softwave.api_finance_ia.dto.response.KpiResumoDTO;
import softwave.api_finance_ia.feign.ClientesClient;
import softwave.api_finance_ia.feign.CobrancasClient;
import softwave.api_finance_ia.feign.TransacoesClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MetricaFinanceiraService {

    private static final Logger log = LoggerFactory.getLogger(MetricaFinanceiraService.class);

    private final TransacoesClient transacoesClient;
    private final CobrancasClient cobrancasClient;
    private final ClientesClient clientesClient;

    @Value("${features.usar-metricas-mock:true}")
    private boolean usarMetricasMock;

    @Value("${features.fallback-metricas-mock-em-falha:true}")
    private boolean fallbackMetricasMockEmFalha;

    public MetricaFinanceiraService(
            TransacoesClient transacoesClient,
            CobrancasClient cobrancasClient,
            ClientesClient clientesClient
    ) {
        this.transacoesClient = transacoesClient;
        this.cobrancasClient = cobrancasClient;
        this.clientesClient = clientesClient;
    }

    public Map<String, Object> obterMetricasBase(GerarInsightRequestDTO request) {
        if (usarMetricasMock) {
            return metricasMock();
        }
        try {
            TransacaoResumoResponse transacoes = transacoesClient.resumo(
                    request.getTenantId(), request.getDataInicio(), request.getDataFim());
            CobrancaResumoResponse cobrancas = cobrancasClient.resumo(
                    request.getTenantId(), request.getDataInicio(), request.getDataFim());
            ClienteRankingReceitaResponse ranking = clientesClient.rankingReceita(
                    request.getTenantId(), request.getDataInicio(), request.getDataFim(), 10);
            return mergeMetricas(transacoes, cobrancas, ranking);
        } catch (Exception ex) {
            log.warn("Falha ao obter metricas via microsservicos (tenantId={}): {}", request.getTenantId(), ex.getMessage());
            if (fallbackMetricasMockEmFalha) {
                return metricasMock();
            }
            throw ex;
        }
    }

    public KpiResumoDTO obterResumoKpis(Long tenantId) {
        if (usarMetricasMock) {
            return new KpiResumoDTO(85400.0, 42900.0, 49.8, 12.0);
        }
        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.minusMonths(3);
        try {
            TransacaoResumoResponse transacoes = transacoesClient.resumo(tenantId, inicio, fim);
            CobrancaResumoResponse cobrancas = cobrancasClient.resumo(tenantId, inicio, fim);
            double receita = nz(transacoes.getReceitaTotal());
            double despesa = nz(transacoes.getDespesaTotal());
            double margem = receita > 0 ? ((receita - despesa) / receita) * 100.0 : 0.0;
            double inadimplencia = nz(cobrancas.getInadimplenciaPercentual());
            return new KpiResumoDTO(receita, despesa, margem, inadimplencia);
        } catch (Exception ex) {
            log.warn("Falha ao obter KPIs via microsservicos (tenantId={}): {}", tenantId, ex.getMessage());
            if (fallbackMetricasMockEmFalha) {
                return new KpiResumoDTO(85400.0, 42900.0, 49.8, 12.0);
            }
            throw ex;
        }
    }

    private static Map<String, Object> mergeMetricas(
            TransacaoResumoResponse t,
            CobrancaResumoResponse c,
            ClienteRankingReceitaResponse r
    ) {
        Map<String, Object> metricas = new LinkedHashMap<>();
        metricas.put("receitaTotal", nz(t.getReceitaTotal()));
        metricas.put("despesaTotal", nz(t.getDespesaTotal()));
        metricas.put("ticketMedio", nz(t.getTicketMedio()));
        metricas.put("quantidadeTransacoes", t.getQuantidadeTransacoes() != null ? t.getQuantidadeTransacoes() : 0L);
        metricas.put("receitaPorCategoria", t.getReceitaPorCategoria() != null ? t.getReceitaPorCategoria() : Map.of());
        metricas.put("despesaPorCategoria", t.getDespesaPorCategoria() != null ? t.getDespesaPorCategoria() : Map.of());
        metricas.put("inadimplenciaPercentual", nz(c.getInadimplenciaPercentual()));
        metricas.put("valorRecebidoCobrancas", nz(c.getValorRecebido()));
        metricas.put("valorVencidoCobrancas", nz(c.getValorVencido()));
        metricas.put("valorAVencerCobrancas", nz(c.getValorAVencer()));
        metricas.put("quantidadeTitulosAbertos", c.getQuantidadeTitulosAbertos() != null ? c.getQuantidadeTitulosAbertos() : 0L);
        metricas.put("rankingClientesReceita", r.getItens() != null ? r.getItens() : java.util.List.of());
        double receita = nz(t.getReceitaTotal());
        double despesa = nz(t.getDespesaTotal());
        metricas.put("margemLucroPercentual", receita > 0 ? ((receita - despesa) / receita) * 100.0 : 0.0);
        return metricas;
    }

    private static Map<String, Object> metricasMock() {
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("receitaTotal", 85400.0);
        metricas.put("despesaTotal", 42900.0);
        metricas.put("margemLucroPercentual", 49.8);
        metricas.put("inadimplenciaPercentual", 12.0);
        metricas.put("ticketMedio", 8540.0);
        metricas.put("fonte", "MOCK");
        return metricas;
    }

    private static double nz(Double v) {
        return v != null ? v : 0.0;
    }
}

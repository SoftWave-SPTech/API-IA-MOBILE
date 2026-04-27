package softwave.api_finance_ia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import softwave.api_finance_ia.dto.request.GerarInsightRequestDTO;
import softwave.api_finance_ia.dto.response.InsightFinanceiroResponseDTO;
import softwave.api_finance_ia.dto.response.KpiResumoDTO;
import softwave.api_finance_ia.entity.EnumTipoInsight;
import softwave.api_finance_ia.entity.InsightFinanceiro;
import softwave.api_finance_ia.exception.BadRequestException;
import softwave.api_finance_ia.exception.EntidadeNaoEncontradaException;
import softwave.api_finance_ia.repository.InsightFinanceiroRepository;

import java.util.Map;

@Service
public class InsightFinanceiroService {

    private final InsightFinanceiroRepository repository;
    private final MetricaFinanceiraService metricaFinanceiraService;
    private final PromptBuilderService promptBuilderService;
    private final OpenRouterService openRouterService;
    private final ObjectMapper objectMapper;
    private final TenantResolverService tenantResolverService;

    public InsightFinanceiroService(
            InsightFinanceiroRepository repository,
            MetricaFinanceiraService metricaFinanceiraService,
            PromptBuilderService promptBuilderService,
            OpenRouterService openRouterService,
            ObjectMapper objectMapper,
            TenantResolverService tenantResolverService
    ) {
        this.repository = repository;
        this.metricaFinanceiraService = metricaFinanceiraService;
        this.promptBuilderService = promptBuilderService;
        this.openRouterService = openRouterService;
        this.objectMapper = objectMapper;
        this.tenantResolverService = tenantResolverService;
    }

    public InsightFinanceiroResponseDTO gerar(GerarInsightRequestDTO request) {
        return gerar(request, null);
    }

    public InsightFinanceiroResponseDTO gerar(GerarInsightRequestDTO request, Long userId) {
        validarPeriodo(request);
        Long resolvedTenantId = tenantResolverService.resolveTenantId(userId, request.getTenantId());
        if (resolvedTenantId == null) {
            throw new BadRequestException("Nao foi possivel resolver tenantId para gerar o insight.");
        }
        request.setTenantId(resolvedTenantId);
        Map<String, Object> metricas = metricaFinanceiraService.obterMetricasBase(request);
        String prompt = promptBuilderService.buildPrompt(request, metricas);
        OpenRouterService.InsightEstruturado resposta = openRouterService.gerarInsightEstruturado(prompt);

        InsightFinanceiro insight = new InsightFinanceiro();
        insight.setTenantId(request.getTenantId());
        insight.setUserId(userId);
        insight.setTipoInsight(request.getTipoInsight());
        insight.setDataInicio(request.getDataInicio());
        insight.setDataFim(request.getDataFim());
        insight.setResumoIA(resposta.resumo());
        insight.setBullets(toJsonSafe(resposta.bullets()));
        insight.setRiscos(toJsonSafe(resposta.riscos()));
        insight.setOportunidades(toJsonSafe(resposta.oportunidades()));
        insight.setScoreConfianca(resposta.scoreConfianca());
        insight.setModeloIA(resposta.modeloIA());

        return InsightFinanceiroResponseDTO.fromEntity(repository.save(insight));
    }

    public Page<InsightFinanceiroResponseDTO> listar(Long tenantId, EnumTipoInsight tipoInsight, int page, int size) {
        return listar(tenantId, tipoInsight, page, size, null);
    }

    public Page<InsightFinanceiroResponseDTO> listar(Long tenantId, EnumTipoInsight tipoInsight, int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InsightFinanceiro> insights;
        if (userId != null) {
            insights = (tipoInsight == null)
                    ? repository.findByUserId(userId, pageable)
                    : repository.findByUserIdAndTipoInsight(userId, tipoInsight, pageable);
        } else {
            if (tenantId == null) {
                throw new BadRequestException("tenantId e obrigatorio quando nao houver usuario autenticado.");
            }
            insights = (tipoInsight == null)
                    ? repository.findByTenantId(tenantId, pageable)
                    : repository.findByTenantIdAndTipoInsight(tenantId, tipoInsight, pageable);
        }
        return insights.map(InsightFinanceiroResponseDTO::fromEntity);
    }

    public InsightFinanceiroResponseDTO buscarPorId(Long id) {
        InsightFinanceiro entity = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Insight nao encontrado."));
        return InsightFinanceiroResponseDTO.fromEntity(entity);
    }

    public InsightFinanceiroResponseDTO buscarUltimoPorTipo(Long tenantId, EnumTipoInsight tipoInsight) {
        return buscarUltimoPorTipo(tenantId, tipoInsight, null);
    }

    public InsightFinanceiroResponseDTO buscarUltimoPorTipo(Long tenantId, EnumTipoInsight tipoInsight, Long userId) {
        InsightFinanceiro entity;
        if (userId != null) {
            entity = repository
                    .findTopByUserIdAndTipoInsightOrderByCriadoEmDesc(userId, tipoInsight)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Nenhum insight encontrado para o tipo informado."));
        } else {
            if (tenantId == null) {
                throw new BadRequestException("tenantId e obrigatorio quando nao houver usuario autenticado.");
            }
            entity = repository
                    .findTopByTenantIdAndTipoInsightOrderByCriadoEmDesc(tenantId, tipoInsight)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Nenhum insight encontrado para o tipo informado."));
        }
        return InsightFinanceiroResponseDTO.fromEntity(entity);
    }

    public KpiResumoDTO obterResumoKpis(Long tenantId) {
        return metricaFinanceiraService.obterResumoKpis(tenantId);
    }

    public KpiResumoDTO obterResumoKpis(Long tenantId, Long userId) {
        Long resolvedTenantId = tenantResolverService.resolveTenantId(userId, tenantId);
        if (resolvedTenantId == null) {
            throw new BadRequestException("Nao foi possivel resolver tenantId para obter resumo de KPIs.");
        }
        return metricaFinanceiraService.obterResumoKpis(resolvedTenantId);
    }

    private void validarPeriodo(GerarInsightRequestDTO request) {
        if (request.getDataInicio().isAfter(request.getDataFim())) {
            throw new BadRequestException("dataInicio nao pode ser maior que dataFim.");
        }
    }

    private String toJsonSafe(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "[]";
        }
    }
}

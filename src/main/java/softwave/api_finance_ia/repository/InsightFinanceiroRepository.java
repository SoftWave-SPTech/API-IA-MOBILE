package softwave.api_finance_ia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import softwave.api_finance_ia.entity.EnumTipoInsight;
import softwave.api_finance_ia.entity.InsightFinanceiro;

import java.time.LocalDate;
import java.util.Optional;

public interface InsightFinanceiroRepository extends JpaRepository<InsightFinanceiro, Long> {
    Page<InsightFinanceiro> findByTenantId(Long tenantId, Pageable pageable);

    Page<InsightFinanceiro> findByTenantIdAndTipoInsight(Long tenantId, EnumTipoInsight tipoInsight, Pageable pageable);

    Page<InsightFinanceiro> findByTenantIdAndDataInicioGreaterThanEqualAndDataFimLessThanEqual(
            Long tenantId,
            LocalDate dataInicio,
            LocalDate dataFim,
            Pageable pageable
    );

    Optional<InsightFinanceiro> findTopByTenantIdAndTipoInsightOrderByCriadoEmDesc(Long tenantId, EnumTipoInsight tipoInsight);
}

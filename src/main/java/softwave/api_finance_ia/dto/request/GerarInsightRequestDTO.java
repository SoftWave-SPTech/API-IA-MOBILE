package softwave.api_finance_ia.dto.request;

import jakarta.validation.constraints.NotNull;
import softwave.api_finance_ia.entity.EnumTipoInsight;

import java.time.LocalDate;

public class GerarInsightRequestDTO {

    private Long tenantId;

    @NotNull
    private EnumTipoInsight tipoInsight;

    @NotNull
    private LocalDate dataInicio;

    @NotNull
    private LocalDate dataFim;

    private boolean incluirComparativoPeriodoAnterior;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public EnumTipoInsight getTipoInsight() {
        return tipoInsight;
    }

    public void setTipoInsight(EnumTipoInsight tipoInsight) {
        this.tipoInsight = tipoInsight;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public boolean isIncluirComparativoPeriodoAnterior() {
        return incluirComparativoPeriodoAnterior;
    }

    public void setIncluirComparativoPeriodoAnterior(boolean incluirComparativoPeriodoAnterior) {
        this.incluirComparativoPeriodoAnterior = incluirComparativoPeriodoAnterior;
    }
}

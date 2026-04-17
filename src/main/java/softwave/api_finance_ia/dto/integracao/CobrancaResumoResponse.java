package softwave.api_finance_ia.dto.integracao;

import java.time.LocalDate;

/**
 * Contrato: GET /internal/v1/tenants/{tenantId}/cobrancas/resumo
 * Ver docs/CONTRATO_INTEGRACAO_BACK_FINANCEIRO.md
 */
public class CobrancaResumoResponse {

    private Long tenantId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double inadimplenciaPercentual;
    private Double valorRecebido;
    private Double valorVencido;
    private Double valorAVencer;
    private Long quantidadeTitulosAbertos;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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

    public Double getInadimplenciaPercentual() {
        return inadimplenciaPercentual;
    }

    public void setInadimplenciaPercentual(Double inadimplenciaPercentual) {
        this.inadimplenciaPercentual = inadimplenciaPercentual;
    }

    public Double getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(Double valorRecebido) {
        this.valorRecebido = valorRecebido;
    }

    public Double getValorVencido() {
        return valorVencido;
    }

    public void setValorVencido(Double valorVencido) {
        this.valorVencido = valorVencido;
    }

    public Double getValorAVencer() {
        return valorAVencer;
    }

    public void setValorAVencer(Double valorAVencer) {
        this.valorAVencer = valorAVencer;
    }

    public Long getQuantidadeTitulosAbertos() {
        return quantidadeTitulosAbertos;
    }

    public void setQuantidadeTitulosAbertos(Long quantidadeTitulosAbertos) {
        this.quantidadeTitulosAbertos = quantidadeTitulosAbertos;
    }
}

package softwave.api_finance_ia.dto.integracao;

import java.time.LocalDate;
import java.util.Map;

/**
 * Contrato: GET /internal/v1/tenants/{tenantId}/transacoes/resumo
 * Ver docs/CONTRATO_INTEGRACAO_BACK_FINANCEIRO.md
 */
public class TransacaoResumoResponse {

    private Long tenantId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Double receitaTotal;
    private Double despesaTotal;
    private Double ticketMedio;
    private Long quantidadeTransacoes;
    private Map<String, Double> receitaPorCategoria;
    private Map<String, Double> despesaPorCategoria;

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

    public Double getReceitaTotal() {
        return receitaTotal;
    }

    public void setReceitaTotal(Double receitaTotal) {
        this.receitaTotal = receitaTotal;
    }

    public Double getDespesaTotal() {
        return despesaTotal;
    }

    public void setDespesaTotal(Double despesaTotal) {
        this.despesaTotal = despesaTotal;
    }

    public Double getTicketMedio() {
        return ticketMedio;
    }

    public void setTicketMedio(Double ticketMedio) {
        this.ticketMedio = ticketMedio;
    }

    public Long getQuantidadeTransacoes() {
        return quantidadeTransacoes;
    }

    public void setQuantidadeTransacoes(Long quantidadeTransacoes) {
        this.quantidadeTransacoes = quantidadeTransacoes;
    }

    public Map<String, Double> getReceitaPorCategoria() {
        return receitaPorCategoria;
    }

    public void setReceitaPorCategoria(Map<String, Double> receitaPorCategoria) {
        this.receitaPorCategoria = receitaPorCategoria;
    }

    public Map<String, Double> getDespesaPorCategoria() {
        return despesaPorCategoria;
    }

    public void setDespesaPorCategoria(Map<String, Double> despesaPorCategoria) {
        this.despesaPorCategoria = despesaPorCategoria;
    }
}

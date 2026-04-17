package softwave.api_finance_ia.dto.integracao;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato: GET /internal/v1/tenants/{tenantId}/clientes/ranking-receita
 * Ver docs/CONTRATO_INTEGRACAO_BACK_FINANCEIRO.md
 */
public class ClienteRankingReceitaResponse {

    private Long tenantId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private List<ClienteRankingItemResponse> itens;

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

    public List<ClienteRankingItemResponse> getItens() {
        return itens;
    }

    public void setItens(List<ClienteRankingItemResponse> itens) {
        this.itens = itens;
    }
}

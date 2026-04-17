package softwave.api_finance_ia.dto.integracao;

/**
 * Item do ranking de clientes por receita.
 * Ver docs/CONTRATO_INTEGRACAO_BACK_FINANCEIRO.md
 */
public class ClienteRankingItemResponse {

    private Long clienteId;
    private String nome;
    private Double valorRecebido;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(Double valorRecebido) {
        this.valorRecebido = valorRecebido;
    }
}

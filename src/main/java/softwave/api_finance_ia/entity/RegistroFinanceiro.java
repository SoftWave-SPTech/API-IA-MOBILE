package softwave.api_finance_ia.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "registro_financeiro")
public class RegistroFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer ano;

    @Column(name = "honorario_sucumbencia")
    private BigDecimal honorarioSucumbencia;

    private Integer mes;

    @Column(name = "metodo_pagamento")
    private Integer metodoPagamento;

    @Column(name = "parcela_atual")
    private Integer parcelaAtual;

    @Column(name = "status_financeiro")
    private Integer statusFinanceiro;

    @Column(name = "tipo_pagamento")
    private Integer tipoPagamento;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @Column(name = "valor_pagar")
    private BigDecimal valorPagar;

    @Column(name = "valor_pago")
    private BigDecimal valorPago;

    @Column(name = "valor_parcela")
    private BigDecimal valorParcela;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "processo_id")
    private Long processoId;

    public Long getId() {
        return id;
    }

    public Integer getAno() {
        return ano;
    }

    public Integer getMes() {
        return mes;
    }

    public Integer getStatusFinanceiro() {
        return statusFinanceiro;
    }

    public BigDecimal getValorPagar() {
        return valorPagar;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getProcessoId() {
        return processoId;
    }
}

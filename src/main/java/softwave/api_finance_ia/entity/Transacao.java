package softwave.api_finance_ia.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "honorario_id")
    private Long honorarioId;

    private String titulo;

    private BigDecimal valor;

    private String tipo;

    @Column(name = "status_financeiro")
    private String statusFinanceiro;

    @Column(name = "status_aprovacao")
    private String statusAprovacao;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Lob
    private String descricao;

    @Lob
    private String observacoes;

    private String contraparte;

    public Long getId() {
        return id;
    }

    public Long getHonorarioId() {
        return honorarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    public String getStatusFinanceiro() {
        return statusFinanceiro;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }
}

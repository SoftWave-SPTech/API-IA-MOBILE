package softwave.api_finance_ia.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "honorario")
public class Honorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processo_id")
    private Long processoId;

    private String titulo;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(length = 40)
    private String status;

    private Integer parcelas;

    public Long getId() {
        return id;
    }

    public Long getProcessoId() {
        return processoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public String getStatus() {
        return status;
    }

    public Integer getParcelas() {
        return parcelas;
    }
}

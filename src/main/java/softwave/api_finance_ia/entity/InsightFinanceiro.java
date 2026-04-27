package softwave.api_finance_ia.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "insight_financeiro")
public class InsightFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EnumTipoInsight tipoInsight;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String resumoIA;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String bullets;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String riscos;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String oportunidades;

    private Integer scoreConfianca;

    @Column(length = 80)
    private String modeloIA;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getResumoIA() {
        return resumoIA;
    }

    public void setResumoIA(String resumoIA) {
        this.resumoIA = resumoIA;
    }

    public String getBullets() {
        return bullets;
    }

    public void setBullets(String bullets) {
        this.bullets = bullets;
    }

    public String getRiscos() {
        return riscos;
    }

    public void setRiscos(String riscos) {
        this.riscos = riscos;
    }

    public String getOportunidades() {
        return oportunidades;
    }

    public void setOportunidades(String oportunidades) {
        this.oportunidades = oportunidades;
    }

    public Integer getScoreConfianca() {
        return scoreConfianca;
    }

    public void setScoreConfianca(Integer scoreConfianca) {
        this.scoreConfianca = scoreConfianca;
    }

    public String getModeloIA() {
        return modeloIA;
    }

    public void setModeloIA(String modeloIA) {
        this.modeloIA = modeloIA;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}

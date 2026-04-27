package softwave.api_finance_ia.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import softwave.api_finance_ia.entity.EnumTipoInsight;
import softwave.api_finance_ia.entity.InsightFinanceiro;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class InsightFinanceiroResponseDTO {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Long id;
    private Long tenantId;
    private Long userId;
    private EnumTipoInsight tipoInsight;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String resumoIA;
    private List<String> bullets;
    private List<String> riscos;
    private List<String> oportunidades;
    private Integer scoreConfianca;
    private String modeloIA;
    private LocalDateTime criadoEm;

    public static InsightFinanceiroResponseDTO fromEntity(InsightFinanceiro entity) {
        InsightFinanceiroResponseDTO dto = new InsightFinanceiroResponseDTO();
        dto.setId(entity.getId());
        dto.setTenantId(entity.getTenantId());
        dto.setUserId(entity.getUserId());
        dto.setTipoInsight(entity.getTipoInsight());
        dto.setDataInicio(entity.getDataInicio());
        dto.setDataFim(entity.getDataFim());
        dto.setResumoIA(entity.getResumoIA());
        dto.setBullets(readList(entity.getBullets()));
        dto.setRiscos(readList(entity.getRiscos()));
        dto.setOportunidades(readList(entity.getOportunidades()));
        dto.setScoreConfianca(entity.getScoreConfianca());
        dto.setModeloIA(entity.getModeloIA());
        dto.setCriadoEm(entity.getCriadoEm());
        return dto;
    }

    private static List<String> readList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Collections.emptyList();
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

    public List<String> getBullets() {
        return bullets;
    }

    public void setBullets(List<String> bullets) {
        this.bullets = bullets;
    }

    public List<String> getRiscos() {
        return riscos;
    }

    public void setRiscos(List<String> riscos) {
        this.riscos = riscos;
    }

    public List<String> getOportunidades() {
        return oportunidades;
    }

    public void setOportunidades(List<String> oportunidades) {
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

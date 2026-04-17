package softwave.api_finance_ia.dto.response;

public class KpiResumoDTO {
    private double receitaTotal;
    private double despesaTotal;
    private double margemLucroPercentual;
    private double inadimplenciaPercentual;

    public KpiResumoDTO() {
    }

    public KpiResumoDTO(double receitaTotal, double despesaTotal, double margemLucroPercentual, double inadimplenciaPercentual) {
        this.receitaTotal = receitaTotal;
        this.despesaTotal = despesaTotal;
        this.margemLucroPercentual = margemLucroPercentual;
        this.inadimplenciaPercentual = inadimplenciaPercentual;
    }

    public double getReceitaTotal() {
        return receitaTotal;
    }

    public void setReceitaTotal(double receitaTotal) {
        this.receitaTotal = receitaTotal;
    }

    public double getDespesaTotal() {
        return despesaTotal;
    }

    public void setDespesaTotal(double despesaTotal) {
        this.despesaTotal = despesaTotal;
    }

    public double getMargemLucroPercentual() {
        return margemLucroPercentual;
    }

    public void setMargemLucroPercentual(double margemLucroPercentual) {
        this.margemLucroPercentual = margemLucroPercentual;
    }

    public double getInadimplenciaPercentual() {
        return inadimplenciaPercentual;
    }

    public void setInadimplenciaPercentual(double inadimplenciaPercentual) {
        this.inadimplenciaPercentual = inadimplenciaPercentual;
    }
}

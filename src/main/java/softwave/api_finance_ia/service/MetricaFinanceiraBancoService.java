package softwave.api_finance_ia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.api_finance_ia.dto.integracao.ClienteRankingItemResponse;
import softwave.api_finance_ia.entity.Honorario;
import softwave.api_finance_ia.entity.RegistroFinanceiro;
import softwave.api_finance_ia.entity.Transacao;
import softwave.api_finance_ia.repository.HonorarioRepository;
import softwave.api_finance_ia.repository.RegistroFinanceiroRepository;
import softwave.api_finance_ia.repository.TransacaoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Agrega metricas a partir das tabelas {@code transacao}, {@code honorario} e {@code registro_financeiro}.
 * O {@code tenantId} da API e tratado como {@code cliente_id} em {@code registro_financeiro} quando
 * {@code features.metricas-tenant-eh-cliente-id} e true.
 */
@Service
public class MetricaFinanceiraBancoService {

    private static final double MOCK_INADIMPLENCIA_SEM_TITULOS = 12.0;

    private final TransacaoRepository transacaoRepository;
    private final HonorarioRepository honorarioRepository;
    private final RegistroFinanceiroRepository registroFinanceiroRepository;

    @Value("${features.metricas-tenant-eh-cliente-id:true}")
    private boolean metricasTenantEhClienteId;

    @Value("${features.metricas.tipo-receita-valores:1,R,RECEITA,ENTRADA,CREDITO}")
    private String tipoReceitaValores;

    @Value("${features.metricas.tipo-despesa-valores:2,S,DESPESA,SAIDA,DEBITO}")
    private String tipoDespesaValores;

    @Value("${features.metricas.status-pago-valores:1,PAGO,QUITADO,RECEBIDO}")
    private String statusPagoValores;

    public MetricaFinanceiraBancoService(
            TransacaoRepository transacaoRepository,
            HonorarioRepository honorarioRepository,
            RegistroFinanceiroRepository registroFinanceiroRepository
    ) {
        this.transacaoRepository = transacaoRepository;
        this.honorarioRepository = honorarioRepository;
        this.registroFinanceiroRepository = registroFinanceiroRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterMetricas(Long tenantId, LocalDate dataInicio, LocalDate dataFim) {
        List<RegistroFinanceiro> registrosCliente = metricasTenantEhClienteId
                ? registroFinanceiroRepository.findByClienteId(tenantId)
                : registroFinanceiroRepository.findAll();

        List<RegistroFinanceiro> registrosPeriodo = registrosCliente.stream()
                .filter(r -> r.getAno() != null && r.getMes() != null)
                .filter(r -> mesNoPeriodo(r.getAno(), r.getMes(), dataInicio, dataFim))
                .toList();

        Set<Long> processoIds = registrosCliente.stream()
                .map(RegistroFinanceiro::getProcessoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (processoIds.isEmpty()) {
            return vazias(dataInicio, dataFim);
        }

        List<Honorario> honorarios = honorarioRepository.findByProcessoIdIn(processoIds);
        Set<Long> honorarioIds = honorarios.stream().map(Honorario::getId).collect(Collectors.toSet());

        List<Transacao> transacoes = honorarioIds.isEmpty()
                ? List.of()
                : transacaoRepository.findByHonorarioIdIn(honorarioIds);

        List<Transacao> transacoesPeriodo = transacoes.stream()
                .filter(t -> dataReferencia(t).map(d -> !d.isBefore(dataInicio) && !d.isAfter(dataFim)).orElse(false))
                .toList();

        Set<String> tiposReceita = splitCsvUpper(tipoReceitaValores);
        Set<String> tiposDespesa = splitCsvUpper(tipoDespesaValores);
        Set<String> statusPagos = splitCsvUpper(statusPagoValores);

        double receitaTrans = transacoesPeriodo.stream()
                .filter(t -> isReceita(t.getTipo(), tiposReceita, tiposDespesa))
                .mapToDouble(t -> nz(t.getValor()))
                .sum();

        double despesaTrans = transacoesPeriodo.stream()
                .filter(t -> isDespesa(t.getTipo(), tiposDespesa))
                .mapToDouble(t -> nz(t.getValor()))
                .sum();

        double receitaHon = honorarios.stream()
                .filter(h -> h.getDataInicio() != null
                        && !h.getDataInicio().isBefore(dataInicio)
                        && !h.getDataInicio().isAfter(dataFim))
                .filter(h -> "PAGO".equalsIgnoreCase(nullToEmpty(h.getStatus())))
                .mapToDouble(h -> nz(h.getValorTotal()))
                .sum();

        double receitaTotal = receitaTrans > 0.01 ? receitaTrans : receitaHon;
        double despesaTotal = despesaTrans;

        long qtdTransacoes = transacoesPeriodo.size();
        double ticketMedio = qtdTransacoes > 0 ? receitaTotal / qtdTransacoes : 0.0;

        Map<String, Double> receitaPorCategoria = categorizar(
                transacoesPeriodo.stream().filter(t -> isReceita(t.getTipo(), tiposReceita, tiposDespesa)));
        Map<String, Double> despesaPorCategoria = categorizar(
                transacoesPeriodo.stream().filter(t -> isDespesa(t.getTipo(), tiposDespesa)));

        LocalDate hoje = LocalDate.now();
        List<Transacao> titulosReferencia = transacoes.stream()
                .filter(t -> t.getDataVencimento() != null)
                .toList();
        long titulosVencidosNaoPagos = titulosReferencia.stream()
                .filter(t -> naoPago(t, statusPagos) && t.getDataVencimento().isBefore(hoje))
                .count();
        long titulosComVencimento = titulosReferencia.size();
        double inadimplenciaPercentual = titulosComVencimento > 0
                ? (100.0 * titulosVencidosNaoPagos) / titulosComVencimento
                : MOCK_INADIMPLENCIA_SEM_TITULOS;

        double valorRecebidoCobrancas = registrosPeriodo.stream()
                .filter(r -> registroPagoOuComValorPago(r, statusPagos))
                .mapToDouble(r -> nz(r.getValorPago()))
                .sum();

        double valorVencidoCobrancas = transacoes.stream()
                .filter(t -> naoPago(t, statusPagos))
                .filter(t -> t.getDataVencimento() != null && t.getDataVencimento().isBefore(hoje))
                .mapToDouble(t -> nz(t.getValor()))
                .sum();

        double valorAVencerCobrancas = transacoes.stream()
                .filter(t -> naoPago(t, statusPagos))
                .filter(t -> t.getDataVencimento() != null && !t.getDataVencimento().isBefore(hoje))
                .mapToDouble(t -> nz(t.getValor()))
                .sum();

        long quantidadeTitulosAbertos = transacoes.stream().filter(t -> naoPago(t, statusPagos)).count();

        List<ClienteRankingItemResponse> ranking = montarRanking(registrosPeriodo, tenantId, statusPagos);

        double margemLucroPercentual = receitaTotal > 0 ? ((receitaTotal - despesaTotal) / receitaTotal) * 100.0 : 0.0;

        Map<String, Object> metricas = new LinkedHashMap<>();
        metricas.put("receitaTotal", receitaTotal);
        metricas.put("despesaTotal", despesaTotal);
        metricas.put("ticketMedio", ticketMedio);
        metricas.put("quantidadeTransacoes", qtdTransacoes);
        metricas.put("receitaPorCategoria", receitaPorCategoria);
        metricas.put("despesaPorCategoria", despesaPorCategoria);
        metricas.put("inadimplenciaPercentual", inadimplenciaPercentual);
        metricas.put("valorRecebidoCobrancas", valorRecebidoCobrancas);
        metricas.put("valorVencidoCobrancas", valorVencidoCobrancas);
        metricas.put("valorAVencerCobrancas", valorAVencerCobrancas);
        metricas.put("quantidadeTitulosAbertos", quantidadeTitulosAbertos);
        metricas.put("rankingClientesReceita", ranking);
        metricas.put("margemLucroPercentual", margemLucroPercentual);
        metricas.put("fonte", "BANCO");

        return metricas;
    }

    @Transactional(readOnly = true)
    public KpiResumoFromBanco obterKpisResumo(Long tenantId, LocalDate dataInicio, LocalDate dataFim) {
        Map<String, Object> m = obterMetricas(tenantId, dataInicio, dataFim);
        return new KpiResumoFromBanco(
                asDouble(m.get("receitaTotal")),
                asDouble(m.get("despesaTotal")),
                asDouble(m.get("margemLucroPercentual")),
                asDouble(m.get("inadimplenciaPercentual"))
        );
    }

    private static Map<String, Object> vazias(LocalDate dataInicio, LocalDate dataFim) {
        Map<String, Object> metricas = new LinkedHashMap<>();
        metricas.put("receitaTotal", 0.0);
        metricas.put("despesaTotal", 0.0);
        metricas.put("ticketMedio", 0.0);
        metricas.put("quantidadeTransacoes", 0L);
        metricas.put("receitaPorCategoria", Map.of());
        metricas.put("despesaPorCategoria", Map.of());
        metricas.put("inadimplenciaPercentual", MOCK_INADIMPLENCIA_SEM_TITULOS);
        metricas.put("valorRecebidoCobrancas", 0.0);
        metricas.put("valorVencidoCobrancas", 0.0);
        metricas.put("valorAVencerCobrancas", 0.0);
        metricas.put("quantidadeTitulosAbertos", 0L);
        metricas.put("rankingClientesReceita", List.of());
        metricas.put("margemLucroPercentual", 0.0);
        metricas.put("fonte", "BANCO_VAZIO");
        metricas.put("periodo", dataInicio + ".." + dataFim);
        return metricas;
    }

    private List<ClienteRankingItemResponse> montarRanking(
            List<RegistroFinanceiro> registrosPeriodo,
            Long tenantId,
            Set<String> statusPagos
    ) {
        Map<Long, Double> porCliente = registrosPeriodo.stream()
                .filter(r -> r.getClienteId() != null)
                .filter(r -> registroPagoOuComValorPago(r, statusPagos))
                .collect(Collectors.groupingBy(RegistroFinanceiro::getClienteId,
                        Collectors.summingDouble(r -> nz(r.getValorPago()))));

        List<ClienteRankingItemResponse> itens = new ArrayList<>();
        porCliente.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> {
                    ClienteRankingItemResponse row = new ClienteRankingItemResponse();
                    row.setClienteId(e.getKey());
                    row.setNome(null);
                    row.setValorRecebido(e.getValue());
                    itens.add(row);
                });

        if (metricasTenantEhClienteId && itens.isEmpty() && !registrosPeriodo.isEmpty()) {
            double total = registrosPeriodo.stream()
                    .filter(r -> registroPagoOuComValorPago(r, statusPagos))
                    .mapToDouble(r -> nz(r.getValorPago()))
                    .sum();
            ClienteRankingItemResponse row = new ClienteRankingItemResponse();
            row.setClienteId(tenantId);
            row.setNome(null);
            row.setValorRecebido(total);
            itens.add(row);
        }
        return itens;
    }

    private static Map<String, Double> categorizar(java.util.stream.Stream<Transacao> stream) {
        return stream.collect(Collectors.groupingBy(
                t -> {
                    String tit = t.getTitulo();
                    return (tit == null || tit.isBlank()) ? "OUTROS" : tit.trim();
                },
                Collectors.summingDouble(t -> nz(t.getValor()))
        ));
    }

    private static boolean mesNoPeriodo(int ano, int mes, LocalDate ini, LocalDate fim) {
        LocalDate ref = LocalDate.of(ano, mes, 1);
        LocalDate iniMes = ini.withDayOfMonth(1);
        LocalDate fimMes = fim.withDayOfMonth(1);
        return !ref.isBefore(iniMes) && !ref.isAfter(fimMes);
    }

    private static java.util.Optional<LocalDate> dataReferencia(Transacao t) {
        if (t.getDataPagamento() != null) {
            return java.util.Optional.of(t.getDataPagamento());
        }
        if (t.getDataEmissao() != null) {
            return java.util.Optional.of(t.getDataEmissao());
        }
        return java.util.Optional.empty();
    }

    private static boolean naoPago(Transacao t, Set<String> statusPagos) {
        if (t.getDataPagamento() != null) {
            return false;
        }
        String status = nullToEmpty(t.getStatusFinanceiro()).toUpperCase(Locale.ROOT);
        return !statusPagos.contains(status);
    }

    private static boolean isDespesa(String tipo, Set<String> tiposDespesa) {
        if (tipo == null || tipo.isBlank()) {
            return false;
        }
        String t = tipo.trim().toUpperCase(Locale.ROOT);
        if (tiposDespesa.contains(t)) {
            return true;
        }
        return t.contains("DESP") || t.contains("DEB") || t.contains("SAIDA");
    }

    private static boolean isReceita(String tipo, Set<String> tiposReceita, Set<String> tiposDespesa) {
        if (tipo == null || tipo.isBlank()) {
            return true;
        }
        String t = tipo.trim().toUpperCase(Locale.ROOT);
        if (tiposReceita.contains(t)) {
            return true;
        }
        if (tiposDespesa.contains(t)) {
            return false;
        }
        if (t.contains("DESP") || t.contains("DEB") || t.contains("SAIDA")) {
            return false;
        }
        return t.contains("REC") || t.contains("CRED") || t.contains("ENTRADA") || "1".equals(t);
    }

    private static boolean registroPagoOuComValorPago(RegistroFinanceiro r, Set<String> statusPagos) {
        if (nz(r.getValorPago()) > 0.0) {
            return true;
        }
        String status = r.getStatusFinanceiro() == null ? "" : String.valueOf(r.getStatusFinanceiro()).trim().toUpperCase(Locale.ROOT);
        return statusPagos.contains(status);
    }

    private static Set<String> splitCsvUpper(String csv) {
        return Arrays.stream(nullToEmpty(csv).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static double nz(BigDecimal v) {
        return v != null ? v.doubleValue() : 0.0;
    }

    private static double asDouble(Object o) {
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        return 0.0;
    }

    public record KpiResumoFromBanco(double receita, double despesa, double margemPercentual, double inadimplenciaPercentual) {
    }
}

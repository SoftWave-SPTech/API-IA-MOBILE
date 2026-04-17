package softwave.api_finance_ia.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import softwave.api_finance_ia.dto.integracao.CobrancaResumoResponse;

import java.time.LocalDate;

@FeignClient(name = "cobrancasClient", url = "${services.cobrancas.url:http://localhost:8080}")
public interface CobrancasClient {

    @GetMapping("/internal/v1/tenants/{tenantId}/cobrancas/resumo")
    CobrancaResumoResponse resumo(
            @PathVariable("tenantId") Long tenantId,
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    );
}

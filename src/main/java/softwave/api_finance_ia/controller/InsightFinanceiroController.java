package softwave.api_finance_ia.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softwave.api_finance_ia.dto.request.GerarInsightRequestDTO;
import softwave.api_finance_ia.dto.response.InsightFinanceiroResponseDTO;
import softwave.api_finance_ia.dto.response.KpiResumoDTO;
import softwave.api_finance_ia.entity.EnumTipoInsight;
import softwave.api_finance_ia.service.InsightFinanceiroService;

@RestController
@RequestMapping("/insights")
public class InsightFinanceiroController {

    private final InsightFinanceiroService insightFinanceiroService;

    public InsightFinanceiroController(InsightFinanceiroService insightFinanceiroService) {
        this.insightFinanceiroService = insightFinanceiroService;
    }

    @PostMapping("/gerar")
    public ResponseEntity<InsightFinanceiroResponseDTO> gerar(@Valid @RequestBody GerarInsightRequestDTO request) {
        InsightFinanceiroResponseDTO response = insightFinanceiroService.gerar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<InsightFinanceiroResponseDTO>> listar(
            @RequestParam Long tenantId,
            @RequestParam(required = false) EnumTipoInsight tipoInsight,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(insightFinanceiroService.listar(tenantId, tipoInsight, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsightFinanceiroResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(insightFinanceiroService.buscarPorId(id));
    }

    @GetMapping("/ultimo")
    public ResponseEntity<InsightFinanceiroResponseDTO> buscarUltimoPorTipo(
            @RequestParam Long tenantId,
            @RequestParam EnumTipoInsight tipoInsight
    ) {
        return ResponseEntity.ok(insightFinanceiroService.buscarUltimoPorTipo(tenantId, tipoInsight));
    }

    @GetMapping("/kpis/resumo")
    public ResponseEntity<KpiResumoDTO> obterResumoKpis(@RequestParam Long tenantId) {
        return ResponseEntity.ok(insightFinanceiroService.obterResumoKpis(tenantId));
    }
}

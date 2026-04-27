package softwave.api_finance_ia.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softwave.api_finance_ia.service.JwtUserResolver;
import softwave.api_finance_ia.dto.request.GerarInsightRequestDTO;
import softwave.api_finance_ia.dto.response.InsightFinanceiroResponseDTO;
import softwave.api_finance_ia.dto.response.KpiResumoDTO;
import softwave.api_finance_ia.entity.EnumTipoInsight;
import softwave.api_finance_ia.service.InsightFinanceiroService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/insights")
public class InsightFinanceiroController {

    private final InsightFinanceiroService insightFinanceiroService;
    private final JwtUserResolver jwtUserResolver;

    public InsightFinanceiroController(InsightFinanceiroService insightFinanceiroService, JwtUserResolver jwtUserResolver) {
        this.insightFinanceiroService = insightFinanceiroService;
        this.jwtUserResolver = jwtUserResolver;
    }

    @PostMapping("/gerar")
    public ResponseEntity<InsightFinanceiroResponseDTO> gerar(
            @Valid @RequestBody GerarInsightRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        Long userId = jwtUserResolver.resolveUserIdOptional(httpRequest);
        InsightFinanceiroResponseDTO response = insightFinanceiroService.gerar(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<InsightFinanceiroResponseDTO>> listar(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) EnumTipoInsight tipoInsight,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest
    ) {
        Long userId = jwtUserResolver.resolveUserIdOptional(httpRequest);
        return ResponseEntity.ok(insightFinanceiroService.listar(tenantId, tipoInsight, page, size, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsightFinanceiroResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(insightFinanceiroService.buscarPorId(id));
    }

    @GetMapping("/ultimo")
    public ResponseEntity<InsightFinanceiroResponseDTO> buscarUltimoPorTipo(
            @RequestParam(required = false) Long tenantId,
            @RequestParam EnumTipoInsight tipoInsight,
            HttpServletRequest httpRequest
    ) {
        Long userId = jwtUserResolver.resolveUserIdOptional(httpRequest);
        return ResponseEntity.ok(insightFinanceiroService.buscarUltimoPorTipo(tenantId, tipoInsight, userId));
    }

    @GetMapping("/kpis/resumo")
    public ResponseEntity<KpiResumoDTO> obterResumoKpis(
            @RequestParam(required = false) Long tenantId,
            HttpServletRequest httpRequest
    ) {
        Long userId = jwtUserResolver.resolveUserIdOptional(httpRequest);
        return ResponseEntity.ok(insightFinanceiroService.obterResumoKpis(tenantId, userId));
    }
}

package io.github.amancio10.bibliotecavirtualapi.circulacao.multa;

import io.github.amancio10.bibliotecavirtualapi.circulacao.ControleAcessoCirculacao;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto.CancelamentoMultaRequest;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto.MultaFiltro;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto.MultaResponse;
import io.github.amancio10.bibliotecavirtualapi.common.web.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/multas")
@RequiredArgsConstructor
@Tag(name = "Multas")
public class MultaController {

    private final MultaService service;

    @GetMapping
    @Operation(summary = "Lista multas (filtros: leitorId, status, motivo)")
    public PaginaResponse<MultaResponse> listar(MultaFiltro filtro,
                                                @PageableDefault(sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return PaginaResponse.de(service.listar(filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca multa por id")
    public MultaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/pagamento")
    @Operation(summary = "Registra pagamento da multa (operador)")
    public MultaResponse pagar(@PathVariable Long id,
                               @RequestHeader(ControleAcessoCirculacao.HEADER_USUARIO) Long usuarioId) {
        return service.registrarPagamento(id, usuarioId);
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela (abona) a multa (somente ADMIN)")
    public MultaResponse cancelar(@PathVariable Long id,
                                  @RequestHeader(ControleAcessoCirculacao.HEADER_USUARIO) Long usuarioId,
                                  @RequestBody @Valid CancelamentoMultaRequest req) {
        return service.cancelar(id, usuarioId, req.motivo());
    }
}

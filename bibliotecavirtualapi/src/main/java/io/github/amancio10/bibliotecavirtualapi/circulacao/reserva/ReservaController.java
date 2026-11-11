package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva;

import io.github.amancio10.bibliotecavirtualapi.circulacao.ControleAcessoCirculacao;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.dto.ReservaFiltro;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.dto.ReservaRequest;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.dto.ReservaResponse;
import io.github.amancio10.bibliotecavirtualapi.common.web.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Fila de reservas por obra")
public class ReservaController {

    private static final String HEADER = ControleAcessoCirculacao.HEADER_USUARIO;

    private final ReservaService service;

    @PostMapping
    @Operation(summary = "Reserva uma obra (operador ou o próprio leitor)")
    public ResponseEntity<ReservaResponse> reservar(@RequestHeader(HEADER) Long usuarioId,
                                                    @RequestBody @Valid ReservaRequest req) {
        ReservaResponse reserva = service.reservar(req.leitorId(), req.livroId(), usuarioId);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(reserva.id()).toUri();
        return ResponseEntity.created(uri).body(reserva);
    }

    @GetMapping
    @Operation(summary = "Lista reservas (filtros: leitorId, livroId, status)")
    public PaginaResponse<ReservaResponse> listar(ReservaFiltro filtro,
                                                  @PageableDefault(sort = "dataReserva", direction = Sort.Direction.DESC) Pageable pageable) {
        return PaginaResponse.de(service.listar(filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca reserva (inclui posição na fila)")
    public ReservaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela a reserva (libera o exemplar separado, se houver)")
    public ReservaResponse cancelar(@PathVariable Long id, @RequestHeader(HEADER) Long usuarioId) {
        return service.cancelar(id, usuarioId);
    }
}

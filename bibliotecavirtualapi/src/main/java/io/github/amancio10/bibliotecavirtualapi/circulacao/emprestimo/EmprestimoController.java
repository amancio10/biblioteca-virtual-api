package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo;

import io.github.amancio10.bibliotecavirtualapi.circulacao.ControleAcessoCirculacao;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto.DevolucaoRequest;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto.DevolucaoResponse;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto.EmprestimoFiltro;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto.EmprestimoRequest;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto.EmprestimoResponse;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto.ExtravioRequest;
import io.github.amancio10.bibliotecavirtualapi.common.web.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/emprestimos")
@RequiredArgsConstructor
@Tag(name = "Empréstimos", description = "Fluxo de empréstimo, renovação, devolução e extravio")
public class EmprestimoController {

    private static final String HEADER = ControleAcessoCirculacao.HEADER_USUARIO;

    private final EmprestimoService service;

    @PostMapping
    @Operation(summary = "Realiza empréstimo (operador)")
    public ResponseEntity<EmprestimoResponse> emprestar(
            @Parameter(description = "Id do operador (provisório até autenticação)") @RequestHeader(HEADER) Long usuarioId,
            @RequestBody @Valid EmprestimoRequest req) {
        EmprestimoResponse emprestimo = service.emprestar(req.leitorId(), req.codigoTombo(), usuarioId);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(emprestimo.id()).toUri();
        return ResponseEntity.created(uri).body(emprestimo);
    }

    @GetMapping
    @Operation(summary = "Lista empréstimos (filtros: leitorId, livroId, status, atrasados)")
    public PaginaResponse<EmprestimoResponse> listar(EmprestimoFiltro filtro,
                                                     @PageableDefault(sort = "dataEmprestimo", direction = Sort.Direction.DESC) Pageable pageable) {
        return PaginaResponse.de(service.listar(filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca empréstimo por id (inclui histórico de renovações)")
    public EmprestimoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/devolucao")
    @Operation(summary = "Registra devolução (gera multa/suspensão se houver atraso e atende a fila de reservas)")
    public DevolucaoResponse devolver(@PathVariable Long id,
                                      @RequestHeader(HEADER) Long usuarioId,
                                      @RequestBody(required = false) @Valid DevolucaoRequest req) {
        return service.devolver(id, usuarioId, req == null ? null : req.observacao());
    }

    @PostMapping("/{id}/renovacao")
    @Operation(summary = "Renova o empréstimo (operador ou o próprio leitor)")
    public EmprestimoResponse renovar(@PathVariable Long id, @RequestHeader(HEADER) Long usuarioId) {
        return service.renovar(id, usuarioId);
    }

    @PostMapping("/{id}/extravio")
    @Operation(summary = "Registra extravio do exemplar (gera multa de reposição)")
    public EmprestimoResponse registrarExtravio(@PathVariable Long id,
                                                @RequestHeader(HEADER) Long usuarioId,
                                                @RequestBody @Valid ExtravioRequest req) {
        return service.registrarExtravio(id, usuarioId, req.valorReposicao(), req.observacao());
    }
}

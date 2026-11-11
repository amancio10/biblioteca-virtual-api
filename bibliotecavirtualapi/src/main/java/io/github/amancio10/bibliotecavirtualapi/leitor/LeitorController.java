package io.github.amancio10.bibliotecavirtualapi.leitor;

import io.github.amancio10.bibliotecavirtualapi.common.web.PaginaResponse;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.BloqueioRequest;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.LeitorCreateRequest;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.LeitorFiltro;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.LeitorResponse;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.LeitorUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/leitores")
@RequiredArgsConstructor
@Tag(name = "Leitores", description = "Cadastro e situação dos leitores")
public class LeitorController {

    private final LeitorService service;

    @PostMapping
    @Operation(summary = "Cadastra leitor (cria também o usuário de acesso)")
    public ResponseEntity<LeitorResponse> cadastrar(@RequestBody @Valid LeitorCreateRequest req) {
        LeitorResponse leitor = service.cadastrar(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(leitor.id()).toUri();
        return ResponseEntity.created(uri).body(leitor);
    }

    @GetMapping
    @Operation(summary = "Lista leitores com filtros opcionais (nome, cpf, carteirinha, status, tipo)")
    public PaginaResponse<LeitorResponse> listar(LeitorFiltro filtro,
                                                 @PageableDefault(sort = "usuario.nome") Pageable pageable) {
        return PaginaResponse.de(service.listar(filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca leitor por id")
    public LeitorResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados cadastrais do leitor")
    public LeitorResponse atualizar(@PathVariable Long id, @RequestBody @Valid LeitorUpdateRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/bloqueio")
    @Operation(summary = "Bloqueia o leitor manualmente")
    public LeitorResponse bloquear(@PathVariable Long id, @RequestBody @Valid BloqueioRequest req) {
        return service.bloquear(id, req.motivo());
    }

    @PatchMapping("/{id}/desbloqueio")
    @Operation(summary = "Remove o bloqueio manual do leitor")
    public LeitorResponse desbloquear(@PathVariable Long id) {
        return service.desbloquear(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa o cadastro do leitor (exclusão lógica)")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }
}

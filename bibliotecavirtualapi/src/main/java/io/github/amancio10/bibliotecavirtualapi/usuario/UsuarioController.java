package io.github.amancio10.bibliotecavirtualapi.usuario;

import io.github.amancio10.bibliotecavirtualapi.common.web.PaginaResponse;
import io.github.amancio10.bibliotecavirtualapi.usuario.dto.UsuarioRequest;
import io.github.amancio10.bibliotecavirtualapi.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Operadores do sistema (administradores e bibliotecários)")
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    @Operation(summary = "Cadastra um operador (ADMIN ou BIBLIOTECARIO)")
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest req) {
        Usuario usuario = service.criarOperador(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(UsuarioResponse.de(usuario));
    }

    @GetMapping
    @Operation(summary = "Lista operadores")
    public PaginaResponse<UsuarioResponse> listar(@PageableDefault(sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return PaginaResponse.de(service.listarOperadores(pageable), UsuarioResponse::de);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca usuário por id")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return UsuarioResponse.de(service.buscar(id));
    }

    @PatchMapping("/{id}/ativacao")
    @Operation(summary = "Ativa o usuário")
    public UsuarioResponse ativar(@PathVariable Long id) {
        return UsuarioResponse.de(service.ativar(id));
    }

    @PatchMapping("/{id}/desativacao")
    @Operation(summary = "Desativa o usuário (impede login e operações)")
    public UsuarioResponse desativar(@PathVariable Long id) {
        return UsuarioResponse.de(service.desativar(id));
    }
}

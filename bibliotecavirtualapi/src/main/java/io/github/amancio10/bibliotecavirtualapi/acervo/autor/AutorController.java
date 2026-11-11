package io.github.amancio10.bibliotecavirtualapi.acervo.autor;

import io.github.amancio10.bibliotecavirtualapi.acervo.autor.dto.AutorRequest;
import io.github.amancio10.bibliotecavirtualapi.acervo.autor.dto.AutorResponse;
import io.github.amancio10.bibliotecavirtualapi.common.web.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/autores")
@RequiredArgsConstructor
@Tag(name = "Autores")
public class AutorController {

    private final AutorService service;

    @PostMapping
    @Operation(summary = "Cadastra autor")
    public ResponseEntity<AutorResponse> criar(@RequestBody @Valid AutorRequest req) {
        Autor autor = service.criar(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(autor.getId()).toUri();
        return ResponseEntity.created(uri).body(AutorResponse.de(autor));
    }

    @GetMapping
    @Operation(summary = "Lista autores (filtro opcional por nome)")
    public PaginaResponse<AutorResponse> listar(@RequestParam(required = false) String nome,
                                                @PageableDefault(sort = "nome") Pageable pageable) {
        return PaginaResponse.de(service.listar(nome, pageable), AutorResponse::de);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca autor por id")
    public AutorResponse buscar(@PathVariable Long id) {
        return AutorResponse.de(service.buscar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza autor")
    public AutorResponse atualizar(@PathVariable Long id, @RequestBody @Valid AutorRequest req) {
        return AutorResponse.de(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui autor (somente se não houver livros vinculados)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

package io.github.amancio10.bibliotecavirtualapi.acervo.editora;

import io.github.amancio10.bibliotecavirtualapi.acervo.editora.dto.EditoraRequest;
import io.github.amancio10.bibliotecavirtualapi.acervo.editora.dto.EditoraResponse;
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
@RequestMapping("/api/editoras")
@RequiredArgsConstructor
@Tag(name = "Editoras")
public class EditoraController {

    private final EditoraService service;

    @PostMapping
    @Operation(summary = "Cadastra editora")
    public ResponseEntity<EditoraResponse> criar(@RequestBody @Valid EditoraRequest req) {
        Editora editora = service.criar(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(editora.getId()).toUri();
        return ResponseEntity.created(uri).body(EditoraResponse.de(editora));
    }

    @GetMapping
    @Operation(summary = "Lista editoras (filtro opcional por nome)")
    public PaginaResponse<EditoraResponse> listar(@RequestParam(required = false) String nome,
                                                  @PageableDefault(sort = "nome") Pageable pageable) {
        return PaginaResponse.de(service.listar(nome, pageable), EditoraResponse::de);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca editora por id")
    public EditoraResponse buscar(@PathVariable Long id) {
        return EditoraResponse.de(service.buscar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza editora")
    public EditoraResponse atualizar(@PathVariable Long id, @RequestBody @Valid EditoraRequest req) {
        return EditoraResponse.de(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui editora (somente se não houver livros vinculados)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

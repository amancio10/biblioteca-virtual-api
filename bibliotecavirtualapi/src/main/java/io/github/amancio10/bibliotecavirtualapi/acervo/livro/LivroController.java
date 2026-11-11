package io.github.amancio10.bibliotecavirtualapi.acervo.livro;

import io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto.DisponibilidadeResponse;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto.LivroFiltro;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto.LivroRequest;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto.LivroResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/livros")
@RequiredArgsConstructor
@Tag(name = "Livros", description = "Catálogo de obras")
public class LivroController {

    private final LivroService service;

    @PostMapping
    @Operation(summary = "Cadastra livro (obra)")
    public ResponseEntity<LivroResponse> criar(@RequestBody @Valid LivroRequest req) {
        LivroResponse livro = service.criar(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(livro.id()).toUri();
        return ResponseEntity.created(uri).body(livro);
    }

    @GetMapping
    @Operation(summary = "Pesquisa o catálogo (título, isbn, autor, categoriaId, editoraId)")
    public PaginaResponse<LivroResponse> listar(LivroFiltro filtro,
                                                @PageableDefault(sort = "titulo") Pageable pageable) {
        return PaginaResponse.de(service.listar(filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca livro por id")
    public LivroResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/disponibilidade")
    @Operation(summary = "Quantidade de exemplares disponíveis e tamanho da fila de reservas")
    public DisponibilidadeResponse disponibilidade(@PathVariable Long id) {
        return service.disponibilidade(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza livro")
    public LivroResponse atualizar(@PathVariable Long id, @RequestBody @Valid LivroRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa o livro (exclusão lógica)")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativacao")
    @Operation(summary = "Reativa um livro inativado")
    public LivroResponse reativar(@PathVariable Long id) {
        return service.reativar(id);
    }
}

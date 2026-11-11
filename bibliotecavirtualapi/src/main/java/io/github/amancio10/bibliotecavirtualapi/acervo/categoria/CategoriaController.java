package io.github.amancio10.bibliotecavirtualapi.acervo.categoria;

import io.github.amancio10.bibliotecavirtualapi.acervo.categoria.dto.CategoriaRequest;
import io.github.amancio10.bibliotecavirtualapi.acervo.categoria.dto.CategoriaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias")
public class CategoriaController {

    private final CategoriaService service;

    @PostMapping
    @Operation(summary = "Cadastra categoria")
    public ResponseEntity<CategoriaResponse> criar(@RequestBody @Valid CategoriaRequest req) {
        Categoria categoria = service.criar(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(categoria.getId()).toUri();
        return ResponseEntity.created(uri).body(CategoriaResponse.de(categoria));
    }

    @GetMapping
    @Operation(summary = "Lista todas as categorias")
    public List<CategoriaResponse> listar() {
        return service.listar().stream().map(CategoriaResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca categoria por id")
    public CategoriaResponse buscar(@PathVariable Long id) {
        return CategoriaResponse.de(service.buscar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza categoria")
    public CategoriaResponse atualizar(@PathVariable Long id, @RequestBody @Valid CategoriaRequest req) {
        return CategoriaResponse.de(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui categoria (somente se não houver livros vinculados)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

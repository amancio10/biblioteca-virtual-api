package io.github.amancio10.bibliotecavirtualapi.acervo.exemplar;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto.BaixaRequest;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto.ExemplarRequest;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto.ExemplarResponse;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto.ExemplarUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Exemplares", description = "Cópias físicas dos livros")
public class ExemplarController {

    private final ExemplarService service;

    @PostMapping("/livros/{livroId}/exemplares")
    @Operation(summary = "Cadastra exemplar de um livro (já atende a fila de reservas, se houver)")
    public ResponseEntity<ExemplarResponse> cadastrar(@PathVariable Long livroId, @RequestBody @Valid ExemplarRequest req) {
        ExemplarResponse exemplar = service.cadastrar(livroId, req);
        var uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/exemplares/{id}").buildAndExpand(exemplar.id()).toUri();
        return ResponseEntity.created(uri).body(exemplar);
    }

    @GetMapping("/livros/{livroId}/exemplares")
    @Operation(summary = "Lista exemplares de um livro")
    public List<ExemplarResponse> listarPorLivro(@PathVariable Long livroId) {
        return service.listarPorLivro(livroId);
    }

    @GetMapping("/exemplares/{id}")
    @Operation(summary = "Busca exemplar por id")
    public ExemplarResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/exemplares/tombo/{codigoTombo}")
    @Operation(summary = "Busca exemplar pelo código de tombo (leitura de código de barras)")
    public ExemplarResponse buscarPorTombo(@PathVariable String codigoTombo) {
        return service.buscarPorTombo(codigoTombo);
    }

    @PutMapping("/exemplares/{id}")
    @Operation(summary = "Atualiza conservação, localização e observação")
    public ExemplarResponse atualizar(@PathVariable Long id, @RequestBody @Valid ExemplarUpdateRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/exemplares/{id}/manutencao")
    @Operation(summary = "Envia exemplar disponível para manutenção")
    public ExemplarResponse enviarParaManutencao(@PathVariable Long id) {
        return service.enviarParaManutencao(id);
    }

    @PatchMapping("/exemplares/{id}/retorno-manutencao")
    @Operation(summary = "Retorna exemplar da manutenção (vai para a fila de reservas ou estante)")
    public ExemplarResponse retornarDaManutencao(@PathVariable Long id) {
        return service.retornarDaManutencao(id);
    }

    @PatchMapping("/exemplares/{id}/baixa")
    @Operation(summary = "Dá baixa definitiva no exemplar")
    public ExemplarResponse baixar(@PathVariable Long id, @RequestBody @Valid BaixaRequest req) {
        return service.baixar(id, req.motivo());
    }
}

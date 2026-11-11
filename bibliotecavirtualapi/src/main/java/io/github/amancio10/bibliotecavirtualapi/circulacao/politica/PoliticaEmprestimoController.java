package io.github.amancio10.bibliotecavirtualapi.circulacao.politica;

import io.github.amancio10.bibliotecavirtualapi.circulacao.politica.dto.PoliticaEmprestimoRequest;
import io.github.amancio10.bibliotecavirtualapi.circulacao.politica.dto.PoliticaEmprestimoResponse;
import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/politicas-emprestimo")
@RequiredArgsConstructor
@Tag(name = "Políticas de empréstimo", description = "Prazos, limites e multas por tipo de leitor")
public class PoliticaEmprestimoController {

    private final PoliticaEmprestimoService service;

    @GetMapping
    @Operation(summary = "Lista as políticas vigentes")
    public List<PoliticaEmprestimoResponse> listar() {
        return service.listar().stream().map(PoliticaEmprestimoResponse::de).toList();
    }

    @GetMapping("/{tipoLeitor}")
    @Operation(summary = "Busca a política de um tipo de leitor")
    public PoliticaEmprestimoResponse buscar(@PathVariable TipoLeitor tipoLeitor) {
        return PoliticaEmprestimoResponse.de(service.buscar(tipoLeitor));
    }

    @PutMapping("/{tipoLeitor}")
    @Operation(summary = "Atualiza a política (vale para novos empréstimos/multas)")
    public PoliticaEmprestimoResponse atualizar(@PathVariable TipoLeitor tipoLeitor,
                                                @RequestBody @Valid PoliticaEmprestimoRequest req) {
        return PoliticaEmprestimoResponse.de(service.atualizar(tipoLeitor, req));
    }
}

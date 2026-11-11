package io.github.amancio10.bibliotecavirtualapi.circulacao.politica;

import io.github.amancio10.bibliotecavirtualapi.circulacao.politica.dto.PoliticaEmprestimoRequest;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoliticaEmprestimoService {

    private final PoliticaEmprestimoRepository repository;

    @Transactional(readOnly = true)
    public PoliticaEmprestimo buscar(TipoLeitor tipo) {
        return repository.findByTipoLeitor(tipo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Política de empréstimo", tipo));
    }

    @Transactional(readOnly = true)
    public List<PoliticaEmprestimo> listar() {
        return repository.findAll(Sort.by("tipoLeitor"));
    }

    @Transactional
    public PoliticaEmprestimo atualizar(TipoLeitor tipo, PoliticaEmprestimoRequest req) {
        PoliticaEmprestimo politica = buscar(tipo);
        politica.atualizar(req.prazoDias(), req.maxEmprestimosSimultaneos(), req.maxRenovacoes(),
                req.maxReservasSimultaneas(), req.horasRetiradaReserva(), req.valorMultaDiaria(),
                req.diasSuspensaoPorAtraso());
        return politica;
    }
}

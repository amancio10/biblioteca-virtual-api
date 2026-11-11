package io.github.amancio10.bibliotecavirtualapi.acervo.exemplar;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto.ExemplarRequest;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto.ExemplarResponse;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto.ExemplarUpdateRequest;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.Livro;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.LivroService;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.FilaReservaService;
import io.github.amancio10.bibliotecavirtualapi.common.exception.ConflitoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExemplarService {

    private final ExemplarRepository repository;
    private final LivroService livroService;
    private final FilaReservaService filaReservaService;

    /**
     * Um novo exemplar entra pela fila de reservas: se houver leitor aguardando,
     * já nasce RESERVADO para ele.
     */
    @Transactional
    public ExemplarResponse cadastrar(Long livroId, ExemplarRequest req) {
        Livro livro = livroService.buscarEntidade(livroId);
        if (!livro.isAtivo()) {
            throw new RegraNegocioException("Não é possível adicionar exemplares a um livro inativo.");
        }
        String tombo = normalizarTombo(req.codigoTombo());
        if (repository.existsByCodigoTombo(tombo)) {
            throw new ConflitoException("Já existe exemplar com o tombo " + tombo);
        }
        Exemplar exemplar = repository.save(new Exemplar(
                livro,
                tombo,
                req.estadoConservacao() != null ? req.estadoConservacao() : EstadoConservacao.BOM,
                Normalizador.textoOuNulo(req.localizacao()),
                req.dataAquisicao(),
                Normalizador.textoOuNulo(req.observacao())
        ));
        filaReservaService.liberarExemplar(exemplar);
        return ExemplarResponse.de(exemplar);
    }

    @Transactional(readOnly = true)
    public ExemplarResponse buscar(Long id) {
        return ExemplarResponse.de(carregar(id));
    }

    @Transactional(readOnly = true)
    public ExemplarResponse buscarPorTombo(String codigoTombo) {
        return ExemplarResponse.de(buscarEntidadePorTombo(codigoTombo));
    }

    /** Para uso de outros services dentro da mesma transação. */
    @Transactional(readOnly = true)
    public Exemplar buscarEntidadePorTombo(String codigoTombo) {
        String tombo = normalizarTombo(codigoTombo);
        return repository.findByCodigoTombo(tombo).orElseThrow(() -> new RecursoNaoEncontradoException("Exemplar", tombo));
    }

    @Transactional(readOnly = true)
    public List<ExemplarResponse> listarPorLivro(Long livroId) {
        livroService.buscarEntidade(livroId);
        return repository.findByLivroIdOrderByCodigoTombo(livroId).stream().map(ExemplarResponse::de).toList();
    }

    @Transactional
    public ExemplarResponse atualizar(Long id, ExemplarUpdateRequest req) {
        Exemplar exemplar = carregar(id);
        exemplar.atualizar(req.estadoConservacao(), Normalizador.textoOuNulo(req.localizacao()),
                Normalizador.textoOuNulo(req.observacao()));
        return ExemplarResponse.de(exemplar);
    }

    @Transactional
    public ExemplarResponse enviarParaManutencao(Long id) {
        Exemplar exemplar = carregar(id);
        exemplar.enviarParaManutencao();
        return ExemplarResponse.de(exemplar);
    }

    /** Retorno da manutenção: atende a fila de reservas antes de voltar à estante. */
    @Transactional
    public ExemplarResponse retornarDaManutencao(Long id) {
        Exemplar exemplar = carregar(id);
        if (exemplar.getStatus() != StatusExemplar.EM_MANUTENCAO) {
            throw new RegraNegocioException("Exemplar não está em manutenção.");
        }
        filaReservaService.liberarExemplar(exemplar);
        return ExemplarResponse.de(exemplar);
    }

    @Transactional
    public ExemplarResponse baixar(Long id, String motivo) {
        Exemplar exemplar = carregar(id);
        exemplar.baixar(motivo.trim());
        return ExemplarResponse.de(exemplar);
    }

    private Exemplar carregar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Exemplar", id));
    }

    private static String normalizarTombo(String tombo) {
        return tombo.trim().toUpperCase();
    }
}

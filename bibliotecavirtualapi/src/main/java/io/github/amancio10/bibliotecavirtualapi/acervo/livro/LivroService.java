package io.github.amancio10.bibliotecavirtualapi.acervo.livro;

import io.github.amancio10.bibliotecavirtualapi.acervo.autor.AutorService;
import io.github.amancio10.bibliotecavirtualapi.acervo.categoria.CategoriaService;
import io.github.amancio10.bibliotecavirtualapi.acervo.editora.Editora;
import io.github.amancio10.bibliotecavirtualapi.acervo.editora.EditoraService;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.ExemplarRepository;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.StatusExemplar;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto.DisponibilidadeResponse;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto.LivroFiltro;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto.LivroRequest;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto.LivroResponse;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.ReservaRepository;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.StatusReserva;
import io.github.amancio10.bibliotecavirtualapi.common.exception.ConflitoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LivroService {

    private static final String IDIOMA_PADRAO = "pt-BR";

    private final LivroRepository repository;
    private final AutorService autorService;
    private final EditoraService editoraService;
    private final CategoriaService categoriaService;
    private final ExemplarRepository exemplarRepository;
    private final ReservaRepository reservaRepository;

    @Transactional
    public LivroResponse criar(LivroRequest req) {
        String isbn = Normalizador.isbn(req.isbn());
        if (repository.existsByIsbn(isbn)) {
            throw new ConflitoException("Já existe livro com o ISBN " + isbn);
        }
        Livro livro = new Livro(isbn);
        preencher(livro, isbn, req);
        return LivroResponse.de(repository.save(livro));
    }

    @Transactional
    public LivroResponse atualizar(Long id, LivroRequest req) {
        Livro livro = buscarEntidade(id);
        String isbn = Normalizador.isbn(req.isbn());
        if (repository.existsByIsbnAndIdNot(isbn, id)) {
            throw new ConflitoException("Já existe livro com o ISBN " + isbn);
        }
        preencher(livro, isbn, req);
        return LivroResponse.de(livro);
    }

    @Transactional(readOnly = true)
    public LivroResponse buscar(Long id) {
        return LivroResponse.de(buscarEntidade(id));
    }

    /** Para uso de outros services dentro da mesma transação. */
    @Transactional(readOnly = true)
    public Livro buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Livro", id));
    }

    @Transactional(readOnly = true)
    public Page<LivroResponse> listar(LivroFiltro filtro, Pageable pageable) {
        return repository.findAll(LivroSpecifications.porFiltro(filtro), pageable).map(LivroResponse::de);
    }

    @Transactional(readOnly = true)
    public DisponibilidadeResponse disponibilidade(Long id) {
        buscarEntidade(id);
        return new DisponibilidadeResponse(
                id,
                exemplarRepository.countByLivroIdAndStatusNotIn(id, EnumSet.of(StatusExemplar.BAIXADO, StatusExemplar.EXTRAVIADO)),
                exemplarRepository.countByLivroIdAndStatus(id, StatusExemplar.DISPONIVEL),
                exemplarRepository.countByLivroIdAndStatus(id, StatusExemplar.EMPRESTADO),
                reservaRepository.countByLivroIdAndStatus(id, StatusReserva.AGUARDANDO)
        );
    }

    /** Exclusão lógica: o livro some do catálogo, mas o histórico é preservado. */
    @Transactional
    public void inativar(Long id) {
        Livro livro = buscarEntidade(id);
        if (exemplarRepository.existsByLivroIdAndStatusIn(id, EnumSet.of(StatusExemplar.EMPRESTADO, StatusExemplar.RESERVADO))) {
            throw new RegraNegocioException("Livro possui exemplares emprestados ou reservados.");
        }
        if (reservaRepository.existsByLivroIdAndStatus(id, StatusReserva.AGUARDANDO)) {
            throw new RegraNegocioException("Livro possui reservas na fila. Cancele-as antes de inativar.");
        }
        livro.inativar();
    }

    @Transactional
    public LivroResponse reativar(Long id) {
        Livro livro = buscarEntidade(id);
        livro.reativar();
        return LivroResponse.de(livro);
    }

    private void preencher(Livro livro, String isbn, LivroRequest req) {
        Editora editora = req.editoraId() == null ? null : editoraService.buscar(req.editoraId());

        // remove ids duplicados mantendo a ordem de autoria
        List<Long> autorIds = List.copyOf(new LinkedHashSet<>(req.autorIds()));
        Set<Long> categoriaIds = req.categoriaIds() == null ? Set.of() : req.categoriaIds();

        livro.atualizarDados(
                isbn,
                req.titulo().trim(),
                Normalizador.textoOuNulo(req.subtitulo()),
                Normalizador.textoOuNulo(req.sinopse()),
                editora,
                req.anoPublicacao(),
                req.edicao(),
                req.numeroPaginas(),
                req.idioma() == null || req.idioma().isBlank() ? IDIOMA_PADRAO : req.idioma().trim(),
                Normalizador.textoOuNulo(req.capaUrl())
        );
        livro.definirAutores(autorService.buscarTodos(autorIds));
        livro.definirCategorias(categoriaIds.isEmpty() ? List.of() : categoriaService.buscarTodas(categoriaIds));
    }
}

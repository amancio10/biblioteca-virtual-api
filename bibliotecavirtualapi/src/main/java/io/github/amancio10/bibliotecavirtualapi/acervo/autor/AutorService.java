package io.github.amancio10.bibliotecavirtualapi.acervo.autor;

import io.github.amancio10.bibliotecavirtualapi.acervo.autor.dto.AutorRequest;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutorService {

    private final AutorRepository repository;

    @Transactional
    public Autor criar(AutorRequest req) {
        return repository.save(new Autor(req.nome().trim(), Normalizador.textoOuNulo(req.nacionalidade()),
                req.dataNascimento(), Normalizador.textoOuNulo(req.biografia())));
    }

    @Transactional
    public Autor atualizar(Long id, AutorRequest req) {
        Autor autor = buscar(id);
        autor.atualizar(req.nome().trim(), Normalizador.textoOuNulo(req.nacionalidade()),
                req.dataNascimento(), Normalizador.textoOuNulo(req.biografia()));
        return autor;
    }

    @Transactional(readOnly = true)
    public Autor buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Autor", id));
    }

    /** Busca vários ids garantindo que todos existam (mantém a ordem informada). */
    @Transactional(readOnly = true)
    public List<Autor> buscarTodos(List<Long> ids) {
        List<Autor> encontrados = repository.findAllById(ids);
        Collection<Long> faltantes = new HashSet<>(ids);
        encontrados.forEach(a -> faltantes.remove(a.getId()));
        if (!faltantes.isEmpty()) {
            throw new RecursoNaoEncontradoException("Autor", faltantes);
        }
        return ids.stream()
                .map(id -> encontrados.stream().filter(a -> a.getId().equals(id)).findFirst().orElseThrow())
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<Autor> listar(String nome, Pageable pageable) {
        return nome == null || nome.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
    }

    /** Se o autor estiver vinculado a livros, a FK do banco impede (HTTP 409). */
    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
        repository.flush();
    }
}

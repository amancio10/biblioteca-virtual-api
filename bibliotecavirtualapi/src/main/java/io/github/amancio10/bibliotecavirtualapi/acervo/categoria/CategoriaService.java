package io.github.amancio10.bibliotecavirtualapi.acervo.categoria;

import io.github.amancio10.bibliotecavirtualapi.acervo.categoria.dto.CategoriaRequest;
import io.github.amancio10.bibliotecavirtualapi.common.exception.ConflitoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;

    @Transactional
    public Categoria criar(CategoriaRequest req) {
        String nome = req.nome().trim();
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe categoria com o nome " + nome);
        }
        return repository.save(new Categoria(nome, Normalizador.textoOuNulo(req.descricao())));
    }

    @Transactional
    public Categoria atualizar(Long id, CategoriaRequest req) {
        Categoria categoria = buscar(id);
        String nome = req.nome().trim();
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe categoria com o nome " + nome);
        }
        categoria.atualizar(nome, Normalizador.textoOuNulo(req.descricao()));
        return categoria;
    }

    @Transactional(readOnly = true)
    public Categoria buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    @Transactional(readOnly = true)
    public List<Categoria> buscarTodas(Collection<Long> ids) {
        List<Categoria> encontradas = repository.findAllById(ids);
        Collection<Long> faltantes = new HashSet<>(ids);
        encontradas.forEach(c -> faltantes.remove(c.getId()));
        if (!faltantes.isEmpty()) {
            throw new RecursoNaoEncontradoException("Categoria", faltantes);
        }
        return encontradas;
    }

    /** Categorias são poucas: listagem completa, sem paginação. */
    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return repository.findAll(Sort.by("nome"));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
        repository.flush();
    }
}

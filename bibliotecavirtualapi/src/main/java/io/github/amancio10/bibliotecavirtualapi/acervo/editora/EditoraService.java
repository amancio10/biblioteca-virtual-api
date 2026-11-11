package io.github.amancio10.bibliotecavirtualapi.acervo.editora;

import io.github.amancio10.bibliotecavirtualapi.acervo.editora.dto.EditoraRequest;
import io.github.amancio10.bibliotecavirtualapi.common.exception.ConflitoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditoraService {

    private final EditoraRepository repository;

    @Transactional
    public Editora criar(EditoraRequest req) {
        String nome = req.nome().trim();
        String cnpj = Normalizador.textoOuNulo(Normalizador.somenteDigitos(req.cnpj()));
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe editora com o nome " + nome);
        }
        if (cnpj != null && repository.existsByCnpj(cnpj)) {
            throw new ConflitoException("Já existe editora com este CNPJ.");
        }
        return repository.save(new Editora(nome, cnpj, Normalizador.textoOuNulo(req.site())));
    }

    @Transactional
    public Editora atualizar(Long id, EditoraRequest req) {
        Editora editora = buscar(id);
        String nome = req.nome().trim();
        String cnpj = Normalizador.textoOuNulo(Normalizador.somenteDigitos(req.cnpj()));
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe editora com o nome " + nome);
        }
        if (cnpj != null && repository.existsByCnpjAndIdNot(cnpj, id)) {
            throw new ConflitoException("Já existe editora com este CNPJ.");
        }
        editora.atualizar(nome, cnpj, Normalizador.textoOuNulo(req.site()));
        return editora;
    }

    @Transactional(readOnly = true)
    public Editora buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Editora", id));
    }

    @Transactional(readOnly = true)
    public Page<Editora> listar(String nome, Pageable pageable) {
        return nome == null || nome.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
        repository.flush();
    }
}

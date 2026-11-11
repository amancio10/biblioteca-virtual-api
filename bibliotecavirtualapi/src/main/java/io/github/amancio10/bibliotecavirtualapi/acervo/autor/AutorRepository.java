package io.github.amancio10.bibliotecavirtualapi.acervo.autor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    Page<Autor> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}

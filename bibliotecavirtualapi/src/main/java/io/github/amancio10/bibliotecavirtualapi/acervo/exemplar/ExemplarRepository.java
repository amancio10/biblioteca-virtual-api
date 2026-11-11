package io.github.amancio10.bibliotecavirtualapi.acervo.exemplar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExemplarRepository extends JpaRepository<Exemplar, Long> {

    Optional<Exemplar> findByCodigoTombo(String codigoTombo);

    boolean existsByCodigoTombo(String codigoTombo);

    List<Exemplar> findByLivroIdOrderByCodigoTombo(Long livroId);

    Optional<Exemplar> findFirstByLivroIdAndStatusOrderByIdAsc(Long livroId, StatusExemplar status);

    long countByLivroId(Long livroId);

    long countByLivroIdAndStatus(Long livroId, StatusExemplar status);

    long countByLivroIdAndStatusNotIn(Long livroId, Collection<StatusExemplar> status);

    boolean existsByLivroIdAndStatusIn(Long livroId, Collection<StatusExemplar> status);
}

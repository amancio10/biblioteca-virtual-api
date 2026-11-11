package io.github.amancio10.bibliotecavirtualapi.leitor;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LeitorRepository extends JpaRepository<Leitor, Long>, JpaSpecificationExecutor<Leitor> {

    boolean existsByCpf(String cpf);

    @Query(value = "select nextval('leitor_carteirinha_seq')", nativeQuery = true)
    long proximoSequencialCarteirinha();

    /**
     * Lock pessimista no leitor durante empréstimo/reserva: serializa operações
     * concorrentes do mesmo leitor para que os limites (máx. empréstimos/reservas)
     * não sejam ultrapassados por requisições simultâneas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Leitor l join fetch l.usuario where l.id = :id")
    Optional<Leitor> findByIdParaAtualizacao(Long id);
}

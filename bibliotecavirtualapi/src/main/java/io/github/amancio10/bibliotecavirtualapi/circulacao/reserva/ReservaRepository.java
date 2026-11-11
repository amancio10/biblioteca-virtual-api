package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    long countByLeitorIdAndStatusIn(Long leitorId, Collection<StatusReserva> status);

    Optional<Reserva> findFirstByLeitorIdAndLivroIdAndStatusIn(Long leitorId, Long livroId, Collection<StatusReserva> status);

    /** Próximo da fila (FIFO por data da reserva). */
    Optional<Reserva> findFirstByLivroIdAndStatusOrderByDataReservaAscIdAsc(Long livroId, StatusReserva status);

    Optional<Reserva> findByExemplarIdAndStatus(Long exemplarId, StatusReserva status);

    boolean existsByLivroIdAndStatus(Long livroId, StatusReserva status);

    long countByLivroIdAndStatus(Long livroId, StatusReserva status);

    List<Reserva> findByStatusAndExpiraEmBefore(StatusReserva status, OffsetDateTime limite);

    /** Posição na fila = reservas aguardando criadas antes desta + 1. */
    @Query("""
            select count(r) + 1 from Reserva r
            where r.livro.id = :livroId
              and r.status = io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.StatusReserva.AGUARDANDO
              and (r.dataReserva < :dataReserva or (r.dataReserva = :dataReserva and r.id < :id))
            """)
    long posicaoNaFila(Long livroId, OffsetDateTime dataReserva, Long id);
}

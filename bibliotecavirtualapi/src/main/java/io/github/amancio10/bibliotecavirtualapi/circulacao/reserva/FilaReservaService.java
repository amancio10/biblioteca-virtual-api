package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.Exemplar;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.ExemplarRepository;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.StatusExemplar;
import io.github.amancio10.bibliotecavirtualapi.circulacao.politica.PoliticaEmprestimoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Ponto único que decide o destino de um exemplar liberado
 * (devolução, novo exemplar, cancelamento/expiração de reserva, fim de manutenção):
 * vai para o próximo da fila de reservas ou volta para a estante.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilaReservaService {

    private final ReservaRepository reservaRepository;
    private final ExemplarRepository exemplarRepository;
    private final PoliticaEmprestimoService politicaService;
    private final Clock clock;

    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Reserva> liberarExemplar(Exemplar exemplar) {
        Optional<Reserva> proxima = reservaRepository
                .findFirstByLivroIdAndStatusOrderByDataReservaAscIdAsc(exemplar.getLivro().getId(), StatusReserva.AGUARDANDO);

        if (proxima.isEmpty()) {
            if (!exemplar.isDisponivel()) {
                exemplar.disponibilizar();
            }
            return Optional.empty();
        }

        Reserva reserva = proxima.get();
        OffsetDateTime agora = OffsetDateTime.now(clock);
        int horas = politicaService.buscar(reserva.getLeitor().getTipo()).getHorasRetiradaReserva();

        exemplar.reservar();
        reserva.disponibilizar(exemplar, agora, agora.plusHours(horas));
        log.info("Exemplar {} separado para a reserva {} (leitor {})",
                exemplar.getCodigoTombo(), reserva.getId(), reserva.getLeitor().getId());
        // ponto de extensão: notificar leitor (e-mail) via evento
        return proxima;
    }

    /** Se houver exemplar da obra na estante, separa para o primeiro da fila. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Reserva> separarExemplarDisponivel(Long livroId) {
        return exemplarRepository.findFirstByLivroIdAndStatusOrderByIdAsc(livroId, StatusExemplar.DISPONIVEL)
                .flatMap(this::liberarExemplar);
    }
}

package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.dto;

import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.Reserva;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.StatusReserva;

import java.time.OffsetDateTime;

/**
 * {@code posicaoFila} só é preenchida na consulta individual de reservas AGUARDANDO.
 * {@code codigoTombo} indica o exemplar separado quando a reserva está DISPONIVEL.
 */
public record ReservaResponse(
        Long id,
        Long leitorId,
        String nomeLeitor,
        Long livroId,
        String tituloLivro,
        StatusReserva status,
        Long posicaoFila,
        String codigoTombo,
        OffsetDateTime dataReserva,
        OffsetDateTime disponivelEm,
        OffsetDateTime expiraEm,
        OffsetDateTime finalizadaEm
) {

    public static ReservaResponse de(Reserva r) {
        return de(r, null);
    }

    public static ReservaResponse de(Reserva r, Long posicaoFila) {
        return new ReservaResponse(
                r.getId(),
                r.getLeitor().getId(),
                r.getLeitor().getNome(),
                r.getLivro().getId(),
                r.getLivro().getTitulo(),
                r.getStatus(),
                posicaoFila,
                r.getExemplar() == null ? null : r.getExemplar().getCodigoTombo(),
                r.getDataReserva(),
                r.getDisponivelEm(),
                r.getExpiraEm(),
                r.getFinalizadaEm()
        );
    }
}

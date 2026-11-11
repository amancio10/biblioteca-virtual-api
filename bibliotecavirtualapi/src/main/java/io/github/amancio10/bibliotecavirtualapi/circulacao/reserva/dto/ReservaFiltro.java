package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.dto;

import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.StatusReserva;

public record ReservaFiltro(Long leitorId, Long livroId, StatusReserva status) {
}

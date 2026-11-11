package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.dto;

import jakarta.validation.constraints.NotNull;

public record ReservaRequest(
        @NotNull Long leitorId,
        @NotNull Long livroId
) {
}

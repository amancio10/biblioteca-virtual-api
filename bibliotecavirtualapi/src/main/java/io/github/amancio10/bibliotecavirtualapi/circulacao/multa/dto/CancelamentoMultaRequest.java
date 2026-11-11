package io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelamentoMultaRequest(@NotBlank @Size(max = 255) String motivo) {
}

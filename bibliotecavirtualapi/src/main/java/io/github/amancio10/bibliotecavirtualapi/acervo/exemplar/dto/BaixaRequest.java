package io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BaixaRequest(@NotBlank @Size(max = 255) String motivo) {
}

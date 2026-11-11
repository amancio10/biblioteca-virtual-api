package io.github.amancio10.bibliotecavirtualapi.leitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BloqueioRequest(@NotBlank @Size(max = 255) String motivo) {
}

package io.github.amancio10.bibliotecavirtualapi.acervo.categoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 80) String nome,
        @Size(max = 255) String descricao
) {
}

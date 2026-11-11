package io.github.amancio10.bibliotecavirtualapi.acervo.autor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AutorRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 80) String nacionalidade,
        @Past LocalDate dataNascimento,
        String biografia
) {
}

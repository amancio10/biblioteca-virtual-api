package io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.EstadoConservacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ExemplarRequest(
        @NotBlank @Size(max = 30) String codigoTombo,
        EstadoConservacao estadoConservacao,
        @Size(max = 50) String localizacao,
        @PastOrPresent LocalDate dataAquisicao,
        @Size(max = 255) String observacao
) {
}

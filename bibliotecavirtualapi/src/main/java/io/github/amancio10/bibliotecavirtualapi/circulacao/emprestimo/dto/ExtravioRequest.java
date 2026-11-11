package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ExtravioRequest(
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal valorReposicao,
        @Size(max = 255) String observacao
) {
}

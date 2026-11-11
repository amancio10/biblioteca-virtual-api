package io.github.amancio10.bibliotecavirtualapi.circulacao.politica.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PoliticaEmprestimoRequest(
        @NotNull @Positive @Max(365) Integer prazoDias,
        @NotNull @Positive @Max(100) Integer maxEmprestimosSimultaneos,
        @NotNull @PositiveOrZero @Max(20) Integer maxRenovacoes,
        @NotNull @PositiveOrZero @Max(50) Integer maxReservasSimultaneas,
        @NotNull @Positive @Max(720) Integer horasRetiradaReserva,
        @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal valorMultaDiaria,
        @NotNull @PositiveOrZero @Max(30) Integer diasSuspensaoPorAtraso
) {
}

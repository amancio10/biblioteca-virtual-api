package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** O exemplar é identificado pelo tombo (código de barras lido no balcão). */
public record EmprestimoRequest(
        @NotNull Long leitorId,
        @NotBlank @Size(max = 30) String codigoTombo
) {
}

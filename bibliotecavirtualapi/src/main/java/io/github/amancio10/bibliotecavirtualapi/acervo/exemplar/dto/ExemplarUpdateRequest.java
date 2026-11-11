package io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.EstadoConservacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Status não é alterado aqui: usa os endpoints de transição (manutenção, baixa...). */
public record ExemplarUpdateRequest(
        @NotNull EstadoConservacao estadoConservacao,
        @Size(max = 50) String localizacao,
        @Size(max = 255) String observacao
) {
}

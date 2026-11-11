package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto;

import jakarta.validation.constraints.Size;

public record DevolucaoRequest(@Size(max = 255) String observacao) {
}

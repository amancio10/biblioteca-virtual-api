package io.github.amancio10.bibliotecavirtualapi.acervo.editora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.br.CNPJ;

public record EditoraRequest(
        @NotBlank @Size(max = 150) String nome,
        @CNPJ String cnpj,
        @URL @Size(max = 255) String site
) {
}

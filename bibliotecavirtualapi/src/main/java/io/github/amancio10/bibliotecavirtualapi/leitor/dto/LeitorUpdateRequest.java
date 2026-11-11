package io.github.amancio10.bibliotecavirtualapi.leitor.dto;

import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** CPF, e-mail e data de nascimento não são alteráveis por aqui. */
public record LeitorUpdateRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 20) String telefone,
        @NotNull TipoLeitor tipo,
        @Valid EnderecoDto endereco
) {
}

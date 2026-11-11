package io.github.amancio10.bibliotecavirtualapi.leitor.dto;

import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record LeitorCreateRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres") String senha,
        @NotBlank @CPF String cpf,
        @NotNull @Past LocalDate dataNascimento,
        @Size(max = 20) String telefone,
        TipoLeitor tipo,
        @Valid EnderecoDto endereco
) {
}

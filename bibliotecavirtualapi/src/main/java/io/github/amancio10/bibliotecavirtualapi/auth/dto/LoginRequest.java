package io.github.amancio10.bibliotecavirtualapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 72) String senha
) {

    /** Evita que a senha apareça em logs caso o objeto seja impresso. */
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", senha=***]";
    }
}

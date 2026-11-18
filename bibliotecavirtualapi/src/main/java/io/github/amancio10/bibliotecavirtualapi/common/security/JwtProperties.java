package io.github.amancio10.bibliotecavirtualapi.common.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configurações do JWT lidas do application.yaml (prefixo biblioteca.security.jwt).
 * A validação faz a aplicação falhar na subida se o segredo for curto demais.
 *
 * @param secret    chave HMAC usada para assinar/validar o token (HS256 exige no mínimo 256 bits = 32 bytes)
 * @param expiracao tempo de vida do token (ex.: 2h, 30m)
 * @param emissor   valor do claim "iss" — identifica quem emitiu o token
 */
@Validated
@ConfigurationProperties(prefix = "biblioteca.security.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "deve ter no mínimo 32 caracteres (HS256)") String secret,
        @NotNull Duration expiracao,
        @NotBlank String emissor
) {
}

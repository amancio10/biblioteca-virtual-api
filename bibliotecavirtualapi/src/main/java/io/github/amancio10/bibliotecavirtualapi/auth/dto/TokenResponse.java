package io.github.amancio10.bibliotecavirtualapi.auth.dto;

import io.github.amancio10.bibliotecavirtualapi.auth.TokenService.TokenGerado;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import io.github.amancio10.bibliotecavirtualapi.usuario.dto.UsuarioResponse;

import java.time.Duration;
import java.time.Instant;

/**
 * Resposta do login.
 *
 * @param accessToken      o JWT — enviar nas próximas requisições no header "Authorization: Bearer &lt;token&gt;"
 * @param tipo             sempre "Bearer"
 * @param expiraEmSegundos tempo de vida restante do token
 * @param expiraEm         instante exato da expiração (UTC)
 * @param usuario          dados do usuário logado (para o front exibir nome/perfil)
 */
public record TokenResponse(
        String accessToken,
        String tipo,
        long expiraEmSegundos,
        Instant expiraEm,
        UsuarioResponse usuario
) {

    public static TokenResponse de(TokenGerado token, Usuario usuario, Instant agora) {
        long segundos = Duration.between(agora, token.expiraEm()).toSeconds();
        return new TokenResponse(token.valor(), "Bearer", segundos, token.expiraEm(), UsuarioResponse.de(usuario));
    }
}

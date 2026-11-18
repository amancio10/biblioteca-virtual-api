package io.github.amancio10.bibliotecavirtualapi.auth;

import io.github.amancio10.bibliotecavirtualapi.common.security.JwtProperties;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

/** Emite o JWT de um usuário autenticado. */
@Service
@RequiredArgsConstructor
public class TokenService {

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_PERFIL = "perfil";

    private final JwtEncoder encoder;
    private final JwtProperties props;
    private final Clock clock;

    public TokenGerado gerar(Usuario usuario) {
        Instant agora = clock.instant();
        Instant expiraEm = agora.plus(props.expiracao());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.emissor())                         // iss: quem emitiu
                .subject(usuario.getId().toString())              // sub: id do usuário (identidade principal)
                .issuedAt(agora)                                  // iat: quando foi emitido
                .expiresAt(expiraEm)                              // exp: até quando vale
                .claim(CLAIM_EMAIL, usuario.getEmail())
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())  // usado na Parte 3 para as permissões
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenGerado(token, expiraEm);
    }

    public record TokenGerado(String valor, Instant expiraEm) {
    }
}

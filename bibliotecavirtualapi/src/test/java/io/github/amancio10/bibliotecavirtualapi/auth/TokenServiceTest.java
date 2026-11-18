package io.github.amancio10.bibliotecavirtualapi.auth;

import io.github.amancio10.bibliotecavirtualapi.common.security.JwtProperties;
import io.github.amancio10.bibliotecavirtualapi.common.security.SecurityConfig;
import io.github.amancio10.bibliotecavirtualapi.usuario.Perfil;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testa geração e validação do JWT sem subir o Spring nem o banco. */
class TokenServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres!";

    private final SecurityConfig config = new SecurityConfig();
    private final JwtProperties props = new JwtProperties(SEGREDO, Duration.ofHours(2), "bibliotecavirtualapi");

    private SecretKey chave;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        chave = config.jwtSecretKey(props);
        usuario = new Usuario("Maria Bibliotecária", "Maria@Biblioteca.com", "hash", Perfil.BIBLIOTECARIO);
        ReflectionTestUtils.setField(usuario, "id", 42L);
    }

    @Test
    void geraTokenComClaimsEsperadas() {
        Clock agora = Clock.systemUTC();
        TokenService service = new TokenService(config.jwtEncoder(chave), props, agora);

        TokenService.TokenGerado gerado = service.gerar(usuario);
        Jwt jwt = config.jwtDecoder(chave).decode(gerado.valor());

        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("bibliotecavirtualapi");
        assertThat(jwt.getClaimAsString(TokenService.CLAIM_EMAIL)).isEqualTo("maria@biblioteca.com");
        assertThat(jwt.getClaimAsString(TokenService.CLAIM_PERFIL)).isEqualTo("BIBLIOTECARIO");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void rejeitaTokenExpirado() {
        // relógio no passado: o token nasce já vencido
        Clock ontem = Clock.fixed(Instant.now().minus(Duration.ofDays(1)), ZoneOffset.UTC);
        String token = new TokenService(config.jwtEncoder(chave), props, ontem).gerar(usuario).valor();

        JwtDecoder decoder = config.jwtDecoder(chave);
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaTokenAssinadoComOutraChave() {
        var outraChave = config.jwtSecretKey(new JwtProperties("outra-chave-diferente-com-32-caracteres-ou-mais", Duration.ofHours(2), "x"));
        String token = new TokenService(config.jwtEncoder(outraChave), props, Clock.systemUTC()).gerar(usuario).valor();

        JwtDecoder decoder = config.jwtDecoder(chave);
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }
}

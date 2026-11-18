package io.github.amancio10.bibliotecavirtualapi.auth;

import io.github.amancio10.bibliotecavirtualapi.auth.dto.LoginRequest;
import io.github.amancio10.bibliotecavirtualapi.auth.dto.TokenResponse;
import io.github.amancio10.bibliotecavirtualapi.common.exception.AcessoNegadoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.CredenciaisInvalidasException;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import io.github.amancio10.bibliotecavirtualapi.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final Clock clock;

    /**
     * Hash "de mentira" comparado quando o e-mail não existe. Assim o tempo de resposta
     * é parecido com o de uma senha errada, e ninguém descobre quais e-mails estão
     * cadastrados medindo a demora da API (ataque de enumeração por tempo).
     */
    private final String hashFicticio;

    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder,
                       TokenService tokenService, Clock clock) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.clock = clock;
        this.hashFicticio = passwordEncoder.encode("senha-ficticia-para-equalizar-tempo");
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        Optional<Usuario> encontrado = repository.findByEmailIgnoreCase(req.email().trim());

        String hash = encontrado.map(Usuario::getSenhaHash).orElse(hashFicticio);
        boolean senhaConfere = passwordEncoder.matches(req.senha(), hash);

        // Mesma mensagem para "e-mail não existe" e "senha errada": não revela qual dos dois falhou
        if (encontrado.isEmpty() || !senhaConfere) {
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = encontrado.get();
        if (!usuario.isAtivo()) {
            throw new AcessoNegadoException("Usuário desativado. Procure a administração da biblioteca.");
        }

        usuario.registrarLogin(OffsetDateTime.now(clock)); // salvo no commit (entidade gerenciada)
        return TokenResponse.de(tokenService.gerar(usuario), usuario, clock.instant());
    }
}

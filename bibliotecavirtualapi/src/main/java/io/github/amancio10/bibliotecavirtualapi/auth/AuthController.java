package io.github.amancio10.bibliotecavirtualapi.auth;

import io.github.amancio10.bibliotecavirtualapi.auth.dto.LoginRequest;
import io.github.amancio10.bibliotecavirtualapi.auth.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    @Operation(summary = "Autentica com e-mail e senha e devolve um token JWT")
    public TokenResponse login(@RequestBody @Valid LoginRequest req) {
        return service.login(req);
    }
}

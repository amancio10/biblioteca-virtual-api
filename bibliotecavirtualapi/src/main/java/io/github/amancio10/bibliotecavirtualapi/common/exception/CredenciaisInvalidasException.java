package io.github.amancio10.bibliotecavirtualapi.common.exception;

/** E-mail ou senha incorretos no login (HTTP 401). A mensagem é genérica de propósito. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }
}

package io.github.amancio10.bibliotecavirtualapi.common.exception;

/** Recurso duplicado ou em estado conflitante (HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}

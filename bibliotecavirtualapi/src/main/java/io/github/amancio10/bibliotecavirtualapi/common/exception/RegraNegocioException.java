package io.github.amancio10.bibliotecavirtualapi.common.exception;

/** Violação de regra de negócio (HTTP 422). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}

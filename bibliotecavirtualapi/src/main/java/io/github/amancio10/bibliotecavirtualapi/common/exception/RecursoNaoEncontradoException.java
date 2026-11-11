package io.github.amancio10.bibliotecavirtualapi.common.exception;

/** Recurso inexistente (HTTP 404). */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super("%s não encontrado(a): %s".formatted(recurso, id));
    }
}

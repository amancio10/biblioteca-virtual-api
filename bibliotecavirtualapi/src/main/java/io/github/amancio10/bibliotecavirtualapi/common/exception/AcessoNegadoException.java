package io.github.amancio10.bibliotecavirtualapi.common.exception;

/** Usuário identificado, mas sem permissão para a operação (HTTP 403). */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}

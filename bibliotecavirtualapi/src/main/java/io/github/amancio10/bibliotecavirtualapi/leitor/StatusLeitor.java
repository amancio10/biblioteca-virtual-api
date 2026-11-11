package io.github.amancio10.bibliotecavirtualapi.leitor;

public enum StatusLeitor {
    /** Pode emprestar e reservar. */
    ATIVO,
    /** Suspenso temporariamente por atraso (até a data {@code suspensoAte}). */
    SUSPENSO,
    /** Bloqueado manualmente pela biblioteca (ex.: extravio não ressarcido). */
    BLOQUEADO,
    /** Cadastro encerrado. */
    INATIVO
}

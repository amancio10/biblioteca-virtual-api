package io.github.amancio10.bibliotecavirtualapi.acervo.exemplar;

public enum StatusExemplar {
    DISPONIVEL,
    EMPRESTADO,
    /** Separado para um leitor com reserva (aguardando retirada). */
    RESERVADO,
    EM_MANUTENCAO,
    EXTRAVIADO,
    /** Retirado definitivamente do acervo. */
    BAIXADO
}

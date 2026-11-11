package io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto;

/** Resumo de disponibilidade de uma obra para o catálogo. */
public record DisponibilidadeResponse(
        Long livroId,
        long totalExemplares,
        long disponiveis,
        long emprestados,
        long reservasNaFila
) {
}

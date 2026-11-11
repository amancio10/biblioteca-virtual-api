package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto;

import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.StatusEmprestimo;

/** {@code atrasados=true} retorna apenas empréstimos ativos com prazo vencido. */
public record EmprestimoFiltro(
        Long leitorId,
        Long livroId,
        StatusEmprestimo status,
        Boolean atrasados
) {
}

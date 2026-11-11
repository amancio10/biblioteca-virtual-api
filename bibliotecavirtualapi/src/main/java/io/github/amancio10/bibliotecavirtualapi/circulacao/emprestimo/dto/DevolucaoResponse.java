package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto;

import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto.MultaResponse;

import java.time.LocalDate;

/**
 * Resultado da devolução: empréstimo encerrado, multa gerada (se houve atraso),
 * suspensão aplicada e se o exemplar foi separado para a próxima reserva.
 */
public record DevolucaoResponse(
        EmprestimoResponse emprestimo,
        MultaResponse multa,
        LocalDate leitorSuspensoAte,
        Long exemplarSeparadoParaReservaId
) {
}

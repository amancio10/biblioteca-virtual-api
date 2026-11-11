package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto;

import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.Emprestimo;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.Renovacao;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.StatusEmprestimo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record EmprestimoResponse(
        Long id,
        Long leitorId,
        String nomeLeitor,
        String numeroCarteirinha,
        Long exemplarId,
        String codigoTombo,
        Long livroId,
        String tituloLivro,
        Long reservaId,
        StatusEmprestimo status,
        OffsetDateTime dataEmprestimo,
        LocalDate dataPrevistaDevolucao,
        OffsetDateTime dataDevolucao,
        boolean atrasado,
        long diasAtraso,
        Integer quantidadeRenovacoes,
        String observacao,
        List<RenovacaoResponse> renovacoes
) {

    public record RenovacaoResponse(OffsetDateTime dataRenovacao, LocalDate dataPrevistaAntiga,
                                    LocalDate dataPrevistaNova, Long renovadoPorId) {

        static RenovacaoResponse de(Renovacao r) {
            return new RenovacaoResponse(r.getDataRenovacao(), r.getDataPrevistaAntiga(),
                    r.getDataPrevistaNova(), r.getRenovadoPor().getId());
        }
    }

    /** {@code hoje} vem do Clock da aplicação, para o cálculo de atraso ser testável. */
    public static EmprestimoResponse de(Emprestimo e, LocalDate hoje) {
        var exemplar = e.getExemplar();
        var livro = exemplar.getLivro();
        return new EmprestimoResponse(
                e.getId(),
                e.getLeitor().getId(),
                e.getLeitor().getNome(),
                e.getLeitor().getNumeroCarteirinha(),
                exemplar.getId(),
                exemplar.getCodigoTombo(),
                livro.getId(),
                livro.getTitulo(),
                e.getReserva() == null ? null : e.getReserva().getId(),
                e.getStatus(),
                e.getDataEmprestimo(),
                e.getDataPrevistaDevolucao(),
                e.getDataDevolucao(),
                e.isAtrasado(hoje),
                e.isAtivo() ? e.diasAtraso(hoje) : 0,
                e.getQuantidadeRenovacoes(),
                e.getObservacao(),
                e.getRenovacoes().stream().map(RenovacaoResponse::de).toList()
        );
    }
}

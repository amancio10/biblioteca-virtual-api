package io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto;

import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.MotivoMulta;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.Multa;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.StatusMulta;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MultaResponse(
        Long id,
        Long emprestimoId,
        Long leitorId,
        String nomeLeitor,
        MotivoMulta motivo,
        Integer diasAtraso,
        BigDecimal valor,
        StatusMulta status,
        OffsetDateTime dataPagamento,
        String observacao,
        OffsetDateTime criadoEm
) {

    public static MultaResponse de(Multa m) {
        return new MultaResponse(
                m.getId(),
                m.getEmprestimo().getId(),
                m.getLeitor().getId(),
                m.getLeitor().getNome(),
                m.getMotivo(),
                m.getDiasAtraso(),
                m.getValor(),
                m.getStatus(),
                m.getDataPagamento(),
                m.getObservacao(),
                m.getCriadoEm()
        );
    }
}

package io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.dto;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.EstadoConservacao;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.Exemplar;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.StatusExemplar;

import java.time.LocalDate;

public record ExemplarResponse(
        Long id,
        Long livroId,
        String tituloLivro,
        String codigoTombo,
        StatusExemplar status,
        EstadoConservacao estadoConservacao,
        String localizacao,
        LocalDate dataAquisicao,
        String observacao
) {

    public static ExemplarResponse de(Exemplar e) {
        return new ExemplarResponse(
                e.getId(),
                e.getLivro().getId(),
                e.getLivro().getTitulo(),
                e.getCodigoTombo(),
                e.getStatus(),
                e.getEstadoConservacao(),
                e.getLocalizacao(),
                e.getDataAquisicao(),
                e.getObservacao()
        );
    }
}

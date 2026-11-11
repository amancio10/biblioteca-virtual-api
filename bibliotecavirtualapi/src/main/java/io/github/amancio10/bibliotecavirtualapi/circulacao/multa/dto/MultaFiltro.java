package io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto;

import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.MotivoMulta;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.StatusMulta;

public record MultaFiltro(Long leitorId, StatusMulta status, MotivoMulta motivo) {
}

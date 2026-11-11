package io.github.amancio10.bibliotecavirtualapi.leitor.dto;

import io.github.amancio10.bibliotecavirtualapi.leitor.StatusLeitor;
import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;

/** Filtros opcionais da listagem de leitores (query params). */
public record LeitorFiltro(
        String nome,
        String cpf,
        String carteirinha,
        StatusLeitor status,
        TipoLeitor tipo
) {
}

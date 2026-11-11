package io.github.amancio10.bibliotecavirtualapi.acervo.categoria.dto;

import io.github.amancio10.bibliotecavirtualapi.acervo.categoria.Categoria;

public record CategoriaResponse(Long id, String nome, String descricao) {

    public static CategoriaResponse de(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao());
    }
}

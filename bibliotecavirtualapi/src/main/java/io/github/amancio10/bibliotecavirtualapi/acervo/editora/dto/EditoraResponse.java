package io.github.amancio10.bibliotecavirtualapi.acervo.editora.dto;

import io.github.amancio10.bibliotecavirtualapi.acervo.editora.Editora;

public record EditoraResponse(Long id, String nome, String cnpj, String site) {

    public static EditoraResponse de(Editora e) {
        return new EditoraResponse(e.getId(), e.getNome(), e.getCnpj(), e.getSite());
    }
}

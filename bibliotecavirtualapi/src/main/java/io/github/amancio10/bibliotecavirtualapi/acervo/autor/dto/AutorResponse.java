package io.github.amancio10.bibliotecavirtualapi.acervo.autor.dto;

import io.github.amancio10.bibliotecavirtualapi.acervo.autor.Autor;

import java.time.LocalDate;

public record AutorResponse(
        Long id,
        String nome,
        String nacionalidade,
        LocalDate dataNascimento,
        String biografia
) {

    public static AutorResponse de(Autor a) {
        return new AutorResponse(a.getId(), a.getNome(), a.getNacionalidade(), a.getDataNascimento(), a.getBiografia());
    }
}

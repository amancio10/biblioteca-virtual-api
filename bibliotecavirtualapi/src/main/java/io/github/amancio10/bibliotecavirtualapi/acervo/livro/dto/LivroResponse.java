package io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto;

import io.github.amancio10.bibliotecavirtualapi.acervo.autor.Autor;
import io.github.amancio10.bibliotecavirtualapi.acervo.categoria.Categoria;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.Livro;

import java.util.Comparator;
import java.util.List;

public record LivroResponse(
        Long id,
        String isbn,
        String titulo,
        String subtitulo,
        String sinopse,
        Referencia editora,
        Integer anoPublicacao,
        Integer edicao,
        Integer numeroPaginas,
        String idioma,
        String capaUrl,
        boolean ativo,
        List<Referencia> autores,
        List<Referencia> categorias
) {

    /** Representação resumida de entidades relacionadas. */
    public record Referencia(Long id, String nome) {
    }

    public static LivroResponse de(Livro l) {
        return new LivroResponse(
                l.getId(),
                l.getIsbn(),
                l.getTitulo(),
                l.getSubtitulo(),
                l.getSinopse(),
                l.getEditora() == null ? null : new Referencia(l.getEditora().getId(), l.getEditora().getNome()),
                l.getAnoPublicacao(),
                l.getEdicao(),
                l.getNumeroPaginas(),
                l.getIdioma(),
                l.getCapaUrl(),
                l.isAtivo(),
                l.getAutores().stream().map(LivroResponse::ref).toList(),
                l.getCategorias().stream()
                        .sorted(Comparator.comparing(Categoria::getNome))
                        .map(c -> new Referencia(c.getId(), c.getNome()))
                        .toList()
        );
    }

    private static Referencia ref(Autor a) {
        return new Referencia(a.getId(), a.getNome());
    }
}

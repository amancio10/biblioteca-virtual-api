package io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto;

/** Filtros opcionais do catálogo (query params). */
public record LivroFiltro(
        String titulo,
        String isbn,
        String autor,
        Long categoriaId,
        Long editoraId,
        Boolean incluirInativos
) {
}

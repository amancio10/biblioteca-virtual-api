package io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.URL;

import java.util.List;
import java.util.Set;

public record LivroRequest(
        @NotBlank @ISBN(type = ISBN.Type.ANY) String isbn,
        @NotBlank @Size(max = 255) String titulo,
        @Size(max = 255) String subtitulo,
        String sinopse,
        Long editoraId,
        @Min(1400) @Max(2100) Integer anoPublicacao,
        @Positive Integer edicao,
        @Positive Integer numeroPaginas,
        @Size(max = 5) String idioma,
        @URL @Size(max = 500) String capaUrl,
        /* ordem da lista = ordem de autoria */
        @NotEmpty List<@NotNull Long> autorIds,
        Set<@NotNull Long> categoriaIds
) {
}

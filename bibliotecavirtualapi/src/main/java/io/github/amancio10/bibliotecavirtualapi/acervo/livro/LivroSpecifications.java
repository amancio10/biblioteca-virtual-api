package io.github.amancio10.bibliotecavirtualapi.acervo.livro;

import io.github.amancio10.bibliotecavirtualapi.acervo.autor.Autor;
import io.github.amancio10.bibliotecavirtualapi.acervo.categoria.Categoria;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.dto.LivroFiltro;
import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtros por autor/categoria usam EXISTS (subquery) em vez de JOIN para não
 * duplicar linhas na paginação.
 */
final class LivroSpecifications {

    private LivroSpecifications() {
    }

    static Specification<Livro> porFiltro(LivroFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (filtro.titulo() != null && !filtro.titulo().isBlank()) {
                p.add(cb.like(cb.lower(root.get("titulo")), "%" + filtro.titulo().trim().toLowerCase() + "%"));
            }
            if (filtro.isbn() != null && !filtro.isbn().isBlank()) {
                p.add(cb.equal(root.get("isbn"), Normalizador.isbn(filtro.isbn())));
            }
            if (filtro.editoraId() != null) {
                p.add(cb.equal(root.get("editora").get("id"), filtro.editoraId()));
            }
            if (filtro.autor() != null && !filtro.autor().isBlank()) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<Livro> l = sub.from(Livro.class);
                Join<Livro, Autor> a = l.join("autores");
                sub.select(l.get("id")).where(
                        cb.equal(l.get("id"), root.get("id")),
                        cb.like(cb.lower(a.get("nome")), "%" + filtro.autor().trim().toLowerCase() + "%"));
                p.add(cb.exists(sub));
            }
            if (filtro.categoriaId() != null) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<Livro> l = sub.from(Livro.class);
                Join<Livro, Categoria> c = l.join("categorias");
                sub.select(l.get("id")).where(
                        cb.equal(l.get("id"), root.get("id")),
                        cb.equal(c.get("id"), filtro.categoriaId()));
                p.add(cb.exists(sub));
            }
            if (!Boolean.TRUE.equals(filtro.incluirInativos())) {
                p.add(cb.isTrue(root.get("ativo")));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}

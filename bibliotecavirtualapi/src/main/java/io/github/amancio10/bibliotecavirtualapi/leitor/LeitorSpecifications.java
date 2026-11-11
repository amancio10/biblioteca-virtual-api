package io.github.amancio10.bibliotecavirtualapi.leitor;

import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.LeitorFiltro;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class LeitorSpecifications {

    private LeitorSpecifications() {
    }

    static Specification<Leitor> porFiltro(LeitorFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            Join<Leitor, Usuario> usuario = root.join("usuario");

            if (filtro.nome() != null && !filtro.nome().isBlank()) {
                predicados.add(cb.like(cb.lower(usuario.get("nome")), "%" + filtro.nome().trim().toLowerCase() + "%"));
            }
            if (filtro.cpf() != null && !filtro.cpf().isBlank()) {
                predicados.add(cb.equal(root.get("cpf"), Normalizador.somenteDigitos(filtro.cpf())));
            }
            if (filtro.carteirinha() != null && !filtro.carteirinha().isBlank()) {
                predicados.add(cb.equal(root.get("numeroCarteirinha"), filtro.carteirinha().trim().toUpperCase()));
            }
            if (filtro.status() != null) {
                predicados.add(cb.equal(root.get("status"), filtro.status()));
            }
            if (filtro.tipo() != null) {
                predicados.add(cb.equal(root.get("tipo"), filtro.tipo()));
            }
            return cb.and(predicados.toArray(Predicate[]::new));
        };
    }
}

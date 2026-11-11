package io.github.amancio10.bibliotecavirtualapi.circulacao.multa;

import io.github.amancio10.bibliotecavirtualapi.circulacao.ControleAcessoCirculacao;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto.MultaFiltro;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto.MultaResponse;
import io.github.amancio10.bibliotecavirtualapi.common.exception.AcessoNegadoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.usuario.Perfil;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MultaService {

    private final MultaRepository repository;
    private final ControleAcessoCirculacao controleAcesso;
    private final Clock clock;

    @Transactional(readOnly = true)
    public MultaResponse buscar(Long id) {
        return MultaResponse.de(carregar(id));
    }

    @Transactional(readOnly = true)
    public Page<MultaResponse> listar(MultaFiltro filtro, Pageable pageable) {
        Specification<Multa> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (filtro.leitorId() != null) p.add(cb.equal(root.get("leitor").get("id"), filtro.leitorId()));
            if (filtro.status() != null) p.add(cb.equal(root.get("status"), filtro.status()));
            if (filtro.motivo() != null) p.add(cb.equal(root.get("motivo"), filtro.motivo()));
            return cb.and(p.toArray(Predicate[]::new));
        };
        return repository.findAll(spec, pageable).map(MultaResponse::de);
    }

    @Transactional
    public MultaResponse registrarPagamento(Long id, Long usuarioId) {
        Usuario operador = controleAcesso.exigirOperador(usuarioId);
        Multa multa = carregar(id);
        multa.pagar(OffsetDateTime.now(clock), operador);
        return MultaResponse.de(multa);
    }

    /** Cancelamento (abono) é restrito a administradores. */
    @Transactional
    public MultaResponse cancelar(Long id, Long usuarioId, String motivo) {
        Usuario operador = controleAcesso.exigirOperador(usuarioId);
        if (operador.getPerfil() != Perfil.ADMIN) {
            throw new AcessoNegadoException("Somente administradores podem cancelar multas.");
        }
        Multa multa = carregar(id);
        multa.cancelar(operador, motivo.trim());
        return MultaResponse.de(multa);
    }

    private Multa carregar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Multa", id));
    }
}

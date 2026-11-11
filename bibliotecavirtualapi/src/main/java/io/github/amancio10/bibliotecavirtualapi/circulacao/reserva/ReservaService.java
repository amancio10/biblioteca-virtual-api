package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.Exemplar;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.Livro;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.LivroService;
import io.github.amancio10.bibliotecavirtualapi.circulacao.ControleAcessoCirculacao;
import io.github.amancio10.bibliotecavirtualapi.circulacao.ValidadorPendencias;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.EmprestimoRepository;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.StatusEmprestimo;
import io.github.amancio10.bibliotecavirtualapi.circulacao.politica.PoliticaEmprestimo;
import io.github.amancio10.bibliotecavirtualapi.circulacao.politica.PoliticaEmprestimoService;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.dto.ReservaFiltro;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.dto.ReservaResponse;
import io.github.amancio10.bibliotecavirtualapi.common.exception.ConflitoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.leitor.Leitor;
import io.github.amancio10.bibliotecavirtualapi.leitor.LeitorService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository repository;
    private final EmprestimoRepository emprestimoRepository;
    private final LeitorService leitorService;
    private final LivroService livroService;
    private final PoliticaEmprestimoService politicaService;
    private final FilaReservaService filaReservaService;
    private final ValidadorPendencias validadorPendencias;
    private final ControleAcessoCirculacao controleAcesso;
    private final Clock clock;

    /**
     * Reserva uma obra. Se houver exemplar na estante, ele já é separado
     * (status DISPONIVEL, com prazo para retirada); senão o leitor entra na fila.
     */
    @Transactional
    public ReservaResponse reservar(Long leitorId, Long livroId, Long usuarioId) {
        Leitor leitor = leitorService.buscarParaAtualizacao(leitorId);
        controleAcesso.exigirOperadorOuProprioLeitor(usuarioId, leitor);
        validadorPendencias.validar(leitor, LocalDate.now(clock));

        Livro livro = livroService.buscarEntidade(livroId);
        if (!livro.isAtivo()) {
            throw new RegraNegocioException("Livro inativo no acervo.");
        }
        if (repository.findFirstByLeitorIdAndLivroIdAndStatusIn(leitorId, livroId, StatusReserva.ABERTAS).isPresent()) {
            throw new ConflitoException("Leitor já possui reserva em aberto para esta obra.");
        }
        if (emprestimoRepository.existsByLeitorIdAndExemplarLivroIdAndStatus(leitorId, livroId, StatusEmprestimo.ATIVO)) {
            throw new RegraNegocioException("Leitor já está com um exemplar desta obra.");
        }

        PoliticaEmprestimo politica = politicaService.buscar(leitor.getTipo());
        long abertas = repository.countByLeitorIdAndStatusIn(leitorId, StatusReserva.ABERTAS);
        if (abertas >= politica.getMaxReservasSimultaneas()) {
            throw new RegraNegocioException("Limite de %d reservas simultâneas atingido."
                    .formatted(politica.getMaxReservasSimultaneas()));
        }

        OffsetDateTime agora = OffsetDateTime.now(clock);
        Reserva reserva = repository.save(new Reserva(leitor, livro, agora));

        // exemplar na estante? separa imediatamente (a fila está vazia, senão ele não estaria disponível)
        filaReservaService.separarExemplarDisponivel(livroId);

        return ReservaResponse.de(reserva, posicao(reserva));
    }

    @Transactional
    public ReservaResponse cancelar(Long id, Long usuarioId) {
        Reserva reserva = carregar(id);
        controleAcesso.exigirOperadorOuProprioLeitor(usuarioId, reserva.getLeitor());

        Exemplar separado = reserva.getStatus() == StatusReserva.DISPONIVEL ? reserva.getExemplar() : null;
        reserva.cancelar(OffsetDateTime.now(clock));
        if (separado != null) {
            filaReservaService.liberarExemplar(separado);
        }
        return ReservaResponse.de(reserva);
    }

    /** Executado pelo job: reservas não retiradas no prazo liberam o exemplar para o próximo. */
    @Transactional
    public int expirarVencidas() {
        OffsetDateTime agora = OffsetDateTime.now(clock);
        List<Reserva> vencidas = repository.findByStatusAndExpiraEmBefore(StatusReserva.DISPONIVEL, agora);
        for (Reserva reserva : vencidas) {
            Exemplar exemplar = reserva.getExemplar();
            reserva.expirar(agora);
            filaReservaService.liberarExemplar(exemplar);
            log.info("Reserva {} expirada; exemplar {} liberado", reserva.getId(), exemplar.getCodigoTombo());
        }
        return vencidas.size();
    }

    @Transactional(readOnly = true)
    public ReservaResponse buscar(Long id) {
        Reserva reserva = carregar(id);
        return ReservaResponse.de(reserva, posicao(reserva));
    }

    @Transactional(readOnly = true)
    public Page<ReservaResponse> listar(ReservaFiltro filtro, Pageable pageable) {
        Specification<Reserva> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (filtro.leitorId() != null) p.add(cb.equal(root.get("leitor").get("id"), filtro.leitorId()));
            if (filtro.livroId() != null) p.add(cb.equal(root.get("livro").get("id"), filtro.livroId()));
            if (filtro.status() != null) p.add(cb.equal(root.get("status"), filtro.status()));
            return cb.and(p.toArray(Predicate[]::new));
        };
        return repository.findAll(spec, pageable).map(ReservaResponse::de);
    }

    private Long posicao(Reserva r) {
        return r.getStatus() == StatusReserva.AGUARDANDO
                ? repository.posicaoNaFila(r.getLivro().getId(), r.getDataReserva(), r.getId())
                : null;
    }

    private Reserva carregar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Reserva", id));
    }
}

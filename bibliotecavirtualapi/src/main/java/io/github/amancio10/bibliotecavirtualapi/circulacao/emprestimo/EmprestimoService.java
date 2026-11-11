package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.Exemplar;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.ExemplarService;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.StatusExemplar;
import io.github.amancio10.bibliotecavirtualapi.circulacao.ControleAcessoCirculacao;
import io.github.amancio10.bibliotecavirtualapi.circulacao.ValidadorPendencias;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto.DevolucaoResponse;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto.EmprestimoFiltro;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.dto.EmprestimoResponse;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.dto.MultaResponse;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.Multa;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.MultaRepository;
import io.github.amancio10.bibliotecavirtualapi.circulacao.politica.PoliticaEmprestimo;
import io.github.amancio10.bibliotecavirtualapi.circulacao.politica.PoliticaEmprestimoService;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.FilaReservaService;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.Reserva;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.ReservaRepository;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.StatusReserva;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import io.github.amancio10.bibliotecavirtualapi.leitor.Leitor;
import io.github.amancio10.bibliotecavirtualapi.leitor.LeitorService;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmprestimoService {

    private final EmprestimoRepository repository;
    private final MultaRepository multaRepository;
    private final ReservaRepository reservaRepository;
    private final LeitorService leitorService;
    private final ExemplarService exemplarService;
    private final PoliticaEmprestimoService politicaService;
    private final FilaReservaService filaReservaService;
    private final ValidadorPendencias validadorPendencias;
    private final ControleAcessoCirculacao controleAcesso;
    private final Clock clock;

    /**
     * Regras do empréstimo:
     * <ol>
     *   <li>operador ativo; leitor apto (ativo, sem multas pendentes, sem atrasos);</li>
     *   <li>limite de empréstimos simultâneos da política do tipo de leitor;</li>
     *   <li>leitor não pode ter dois exemplares da mesma obra;</li>
     *   <li>exemplar DISPONIVEL, ou RESERVADO para o próprio leitor (atende a reserva).</li>
     * </ol>
     */
    @Transactional
    public EmprestimoResponse emprestar(Long leitorId, String codigoTombo, Long usuarioId) {
        Usuario operador = controleAcesso.exigirOperador(usuarioId);
        Leitor leitor = leitorService.buscarParaAtualizacao(leitorId); // lock: serializa operações do leitor
        LocalDate hoje = LocalDate.now(clock);
        OffsetDateTime agora = OffsetDateTime.now(clock);

        validadorPendencias.validar(leitor, hoje);
        PoliticaEmprestimo politica = politicaService.buscar(leitor.getTipo());

        long ativos = repository.countByLeitorIdAndStatus(leitorId, StatusEmprestimo.ATIVO);
        if (ativos >= politica.getMaxEmprestimosSimultaneos()) {
            throw new RegraNegocioException("Limite de %d empréstimos simultâneos atingido."
                    .formatted(politica.getMaxEmprestimosSimultaneos()));
        }

        Exemplar exemplar = exemplarService.buscarEntidadePorTombo(codigoTombo);
        Long livroId = exemplar.getLivro().getId();
        if (!exemplar.getLivro().isAtivo()) {
            throw new RegraNegocioException("Livro inativo no acervo.");
        }
        if (repository.existsByLeitorIdAndExemplarLivroIdAndStatus(leitorId, livroId, StatusEmprestimo.ATIVO)) {
            throw new RegraNegocioException("Leitor já possui um exemplar desta obra emprestado.");
        }

        Optional<Reserva> reservaDoLeitor = reservaRepository
                .findFirstByLeitorIdAndLivroIdAndStatusIn(leitorId, livroId, StatusReserva.ABERTAS);
        Exemplar exemplarReservadoOutro = validarDisponibilidade(exemplar, reservaDoLeitor);

        exemplar.emprestar();
        reservaDoLeitor.ifPresent(r -> r.atender(agora));
        if (exemplarReservadoOutro != null) {
            // leitor tinha outro exemplar separado e levou este: o separado vai para o próximo da fila
            filaReservaService.liberarExemplar(exemplarReservadoOutro);
        }

        Emprestimo emprestimo = new Emprestimo(leitor, exemplar, reservaDoLeitor.orElse(null), operador,
                agora, hoje.plusDays(politica.getPrazoDias()));
        log.info("Empréstimo do exemplar {} para leitor {} até {}", exemplar.getCodigoTombo(), leitorId,
                emprestimo.getDataPrevistaDevolucao());
        return EmprestimoResponse.de(repository.save(emprestimo), hoje);
    }

    /**
     * @return exemplar diferente que estava separado para o leitor (a ser liberado), ou null
     */
    private Exemplar validarDisponibilidade(Exemplar exemplar, Optional<Reserva> reservaDoLeitor) {
        if (exemplar.getStatus() == StatusExemplar.DISPONIVEL) {
            return reservaDoLeitor
                    .filter(r -> r.getStatus() == StatusReserva.DISPONIVEL)
                    .map(Reserva::getExemplar)
                    .orElse(null);
        }
        if (exemplar.getStatus() == StatusExemplar.RESERVADO) {
            boolean reservadoParaEste = reservaDoLeitor
                    .filter(r -> r.getStatus() == StatusReserva.DISPONIVEL)
                    .map(r -> r.getExemplar().getId().equals(exemplar.getId()))
                    .orElse(false);
            if (!reservadoParaEste) {
                throw new RegraNegocioException("Exemplar %s está reservado para outro leitor."
                        .formatted(exemplar.getCodigoTombo()));
            }
            return null;
        }
        throw new RegraNegocioException("Exemplar %s indisponível (status %s)."
                .formatted(exemplar.getCodigoTombo(), exemplar.getStatus()));
    }

    /**
     * Devolução: calcula atraso, gera multa e suspensão conforme a política,
     * e encaminha o exemplar para a fila de reservas ou de volta à estante.
     */
    @Transactional
    public DevolucaoResponse devolver(Long id, Long usuarioId, String observacao) {
        Usuario operador = controleAcesso.exigirOperador(usuarioId);
        Emprestimo emprestimo = carregar(id);
        LocalDate hoje = LocalDate.now(clock);
        long diasAtraso = emprestimo.diasAtraso(hoje);

        emprestimo.devolver(OffsetDateTime.now(clock), operador, Normalizador.textoOuNulo(observacao));

        Multa multa = null;
        LocalDate suspensoAte = null;
        if (diasAtraso > 0) {
            Leitor leitor = emprestimo.getLeitor();
            PoliticaEmprestimo politica = politicaService.buscar(leitor.getTipo());
            multa = gerarMultaAtraso(emprestimo, diasAtraso, politica);

            if (politica.getDiasSuspensaoPorAtraso() > 0) {
                suspensoAte = hoje.plusDays(diasAtraso * politica.getDiasSuspensaoPorAtraso());
                leitor.suspenderAte(suspensoAte);
            }
        }

        Reserva reservaAtendida = filaReservaService.liberarExemplar(emprestimo.getExemplar()).orElse(null);
        return new DevolucaoResponse(
                EmprestimoResponse.de(emprestimo, hoje),
                multa == null ? null : MultaResponse.de(multa),
                suspensoAte,
                reservaAtendida == null ? null : reservaAtendida.getId());
    }

    /**
     * Renovação: permitida ao operador ou ao próprio leitor, se o empréstimo
     * estiver no prazo, sem fila de reserva para a obra e dentro do limite da política.
     */
    @Transactional
    public EmprestimoResponse renovar(Long id, Long usuarioId) {
        Emprestimo emprestimo = carregar(id);
        Leitor leitor = emprestimo.getLeitor();
        Usuario usuario = controleAcesso.exigirOperadorOuProprioLeitor(usuarioId, leitor);
        LocalDate hoje = LocalDate.now(clock);

        if (!emprestimo.isAtivo()) {
            throw new RegraNegocioException("Somente empréstimos ativos podem ser renovados.");
        }
        if (emprestimo.isAtrasado(hoje)) {
            throw new RegraNegocioException("Empréstimo em atraso não pode ser renovado. Realize a devolução.");
        }
        validadorPendencias.validar(leitor, hoje);

        PoliticaEmprestimo politica = politicaService.buscar(leitor.getTipo());
        if (emprestimo.getQuantidadeRenovacoes() >= politica.getMaxRenovacoes()) {
            throw new RegraNegocioException("Limite de %d renovações atingido.".formatted(politica.getMaxRenovacoes()));
        }
        if (reservaRepository.existsByLivroIdAndStatus(emprestimo.getExemplar().getLivro().getId(), StatusReserva.AGUARDANDO)) {
            throw new RegraNegocioException("Há leitores na fila de reserva desta obra. Renovação não permitida.");
        }

        LocalDate novaData = emprestimo.getDataPrevistaDevolucao().plusDays(politica.getPrazoDias());
        emprestimo.renovar(novaData, usuario, OffsetDateTime.now(clock));
        return EmprestimoResponse.de(emprestimo, hoje);
    }

    /** Extravio: encerra o empréstimo, baixa o exemplar como extraviado e gera multa de reposição. */
    @Transactional
    public EmprestimoResponse registrarExtravio(Long id, Long usuarioId, BigDecimal valorReposicao, String observacao) {
        Usuario operador = controleAcesso.exigirOperador(usuarioId);
        Emprestimo emprestimo = carregar(id);
        LocalDate hoje = LocalDate.now(clock);
        long diasAtraso = emprestimo.diasAtraso(hoje);

        emprestimo.registrarExtravio(operador, Normalizador.textoOuNulo(observacao));
        emprestimo.getExemplar().marcarExtraviado();

        multaRepository.save(Multa.porExtravio(emprestimo, valorReposicao, Normalizador.textoOuNulo(observacao)));
        if (diasAtraso > 0) {
            gerarMultaAtraso(emprestimo, diasAtraso, politicaService.buscar(emprestimo.getLeitor().getTipo()));
        }
        return EmprestimoResponse.de(emprestimo, hoje);
    }

    @Transactional(readOnly = true)
    public EmprestimoResponse buscar(Long id) {
        return EmprestimoResponse.de(carregar(id), LocalDate.now(clock));
    }

    @Transactional(readOnly = true)
    public Page<EmprestimoResponse> listar(EmprestimoFiltro filtro, Pageable pageable) {
        LocalDate hoje = LocalDate.now(clock);
        Specification<Emprestimo> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (filtro.leitorId() != null) p.add(cb.equal(root.get("leitor").get("id"), filtro.leitorId()));
            if (filtro.livroId() != null) p.add(cb.equal(root.get("exemplar").get("livro").get("id"), filtro.livroId()));
            if (filtro.status() != null) p.add(cb.equal(root.get("status"), filtro.status()));
            if (Boolean.TRUE.equals(filtro.atrasados())) {
                p.add(cb.equal(root.get("status"), StatusEmprestimo.ATIVO));
                p.add(cb.lessThan(root.get("dataPrevistaDevolucao"), hoje));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return repository.findAll(spec, pageable).map(e -> EmprestimoResponse.de(e, hoje));
    }

    private Emprestimo carregar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo", id));
    }

    private Multa gerarMultaAtraso(Emprestimo emprestimo, long diasAtraso, PoliticaEmprestimo politica) {
        if (politica.getValorMultaDiaria().signum() <= 0) {
            return null;
        }
        return multaRepository.save(Multa.porAtraso(emprestimo, diasAtraso, politica.getValorMultaDiaria()));
    }
}

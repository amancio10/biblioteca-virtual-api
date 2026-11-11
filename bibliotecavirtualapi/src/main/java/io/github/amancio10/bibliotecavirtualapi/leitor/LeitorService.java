package io.github.amancio10.bibliotecavirtualapi.leitor;

import io.github.amancio10.bibliotecavirtualapi.common.exception.ConflitoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.LeitorCreateRequest;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.LeitorFiltro;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.LeitorResponse;
import io.github.amancio10.bibliotecavirtualapi.leitor.dto.LeitorUpdateRequest;
import io.github.amancio10.bibliotecavirtualapi.usuario.Perfil;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import io.github.amancio10.bibliotecavirtualapi.usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;

/**
 * Métodos públicos retornam DTOs (mapeados dentro da transação, pois
 * open-in-view está desligado). {@link #buscarParaAtualizacao} expõe a
 * entidade para uso de outros services na mesma transação.
 */
@Service
@RequiredArgsConstructor
public class LeitorService {

    private final LeitorRepository repository;
    private final UsuarioService usuarioService;
    private final Clock clock;

    @Transactional
    public LeitorResponse cadastrar(LeitorCreateRequest req) {
        String cpf = Normalizador.somenteDigitos(req.cpf());
        if (repository.existsByCpf(cpf)) {
            throw new ConflitoException("Já existe leitor cadastrado com este CPF.");
        }

        Usuario usuario = usuarioService.criar(req.nome(), req.email(), req.senha(), Perfil.LEITOR);
        Leitor leitor = new Leitor(
                usuario,
                gerarNumeroCarteirinha(),
                cpf,
                req.dataNascimento(),
                Normalizador.textoOuNulo(req.telefone()),
                req.tipo() != null ? req.tipo() : TipoLeitor.COMUM,
                req.endereco() != null ? req.endereco().paraEntidade() : null
        );
        return LeitorResponse.de(repository.save(leitor));
    }

    @Transactional
    public LeitorResponse atualizar(Long id, LeitorUpdateRequest req) {
        Leitor leitor = carregar(id);
        leitor.getUsuario().alterarNome(req.nome().trim());
        leitor.atualizar(
                Normalizador.textoOuNulo(req.telefone()),
                req.tipo(),
                req.endereco() != null ? req.endereco().paraEntidade() : null
        );
        return LeitorResponse.de(leitor);
    }

    @Transactional
    public LeitorResponse buscar(Long id) {
        return LeitorResponse.de(carregar(id));
    }

    @Transactional(readOnly = true)
    public Page<LeitorResponse> listar(LeitorFiltro filtro, Pageable pageable) {
        return repository.findAll(LeitorSpecifications.porFiltro(filtro), pageable).map(LeitorResponse::de);
    }

    @Transactional
    public LeitorResponse bloquear(Long id, String motivo) {
        Leitor leitor = carregar(id);
        leitor.bloquear(motivo.trim());
        return LeitorResponse.de(leitor);
    }

    @Transactional
    public LeitorResponse desbloquear(Long id) {
        Leitor leitor = carregar(id);
        leitor.desbloquear();
        return LeitorResponse.de(leitor);
    }

    @Transactional
    public void inativar(Long id) {
        carregar(id).inativar();
    }

    /** Busca com lock pessimista: usar dentro de transações de empréstimo/reserva. */
    @Transactional
    public Leitor buscarParaAtualizacao(Long id) {
        return repository.findByIdParaAtualizacao(id).orElseThrow(() -> new RecursoNaoEncontradoException("Leitor", id));
    }

    /** Carrega e já normaliza suspensões vencidas. */
    private Leitor carregar(Long id) {
        Leitor leitor = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Leitor", id));
        leitor.atualizarSituacao(LocalDate.now(clock));
        return leitor;
    }

    /** Formato BV + ano + 6 dígitos (ex.: BV2026000001). */
    private String gerarNumeroCarteirinha() {
        long seq = repository.proximoSequencialCarteirinha();
        return "BV%d%06d".formatted(Year.now(clock).getValue(), seq);
    }
}

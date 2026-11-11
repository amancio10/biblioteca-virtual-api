package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.Exemplar;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.Livro;
import io.github.amancio10.bibliotecavirtualapi.common.entity.EntidadeAuditavel;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.leitor.Leitor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

import static io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.StatusReserva.*;

@Getter
@Entity
@Table(name = "reserva")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reserva extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exemplar_id")
    private Exemplar exemplar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReserva status = AGUARDANDO;

    @Column(name = "data_reserva", nullable = false)
    private OffsetDateTime dataReserva;

    @Column(name = "disponivel_em")
    private OffsetDateTime disponivelEm;

    @Column(name = "expira_em")
    private OffsetDateTime expiraEm;

    @Column(name = "finalizada_em")
    private OffsetDateTime finalizadaEm;

    @Version
    private Long version;

    public Reserva(Leitor leitor, Livro livro, OffsetDateTime agora) {
        this.leitor = leitor;
        this.livro = livro;
        this.dataReserva = agora;
    }

    /** Um exemplar foi separado para este leitor. */
    public void disponibilizar(Exemplar exemplar, OffsetDateTime agora, OffsetDateTime expiraEm) {
        exigirStatus(AGUARDANDO, "disponibilizar");
        this.exemplar = exemplar;
        this.status = DISPONIVEL;
        this.disponivelEm = agora;
        this.expiraEm = expiraEm;
    }

    public void atender(OffsetDateTime agora) {
        if (!StatusReserva.ABERTAS.contains(status)) {
            throw new RegraNegocioException("Reserva %d está %s e não pode ser atendida.".formatted(id, status));
        }
        this.status = ATENDIDA;
        this.finalizadaEm = agora;
    }

    public void cancelar(OffsetDateTime agora) {
        if (!StatusReserva.ABERTAS.contains(status)) {
            throw new RegraNegocioException("Reserva %d está %s e não pode ser cancelada.".formatted(id, status));
        }
        this.status = CANCELADA;
        this.finalizadaEm = agora;
    }

    public void expirar(OffsetDateTime agora) {
        exigirStatus(DISPONIVEL, "expirar");
        this.status = EXPIRADA;
        this.finalizadaEm = agora;
    }

    public boolean isAberta() {
        return StatusReserva.ABERTAS.contains(status);
    }

    private void exigirStatus(StatusReserva esperado, String acao) {
        if (status != esperado) {
            throw new RegraNegocioException("Não é possível %s a reserva %d: status atual %s.".formatted(acao, id, status));
        }
    }
}

package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.Exemplar;
import io.github.amancio10.bibliotecavirtualapi.circulacao.reserva.Reserva;
import io.github.amancio10.bibliotecavirtualapi.common.entity.EntidadeAuditavel;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.leitor.Leitor;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "emprestimo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emprestimo extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exemplar_id", nullable = false)
    private Exemplar exemplar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Usuario registradoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recebido_por_id")
    private Usuario recebidoPor;

    @Column(name = "data_emprestimo", nullable = false)
    private OffsetDateTime dataEmprestimo;

    @Column(name = "data_prevista_devolucao", nullable = false)
    private LocalDate dataPrevistaDevolucao;

    @Column(name = "data_devolucao")
    private OffsetDateTime dataDevolucao;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "quantidade_renovacoes", nullable = false)
    private Integer quantidadeRenovacoes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEmprestimo status = StatusEmprestimo.ATIVO;

    @Column(length = 255)
    private String observacao;

    @Version
    private Long version;

    @OrderBy("dataRenovacao")
    @OneToMany(mappedBy = "emprestimo", cascade = CascadeType.PERSIST)
    private List<Renovacao> renovacoes = new ArrayList<>();

    public Emprestimo(Leitor leitor, Exemplar exemplar, Reserva reserva, Usuario registradoPor,
                      OffsetDateTime dataEmprestimo, LocalDate dataPrevistaDevolucao) {
        this.leitor = leitor;
        this.exemplar = exemplar;
        this.reserva = reserva;
        this.registradoPor = registradoPor;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
    }

    public boolean isAtivo() {
        return status == StatusEmprestimo.ATIVO;
    }

    public boolean isAtrasado(LocalDate hoje) {
        return isAtivo() && hoje.isAfter(dataPrevistaDevolucao);
    }

    /** Dias de atraso na data informada (0 se no prazo). */
    public long diasAtraso(LocalDate referencia) {
        long dias = ChronoUnit.DAYS.between(dataPrevistaDevolucao, referencia);
        return Math.max(dias, 0);
    }

    public void renovar(LocalDate novaData, Usuario renovadoPor, OffsetDateTime agora) {
        exigirAtivo("renovar");
        renovacoes.add(new Renovacao(this, agora, dataPrevistaDevolucao, novaData, renovadoPor));
        this.dataPrevistaDevolucao = novaData;
        this.quantidadeRenovacoes++;
    }

    public void devolver(OffsetDateTime agora, Usuario recebidoPor, String observacao) {
        exigirAtivo("devolver");
        this.status = StatusEmprestimo.DEVOLVIDO;
        this.dataDevolucao = agora;
        this.recebidoPor = recebidoPor;
        this.observacao = observacao;
    }

    public void registrarExtravio(Usuario operador, String observacao) {
        exigirAtivo("registrar extravio de");
        this.status = StatusEmprestimo.EXTRAVIADO;
        this.recebidoPor = operador;
        this.observacao = observacao;
    }

    private void exigirAtivo(String acao) {
        if (!isAtivo()) {
            throw new RegraNegocioException("Não é possível %s um empréstimo %s.".formatted(acao, status));
        }
    }
}

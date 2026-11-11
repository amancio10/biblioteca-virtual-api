package io.github.amancio10.bibliotecavirtualapi.acervo.exemplar;

import io.github.amancio10.bibliotecavirtualapi.acervo.livro.Livro;
import io.github.amancio10.bibliotecavirtualapi.common.entity.EntidadeAuditavel;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
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

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import static io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.StatusExemplar.*;

/**
 * Cópia física de um livro. Todas as transições de status passam por métodos
 * de domínio, que validam o estado de origem (máquina de estados).
 */
@Getter
@Entity
@Table(name = "exemplar")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exemplar extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Column(name = "codigo_tombo", nullable = false, length = 30)
    private String codigoTombo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusExemplar status = DISPONIVEL;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conservacao", nullable = false, length = 20)
    private EstadoConservacao estadoConservacao;

    @Column(length = 50)
    private String localizacao;

    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @Column(length = 255)
    private String observacao;

    @Version
    private Long version;

    public Exemplar(Livro livro, String codigoTombo, EstadoConservacao estadoConservacao,
                    String localizacao, LocalDate dataAquisicao, String observacao) {
        this.livro = livro;
        this.codigoTombo = codigoTombo;
        this.estadoConservacao = estadoConservacao;
        this.localizacao = localizacao;
        this.dataAquisicao = dataAquisicao;
        this.observacao = observacao;
    }

    public void atualizar(EstadoConservacao estado, String localizacao, String observacao) {
        this.estadoConservacao = estado;
        this.localizacao = localizacao;
        this.observacao = observacao;
    }

    public void emprestar() {
        transicionar(EMPRESTADO, EnumSet.of(DISPONIVEL, RESERVADO));
    }

    /** RESERVADO -> RESERVADO ocorre quando a reserva expira e o exemplar passa ao próximo da fila. */
    public void reservar() {
        transicionar(RESERVADO, EnumSet.of(DISPONIVEL, EMPRESTADO, RESERVADO, EM_MANUTENCAO));
    }

    /** Volta para a estante (após devolução, fim de reserva ou manutenção). */
    public void disponibilizar() {
        transicionar(DISPONIVEL, EnumSet.of(EMPRESTADO, RESERVADO, EM_MANUTENCAO));
    }

    public void enviarParaManutencao() {
        transicionar(EM_MANUTENCAO, EnumSet.of(DISPONIVEL));
    }

    public void marcarExtraviado() {
        transicionar(EXTRAVIADO, EnumSet.of(EMPRESTADO));
    }

    public void baixar(String motivo) {
        transicionar(BAIXADO, EnumSet.of(DISPONIVEL, EM_MANUTENCAO, EXTRAVIADO));
        this.observacao = motivo;
    }

    public boolean isDisponivel() {
        return status == DISPONIVEL;
    }

    private void transicionar(StatusExemplar destino, Set<StatusExemplar> origensPermitidas) {
        if (!origensPermitidas.contains(status)) {
            throw new RegraNegocioException("Exemplar %s está %s e não pode passar para %s."
                    .formatted(codigoTombo, status, destino));
        }
        this.status = destino;
    }
}

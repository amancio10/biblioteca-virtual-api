package io.github.amancio10.bibliotecavirtualapi.circulacao.multa;

import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.Emprestimo;
import io.github.amancio10.bibliotecavirtualapi.common.entity.EntidadeAuditavel;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.leitor.Leitor;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "multa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Multa extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emprestimo_id", nullable = false)
    private Emprestimo emprestimo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoMulta motivo;

    @Column(name = "dias_atraso", nullable = false)
    private Integer diasAtraso;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMulta status = StatusMulta.PENDENTE;

    @Column(name = "data_pagamento")
    private OffsetDateTime dataPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recebido_por_id")
    private Usuario recebidoPor;

    @Column(length = 255)
    private String observacao;

    private Multa(Emprestimo emprestimo, MotivoMulta motivo, int diasAtraso, BigDecimal valor, String observacao) {
        this.emprestimo = emprestimo;
        this.leitor = emprestimo.getLeitor();
        this.motivo = motivo;
        this.diasAtraso = diasAtraso;
        this.valor = valor;
        this.observacao = observacao;
    }

    public static Multa porAtraso(Emprestimo emprestimo, long diasAtraso, BigDecimal valorDiario) {
        BigDecimal valor = valorDiario.multiply(BigDecimal.valueOf(diasAtraso));
        return new Multa(emprestimo, MotivoMulta.ATRASO, Math.toIntExact(diasAtraso), valor, null);
    }

    public static Multa porExtravio(Emprestimo emprestimo, BigDecimal valorReposicao, String observacao) {
        return new Multa(emprestimo, MotivoMulta.EXTRAVIO, 0, valorReposicao, observacao);
    }

    public void pagar(OffsetDateTime agora, Usuario operador) {
        exigirPendente("pagar");
        this.status = StatusMulta.PAGA;
        this.dataPagamento = agora;
        this.recebidoPor = operador;
    }

    public void cancelar(Usuario operador, String motivo) {
        exigirPendente("cancelar");
        this.status = StatusMulta.CANCELADA;
        this.recebidoPor = operador;
        this.observacao = motivo;
    }

    private void exigirPendente(String acao) {
        if (status != StatusMulta.PENDENTE) {
            throw new RegraNegocioException("Não é possível %s a multa %d: status %s.".formatted(acao, id, status));
        }
    }
}

package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo;

import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Histórico imutável de cada renovação de um empréstimo. */
@Getter
@Entity
@Table(name = "renovacao")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Renovacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emprestimo_id", nullable = false)
    private Emprestimo emprestimo;

    @Column(name = "data_renovacao", nullable = false)
    private OffsetDateTime dataRenovacao;

    @Column(name = "data_prevista_antiga", nullable = false)
    private LocalDate dataPrevistaAntiga;

    @Column(name = "data_prevista_nova", nullable = false)
    private LocalDate dataPrevistaNova;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "renovado_por_id", nullable = false)
    private Usuario renovadoPor;

    Renovacao(Emprestimo emprestimo, OffsetDateTime dataRenovacao, LocalDate antiga, LocalDate nova, Usuario renovadoPor) {
        this.emprestimo = emprestimo;
        this.dataRenovacao = dataRenovacao;
        this.dataPrevistaAntiga = antiga;
        this.dataPrevistaNova = nova;
        this.renovadoPor = renovadoPor;
    }
}

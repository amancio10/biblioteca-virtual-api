package io.github.amancio10.bibliotecavirtualapi.circulacao.politica;

import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Regras de circulação por tipo de leitor, configuráveis sem deploy. */
@Getter
@Entity
@Table(name = "politica_emprestimo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PoliticaEmprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_leitor", nullable = false, length = 20, unique = true)
    private TipoLeitor tipoLeitor;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "prazo_dias", nullable = false)
    private Integer prazoDias;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "max_emprestimos_simultaneos", nullable = false)
    private Integer maxEmprestimosSimultaneos;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "max_renovacoes", nullable = false)
    private Integer maxRenovacoes;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "max_reservas_simultaneas", nullable = false)
    private Integer maxReservasSimultaneas;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "horas_retirada_reserva", nullable = false)
    private Integer horasRetiradaReserva;

    @Column(name = "valor_multa_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMultaDiaria;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "dias_suspensao_por_atraso", nullable = false)
    private Integer diasSuspensaoPorAtraso;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    public void atualizar(int prazoDias, int maxEmprestimos, int maxRenovacoes, int maxReservas,
                          int horasRetirada, BigDecimal valorMultaDiaria, int diasSuspensao) {
        this.prazoDias = prazoDias;
        this.maxEmprestimosSimultaneos = maxEmprestimos;
        this.maxRenovacoes = maxRenovacoes;
        this.maxReservasSimultaneas = maxReservas;
        this.horasRetiradaReserva = horasRetirada;
        this.valorMultaDiaria = valorMultaDiaria;
        this.diasSuspensaoPorAtraso = diasSuspensao;
    }
}

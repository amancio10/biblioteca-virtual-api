package io.github.amancio10.bibliotecavirtualapi.leitor;

import io.github.amancio10.bibliotecavirtualapi.common.entity.EntidadeAuditavel;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "leitor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Leitor extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "numero_carteirinha", nullable = false, length = 20)
    private String numeroCarteirinha;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLeitor tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLeitor status = StatusLeitor.ATIVO;

    @Column(name = "suspenso_ate")
    private LocalDate suspensoAte;

    @Column(name = "motivo_bloqueio")
    private String motivoBloqueio;

    @Embedded
    private Endereco endereco;

    public Leitor(Usuario usuario, String numeroCarteirinha, String cpf, LocalDate dataNascimento,
                  String telefone, TipoLeitor tipo, Endereco endereco) {
        this.usuario = usuario;
        this.numeroCarteirinha = numeroCarteirinha;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.tipo = tipo;
        this.endereco = endereco;
    }

    public void atualizar(String telefone, TipoLeitor tipo, Endereco endereco) {
        this.telefone = telefone;
        this.tipo = tipo;
        this.endereco = endereco;
    }

    /**
     * Encerra automaticamente a suspensão vencida. {@code suspensoAte} é inclusivo:
     * o leitor volta a ficar ativo no dia seguinte.
     */
    public void atualizarSituacao(LocalDate hoje) {
        if (status == StatusLeitor.SUSPENSO && hoje.isAfter(suspensoAte)) {
            status = StatusLeitor.ATIVO;
            suspensoAte = null;
        }
    }

    /** Garante que o leitor pode emprestar/reservar. */
    public void validarApto(LocalDate hoje) {
        atualizarSituacao(hoje);
        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("O usuário do leitor está desativado.");
        }
        switch (status) {
            case ATIVO -> { }
            case SUSPENSO -> throw new RegraNegocioException("Leitor suspenso até " + suspensoAte + ".");
            case BLOQUEADO -> throw new RegraNegocioException("Leitor bloqueado: " + motivoBloqueio);
            case INATIVO -> throw new RegraNegocioException("Cadastro do leitor está inativo.");
        }
    }

    /** Aplica (ou estende) suspensão. Não sobrepõe bloqueio/inativação. */
    public void suspenderAte(LocalDate data) {
        if (status == StatusLeitor.BLOQUEADO || status == StatusLeitor.INATIVO) {
            return;
        }
        if (suspensoAte == null || data.isAfter(suspensoAte)) {
            suspensoAte = data;
        }
        status = StatusLeitor.SUSPENSO;
    }

    public void bloquear(String motivo) {
        if (status == StatusLeitor.INATIVO) {
            throw new RegraNegocioException("Não é possível bloquear um cadastro inativo.");
        }
        status = StatusLeitor.BLOQUEADO;
        motivoBloqueio = motivo;
        suspensoAte = null;
    }

    public void desbloquear() {
        if (status != StatusLeitor.BLOQUEADO) {
            throw new RegraNegocioException("Leitor não está bloqueado.");
        }
        status = StatusLeitor.ATIVO;
        motivoBloqueio = null;
    }

    public void inativar() {
        status = StatusLeitor.INATIVO;
        suspensoAte = null;
        usuario.desativar();
    }

    public String getNome() {
        return usuario.getNome();
    }
}

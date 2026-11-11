package io.github.amancio10.bibliotecavirtualapi.circulacao;

import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.EstadoConservacao;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.Exemplar;
import io.github.amancio10.bibliotecavirtualapi.acervo.exemplar.StatusExemplar;
import io.github.amancio10.bibliotecavirtualapi.acervo.livro.Livro;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.Emprestimo;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.MotivoMulta;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.Multa;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.leitor.Leitor;
import io.github.amancio10.bibliotecavirtualapi.leitor.StatusLeitor;
import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;
import io.github.amancio10.bibliotecavirtualapi.usuario.Perfil;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes das regras de negócio que vivem nas entidades (sem Spring, sem banco). */
class RegrasDominioCirculacaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 11);
    private static final OffsetDateTime AGORA = HOJE.atTime(10, 0).atOffset(ZoneOffset.ofHours(-3));

    private Usuario operador;
    private Leitor leitor;
    private Exemplar exemplar;

    @BeforeEach
    void setUp() {
        operador = new Usuario("Bibliotecaria", "bib@bv.com", "hash", Perfil.BIBLIOTECARIO);
        Usuario usuarioLeitor = new Usuario("Ana", "ana@bv.com", "hash", Perfil.LEITOR);
        leitor = new Leitor(usuarioLeitor, "BV2026000001", "52998224725", LocalDate.of(1995, 1, 1),
                null, TipoLeitor.COMUM, null);
        exemplar = new Exemplar(new Livro("9780306406157"), "T-1", EstadoConservacao.BOM, null, null, null);
    }

    private Emprestimo emprestimoComVencimento(LocalDate vencimento) {
        exemplar.emprestar();
        return new Emprestimo(leitor, exemplar, null, operador, AGORA.minusDays(14), vencimento);
    }

    @Nested
    @DisplayName("Empréstimo")
    class EmprestimoTest {

        @Test
        void noPrazoNaoTemAtraso() {
            Emprestimo e = emprestimoComVencimento(HOJE);
            assertThat(e.isAtrasado(HOJE)).isFalse();
            assertThat(e.diasAtraso(HOJE)).isZero();
        }

        @Test
        void calculaDiasDeAtraso() {
            Emprestimo e = emprestimoComVencimento(HOJE.minusDays(5));
            assertThat(e.isAtrasado(HOJE)).isTrue();
            assertThat(e.diasAtraso(HOJE)).isEqualTo(5);
        }

        @Test
        void renovacaoRegistraHistoricoEEstendePrazo() {
            Emprestimo e = emprestimoComVencimento(HOJE.plusDays(3));
            e.renovar(HOJE.plusDays(17), operador, AGORA);

            assertThat(e.getDataPrevistaDevolucao()).isEqualTo(HOJE.plusDays(17));
            assertThat(e.getQuantidadeRenovacoes()).isEqualTo(1);
            assertThat(e.getRenovacoes()).singleElement()
                    .satisfies(r -> assertThat(r.getDataPrevistaAntiga()).isEqualTo(HOJE.plusDays(3)));
        }

        @Test
        void naoDevolveDuasVezes() {
            Emprestimo e = emprestimoComVencimento(HOJE);
            e.devolver(AGORA, operador, null);
            assertThatThrownBy(() -> e.devolver(AGORA, operador, null)).isInstanceOf(RegraNegocioException.class);
        }
    }

    @Nested
    @DisplayName("Multa")
    class MultaTest {

        @Test
        void multaPorAtrasoEhDiasVezesValorDiario() {
            Emprestimo e = emprestimoComVencimento(HOJE.minusDays(4));
            Multa multa = Multa.porAtraso(e, e.diasAtraso(HOJE), new BigDecimal("1.50"));

            assertThat(multa.getMotivo()).isEqualTo(MotivoMulta.ATRASO);
            assertThat(multa.getDiasAtraso()).isEqualTo(4);
            assertThat(multa.getValor()).isEqualByComparingTo("6.00");
            assertThat(multa.getLeitor()).isSameAs(leitor);
        }

        @Test
        void multaPagaNaoPodeSerCancelada() {
            Multa multa = Multa.porExtravio(emprestimoComVencimento(HOJE), new BigDecimal("80"), null);
            multa.pagar(AGORA, operador);
            assertThatThrownBy(() -> multa.cancelar(operador, "abono")).isInstanceOf(RegraNegocioException.class);
        }
    }

    @Nested
    @DisplayName("Leitor")
    class LeitorTest {

        @Test
        void suspensaoImpedeEmprestimoAteAData() {
            leitor.suspenderAte(HOJE.plusDays(2));
            assertThatThrownBy(() -> leitor.validarApto(HOJE.plusDays(2))).isInstanceOf(RegraNegocioException.class);
        }

        @Test
        void suspensaoVencidaReativaAutomaticamente() {
            leitor.suspenderAte(HOJE.plusDays(2));
            leitor.validarApto(HOJE.plusDays(3));
            assertThat(leitor.getStatus()).isEqualTo(StatusLeitor.ATIVO);
            assertThat(leitor.getSuspensoAte()).isNull();
        }

        @Test
        void novaSuspensaoNaoEncurtaAAtual() {
            leitor.suspenderAte(HOJE.plusDays(10));
            leitor.suspenderAte(HOJE.plusDays(3));
            assertThat(leitor.getSuspensoAte()).isEqualTo(HOJE.plusDays(10));
        }

        @Test
        void suspensaoNaoSobrepoeBloqueio() {
            leitor.bloquear("extravio");
            leitor.suspenderAte(HOJE.plusDays(5));
            assertThat(leitor.getStatus()).isEqualTo(StatusLeitor.BLOQUEADO);
        }
    }

    @Nested
    @DisplayName("Exemplar")
    class ExemplarTest {

        @Test
        void exemplarEmprestadoNaoVaiParaManutencao() {
            exemplar.emprestar();
            assertThatThrownBy(exemplar::enviarParaManutencao).isInstanceOf(RegraNegocioException.class);
        }

        @Test
        void exemplarBaixadoNaoPodeSerEmprestado() {
            exemplar.baixar("danificado");
            assertThat(exemplar.getStatus()).isEqualTo(StatusExemplar.BAIXADO);
            assertThatThrownBy(exemplar::emprestar).isInstanceOf(RegraNegocioException.class);
        }

        @Test
        void extravioSoOcorreDuranteEmprestimo() {
            assertThatThrownBy(exemplar::marcarExtraviado).isInstanceOf(RegraNegocioException.class);
            exemplar.emprestar();
            exemplar.marcarExtraviado();
            assertThat(exemplar.getStatus()).isEqualTo(StatusExemplar.EXTRAVIADO);
        }
    }
}

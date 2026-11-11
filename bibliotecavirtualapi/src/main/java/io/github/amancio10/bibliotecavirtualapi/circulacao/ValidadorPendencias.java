package io.github.amancio10.bibliotecavirtualapi.circulacao;

import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.EmprestimoRepository;
import io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo.StatusEmprestimo;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.MultaRepository;
import io.github.amancio10.bibliotecavirtualapi.circulacao.multa.StatusMulta;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.leitor.Leitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Regras comuns que impedem o leitor de emprestar, renovar ou reservar. */
@Component
@RequiredArgsConstructor
public class ValidadorPendencias {

    private final EmprestimoRepository emprestimoRepository;
    private final MultaRepository multaRepository;

    public void validar(Leitor leitor, LocalDate hoje) {
        leitor.validarApto(hoje);

        if (multaRepository.existsByLeitorIdAndStatus(leitor.getId(), StatusMulta.PENDENTE)) {
            throw new RegraNegocioException("Leitor possui multas pendentes (total R$ %s)."
                    .formatted(multaRepository.totalPendente(leitor.getId())));
        }
        if (emprestimoRepository.existsByLeitorIdAndStatusAndDataPrevistaDevolucaoBefore(
                leitor.getId(), StatusEmprestimo.ATIVO, hoje)) {
            throw new RegraNegocioException("Leitor possui empréstimos em atraso.");
        }
    }
}

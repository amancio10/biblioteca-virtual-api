package io.github.amancio10.bibliotecavirtualapi.circulacao.politica.dto;

import io.github.amancio10.bibliotecavirtualapi.circulacao.politica.PoliticaEmprestimo;
import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PoliticaEmprestimoResponse(
        TipoLeitor tipoLeitor,
        Integer prazoDias,
        Integer maxEmprestimosSimultaneos,
        Integer maxRenovacoes,
        Integer maxReservasSimultaneas,
        Integer horasRetiradaReserva,
        BigDecimal valorMultaDiaria,
        Integer diasSuspensaoPorAtraso,
        OffsetDateTime atualizadoEm
) {

    public static PoliticaEmprestimoResponse de(PoliticaEmprestimo p) {
        return new PoliticaEmprestimoResponse(
                p.getTipoLeitor(), p.getPrazoDias(), p.getMaxEmprestimosSimultaneos(), p.getMaxRenovacoes(),
                p.getMaxReservasSimultaneas(), p.getHorasRetiradaReserva(), p.getValorMultaDiaria(),
                p.getDiasSuspensaoPorAtraso(), p.getAtualizadoEm());
    }
}

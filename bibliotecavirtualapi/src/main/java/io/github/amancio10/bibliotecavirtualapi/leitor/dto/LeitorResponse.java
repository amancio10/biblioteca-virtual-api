package io.github.amancio10.bibliotecavirtualapi.leitor.dto;

import io.github.amancio10.bibliotecavirtualapi.leitor.Leitor;
import io.github.amancio10.bibliotecavirtualapi.leitor.StatusLeitor;
import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LeitorResponse(
        Long id,
        Long usuarioId,
        String nome,
        String email,
        String numeroCarteirinha,
        String cpf,
        LocalDate dataNascimento,
        String telefone,
        TipoLeitor tipo,
        StatusLeitor status,
        LocalDate suspensoAte,
        String motivoBloqueio,
        EnderecoDto endereco,
        OffsetDateTime criadoEm
) {

    public static LeitorResponse de(Leitor l) {
        return new LeitorResponse(
                l.getId(),
                l.getUsuario().getId(),
                l.getUsuario().getNome(),
                l.getUsuario().getEmail(),
                l.getNumeroCarteirinha(),
                mascararCpf(l.getCpf()),
                l.getDataNascimento(),
                l.getTelefone(),
                l.getTipo(),
                l.getStatus(),
                l.getSuspensoAte(),
                l.getMotivoBloqueio(),
                EnderecoDto.de(l.getEndereco()),
                l.getCriadoEm()
        );
    }

    /** LGPD: expõe apenas parte do CPF. Ex.: ***.456.789-** */
    private static String mascararCpf(String cpf) {
        return "***.%s.%s-**".formatted(cpf.substring(3, 6), cpf.substring(6, 9));
    }
}

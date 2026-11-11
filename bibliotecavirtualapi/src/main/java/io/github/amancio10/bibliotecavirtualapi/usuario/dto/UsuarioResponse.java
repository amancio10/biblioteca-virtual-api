package io.github.amancio10.bibliotecavirtualapi.usuario.dto;

import io.github.amancio10.bibliotecavirtualapi.usuario.Perfil;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;

import java.time.OffsetDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        boolean ativo,
        OffsetDateTime criadoEm
) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.isAtivo(), u.getCriadoEm());
    }
}

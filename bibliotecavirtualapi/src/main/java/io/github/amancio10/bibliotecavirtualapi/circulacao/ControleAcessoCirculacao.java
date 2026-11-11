package io.github.amancio10.bibliotecavirtualapi.circulacao;

import io.github.amancio10.bibliotecavirtualapi.common.exception.AcessoNegadoException;
import io.github.amancio10.bibliotecavirtualapi.leitor.Leitor;
import io.github.amancio10.bibliotecavirtualapi.usuario.Usuario;
import io.github.amancio10.bibliotecavirtualapi.usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Regras de "quem pode fazer o quê" na circulação.
 * Operadores agem por qualquer leitor; o leitor só age por si mesmo
 * (renovar os próprios empréstimos, reservar e cancelar as próprias reservas).
 */
@Component
@RequiredArgsConstructor
public class ControleAcessoCirculacao {

    /** Header provisório até a implementação da autenticação (JWT). */
    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private final UsuarioService usuarioService;

    public Usuario exigirOperador(Long usuarioId) {
        return usuarioService.buscarOperadorAtivo(usuarioId);
    }

    public Usuario exigirOperadorOuProprioLeitor(Long usuarioId, Leitor leitor) {
        Usuario usuario = usuarioService.buscarAtivo(usuarioId);
        boolean proprioLeitor = usuario.getId().equals(leitor.getUsuario().getId());
        if (!usuario.getPerfil().isOperador() && !proprioLeitor) {
            throw new AcessoNegadoException("Leitores só podem operar sobre os próprios registros.");
        }
        return usuario;
    }
}

package io.github.amancio10.bibliotecavirtualapi.usuario;

import io.github.amancio10.bibliotecavirtualapi.common.exception.AcessoNegadoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.ConflitoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RecursoNaoEncontradoException;
import io.github.amancio10.bibliotecavirtualapi.common.exception.RegraNegocioException;
import io.github.amancio10.bibliotecavirtualapi.usuario.dto.UsuarioRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    /** Cria usuário operador (ADMIN/BIBLIOTECARIO). */
    @Transactional
    public Usuario criarOperador(UsuarioRequest req) {
        if (!req.perfil().isOperador()) {
            throw new RegraNegocioException("Leitores devem ser cadastrados em /api/leitores.");
        }
        return criar(req.nome(), req.email(), req.senha(), req.perfil());
    }

    /** Usado também pelo cadastro de leitor (mesma transação). */
    @Transactional
    public Usuario criar(String nome, String email, String senha, Perfil perfil) {
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe usuário com o e-mail " + email);
        }
        return repository.save(new Usuario(nome, email, passwordEncoder.encode(senha), perfil));
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listarOperadores(Pageable pageable) {
        return repository.findByPerfilIn(EnumSet.of(Perfil.ADMIN, Perfil.BIBLIOTECARIO), pageable);
    }

    /**
     * Usuário que executa a operação (header X-Usuario-Id).
     * Quando houver Spring Security, este id virá do token autenticado.
     */
    @Transactional(readOnly = true)
    public Usuario buscarAtivo(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new AcessoNegadoException("Usuário %d não identificado.".formatted(id)));
        if (!usuario.isAtivo()) {
            throw new AcessoNegadoException("Usuário %d está desativado.".formatted(id));
        }
        return usuario;
    }

    /** Operações de balcão (empréstimo, devolução, multas) exigem ADMIN ou BIBLIOTECARIO. */
    @Transactional(readOnly = true)
    public Usuario buscarOperadorAtivo(Long id) {
        Usuario usuario = buscarAtivo(id);
        if (!usuario.getPerfil().isOperador()) {
            throw new AcessoNegadoException("Operação restrita a bibliotecários e administradores.");
        }
        return usuario;
    }

    @Transactional
    public Usuario ativar(Long id) {
        Usuario usuario = buscar(id);
        usuario.ativar();
        return usuario;
    }

    @Transactional
    public Usuario desativar(Long id) {
        Usuario usuario = buscar(id);
        usuario.desativar();
        return usuario;
    }
}

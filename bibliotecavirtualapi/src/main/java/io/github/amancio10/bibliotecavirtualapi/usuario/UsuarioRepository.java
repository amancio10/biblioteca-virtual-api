package io.github.amancio10.bibliotecavirtualapi.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Page<Usuario> findByPerfilIn(Collection<Perfil> perfis, Pageable pageable);
}

package io.github.amancio10.bibliotecavirtualapi.circulacao.politica;

import io.github.amancio10.bibliotecavirtualapi.leitor.TipoLeitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoliticaEmprestimoRepository extends JpaRepository<PoliticaEmprestimo, Long> {

    Optional<PoliticaEmprestimo> findByTipoLeitor(TipoLeitor tipoLeitor);
}

package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long>, JpaSpecificationExecutor<Emprestimo> {

    long countByLeitorIdAndStatus(Long leitorId, StatusEmprestimo status);

    /** Leitor possui algum empréstimo ativo vencido? */
    boolean existsByLeitorIdAndStatusAndDataPrevistaDevolucaoBefore(Long leitorId, StatusEmprestimo status, LocalDate data);

    boolean existsByLeitorIdAndExemplarLivroIdAndStatus(Long leitorId, Long livroId, StatusEmprestimo status);
}

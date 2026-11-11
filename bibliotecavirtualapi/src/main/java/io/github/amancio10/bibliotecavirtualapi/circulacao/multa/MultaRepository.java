package io.github.amancio10.bibliotecavirtualapi.circulacao.multa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface MultaRepository extends JpaRepository<Multa, Long>, JpaSpecificationExecutor<Multa> {

    boolean existsByLeitorIdAndStatus(Long leitorId, StatusMulta status);

    @Query("""
            select coalesce(sum(m.valor), 0) from Multa m
            where m.leitor.id = :leitorId
              and m.status = io.github.amancio10.bibliotecavirtualapi.circulacao.multa.StatusMulta.PENDENTE
            """)
    BigDecimal totalPendente(Long leitorId);
}

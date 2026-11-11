package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Expira periodicamente reservas cujo prazo de retirada venceu. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiracaoReservaJob {

    private final ReservaService reservaService;

    @Scheduled(cron = "${biblioteca.jobs.expiracao-reservas-cron}")
    public void executar() {
        int expiradas = reservaService.expirarVencidas();
        if (expiradas > 0) {
            log.info("Job de expiração: {} reserva(s) expirada(s)", expiradas);
        }
    }
}

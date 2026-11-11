package io.github.amancio10.bibliotecavirtualapi.circulacao.reserva;

import java.util.EnumSet;
import java.util.Set;

public enum StatusReserva {
    /** Na fila, aguardando um exemplar ser liberado. */
    AGUARDANDO,
    /** Exemplar separado; leitor tem até {@code expiraEm} para retirar. */
    DISPONIVEL,
    /** Convertida em empréstimo. */
    ATENDIDA,
    EXPIRADA,
    CANCELADA;

    public static final Set<StatusReserva> ABERTAS = EnumSet.of(AGUARDANDO, DISPONIVEL);
}

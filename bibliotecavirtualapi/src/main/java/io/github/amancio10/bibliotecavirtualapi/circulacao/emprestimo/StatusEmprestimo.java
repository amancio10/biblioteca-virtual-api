package io.github.amancio10.bibliotecavirtualapi.circulacao.emprestimo;

/**
 * "Atrasado" não é status persistido: é derivado de
 * {@code status == ATIVO && dataPrevistaDevolucao < hoje}.
 */
public enum StatusEmprestimo {
    ATIVO,
    DEVOLVIDO,
    EXTRAVIADO
}

package io.github.amancio10.bibliotecavirtualapi.common.web;

/** Normaliza valores de entrada antes de persistir. */
public final class Normalizador {

    private Normalizador() {
    }

    /** Remove máscara de documentos (CPF, CNPJ, CEP): mantém só dígitos. */
    public static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    /** ISBN aceita 'X' como dígito verificador do ISBN-10. */
    public static String isbn(String valor) {
        return valor == null ? null : valor.replaceAll("[^0-9Xx]", "").toUpperCase();
    }

    public static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

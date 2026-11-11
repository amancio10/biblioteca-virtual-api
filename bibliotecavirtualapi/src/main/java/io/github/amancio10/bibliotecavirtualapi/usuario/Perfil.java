package io.github.amancio10.bibliotecavirtualapi.usuario;

public enum Perfil {
    ADMIN,
    BIBLIOTECARIO,
    LEITOR;

    public boolean isOperador() {
        return this == ADMIN || this == BIBLIOTECARIO;
    }
}

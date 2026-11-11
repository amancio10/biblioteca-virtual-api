package io.github.amancio10.bibliotecavirtualapi.leitor.dto;

import io.github.amancio10.bibliotecavirtualapi.common.web.Normalizador;
import io.github.amancio10.bibliotecavirtualapi.leitor.Endereco;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoDto(
        @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido") String cep,
        @Size(max = 150) String logradouro,
        @Size(max = 10) String numero,
        @Size(max = 100) String complemento,
        @Size(max = 100) String bairro,
        @Size(max = 100) String cidade,
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF deve ter 2 letras") String uf
) {

    public Endereco paraEntidade() {
        return new Endereco(
                Normalizador.somenteDigitos(cep),
                Normalizador.textoOuNulo(logradouro),
                Normalizador.textoOuNulo(numero),
                Normalizador.textoOuNulo(complemento),
                Normalizador.textoOuNulo(bairro),
                Normalizador.textoOuNulo(cidade),
                uf == null ? null : uf.toUpperCase()
        );
    }

    public static EnderecoDto de(Endereco e) {
        if (e == null) {
            return null;
        }
        return new EnderecoDto(e.getCep(), e.getLogradouro(), e.getNumero(), e.getComplemento(),
                e.getBairro(), e.getCidade(), e.getUf());
    }
}

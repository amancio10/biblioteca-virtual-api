package io.github.amancio10.bibliotecavirtualapi.acervo.autor;

import io.github.amancio10.bibliotecavirtualapi.common.entity.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "autor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Autor extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 80)
    private String nacionalidade;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(columnDefinition = "text")
    private String biografia;

    public Autor(String nome, String nacionalidade, LocalDate dataNascimento, String biografia) {
        atualizar(nome, nacionalidade, dataNascimento, biografia);
    }

    public void atualizar(String nome, String nacionalidade, LocalDate dataNascimento, String biografia) {
        this.nome = nome;
        this.nacionalidade = nacionalidade;
        this.dataNascimento = dataNascimento;
        this.biografia = biografia;
    }
}

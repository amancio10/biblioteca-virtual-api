package io.github.amancio10.bibliotecavirtualapi.acervo.editora;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "editora")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Editora extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 14, unique = true)
    private String cnpj;

    @Column(length = 255)
    private String site;

    public Editora(String nome, String cnpj, String site) {
        atualizar(nome, cnpj, site);
    }

    public void atualizar(String nome, String cnpj, String site) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.site = site;
    }
}

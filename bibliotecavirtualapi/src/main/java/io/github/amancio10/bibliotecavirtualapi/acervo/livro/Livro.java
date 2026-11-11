package io.github.amancio10.bibliotecavirtualapi.acervo.livro;

import io.github.amancio10.bibliotecavirtualapi.acervo.autor.Autor;
import io.github.amancio10.bibliotecavirtualapi.acervo.categoria.Categoria;
import io.github.amancio10.bibliotecavirtualapi.acervo.editora.Editora;
import io.github.amancio10.bibliotecavirtualapi.common.entity.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@Table(name = "livro")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Livro extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 13)
    private String isbn;

    @Column(nullable = false)
    private String titulo;

    private String subtitulo;

    @Column(columnDefinition = "text")
    private String sinopse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editora_id")
    private Editora editora;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "ano_publicacao")
    private Integer anoPublicacao;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Integer edicao;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    @Column(nullable = false, length = 5)
    private String idioma;

    @Column(name = "capa_url", length = 500)
    private String capaUrl;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Ordem de autoria persistida em livro_autor.ordem (1 = autor principal). */
    @ManyToMany
    @JoinTable(name = "livro_autor",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id"))
    @OrderColumn(name = "ordem")
    @ListIndexBase(1)
    private List<Autor> autores = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "livro_categoria",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();

    public Livro(String isbn) {
        this.isbn = isbn;
    }

    public void atualizarDados(String isbn, String titulo, String subtitulo, String sinopse, Editora editora,
                               Integer anoPublicacao, Integer edicao, Integer numeroPaginas,
                               String idioma, String capaUrl) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.sinopse = sinopse;
        this.editora = editora;
        this.anoPublicacao = anoPublicacao;
        this.edicao = edicao;
        this.numeroPaginas = numeroPaginas;
        this.idioma = idioma;
        this.capaUrl = capaUrl;
    }

    public void definirAutores(List<Autor> autores) {
        this.autores.clear();
        this.autores.addAll(autores);
    }

    public void definirCategorias(Collection<Categoria> categorias) {
        this.categorias.clear();
        this.categorias.addAll(categorias);
    }

    public void inativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }
}

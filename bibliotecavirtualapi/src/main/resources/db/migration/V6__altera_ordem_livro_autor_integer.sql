-- =====================================================================
-- V6 - livro_autor.ordem para INTEGER
-- =====================================================================
-- A coluna é mapeada via @OrderColumn (JPA), que trabalha com INTEGER.
ALTER TABLE livro_autor ALTER COLUMN ordem TYPE INTEGER;

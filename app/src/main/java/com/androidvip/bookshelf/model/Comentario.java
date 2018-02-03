package com.androidvip.bookshelf.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Comentario {
    @Id public long id;
    private String texto;
    private int capitulo;
    private int pagina;
    private long livroId;

    public long getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getCapitulo() {
        return capitulo;
    }

    public void setCapitulo(int capitulo) {
        this.capitulo = capitulo;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public long getLivroId() {
        return livroId;
    }

    public void setLivroId(long livroId) {
        this.livroId = livroId;
    }
}

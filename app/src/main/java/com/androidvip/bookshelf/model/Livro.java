package com.androidvip.bookshelf.model;

import android.support.annotation.IntDef;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Livro {
    @Id
    public long id;

    private String titulo;
    private String autores;
    private int nota;
    private String tags;
    private long comentarioId;
    private String googleBooksId;
    private Date dataInicioLeitura;
    private Date dataTerminoLeitura;
    private int estadoLeitura;

    @IntDef({ESTADO_DESISTIDO, ESTADO_DESEJADO, ESTADO_LENDO, ESTADO_FINALIZADO})
    private @interface EstadoLeitura {}

    public static final int ESTADO_DESISTIDO = -1;
    public static final int ESTADO_DESEJADO = 0;
    public static final int ESTADO_LENDO = 1;
    public static final int ESTADO_FINALIZADO = 2;

    public Livro() {

    }

    public long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutores() {
        return autores;
    }

    public void setAutores(String autores) {
        this.autores = autores;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getComentarioId() {
        return comentarioId;
    }

    public void setComentarioId(long comentarioId) {
        this.comentarioId = comentarioId;
    }

    public String getGoogleBooksId() {
        return googleBooksId;
    }

    public void setGoogleBooksId(String googleBooksId) {
        this.googleBooksId = googleBooksId;
    }

    public Date getDataInicioLeitura() {
        return dataInicioLeitura;
    }

    public void setDataInicioLeitura(Date dataInicioLeitura) {
        this.dataInicioLeitura = dataInicioLeitura;
    }

    public Date getDataTerminoLeitura() {
        return dataTerminoLeitura;
    }

    public void setDataTerminoLeitura(Date dataTerminoLeitura) {
        this.dataTerminoLeitura = dataTerminoLeitura;
    }

    public int getEstadoLeitura() {
        return estadoLeitura;
    }

    public void setEstadoLeitura(@EstadoLeitura int estadoLeitura) {
        this.estadoLeitura = estadoLeitura;
    }
}

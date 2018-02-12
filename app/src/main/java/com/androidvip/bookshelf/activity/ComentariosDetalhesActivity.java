package com.androidvip.bookshelf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.model.Comentario;

import java.util.Date;

import io.objectbox.Box;

public class ComentariosDetalhesActivity extends AppCompatActivity {
    private long id, livroId;
    EditText editTitulo, editComent, editCapitulo, editPagina;
    FloatingActionButton fab;
    private Box<Comentario> comentarioBox;
    private Comentario comentario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios_detalhes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        comentarioBox = ((App) getApplication()).getBoxStore().boxFor(Comentario.class);

        bindViews();

        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getLongExtra("id", 0);
            livroId = intent.getLongExtra("livroId", 0);
            if (id > 0) {
                comentario = comentarioBox.get(id);
                livroId = comentario.getLivroId();
                popular();
            } else
                getSupportActionBar().setTitle(R.string.comentario_novo);
        }

        fab.setOnClickListener(v -> {
            esconderTeclado();
            Comentario novoComentario;
            if (comentario != null)
                novoComentario = gerarComentario(comentario);
            else
                novoComentario = gerarComentario(null);

            if (novoComentario != null) {
                comentarioBox.put(novoComentario);
                Snackbar.make(fab, R.string.comentario_salvo, Snackbar.LENGTH_SHORT).show();
            } else
                Snackbar.make(fab, R.string.comentario_falha_salvar, Snackbar.LENGTH_LONG).show();
        });
    }

    private void esconderTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null)
            view = new View(this);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return true;
    }

    private Comentario gerarComentario(Comentario coment) {
        Comentario baseComentario = coment == null ? new Comentario() : coment;
        try {
            baseComentario.setTitulo(getText(editTitulo));
            baseComentario.setTexto(getText(editComent));
            baseComentario.setCapitulo(Integer.parseInt(getText(editCapitulo)));
            baseComentario.setPagina(Integer.parseInt(getText(editPagina)));
            baseComentario.setData(new Date(System.currentTimeMillis()));
            baseComentario.setLivroId(livroId);
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.erro)
                    .setMessage(R.string.erro_text_fields)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                    })
                    .show();
            return null;
        }
        return baseComentario;
    }

    private void popular() {
        if (comentario != null) {
            setText(editTitulo, comentario.getTitulo());
            setText(editComent, comentario.getTexto());
            setText(editPagina, String.valueOf(comentario.getPagina()));
            setText(editCapitulo, String.valueOf(comentario.getCapitulo()));
        } else {
            Toast.makeText(this, R.string.comentarios_erro, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private String getText(EditText editText) throws Exception {
        String s = editText.getText().toString().trim();
        if (s.equals("")){
            editText.setError(getString(R.string.input_required));
            throw new Exception();
        }
        return s;
    }

    private void setText(EditText editText, String text) {
        if (text.equals("0"))
            setText(editText, "");
        else {
            editText.setText(text);
            editText.setHint(text);
        }
    }

    private void bindViews() {
        fab = findViewById(R.id.fab_comentarios_detalhes);
        editTitulo = findViewById(R.id.detalhes_comentario_titulo);
        editCapitulo = findViewById(R.id.detalhes_comentario_cap);
        editPagina = findViewById(R.id.detalhes_comentario_pagina);
        editComent = findViewById(R.id.detalhes_comentario_coment);
    }

}

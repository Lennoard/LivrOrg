package com.androidvip.bookshelf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.model.Comment;
import com.androidvip.bookshelf.util.Utils;

import java.util.Date;

import io.objectbox.Box;

public class ComentariosDetalhesActivity extends AppCompatActivity {
    private long id, livroId;
    EditText editTitulo, editComent, editCapitulo, editPagina;
    FloatingActionButton fab;
    private Box<Comment> comentarioBox;
    private Comment comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios_detalhes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        comentarioBox = ((App) getApplication()).getBoxStore().boxFor(Comment.class);

        bindViews();

        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getLongExtra("id", 0);
            livroId = intent.getLongExtra("livroId", 0);
            if (id > 0) {
                comment = comentarioBox.get(id);
                livroId = comment.getBookId();
                popular();
            } else
                getSupportActionBar().setTitle(R.string.comentario_novo);
        }

        fab.setOnClickListener(v -> {
            Utils.hideKeyboard(ComentariosDetalhesActivity.this);
            Comment novoComment;
            if (comment != null)
                novoComment = gerarComentario(comment);
            else
                novoComment = gerarComentario(null);

            if (novoComment != null) {
                comentarioBox.put(novoComment);
                Snackbar.make(fab, R.string.comentario_salvo, Snackbar.LENGTH_SHORT).show();
            } else
                Snackbar.make(fab, R.string.comentario_falha_salvar, Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return true;
    }

    private Comment gerarComentario(Comment coment) {
        Comment baseComment = coment == null ? new Comment() : coment;
        try {
            baseComment.setTitle(getText(editTitulo));
            baseComment.setText(getText(editComent));
            baseComment.setChapter(Integer.parseInt(getText(editCapitulo)));
            baseComment.setPage(Integer.parseInt(getText(editPagina)));
            baseComment.setDate(new Date(System.currentTimeMillis()));
            baseComment.setBookId(livroId);
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.erro)
                    .setMessage(R.string.erro_text_fields)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {})
                    .show();
            return null;
        }
        return baseComment;
    }

    private void popular() {
        if (comment != null) {
            setText(editTitulo, comment.getTitle());
            setText(editComent, comment.getText());
            setText(editPagina, String.valueOf(comment.getPage()));
            setText(editCapitulo, String.valueOf(comment.getChapter()));
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

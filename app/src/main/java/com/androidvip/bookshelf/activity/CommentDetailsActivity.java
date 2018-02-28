package com.androidvip.bookshelf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.androidvip.bookshelf.util.K;
import com.androidvip.bookshelf.util.Utils;

import java.util.Date;

import io.objectbox.Box;

public class CommentDetailsActivity extends AppCompatActivity {
    private long commentId, bookId;
    EditText editTitle, editComment, editChapter, editPage;
    FloatingActionButton fab;
    private Box<Comment> commentBox;
    private Comment comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios_detalhes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        commentBox = ((App) getApplication()).getBoxStore().boxFor(Comment.class);

        bindViews();

        Intent intent = getIntent();
        if (intent != null) {
            // Show details of a comment given its id,
            // populating views accordingly
            commentId = intent.getLongExtra(K.EXTRA_COMMENT_ID, 0);
            bookId = intent.getLongExtra("bookId", 0);
            if (commentId > 0) {
                comment = commentBox.get(commentId);
                bookId = comment.getBookId();
                populate();
            } else
                // No id received, don't populate anything,
                // instead, let the user create one
                getSupportActionBar().setTitle(R.string.comentario_novo);
        }

        fab.setOnClickListener(v -> {
            Utils.hideKeyboard(CommentDetailsActivity.this);
            Comment newComment;
            if (comment != null)
                newComment = generateComment(comment);
            else
                newComment = generateComment(null);

            if (newComment != null) {
                commentBox.put(newComment);
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

    /**
     * Generates a {@link Comment} using information from the text fields
     *
     * @param comment do we have a comment already?
     */
    private Comment generateComment(@Nullable Comment comment) {
        Comment baseComment = comment == null
                ? new Comment() // Creates from zero
                : comment; // Fine, we have a comment already,
                           // but we still need to check the text fields
        try {
            baseComment.setTitle(getText(editTitle));
            baseComment.setText(getText(editComment));
            baseComment.setChapter(Integer.parseInt(getText(editChapter)));
            baseComment.setPage(Integer.parseInt(getText(editPage)));
            baseComment.setDate(new Date(System.currentTimeMillis()));
            baseComment.setBookId(bookId);
        } catch (Exception e) {
            // Empty text field detected, we don't want to save any
            // empty data on the book so alert the user about it
            new AlertDialog.Builder(this)
                    .setTitle(R.string.erro)
                    .setMessage(R.string.erro_text_fields)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {})
                    .show();
            // and if so, return null
            return null;
        }
        return baseComment;
    }

    // Shows information about the current comment on the EditTexts
    private void populate() {
        if (comment != null) {
            setText(editTitle, comment.getTitle());
            setText(editComment, comment.getText());
            setText(editPage, String.valueOf(comment.getPage()));
            setText(editChapter, String.valueOf(comment.getChapter()));
        } else {
            // The current comment (retrieved form the local database) points to nothing
            Toast.makeText(this, R.string.comments_error, Toast.LENGTH_LONG).show();
            // Can we deal with this?
            finish();
        }
    }

    /**
     * Gets the text of an EdtText in a String form
     *
     * @throws Exception if the String is empty
     */
    private String getText(EditText editText) throws Exception {
        String s = editText.getText().toString().trim();
        if (s.equals("")){
            editText.setError(getString(R.string.input_required));
            // Throw an exception so we can handle empty field cases
            throw new Exception();
        }
        return s;
    }

    private void setText(EditText editText, String text) {
        if (text.equals("0"))
            // 0 is not a valid page or chapter, so use a blank String instead
            setText(editText, "");
        else {
            editText.setText(text);
            editText.setHint(text);
        }
    }

    private void bindViews() {
        fab = findViewById(R.id.fab_comentarios_detalhes);
        editTitle = findViewById(R.id.detalhes_comentario_titulo);
        editChapter = findViewById(R.id.detalhes_comentario_cap);
        editPage = findViewById(R.id.detalhes_comentario_pagina);
        editComment = findViewById(R.id.detalhes_comentario_coment);
    }

}

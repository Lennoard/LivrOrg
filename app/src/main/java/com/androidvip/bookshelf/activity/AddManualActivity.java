package com.androidvip.bookshelf.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.model.Book;

import java.util.Date;

import io.objectbox.Box;

import static com.androidvip.bookshelf.util.Utils.readingStateToString;

public class AddManualActivity extends AppCompatActivity {
    private Book book;
    private Box<Book> livroBox;
    private TextView estado, nota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_manual);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);

        livroBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);

        book = new Book();
        book.setReadingState(Book.STATE_READING);

        estado = findViewById(R.id.add_manual_estado_leitura);
        nota = findViewById(R.id.add_manual_nota);

        FloatingActionButton fab = findViewById(R.id.add_manual_fab);
        fab.setOnClickListener(view -> {
            EditText titulo = findViewById(R.id.add_manual_titulo);
            EditText autor = findViewById(R.id.add_manual_autor);
            if (titulo.getText().toString().equals(""))
                Snackbar.make(view, R.string.erro_text_fields, Snackbar.LENGTH_LONG).show();
            else {
                book.setTitle(titulo.getText().toString().trim());
                book.setAuthors(autor.getText().toString().trim());
                livroBox.put(book);
                Toast.makeText(AddManualActivity.this, R.string.livro_adicionado, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }

    public void estadoLeitura(View view) {
        int estadoLeitura = book.getReadingState();
        int checkedItem = estadoLeitura == 0 ? -1 : book.getReadingState() - 1;

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_lista)
                .setSingleChoiceItems(R.array.estado_leitura_array, checkedItem, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            book.setReadingState(Book.STATE_READING);
                            book.setReadingStartDate(new Date(System.currentTimeMillis()));
                            book.setReadingEndDate(null);
                            estado.setText(readingStateToString(Book.STATE_READING, AddManualActivity.this));
                            break;
                        case 1:
                            book.setReadingState(Book.STATE_WISH);
                            estado.setText(readingStateToString(Book.STATE_WISH, AddManualActivity.this));
                            break;
                        case 2:
                            book.setReadingState(Book.STATE_ON_HOLD);
                            estado.setText(readingStateToString(Book.STATE_ON_HOLD, AddManualActivity.this));
                            break;
                        case 3:
                            book.setReadingState(Book.STATE_DROPPED);
                            estado.setText(readingStateToString(Book.STATE_DROPPED, AddManualActivity.this));
                            break;
                        case 4:
                            book.setReadingState(Book.STATE_FINISHED);
                            estado.setText(readingStateToString(Book.STATE_FINISHED, AddManualActivity.this));
                            book.setReadingEndDate(new Date(System.currentTimeMillis()));
                            break;
                    }
                    estado.setText(readingStateToString(which + 1, AddManualActivity.this));
                    dialog.dismiss();
                    livroBox.put(book);
                }).show();
    }

    public void nota(View view) {
        String[] notas = getResources().getStringArray(R.array.notas_array);
        new AlertDialog.Builder(this)
                .setTitle(R.string.nota)
                .setSingleChoiceItems(notas, 0, (dialog, which) -> {
                    book.setScore(which + 1);
                    livroBox.put(book);
                    nota.setText(String.valueOf(which + 1));
                    dialog.dismiss();
                }).show();
    }
}

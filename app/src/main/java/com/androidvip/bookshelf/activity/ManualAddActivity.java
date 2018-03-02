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

public class ManualAddActivity extends AppCompatActivity {
    private Book book;
    private Box<Book> bookBox;
    private TextView state, score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_manual);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);

        bookBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);

        // Creates a new Book and sets a default reading state
        book = new Book();
        book.setReadingState(Book.STATE_READING);

        state = findViewById(R.id.add_manual_estado_leitura);
        score = findViewById(R.id.add_manual_nota);

        FloatingActionButton fab = findViewById(R.id.add_manual_fab);
        fab.setOnClickListener(view -> {
            EditText editTitle = findViewById(R.id.manual_add_title);
            EditText editAuthor = findViewById(R.id.manual_add_author);
            if (editTitle.getText().toString().equals(""))
                Snackbar.make(view, R.string.error_text_fields, Snackbar.LENGTH_LONG).show();
            else {
                book.setTitle(editTitle.getText().toString().trim());
                book.setAuthors(editAuthor.getText().toString().trim());
                bookBox.put(book);
                Toast.makeText(ManualAddActivity.this, R.string.book_added, Toast.LENGTH_SHORT).show();
                // A book has been created, there is nothing
                // else to do here so we finish this activity
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Using finish() instead of the activity's history declaration
        // so we can avoid recreation of the previous activity (thus
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // "Back button on ActionBar"
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }

    // onClick for set reading state
    public void readingState(View view) {
        int readingState = book.getReadingState();
        /*
          If the book has no reading state yet (0), use -1 so the
          dialog shows no default item selected, otherwise use the book's
          reading state previously set by user in this activity
        */
        int checkedItem = readingState == 0 ? -1 : book.getReadingState() - 1;

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_to_list)
                .setSingleChoiceItems(R.array.reading_state_array, checkedItem, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            book.setReadingState(Book.STATE_READING);
                            book.setReadingStartDate(new Date(System.currentTimeMillis()));
                            book.setReadingEndDate(null);
                            state.setText(readingStateToString(Book.STATE_READING, ManualAddActivity.this));
                            break;
                        case 1:
                            book.setReadingState(Book.STATE_WISH);
                            state.setText(readingStateToString(Book.STATE_WISH, ManualAddActivity.this));
                            break;
                        case 2:
                            book.setReadingState(Book.STATE_ON_HOLD);
                            state.setText(readingStateToString(Book.STATE_ON_HOLD, ManualAddActivity.this));
                            break;
                        case 3:
                            book.setReadingState(Book.STATE_DROPPED);
                            state.setText(readingStateToString(Book.STATE_DROPPED, ManualAddActivity.this));
                            break;
                        case 4:
                            book.setReadingState(Book.STATE_FINISHED);
                            state.setText(readingStateToString(Book.STATE_FINISHED, ManualAddActivity.this));
                            book.setReadingEndDate(new Date(System.currentTimeMillis()));
                            break;
                    }
                    state.setText(readingStateToString(which + 1, ManualAddActivity.this));
                    dialog.dismiss();
                    bookBox.put(book);
                }).show();
    }

    // onClick for set book score
    public void score(View view) {
        String[] scores = getResources().getStringArray(R.array.scores_array);
        new AlertDialog.Builder(this)
                .setTitle(R.string.score)
                .setSingleChoiceItems(scores, 0, (dialog, which) -> {
                    book.setScore(which + 1);
                    bookBox.put(book);
                    score.setText(String.valueOf(which + 1));
                    dialog.dismiss();
                }).show();
    }
}

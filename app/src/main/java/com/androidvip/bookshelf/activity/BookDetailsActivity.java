package com.androidvip.bookshelf.activity;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.model.Book;
import com.androidvip.bookshelf.util.K;
import com.androidvip.bookshelf.util.Utils;
import com.google.api.services.books.model.Volume;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.objectbox.Box;

import static android.view.animation.AnimationUtils.loadAnimation;
import static com.androidvip.bookshelf.util.Utils.readingStateToString;
import static com.androidvip.bookshelf.util.Utils.notNull;

public class BookDetailsActivity extends AppCompatActivity {
    private TextSwitcher title, author;
    private TextView description, publication;
    private TextView categories, ratings, readingStart, readingEnd;
    private Button readingStateButton, ratingButton;
    EditText tags;
    ImageView cover, saveTagsButton, favoriteButton;
    Book book = null;
    Volume volume;
    private Box<Book> bookBox;
    private boolean isFavorite;
    private Snackbar snackNet;
    private LinearLayout moreDetailsLayout;
    private boolean isPresentInList;

    private BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
                if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    checkIntent();
                    snackNet.dismiss();
                    if (!favoriteButton.isEnabled())
                        favoriteButton.setEnabled(true);
                    if (!ratingButton.isEnabled()) {
                        ratingButton.setEnabled(true);
                        ratingButton.setTextColor(Color.parseColor("#ffab40"));
                    }
                    if (!readingStateButton.isEnabled()) {
                        readingStateButton.setEnabled(true);
                        readingStateButton.setTextColor(Color.parseColor("#ffab40"));
                    }
                } else {
                    snackNet.show();
                    favoriteButton.setEnabled(false);
                    ratingButton.setEnabled(false);
                    ratingButton.setTextColor(Color.parseColor("#9e9e9e"));
                    readingStateButton.setEnabled(false);
                    readingStateButton.setTextColor(Color.parseColor("#9e9e9e"));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        if (toolbarLayout != null) {
            toolbarLayout.setExpandedTitleColor(Color.parseColor("#00ebe6e4"));
        }

        moreDetailsLayout = findViewById(R.id.detalhes_layout_mais_detalhes);
        moreDetailsLayout.setVisibility(View.GONE);

        bindViews();
        
        title.setText(getString(R.string.loading));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Create the snackbar to be shown when the device goes offline
        snackNet = Snackbar.make(findViewById(R.id.cl), R.string.erro_sem_conexao, Snackbar.LENGTH_INDEFINITE);
        // Initial check, ths will be checked before later by the Broadcast Receiver
        if (!Utils.isOnline(this))
            snackNet.show();

        author.setText("");
        registerReceiver(netReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        checkIntent();
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(netReceiver);
        } catch (Exception ignored){}
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return true;
    }

    //onClick
    public void goToComments(View view) {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra(K.EXTRA_BOOK_ID_DB, book.getId());
        startActivity(intent);
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Fetching extra information from the intent
            long bookId = intent.getLongExtra(K.EXTRA_BOOK_ID_DB, 0);
            String volumeId = intent.getStringExtra(K.EXTRA_VOLUME_ID);

            if (volumeId != null && !volumeId.equals("")) {
                /*
                 * We have a valid volumeId, with this we can fetch volume information
                 * from the Google Books API, so set up the activity accordingly.
                 * In this state, the user should be searching for a volume to add to
                 * his/her lists, therefore, we don't need to show some views now...
                 */
                setUpBox(volumeId);
                isPresentInList = false;
                // ... such as these
                tags.setVisibility(View.INVISIBLE);
                saveTagsButton.setVisibility(View.INVISIBLE);
            } else {
                /*
                 * We have no volumeId and a valid bookId, with this we can fetch book
                 * information from the local database, so set up the activity accordingly.
                 * In this state the user might want to update his reading state, dates
                 * comments etc, so we need to show some views to make it possible.
                 */
                if (bookId > 0) {
                   setUpBox(bookId);
                   isPresentInList = true;
                } else {
                    // We didn't get any valid id, the activity is now useless
                    Toast.makeText(this, R.string.detalhes_erro, Toast.LENGTH_LONG).show();
                    // Can we handle this?
                    finish();
                }
            }
        }
    }

    private View.OnClickListener dateListener(boolean readingStart) {
        // Create a Calendar object
        Calendar today = Calendar.getInstance();
        // Set it to today, right now
        today.setTimeInMillis(System.currentTimeMillis());
        return v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Date newDate = new GregorianCalendar(year, month, dayOfMonth).getTime();
                if (readingStart) {
                    // This listener has been called to update the "readingStartDate" property of a book
                    if (book.getReadingState() != Book.STATE_READING) {
                        /*
                         * The user has selected a date the indicates when he/she started to
                         * read a book, but the book is not in the "reading" state, we may use
                         * this opportunity to ask him/her to update the state to "reading"
                         */
                        new AlertDialog.Builder(BookDetailsActivity.this)
                                .setTitle(R.string.records)
                                .setMessage(R.string.aviso_atualizar_lendo)
                                .setPositiveButton(android.R.string.yes, (dialog1, which) -> {
                                    book.setReadingState(Book.STATE_READING);
                                    readingStateButton.setText(readingStateToString(Book.STATE_READING, BookDetailsActivity.this));
                                    bookBox.put(book);
                                })
                                .setNegativeButton(android.R.string.no, (dialog2, which) -> {})
                                .show();
                    }
                    this.readingStart.setText(getString(R.string.inicio_leitura, Utils.dateToString(newDate)));
                    book.setReadingStartDate(newDate);
                    // Update the database
                    bookBox.put(book);
                } else {
                    // This listener has been called to update the "readingEndDate" property of a book
                    if (book.getReadingState() != Book.STATE_FINISHED) {
                        /*
                         * The user has selected a date the indicates when he/she finished to
                         * read a book, but the book is not in the "finished" state, we may use
                         * this opportunity to ask him/her to update the state to "finished"
                         */
                        new AlertDialog.Builder(BookDetailsActivity.this)
                                .setTitle(R.string.records)
                                .setMessage(R.string.aviso_atualizar_finalizado)
                                .setPositiveButton(android.R.string.yes, (dialog1, which) -> {
                                    book.setReadingState(Book.STATE_FINISHED);
                                    readingStateButton.setText(readingStateToString(Book.STATE_FINISHED, BookDetailsActivity.this));
                                    bookBox.put(book);
                                })
                                .setNegativeButton(android.R.string.no, (dialog12, which) -> {})
                                .show();
                    }
                    readingEnd.setText(getString(R.string.termino_leitura, Utils.dateToString(newDate)));
                    book.setReadingEndDate(newDate);
                    // Update the database
                    bookBox.put(book);
                } // Default date information to show when the dialog is created is set below
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            dialog.show();
            // Regardless of what happened above, update the database
            bookBox.put(book);
        };
    }

    // Shows a dialog allowing the user to set a reading state fot the book
    private View.OnClickListener stateListener = v -> {
        int readingState = book.getReadingState();
        /*
         * The dialog's checked item. If the book has no reading state yet (0),
         * then use -1, which is the default value and checks nothing, otherwise
         * use the book's reading state -1 (reading states start from 1)
         */
        int checkedItem = readingState == 0 ? -1 : book.getReadingState() - 1;
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_lista)
                .setSingleChoiceItems(R.array.reading_state_array, checkedItem, (dialog, which) -> {
                    if (readingState == 0) {
                        /*
                         * A book must have a reading state, 0 is not a valid one. We
                         * are assuming that in this scenario, the user is adding a book
                         * to his/her lists, we will user this opportunity to set title
                         * and authors to the book object as well
                         */
                        // These TextSwitchers have TextViews as children, get them
                        TextView titleView = (TextView) title.getCurrentView();
                        TextView authorsView = (TextView) author.getCurrentView();
                        // Use their text to fill up the book's information
                        book.setTitle(titleView.getText().toString());
                        book.setAuthors(authorsView.getText().toString());
                        // Finally set the Google Books ID with the currently showing volume
                        book.setGoogleBooksId(volume.getId());
                    }
                    switch (which) {
                        case 0:
                            book.setReadingState(Book.STATE_READING);
                            book.setReadingStartDate(new Date(System.currentTimeMillis()));
                            book.setReadingEndDate(null);
                            readingStateButton.setText(readingStateToString(Book.STATE_READING, BookDetailsActivity.this));
                            break;
                        case 1:
                            book.setReadingState(Book.STATE_WISH);
                            readingStateButton.setText(readingStateToString(Book.STATE_WISH, BookDetailsActivity.this));
                            break;
                        case 2:
                            book.setReadingState(Book.STATE_ON_HOLD);
                            readingStateButton.setText(readingStateToString(Book.STATE_ON_HOLD, BookDetailsActivity.this));
                            break;
                        case 3:
                            book.setReadingState(Book.STATE_DROPPED);
                            readingStateButton.setText(readingStateToString(Book.STATE_DROPPED, BookDetailsActivity.this));
                            break;
                        case 4:
                            book.setReadingState(Book.STATE_FINISHED);
                            readingStateButton.setText(readingStateToString(Book.STATE_FINISHED, BookDetailsActivity.this));
                            book.setReadingEndDate(new Date(System.currentTimeMillis()));
                            break;
                    }
                    readingStateButton.setText(readingStateToString(which + 1, BookDetailsActivity.this));
                    dialog.dismiss();
                    // Regardless of what happened above, update the database
                    bookBox.put(book);

                }).show();
    };

    /**
     * Method to set up the book box and get the volume information based
     * on the given id. If the method was called, we have a book saved in the
     * local database and we want to fetch online information with its id.
     *
     * @param bookId the Google Books ID
     */
    private void setUpBox(long bookId) {
        bookBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);
        book = bookBox.get(bookId);

        getVolume(book.getGoogleBooksId());
    }

    /**
     * Method to set up the book box and get the volume information based
     * on the given id. If the method was called, we have only the id and we
     * with this we can fetch the volume information.
     *
     * @param volumeId the Google Books ID
     */
    private void setUpBox(String volumeId) {
        // We cannot fetch the volume's information without internet connection
        if (Utils.isOnline(this)) {
            bookBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);
            getVolume(volumeId);
        }
        // The receiver and onStart() will handle "else" for us
    }

    private void getVolume(String volumeId) {
        if (volumeId != null && !volumeId.equals("")) {
            // Google Books id is valid, proceed
            if (!Utils.isOnline(this) && book != null) {
                // We are not online but we have book
                populateOffline();
            } else {
                // Fetch volume information
                new Thread(() -> {
                    try {
                        volume = Utils.getVolume(volumeId);
                    } catch (Exception ignored) {}
                    runOnUiThread(this::populate);
                }).start();
            }
        } else {
            populateOffline();
        }
    }

    private void populateOffline() {
        title.setText(notNull(book.getTitle(), getString(R.string.loading)));
        author.setText(notNull(book.getAuthors(), getString(R.string.detalhes_erro_autor)));
        description.setText(R.string.detalhes_erro_descricao);
        categories.setText(R.string.detalhes_erro_categoria);

        readingStateButton.setText(readingStateToString(book.getReadingState(), BookDetailsActivity.this));
        ratingButton.setText(scoreToString(book.getScore()));
        tags.setText(notNull(book.getTags(), ""));

        String startStr = Utils.dateToString(book.getReadingStartDate());
        String endStr = Utils.dateToString(book.getReadingEndDate());
        readingStart.setText(startStr.equals("")
                ? getString(R.string.inicio_leitura, "-")
                : getString(R.string.inicio_leitura, startStr));
        readingEnd.setText(endStr.equals("")
                ? getString(R.string.termino_leitura, "-")
                : getString(R.string.termino_leitura, endStr));

        if (isPresentInList) {
            /*
             * We have a book already saved in the local database, set up listeners,
             * show some views so the user can update the reading information, edit
             * tags, favorite, add comments etc.
             */
            setUpListeners();
            moreDetailsLayout.setVisibility(View.VISIBLE);

            // We are offline here, no cover image to fetch
            cover.setImageResource(R.drawable.broken_image);
            favoriteButton.setVisibility(View.VISIBLE);
            if (book.isFavorite())
                favoriteButton.setImageResource(R.drawable.ic_favorite_enabled);
            else
                favoriteButton.setImageResource(R.drawable.ic_favorite);
            isFavorite = book.isFavorite();
        } else {
            favoriteButton.setVisibility(View.INVISIBLE);
        }
    }

    private void populate() {
        if (book == null) {
            // No book provided (adding to a list?), create an empty one
            book = new Book();
            readingStateButton.setText(R.string.add_lista);
            ratingButton.setText(scoreToString(0));
            ratingButton.setEnabled(false);
            ratingButton.setTextColor(Color.parseColor("#9e9e9e"));
        } else {
            populateOffline();
        }

        if (isPresentInList)
            moreDetailsLayout.setVisibility(View.VISIBLE);

        if (volume != null) {
            Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
            if (volumeInfo != null) {
                title.setText(notNull(volumeInfo.getTitle(), getString(R.string.loading)));
                if (volumeInfo.getAuthors() != null)
                    author.setText(TextUtils.join(", ", volumeInfo.getAuthors()));

                publication.setText(getString(R.string.data_publicacao,
                        notNull(volumeInfo.getPublishedDate(), getString(R.string.detalhes_erro_data_publicacao)),
                        notNull(volumeInfo.getPublisher(), getString(R.string.detalhes_erro_publicador))));

                ratings.setText(getString(R.string.classificacoes_format,
                        volumeInfo.getAverageRating() == null ? 0 : volumeInfo.getAverageRating().floatValue(),
                        volumeInfo.getRatingsCount() == null ? 0 : volumeInfo.getRatingsCount(),
                        notNull(volumeInfo.getMaturityRating(), getString(R.string.detalhes_erro_maturidade))));

                String desc = notNull(volumeInfo.getDescription(), getString(R.string.detalhes_erro_descricao));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    description.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_COMPACT));
                else
                    description.setText(Html.fromHtml(desc));

                if (volumeInfo.getCategories() != null) {
                    String cats = TextUtils.join(", ", volumeInfo.getCategories());
                    categories.setText(cats.equals("") || cats.equals(", ") ? getString(R.string.detalhes_erro_categoria) : cats);
                } else
                    categories.setText(R.string.detalhes_erro_categoria);

                if (volumeInfo.getImageLinks() != null)
                    Picasso.with(this)
                            .load(volumeInfo.getImageLinks().getThumbnail())
                            .placeholder(R.drawable.loading_image)
                            .error(R.drawable.broken_image)
                            .into(cover);
                else
                    Picasso.with(this).load(R.drawable.broken_image).into(cover);
            }
        }
        setUpListeners();
    }

    private void setUpListeners() {
        readingStateButton.setOnClickListener(stateListener);
        readingStart.setOnClickListener(dateListener(true));
        readingEnd.setOnClickListener(dateListener(false));
        ratingButton.setOnClickListener(v -> {
            int checkedItem = book.getScore() == 0 ? -1 : book.getScore() -1;
            String[] notas = getResources().getStringArray(R.array.scores_array);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.score)
                    .setSingleChoiceItems(notas, checkedItem, (dialog, which) -> {
                        book.setScore(which + 1);
                        bookBox.put(book);
                        ratingButton.setText(scoreToString(which + 1));
                        dialog.dismiss();
                    }).show();
        });
        favoriteButton.setOnClickListener(view -> {
            if (isFavorite) {
                book.setFavorite(false);
                favoriteButton.setColorFilter(null);
                favoriteButton.setImageResource(R.drawable.ic_favorite);
                isFavorite = false;
            } else {
                book.setFavorite(true);
                favoriteButton.setColorFilter(null);
                favoriteButton.setImageResource(R.drawable.ic_favorite_enabled);
                isFavorite = true;
            }
            bookBox.put(book);
        });
        favoriteButton.setOnLongClickListener(view -> {
            Toast.makeText(BookDetailsActivity.this, R.string.favorite_a_book, Toast.LENGTH_SHORT).show();
            return true;
        });
        saveTagsButton.setOnClickListener(v -> {
            book.setTags(tags.getText().toString());
            bookBox.put(book);
        });
    }

    /**
     * Formats a score string given a book score
     *
     * @param score the book's score set by the user
     * @return the formatted string e.g "Score: 5" or "No score" if no valid score is given
     */
    private String scoreToString(int score) {
        return score == 0 ? getString(R.string.score_no_score) : getString(R.string.score_format, score);
    }

    private void bindViews() {
        title = findViewById(R.id.detalhes_titulo);
        author = findViewById(R.id.detalhes_autores);
        description = findViewById(R.id.detalhes_descricao);
        publication = findViewById(R.id.detalhes_publicacao);
        readingStateButton = findViewById(R.id.detalhes_estado);
        ratingButton = findViewById(R.id.detalhes_nota);
        tags = findViewById(R.id.detalhes_tag_edit);
        saveTagsButton = findViewById(R.id.detalhes_tag_botao);
        cover = findViewById(R.id.detalhes_capa);
        categories = findViewById(R.id.detalhes_categorias);
        ratings = findViewById(R.id.detalhes_classificacoes);
        readingStart = findViewById(R.id.detalhes_inicio_leitura);
        readingEnd = findViewById(R.id.detalhes_fim_leitura);
        favoriteButton = findViewById(R.id.detalhes_favorito);

        setUpAnimations();
    }

    // TextSwitcher animations for smoothness' sake
    private void setUpAnimations() {
        title.setFactory(() -> {
            TextView textView = new TextView(BookDetailsActivity.this);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(20);
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(Color.parseColor("#212121"));
            return textView;
        });

        author.setFactory(() -> {
            TextView textView = new TextView(BookDetailsActivity.this);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(14);
            textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            textView.setSingleLine();
            textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            textView.setSelected(true);
            return textView;
        });

        Animation in = loadAnimation(this, android.R.anim.fade_in);
        Animation out = loadAnimation(this, android.R.anim.fade_out);
        in.setDuration(1000);
        out.setDuration(320);

        title.setInAnimation(in);
        title.setOutAnimation(out);
        author.setInAnimation(in);
        author.setOutAnimation(out);
    }
}

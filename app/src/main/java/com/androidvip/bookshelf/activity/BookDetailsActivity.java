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
        setContentView(R.layout.activity_detalhes);

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
        
        title.setText(getString(R.string.carregando));
    }

    @Override
    protected void onStart() {
        super.onStart();
        snackNet = Snackbar.make(findViewById(R.id.cl), R.string.erro_sem_conexao, Snackbar.LENGTH_INDEFINITE);
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
    public void comments(View view) {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra(K.EXTRA_BOOK_ID, book.getId());
        startActivity(intent);
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            long bookId = intent.getLongExtra(K.EXTRA_BOOK_ID, 0);
            String volumeId = intent.getStringExtra(K.EXTRA_VOLUME_ID);

            if (volumeId != null && !volumeId.equals("")) {
                setUpBox(volumeId);
                isPresentInList = false;
                tags.setVisibility(View.INVISIBLE);
                saveTagsButton.setVisibility(View.INVISIBLE);
            } else {
                if (bookId > 0) {
                   setUpBox(bookId);
                   isPresentInList = true;
                } else {
                    Toast.makeText(this, R.string.detalhes_erro, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private View.OnClickListener dateListener(boolean readingStart) {
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());
        return v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Date newDate = new GregorianCalendar(year, month, dayOfMonth).getTime();
                if (readingStart) {
                    if (book.getReadingState() != Book.STATE_READING) {
                        new AlertDialog.Builder(BookDetailsActivity.this)
                                .setTitle(R.string.registros)
                                .setMessage(R.string.aviso_atualizar_lendo)
                                .setPositiveButton(android.R.string.yes, (dialog1, which) -> {
                                    book.setReadingState(Book.STATE_READING);
                                    readingStateButton.setText(readingStateToString(Book.STATE_READING, BookDetailsActivity.this));
                                    bookBox.put(book);
                                })
                                .setNegativeButton(android.R.string.no, (dialog12, which) -> {})
                                .show();
                    }
                    this.readingStart.setText(getString(R.string.inicio_leitura, Utils.dateToString(newDate)));
                    book.setReadingStartDate(newDate);
                    bookBox.put(book);
                } else {
                    if (book.getReadingState() != Book.STATE_FINISHED) {
                        new AlertDialog.Builder(BookDetailsActivity.this)
                                .setTitle(R.string.registros)
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
                    bookBox.put(book);
                }
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            dialog.show();
            bookBox.put(book);
        };
    }

    private View.OnClickListener estadoListener = v -> {
        int readingState = book.getReadingState();
        int checkedItem = readingState == 0 ? -1 : book.getReadingState() - 1;
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_lista)
                .setSingleChoiceItems(R.array.reading_state_array, checkedItem, (dialog, which) -> {
                    if (readingState == 0){
                        TextView tituloView = (TextView) title.getCurrentView();
                        TextView autoresView = (TextView) author.getCurrentView();
                        book.setTitle(tituloView.getText().toString());
                        book.setAuthors(autoresView.getText().toString());
                        book.setGoogleBooksId(volume.getId());
                    }
                    switch (which) {
                        case 0:
                            book.setReadingState(Book.STATE_READING);
                            book.setReadingStartDate(new Date(System.currentTimeMillis()));
                            book.setReadingEndDate(null);
                            BookDetailsActivity.this.readingStateButton.setText(readingStateToString(Book.STATE_READING, BookDetailsActivity.this));
                            break;
                        case 1:
                            book.setReadingState(Book.STATE_WISH);
                            BookDetailsActivity.this.readingStateButton.setText(readingStateToString(Book.STATE_WISH, BookDetailsActivity.this));
                            break;
                        case 2:
                            book.setReadingState(Book.STATE_ON_HOLD);
                            BookDetailsActivity.this.readingStateButton.setText(readingStateToString(Book.STATE_ON_HOLD, BookDetailsActivity.this));
                            break;
                        case 3:
                            book.setReadingState(Book.STATE_DROPPED);
                            BookDetailsActivity.this.readingStateButton.setText(readingStateToString(Book.STATE_DROPPED, BookDetailsActivity.this));
                            break;
                        case 4:
                            book.setReadingState(Book.STATE_FINISHED);
                            BookDetailsActivity.this.readingStateButton.setText(readingStateToString(Book.STATE_FINISHED, BookDetailsActivity.this));
                            book.setReadingEndDate(new Date(System.currentTimeMillis()));
                            break;
                    }
                    BookDetailsActivity.this.readingStateButton.setText(readingStateToString(which + 1, BookDetailsActivity.this));
                    dialog.dismiss();
                    bookBox.put(book);

                }).show();
    };

    private void setUpBox(long bookId) {
        bookBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);
        book = bookBox.get(bookId);

        getVolume(book.getGoogleBooksId());
    }

    private void setUpBox(String volumeId) {
        if (Utils.isOnline(this)) {
            bookBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);
            getVolume(volumeId);
        }
    }

    private void getVolume(String volumeId) {
        if (volumeId != null && !volumeId.equals("")) {
            // Google Books id is valid, proceed
            if (!Utils.isOnline(this) && book != null) {
                popularOffline();
            } else {
                new Thread(() -> {
                    try {
                        volume = Utils.getVolume(volumeId);
                    } catch (Exception ignored) {}
                    runOnUiThread(this::popular);
                }).start();
            }
        } else {
            popularOffline();
        }
    }

    private void popularOffline() {
        title.setText(notNull(book.getTitle(), getString(R.string.carregando)));
        author.setText(notNull(book.getAuthors(), getString(R.string.detalhes_erro_autor)));
        description.setText(R.string.detalhes_erro_descricao);
        categories.setText(R.string.detalhes_erro_categoria);

        readingStateButton.setText(readingStateToString(book.getReadingState(), BookDetailsActivity.this));
        ratingButton.setText(notaToString(book.getScore()));
        tags.setText(notNull(book.getTags(), ""));

        String inicioStr = Utils.dateToString(book.getReadingStartDate());
        String fimStr = Utils.dateToString(book.getReadingEndDate());
        readingStart.setText(inicioStr.equals("")
                ? getString(R.string.inicio_leitura, "-")
                : getString(R.string.inicio_leitura, inicioStr));
        readingEnd.setText(fimStr.equals("")
                ? getString(R.string.termino_leitura, "-")
                : getString(R.string.termino_leitura, fimStr));

        if (isPresentInList) {
            configurarListeners();
            moreDetailsLayout.setVisibility(View.VISIBLE);
            cover.setImageResource(R.drawable.broken_image);
            favoriteButton.setVisibility(View.VISIBLE);
            if (book.isFavorite())
                favoriteButton.setImageResource(R.drawable.ic_favorito_ativado);
            else
                favoriteButton.setImageResource(R.drawable.ic_favorito);
            isFavorite = book.isFavorite();
        } else {
            favoriteButton.setVisibility(View.INVISIBLE);
        }
    }

    private void popular() {
        if (book == null) {
            book = new Book();
            readingStateButton.setText(R.string.add_lista);
            ratingButton.setText(notaToString(0));
            ratingButton.setEnabled(false);
            ratingButton.setTextColor(Color.parseColor("#9e9e9e"));
        } else {
            popularOffline();
        }

        if (isPresentInList)
            moreDetailsLayout.setVisibility(View.VISIBLE);

        if (volume != null) {
            Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
            if (volumeInfo != null) {
                title.setText(notNull(volumeInfo.getTitle(), getString(R.string.carregando)));
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
                            .placeholder(R.drawable.carregando_imagem)
                            .error(R.drawable.broken_image)
                            .into(cover);
                else
                    Picasso.with(this).load(R.drawable.broken_image).into(cover);
            }
        }
        configurarListeners();
    }

    private void configurarListeners() {
        readingStateButton.setOnClickListener(estadoListener);
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
                        ratingButton.setText(notaToString(which + 1));
                        dialog.dismiss();
                    }).show();
        });
        favoriteButton.setOnClickListener(view -> {
            if (isFavorite) {
                book.setFavorite(false);
                favoriteButton.setColorFilter(null);
                favoriteButton.setImageResource(R.drawable.ic_favorito);
                isFavorite = false;
            } else {
                book.setFavorite(true);
                favoriteButton.setColorFilter(null);
                favoriteButton.setImageResource(R.drawable.ic_favorito_ativado);
                isFavorite = true;
            }
            bookBox.put(book);
        });
        favoriteButton.setOnLongClickListener(view -> {
            Toast.makeText(BookDetailsActivity.this, "Favoritar / Desfavoritar", Toast.LENGTH_SHORT).show();
            return true;
        });
        saveTagsButton.setOnClickListener(v -> {
            book.setTags(tags.getText().toString());
            bookBox.put(book);
        });
    }

    private String notaToString(int nota) {
        return nota == 0 ? getString(R.string.nota_sem_nota) : getString(R.string.nota_format, nota);
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

        configurarAnimacoes();
    }

    private void configurarAnimacoes() {
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

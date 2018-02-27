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
import com.androidvip.bookshelf.util.Utils;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.model.Volume;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.objectbox.Box;

import static android.view.animation.AnimationUtils.loadAnimation;
import static com.androidvip.bookshelf.util.Utils.readingStateToString;
import static com.androidvip.bookshelf.util.Utils.notNull;

public class DetalhesActivity extends AppCompatActivity {
    private TextSwitcher titulo, autores;
    private TextView descricao, publicacao;
    private TextView categorias, classificacoes, inicioLeitura, terminoLeitura;
    private Button estadoLeitura, nota;
    EditText tags;
    ImageView capa, salvarTags, favorito;
    Book book = null;
    Volume volume;
    private Box<Book> livroBox;
    private boolean favoritado;
    private Snackbar snackNet;
    private LinearLayout maisDetalhesLayout;
    private boolean presenteEmLista;

    private BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
                if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    checarIntent();
                    snackNet.dismiss();
                    if (!favorito.isEnabled())
                        favorito.setEnabled(true);
                    if (!nota.isEnabled()) {
                        nota.setEnabled(true);
                        nota.setTextColor(Color.parseColor("#ffab40"));
                    }
                    if (!estadoLeitura.isEnabled()) {
                        estadoLeitura.setEnabled(true);
                        estadoLeitura.setTextColor(Color.parseColor("#ffab40"));
                    }
                } else {
                    snackNet.show();
                    favorito.setEnabled(false);
                    nota.setEnabled(false);
                    nota.setTextColor(Color.parseColor("#9e9e9e"));
                    estadoLeitura.setEnabled(false);
                    estadoLeitura.setTextColor(Color.parseColor("#9e9e9e"));
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

        maisDetalhesLayout = findViewById(R.id.detalhes_layout_mais_detalhes);
        maisDetalhesLayout.setVisibility(View.GONE);

        bindViews();
        
        titulo.setText(getString(R.string.carregando));
    }

    @Override
    protected void onStart() {
        super.onStart();
        snackNet = Snackbar.make(findViewById(R.id.cl), R.string.erro_sem_conexao, Snackbar.LENGTH_INDEFINITE);
        if (!Utils.isOnline(this))
            snackNet.show();

        autores.setText("");
        registerReceiver(netReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        checarIntent();
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
    public void comentarios(View view) {
        Intent intent = new Intent(this, ComentariosActivity.class);
        intent.putExtra("livroId", book.getId());
        startActivity(intent);
    }

    private void checarIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            long livroId = intent.getLongExtra("livroId", 0);
            String volumeId = intent.getStringExtra("volumeId");

            if (volumeId != null && !volumeId.equals("")) {
                configurarBox(volumeId);
                presenteEmLista = false;
                tags.setVisibility(View.INVISIBLE);
                salvarTags.setVisibility(View.INVISIBLE);
            } else {
                if (livroId > 0) {
                   configurarBox(livroId);
                   presenteEmLista = true;
                } else {
                    Toast.makeText(this, R.string.detalhes_erro, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private View.OnClickListener dataListener(boolean inicio) {
        Calendar hoje = Calendar.getInstance();
        hoje.setTimeInMillis(System.currentTimeMillis());
        return v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Date novaData = new GregorianCalendar(year, month, dayOfMonth).getTime();
                if (inicio) {
                    if (book.getReadingState() != Book.STATE_READING) {
                        new AlertDialog.Builder(DetalhesActivity.this)
                                .setTitle(R.string.registros)
                                .setMessage(R.string.aviso_atualizar_lendo)
                                .setPositiveButton(android.R.string.yes, (dialog1, which) -> {
                                    book.setReadingState(Book.STATE_READING);
                                    estadoLeitura.setText(readingStateToString(Book.STATE_READING, DetalhesActivity.this));
                                    livroBox.put(book);
                                })
                                .setNegativeButton(android.R.string.no, (dialog12, which) -> {})
                                .show();
                    }
                    inicioLeitura.setText(getString(R.string.inicio_leitura, Utils.dateToString(novaData)));
                    book.setReadingStartDate(novaData);
                    livroBox.put(book);
                } else {
                    if (book.getReadingState() != Book.STATE_FINISHED) {
                        new AlertDialog.Builder(DetalhesActivity.this)
                                .setTitle(R.string.registros)
                                .setMessage(R.string.aviso_atualizar_finalizado)
                                .setPositiveButton(android.R.string.yes, (dialog1, which) -> {
                                    book.setReadingState(Book.STATE_FINISHED);
                                    estadoLeitura.setText(readingStateToString(Book.STATE_FINISHED, DetalhesActivity.this));
                                    livroBox.put(book);
                                })
                                .setNegativeButton(android.R.string.no, (dialog12, which) -> {})
                                .show();
                    }
                    terminoLeitura.setText(getString(R.string.termino_leitura, Utils.dateToString(novaData)));
                    book.setReadingEndDate(novaData);
                    livroBox.put(book);
                }
            }, hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH));
            dialog.show();
            livroBox.put(book);
        };
    }

    private View.OnClickListener estadoListener = v -> {
        int estadoLeitura = book.getReadingState();
        int checkedItem = estadoLeitura == 0 ? -1 : book.getReadingState() - 1;
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_lista)
                .setSingleChoiceItems(R.array.estado_leitura_array, checkedItem, (dialog, which) -> {
                    if (estadoLeitura == 0){
                        TextView tituloView = (TextView)titulo.getCurrentView();
                        TextView autoresView = (TextView)autores.getCurrentView();
                        book.setTitle(tituloView.getText().toString());
                        book.setAuthors(autoresView.getText().toString());
                        book.setGoogleBooksId(volume.getId());
                    }
                    switch (which) {
                        case 0:
                            book.setReadingState(Book.STATE_READING);
                            book.setReadingStartDate(new Date(System.currentTimeMillis()));
                            book.setReadingEndDate(null);
                            DetalhesActivity.this.estadoLeitura.setText(readingStateToString(Book.STATE_READING, DetalhesActivity.this));
                            break;
                        case 1:
                            book.setReadingState(Book.STATE_WISH);
                            DetalhesActivity.this.estadoLeitura.setText(readingStateToString(Book.STATE_WISH, DetalhesActivity.this));
                            break;
                        case 2:
                            book.setReadingState(Book.STATE_ON_HOLD);
                            DetalhesActivity.this.estadoLeitura.setText(readingStateToString(Book.STATE_ON_HOLD, DetalhesActivity.this));
                            break;
                        case 3:
                            book.setReadingState(Book.STATE_DROPPED);
                            DetalhesActivity.this.estadoLeitura.setText(readingStateToString(Book.STATE_DROPPED, DetalhesActivity.this));
                            break;
                        case 4:
                            book.setReadingState(Book.STATE_FINISHED);
                            DetalhesActivity.this.estadoLeitura.setText(readingStateToString(Book.STATE_FINISHED, DetalhesActivity.this));
                            book.setReadingEndDate(new Date(System.currentTimeMillis()));
                            break;
                    }
                    DetalhesActivity.this.estadoLeitura.setText(readingStateToString(which + 1, DetalhesActivity.this));
                    dialog.dismiss();
                    livroBox.put(book);

                }).show();
    };

    private void configurarBox(long livroId) {
        livroBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);
        book = livroBox.get(livroId);

        obterVolume(book.getGoogleBooksId());
    }

    private void configurarBox(String volumeId) {
        if (Utils.isOnline(this)) {
            livroBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);
            obterVolume(volumeId);
        }
    }

    private void obterVolume(String volumeId) {
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
        titulo.setText(notNull(book.getTitle(), getString(R.string.carregando)));
        autores.setText(notNull(book.getAuthors(), getString(R.string.detalhes_erro_autor)));
        descricao.setText(R.string.detalhes_erro_descricao);
        categorias.setText(R.string.detalhes_erro_categoria);

        estadoLeitura.setText(readingStateToString(book.getReadingState(), DetalhesActivity.this));
        nota.setText(notaToString(book.getScore()));
        tags.setText(notNull(book.getTags(), ""));

        String inicioStr = Utils.dateToString(book.getReadingStartDate());
        String fimStr = Utils.dateToString(book.getReadingEndDate());
        inicioLeitura.setText(inicioStr.equals("")
                ? getString(R.string.inicio_leitura, "-")
                : getString(R.string.inicio_leitura, inicioStr));
        terminoLeitura.setText(fimStr.equals("")
                ? getString(R.string.termino_leitura, "-")
                : getString(R.string.termino_leitura, fimStr));

        if (presenteEmLista) {
            configurarListeners();
            maisDetalhesLayout.setVisibility(View.VISIBLE);
            capa.setImageResource(R.drawable.broken_image);
            favorito.setVisibility(View.VISIBLE);
            if (book.isFavorite())
                favorito.setImageResource(R.drawable.ic_favorito_ativado);
            else
                favorito.setImageResource(R.drawable.ic_favorito);
            favoritado = book.isFavorite();
        } else {
            favorito.setVisibility(View.INVISIBLE);
        }
    }

    private void popular() {
        if (book == null) {
            book = new Book();
            estadoLeitura.setText(R.string.add_lista);
            nota.setText(notaToString(0));
            nota.setEnabled(false);
            nota.setTextColor(Color.parseColor("#9e9e9e"));
        } else {
            popularOffline();
        }

        if (presenteEmLista)
            maisDetalhesLayout.setVisibility(View.VISIBLE);

        if (volume != null) {
            Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
            if (volumeInfo != null) {
                titulo.setText(notNull(volumeInfo.getTitle(), getString(R.string.carregando)));
                if (volumeInfo.getAuthors() != null)
                    autores.setText(TextUtils.join(", ", volumeInfo.getAuthors()));

                publicacao.setText(getString(R.string.data_publicacao,
                        notNull(volumeInfo.getPublishedDate(), getString(R.string.detalhes_erro_data_publicacao)),
                        notNull(volumeInfo.getPublisher(), getString(R.string.detalhes_erro_publicador))));

                classificacoes.setText(getString(R.string.classificacoes_format,
                        volumeInfo.getAverageRating() == null ? 0 : volumeInfo.getAverageRating().floatValue(),
                        volumeInfo.getRatingsCount() == null ? 0 : volumeInfo.getRatingsCount(),
                        notNull(volumeInfo.getMaturityRating(), getString(R.string.detalhes_erro_maturidade))));

                String desc = notNull(volumeInfo.getDescription(), getString(R.string.detalhes_erro_descricao));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    descricao.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_COMPACT));
                else
                    descricao.setText(Html.fromHtml(desc));

                if (volumeInfo.getCategories() != null) {
                    String cats = TextUtils.join(", ", volumeInfo.getCategories());
                    categorias.setText(cats.equals("") || cats.equals(", ") ? getString(R.string.detalhes_erro_categoria) : cats);
                } else
                    categorias.setText(R.string.detalhes_erro_categoria);

                if (volumeInfo.getImageLinks() != null)
                    Picasso.with(this)
                            .load(volumeInfo.getImageLinks().getThumbnail())
                            .placeholder(R.drawable.carregando_imagem)
                            .error(R.drawable.broken_image)
                            .into(capa);
                else
                    Picasso.with(this).load(R.drawable.broken_image).into(capa);
            }
        }
        configurarListeners();
    }

    private void configurarListeners() {
        estadoLeitura.setOnClickListener(estadoListener);
        inicioLeitura.setOnClickListener(dataListener(true));
        terminoLeitura.setOnClickListener(dataListener(false));
        nota.setOnClickListener(v -> {
            int checkedItem = book.getScore() == 0 ? -1 : book.getScore() -1;
            String[] notas = getResources().getStringArray(R.array.notas_array);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.nota)
                    .setSingleChoiceItems(notas, checkedItem, (dialog, which) -> {
                        book.setScore(which + 1);
                        livroBox.put(book);
                        nota.setText(notaToString(which + 1));
                        dialog.dismiss();
                    }).show();
        });
        favorito.setOnClickListener(view -> {
            if (favoritado) {
                book.setFavorite(false);
                favorito.setColorFilter(null);
                favorito.setImageResource(R.drawable.ic_favorito);
                favoritado = false;
            } else {
                book.setFavorite(true);
                favorito.setColorFilter(null);
                favorito.setImageResource(R.drawable.ic_favorito_ativado);
                favoritado = true;
            }
            livroBox.put(book);
        });
        favorito.setOnLongClickListener(view -> {
            Toast.makeText(DetalhesActivity.this, "Favoritar / Desfavoritar", Toast.LENGTH_SHORT).show();
            return true;
        });
        salvarTags.setOnClickListener(v -> {
            book.setTags(tags.getText().toString());
            livroBox.put(book);
        });
    }

    private String notaToString(int nota) {
        return nota == 0 ? getString(R.string.nota_sem_nota) : getString(R.string.nota_format, nota);
    }

    private void bindViews() {
        titulo = findViewById(R.id.detalhes_titulo);
        autores = findViewById(R.id.detalhes_autores);
        descricao = findViewById(R.id.detalhes_descricao);
        publicacao = findViewById(R.id.detalhes_publicacao);
        estadoLeitura = findViewById(R.id.detalhes_estado);
        nota = findViewById(R.id.detalhes_nota);
        tags = findViewById(R.id.detalhes_tag_edit);
        salvarTags = findViewById(R.id.detalhes_tag_botao);
        capa = findViewById(R.id.detalhes_capa);
        categorias = findViewById(R.id.detalhes_categorias);
        classificacoes = findViewById(R.id.detalhes_classificacoes);
        inicioLeitura = findViewById(R.id.detalhes_inicio_leitura);
        terminoLeitura = findViewById(R.id.detalhes_fim_leitura);
        favorito = findViewById(R.id.detalhes_favorito);

        configurarAnimacoes();
    }

    private void configurarAnimacoes() {
        titulo.setFactory(() -> {
            TextView textView = new TextView(DetalhesActivity.this);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(20);
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(Color.parseColor("#212121"));
            return textView;
        });

        autores.setFactory(() -> {
            TextView textView = new TextView(DetalhesActivity.this);
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

        titulo.setInAnimation(in);
        titulo.setOutAnimation(out);
        autores.setInAnimation(in);
        autores.setOutAnimation(out);
    }
}

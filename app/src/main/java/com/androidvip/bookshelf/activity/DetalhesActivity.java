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
import android.widget.ViewSwitcher;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.model.Livro;
import com.androidvip.bookshelf.util.Utils;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.model.Volume;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.objectbox.Box;

import static android.view.animation.AnimationUtils.loadAnimation;
import static com.androidvip.bookshelf.util.Utils.estadoLeituraToString;
import static com.androidvip.bookshelf.util.Utils.notNull;

public class DetalhesActivity extends AppCompatActivity {
    private TextSwitcher titulo, autores;
    private TextView descricao, publicacao;
    private TextView categorias, classificacoes, inicioLeitura, terminoLeitura;
    private Button estadoLeitura, nota;
    EditText tags;
    ImageView capa, salvarTags, favorito;
    Livro livro = null;
    Volume volume;
    private Box<Livro> livroBox;
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        snackNet = Snackbar.make(findViewById(R.id.cl), R.string.erro_sem_conexao, Snackbar.LENGTH_INDEFINITE);
        if (!Utils.isOnline(this))
            snackNet.show();

        titulo.setText(getString(R.string.carregando));
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
        intent.putExtra("livroId", livro.getId());
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
                    if (livro.getEstadoLeitura() != Livro.ESTADO_LENDO) {
                        new AlertDialog.Builder(DetalhesActivity.this)
                                .setTitle(R.string.registros)
                                .setMessage(R.string.aviso_atualizar_lendo)
                                .setPositiveButton(android.R.string.yes, (dialog1, which) -> {
                                    livro.setEstadoLeitura(Livro.ESTADO_LENDO);
                                    estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_LENDO, DetalhesActivity.this));
                                    livroBox.put(livro);
                                })
                                .setNegativeButton(android.R.string.no, (dialog12, which) -> {})
                                .show();
                    }
                    inicioLeitura.setText(getString(R.string.inicio_leitura, Utils.dateToString(novaData)));
                    livro.setDataInicioLeitura(novaData);
                    livroBox.put(livro);
                } else {
                    if (livro.getEstadoLeitura() != Livro.ESTADO_FINALIZADO) {
                        new AlertDialog.Builder(DetalhesActivity.this)
                                .setTitle(R.string.registros)
                                .setMessage(R.string.aviso_atualizar_finalizado)
                                .setPositiveButton(android.R.string.yes, (dialog1, which) -> {
                                    livro.setEstadoLeitura(Livro.ESTADO_FINALIZADO);
                                    estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_FINALIZADO, DetalhesActivity.this));
                                    livroBox.put(livro);
                                })
                                .setNegativeButton(android.R.string.no, (dialog12, which) -> {})
                                .show();
                    }
                    terminoLeitura.setText(getString(R.string.termino_leitura, Utils.dateToString(novaData)));
                    livro.setDataTerminoLeitura(novaData);
                    livroBox.put(livro);
                }
            }, hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH));
            dialog.show();
            livroBox.put(livro);
        };
    }

    private View.OnClickListener estadoListener = v -> {
        int estadoLeitura = livro.getEstadoLeitura();
        int checkedItem = estadoLeitura == 0 ? -1 : livro.getEstadoLeitura() - 1;
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_lista)
                .setSingleChoiceItems(R.array.estado_leitura_array, checkedItem, (dialog, which) -> {
                    if (estadoLeitura == 0){
                        TextView tituloView = (TextView)titulo.getCurrentView();
                        TextView autoresView = (TextView)autores.getCurrentView();
                        livro.setTitulo(tituloView.getText().toString());
                        livro.setAutores(autoresView.getText().toString());
                        livro.setGoogleBooksId(volume.getId());
                    }
                    switch (which) {
                        case 0:
                            livro.setEstadoLeitura(Livro.ESTADO_LENDO);
                            livro.setDataInicioLeitura(new Date(System.currentTimeMillis()));
                            livro.setDataTerminoLeitura(null);
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_LENDO, DetalhesActivity.this));
                            break;
                        case 1:
                            livro.setEstadoLeitura(Livro.ESTADO_DESEJADO);
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_DESEJADO, DetalhesActivity.this));
                            break;
                        case 2:
                            livro.setEstadoLeitura(Livro.ESTADO_EM_ESPERA);
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_EM_ESPERA, DetalhesActivity.this));
                            break;
                        case 3:
                            livro.setEstadoLeitura(Livro.ESTADO_DESISTIDO);
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_DESISTIDO, DetalhesActivity.this));
                            break;
                        case 4:
                            livro.setEstadoLeitura(Livro.ESTADO_FINALIZADO);
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_FINALIZADO, DetalhesActivity.this));
                            livro.setDataTerminoLeitura(new Date(System.currentTimeMillis()));
                            break;
                    }
                    DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(which + 1, DetalhesActivity.this));
                    dialog.dismiss();
                    livroBox.put(livro);
                }).show();
    };

    private void configurarBox(long livroId) {
        livroBox = ((App) getApplication()).getBoxStore().boxFor(Livro.class);
        livro = livroBox.get(livroId);

        obterVolume(livro.getGoogleBooksId());
    }

    private void configurarBox(String volumeId) {
        if (Utils.isOnline(this)) {
            livroBox = ((App) getApplication()).getBoxStore().boxFor(Livro.class);
            obterVolume(volumeId);
        }
    }

    private void obterVolume(String volumeId) {
        if (volumeId != null && !volumeId.equals("")) {
            // Google Book id é válido
            if (!Utils.isOnline(this) && livro != null) {
                popularOffline();
            } else {
                new Thread(() -> {
                    try {
                        volume = Utils.obterVolume(JacksonFactory.getDefaultInstance(), volumeId);
                    } catch (Exception ignored) {}
                    runOnUiThread(this::popular);
                }).start();
            }
        } else {
            popularOffline();
        }
    }

    private void popularOffline() {
        titulo.setText(notNull(livro.getTitulo(), getString(R.string.carregando)));
        autores.setText(notNull(livro.getAutores(), getString(R.string.detalhes_erro_autor)));
        descricao.setText(R.string.detalhes_erro_descricao);
        categorias.setText(R.string.detalhes_erro_categoria);

        estadoLeitura.setText(estadoLeituraToString(livro.getEstadoLeitura(), DetalhesActivity.this));
        nota.setText(notaToString(livro.getNota()));
        tags.setText(notNull(livro.getTags(), ""));

        String inicioStr = Utils.dateToString(livro.getDataInicioLeitura());
        String fimStr = Utils.dateToString(livro.getDataTerminoLeitura());
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
            if (livro.isFavorito())
                favorito.setImageResource(R.drawable.ic_favorito_ativado);
            else
                favorito.setImageResource(R.drawable.ic_favorito);
            favoritado = livro.isFavorito();
        } else
            favorito.setVisibility(View.INVISIBLE);
    }

    private void popular() {
        if (livro == null) {
            livro = new Livro();
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
            int checkedItem = livro.getNota() == 0 ? -1 : livro.getNota() -1;
            String[] notas = getResources().getStringArray(R.array.notas_array);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.nota)
                    .setSingleChoiceItems(notas, checkedItem, (dialog, which) -> {
                        livro.setNota(which + 1);
                        livroBox.put(livro);
                        nota.setText(notaToString(which + 1));
                        dialog.dismiss();
                    }).show();
        });
        favorito.setOnClickListener(view -> {
            if (favoritado) {
                livro.setFavorito(false);
                favorito.setColorFilter(null);
                favorito.setImageResource(R.drawable.ic_favorito);
                favoritado = false;
            } else {
                livro.setFavorito(true);
                favorito.setColorFilter(null);
                favorito.setImageResource(R.drawable.ic_favorito_ativado);
                favoritado = true;
            }
            livroBox.put(livro);
        });
        favorito.setOnLongClickListener(view -> {
            Toast.makeText(DetalhesActivity.this, "Favoritar / Desfavoritar", Toast.LENGTH_SHORT).show();
            return true;
        });
        salvarTags.setOnClickListener(v -> {
            livro.setTags(tags.getText().toString());
            livroBox.put(livro);
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

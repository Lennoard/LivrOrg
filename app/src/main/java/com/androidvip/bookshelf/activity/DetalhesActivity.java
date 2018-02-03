package com.androidvip.bookshelf.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.model.Livro;
import com.androidvip.bookshelf.util.Utils;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.model.Volume;

import java.util.Date;

import io.objectbox.Box;

public class DetalhesActivity extends AppCompatActivity {
    private TextView titulo, autores, descricao, estadoLeitura, nota;
    EditText tags;
    ImageView capa, salvarTags;
    Livro livro;
    Volume volume;
    private Box<Livro> livroBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bindViews();

        Intent intent = getIntent();
        if (intent != null) {
            long livroId = intent.getLongExtra("livroId", 0);
            String volumeId = intent.getStringExtra("volumeId");

            if (volumeId != null && !volumeId.equals(""))
                configurarBox(volumeId);
            else if (livroId > 0)
                configurarBox(livroId);
            else
                Toast.makeText(this, R.string.detalhes_erro, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return true;
    }

    private View.OnClickListener estadoListener = v -> {
        int estadoLeitura = livro.getEstadoLeitura();
        int checkedItem = estadoLeitura == 0 ? -1 : livro.getEstadoLeitura() - 1;
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_lista)
                .setSingleChoiceItems(R.array.estado_leitura_array, checkedItem, (dialog, which) -> {
                    if (estadoLeitura == 0){
                        livro.setTitulo(titulo.getText().toString());
                        livro.setAutores(autores.getText().toString());
                        livro.setGoogleBooksId(volume.getId());
                    }
                    switch (which) {
                        case 0:
                            livro.setEstadoLeitura(Livro.ESTADO_LENDO);
                            livro.setDataInicioLeitura(new Date(System.currentTimeMillis()));
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_LENDO));
                            break;
                        case 1:
                            livro.setEstadoLeitura(Livro.ESTADO_DESEJADO);
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_DESEJADO));
                            break;
                        case 2:
                            livro.setEstadoLeitura(Livro.ESTADO_EM_ESPERA);
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_EM_ESPERA));
                            break;
                        case 3:
                            livro.setEstadoLeitura(Livro.ESTADO_DESISTIDO);
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_DESISTIDO));
                            break;
                        case 4:
                            livro.setEstadoLeitura(Livro.ESTADO_FINALIZADO);
                            DetalhesActivity.this.estadoLeitura.setText(estadoLeituraToString(Livro.ESTADO_FINALIZADO));
                            livro.setDataTerminoLeitura(new Date(System.currentTimeMillis()));
                            break;
                    }
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
        livroBox = ((App) getApplication()).getBoxStore().boxFor(Livro.class);
        livro = new Livro();
        obterVolume(volumeId);
        nota.setEnabled(false);
    }

    private void obterVolume(String volumeId) {
        new Thread(() -> {
            try {
                volume = Utils.obterVolume(JacksonFactory.getDefaultInstance(), volumeId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(this::popular);
        }).start();
    }

    private void popular() {
        if (volume != null) {
            Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
            if (volumeInfo != null) {
                titulo.setText(lidarComNulo(volumeInfo.getTitle()));
                if (volumeInfo.getAuthors() != null)
                    autores.setText(TextUtils.join(", ", volumeInfo.getAuthors()));

                String desc = lidarComNulo(volumeInfo.getDescription()).equals("")
                        ? "Não foi possível encontrar uma descrição para o livro"
                        : lidarComNulo(volumeInfo.getDescription());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    descricao.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    descricao.setText(Html.fromHtml(desc));
                }

                if (volumeInfo.getImageLinks() != null)
                    Utils.carregarImagem(this, volumeInfo.getImageLinks().getThumbnail(), capa);
            }
        }
        if (livro != null) {
            estadoLeitura.setText(estadoLeituraToString(livro.getEstadoLeitura()));
            nota.setText(notaToString(livro.getNota()));
            tags.setText(lidarComNulo(livro.getTags()));
        } else {
            estadoLeitura.setText(R.string.add_lista);
            nota.setText(notaToString(0));
        }

        estadoLeitura.setOnClickListener(estadoListener);
        nota.setOnClickListener(v -> {
            int checkedItem = livro.getNota() == 0 ? -1 : livro.getNota();
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
    }

    private String lidarComNulo(@Nullable String stringNulavel) {
        return stringNulavel == null ? "" : stringNulavel;
    }

    private String estadoLeituraToString(int estadoLeitura) {
        String ret = "";
        switch (estadoLeitura) {
            case Livro.ESTADO_LENDO:
                ret = getString(R.string.estado_leitura_lendo);
                break;
            case Livro.ESTADO_DESEJADO:
                ret = getString(R.string.estado_leitura_desejo);
                break;
            case Livro.ESTADO_EM_ESPERA:
                ret = getString(R.string.estado_leitura_em_espera);
                break;
            case Livro.ESTADO_DESISTIDO:
                ret = getString(R.string.estado_leitura_desistido);
                break;
            case Livro.ESTADO_FINALIZADO:
                ret = getString(R.string.estado_leitura_finalizado);
                break;
            default:
                ret = getString(R.string.add_lista);
                break;
        }
        return ret;
    }

    private String notaToString(int nota) {
        return nota == 0 ? getString(R.string.sem_nota) : getString(R.string.nota_format, nota);
    }

    private void bindViews() {
        titulo = findViewById(R.id.detalhes_titulo);
        autores = findViewById(R.id.detalhes_autores);
        descricao = findViewById(R.id.detalhes_descricao);
        estadoLeitura = findViewById(R.id.detalhes_estado);
        nota = findViewById(R.id.detalhes_nota);
        tags = findViewById(R.id.detalhes_tag_edit);
        salvarTags = findViewById(R.id.detalhes_tag_botao);
        capa = findViewById(R.id.detalhes_capa);
    }

}

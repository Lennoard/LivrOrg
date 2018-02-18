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
import com.androidvip.bookshelf.model.Livro;

import java.util.Date;

import io.objectbox.Box;

import static com.androidvip.bookshelf.util.Utils.estadoLeituraToString;

public class AddManualActivity extends AppCompatActivity {
    private Livro livro;
    private Box<Livro> livroBox;
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

        livroBox = ((App) getApplication()).getBoxStore().boxFor(Livro.class);

        livro = new Livro();
        livro.setEstadoLeitura(Livro.ESTADO_LENDO);

        estado = findViewById(R.id.add_manual_estado_leitura);
        nota = findViewById(R.id.add_manual_nota);

        FloatingActionButton fab = findViewById(R.id.add_manual_fab);
        fab.setOnClickListener(view -> {
            EditText titulo = findViewById(R.id.add_manual_titulo);
            EditText autor = findViewById(R.id.add_manual_autor);
            if (titulo.getText().toString().equals(""))
                Snackbar.make(view, R.string.erro_text_fields, Snackbar.LENGTH_LONG).show();
            else {
                livro.setTitulo(titulo.getText().toString().trim());
                livro.setAutores(autor.getText().toString().trim());
                livroBox.put(livro);
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
        int estadoLeitura = livro.getEstadoLeitura();
        int checkedItem = estadoLeitura == 0 ? -1 : livro.getEstadoLeitura() - 1;

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_lista)
                .setSingleChoiceItems(R.array.estado_leitura_array, checkedItem, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            livro.setEstadoLeitura(Livro.ESTADO_LENDO);
                            livro.setDataInicioLeitura(new Date(System.currentTimeMillis()));
                            livro.setDataTerminoLeitura(null);
                            estado.setText(estadoLeituraToString(Livro.ESTADO_LENDO, AddManualActivity.this));
                            break;
                        case 1:
                            livro.setEstadoLeitura(Livro.ESTADO_DESEJADO);
                            estado.setText(estadoLeituraToString(Livro.ESTADO_DESEJADO, AddManualActivity.this));
                            break;
                        case 2:
                            livro.setEstadoLeitura(Livro.ESTADO_EM_ESPERA);
                            estado.setText(estadoLeituraToString(Livro.ESTADO_EM_ESPERA, AddManualActivity.this));
                            break;
                        case 3:
                            livro.setEstadoLeitura(Livro.ESTADO_DESISTIDO);
                            estado.setText(estadoLeituraToString(Livro.ESTADO_DESISTIDO, AddManualActivity.this));
                            break;
                        case 4:
                            livro.setEstadoLeitura(Livro.ESTADO_FINALIZADO);
                            estado.setText(estadoLeituraToString(Livro.ESTADO_FINALIZADO, AddManualActivity.this));
                            livro.setDataTerminoLeitura(new Date(System.currentTimeMillis()));
                            break;
                    }
                    estado.setText(estadoLeituraToString(which + 1, AddManualActivity.this));
                    dialog.dismiss();
                    livroBox.put(livro);
                }).show();
    }

    public void nota(View view) {
        String[] notas = getResources().getStringArray(R.array.notas_array);
        new AlertDialog.Builder(this)
                .setTitle(R.string.nota)
                .setSingleChoiceItems(notas, 0, (dialog, which) -> {
                    livro.setNota(which + 1);
                    livroBox.put(livro);
                    nota.setText(String.valueOf(which + 1));
                    dialog.dismiss();
                }).show();
    }
}

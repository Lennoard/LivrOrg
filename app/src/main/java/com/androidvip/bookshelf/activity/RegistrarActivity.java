package com.androidvip.bookshelf.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.androidvip.bookshelf.R;

public class RegistrarActivity extends AppCompatActivity {
    FloatingActionButton fab;
    TextInputEditText usuario, senha, confirmarSenha;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = findViewById(R.id.fab_cadastrar);
        usuario = findViewById(R.id.cadastrar_usuario);
        senha = findViewById(R.id.cadastrar_senha);
        confirmarSenha = findViewById(R.id.cadastrar_confirmar_senha);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        fab.hide();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                validarFormulario(false);
                handler.postDelayed(this, 200);
            }
        }, 200);

        fab.setOnClickListener(v -> {
            if (validarFormulario(true)) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("usuario", getText(usuario));
                editor.putString("senha", getText(senha));
                editor.apply();

                Toast.makeText(this, getText(usuario) + " foi cadastrado", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText().toString().trim();
    }

    private boolean validarFormulario(boolean mostrarErros){
        boolean valido = true;
        if (getText(usuario).equals("")) {
            valido = false;
            if (mostrarErros)
                usuario.setError(getString(R.string.login_erro_usuario));
        }
        if (getText(senha).equals("")) {
            valido = false;
            if (mostrarErros)
                senha.setError(getString(R.string.login_erro_senha));
            else
                senha.setError(null);
        }
        if (!getText(senha).equals(getText(confirmarSenha))) {
            valido = false;
            if (mostrarErros)
                senha.setError(getString(R.string.cadastrar_erro_senha));
        }
        if (valido)
            fab.show();
        else
            fab.hide();

        return valido;
    }
}

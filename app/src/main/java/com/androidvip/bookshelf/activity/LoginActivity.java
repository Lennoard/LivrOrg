package com.androidvip.bookshelf.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.androidvip.bookshelf.R;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText usuario, senha;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usuario = findViewById(R.id.login_usuario);
        senha = findViewById(R.id.login_senha);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (sp.getBoolean("logado", false))
            startActivity(new Intent(this, MainActivity.class));
    }

    public void entrar(View view) {
        if (validarFormulario()) {
            sp.edit().putBoolean("logado", true).apply();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    public void registrar(View view) {
        startActivity(new Intent(this, RegistrarActivity.class));
    }

    private String getText(TextInputEditText editText) {
        return editText.getText().toString().trim();
    }

    private boolean validarFormulario(){
        boolean valido = true;
        if (getText(usuario).equals("")) {
            valido = false;
            usuario.setError(getString(R.string.login_erro_usuario));
        }
        if (getText(senha).equals("")) {
            valido = false;
            senha.setError(getString(R.string.login_erro_senha));
        }

        String usuarioEstatico = sp.getString("usuario", "");
        String senhaEstatica = sp.getString("senha", "");
        if (getText(usuario).equals(usuarioEstatico) && getText(senha).equals(senhaEstatica)){
            valido = true;
        } else {
            valido = false;
            Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show();
        }

        return valido;
    }
}

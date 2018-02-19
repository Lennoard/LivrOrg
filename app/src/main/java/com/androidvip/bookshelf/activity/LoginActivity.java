package com.androidvip.bookshelf.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#A1887F"));
        }

        if (sp.getBoolean("logado", false))
            logIn();
    }

    public void logInBotao(View view) {
        if (getText(usuario).length() > 3 && getText(senha).length() > 3 && validarFormulario())
            logIn();
        else
            Toast.makeText(this, "Insira um usuário válido", Toast.LENGTH_SHORT).show();
    }

    public void registrar(View view) {
        startActivity(new Intent(this, RegistrarActivity.class));
    }

    private void logIn() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.logging_in));
        pd.show();
        sp.edit().putBoolean("logado", true).apply();
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            runOnUiThread(() -> {
                pd.dismiss();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            });
        }).start();
    }

    private String getText(TextInputEditText editText) {
        return editText.getText().toString().trim();
    }

    private boolean validarFormulario() {
        boolean valido;
        String usuarioEstatico = sp.getString("usuario", "");
        String senhaEstatica = sp.getString("senha", "");

        if (getText(usuario).equals(usuarioEstatico) && getText(senha).equals(senhaEstatica))
            valido = true;
        else {
            valido = false;
            if (getText(usuario).equals(""))
                usuario.setError(getString(R.string.login_erro_usuario));
            if (getText(senha).equals(""))
                senha.setError(getString(R.string.login_erro_senha));
            Toast.makeText(this, R.string.usuario_invalido, Toast.LENGTH_SHORT).show();
        }

        return valido;
    }
}

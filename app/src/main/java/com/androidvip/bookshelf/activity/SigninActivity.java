package com.androidvip.bookshelf.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.util.K;

public class SigninActivity extends AppCompatActivity {
    FloatingActionButton fab;
    TextInputEditText user, password, confirmPass;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = findViewById(R.id.fab_cadastrar);
        user = findViewById(R.id.cadastrar_usuario);
        password = findViewById(R.id.cadastrar_senha);
        confirmPass = findViewById(R.id.cadastrar_confirmar_senha);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        fab.hide();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                validForm(false);
                handler.postDelayed(this, 200);
            }
        }, 200);

        fab.setOnClickListener(v -> {
            if (validForm(true)) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(K.PREF.USERNAME, getText(user));
                editor.putString(K.PREF.PASSWORD, getText(password));
                editor.apply();

                Toast.makeText(this, getText(user) + " registered", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText().toString().trim();
    }

    private boolean validForm(boolean showErrors){
        boolean valid = true;
        if (getText(user).equals("")) {
            valid = false;
            if (showErrors)
                user.setError(getString(R.string.login_error_user));
        }
        if (getText(password).equals("")) {
            valid = false;
            if (showErrors)
                password.setError(getString(R.string.login_error_password));
            else
                password.setError(null);
        }
        if (!getText(password).equals(getText(confirmPass))) {
            valid = false;
            if (showErrors)
                password.setError(getString(R.string.sign_in_error_password));
        }
        if (valid)
            fab.show();
        else
            fab.hide();

        return valid;
    }
}

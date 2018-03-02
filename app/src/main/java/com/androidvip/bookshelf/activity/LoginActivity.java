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
import com.androidvip.bookshelf.util.K;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText user, password;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user = findViewById(R.id.login_username);
        password = findViewById(R.id.login_password);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // Tint status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#A1887F"));
        }

        // Automatically log in
        if (sp.getBoolean(K.PREF.LOGGED_IN, false))
            logIn();
    }

    public void logInButtonClick(View view) {
        if (getText(user).length() > 0 && getText(password).length() > 0 && validForm())
            logIn();
        else
            Toast.makeText(this, R.string.login_error_invalid_user, Toast.LENGTH_SHORT).show();
    }

    public void registrar(View view) {
        startActivity(new Intent(this, SigninActivity.class));
    }

    private void logIn() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.logging_in));
        pd.show();
        sp.edit().putBoolean(K.PREF.LOGGED_IN, true).apply();
        new Thread(() -> {
            try {
                // Simulation of a fake heavy task
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

    private boolean validForm() {
        boolean valid;
        String staticUser = sp.getString(K.PREF.USERNAME, "");
        String staticPassword = sp.getString(K.PREF.PASSWORD, "");

        if (getText(user).equals(staticUser) && getText(password).equals(staticPassword))
            valid = true;
        else {
            valid = false;
            if (getText(user).equals(""))
                user.setError(getString(R.string.login_error_user));
            if (getText(password).equals(""))
                password.setError(getString(R.string.login_error_password));
            Toast.makeText(this, R.string.invalid_user, Toast.LENGTH_SHORT).show();
        }
        return valid;
    }
}

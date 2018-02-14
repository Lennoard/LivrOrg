package com.androidvip.bookshelf.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidvip.bookshelf.BuildConfig;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.util.Utils;

public class SobreActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() !=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        TextView versao = findViewById(R.id.sobre_versao);
        versao.setText("v" + BuildConfig.VERSION_NAME);
    }

    public void xda(View view) {
        Utils.webPage(this, "https://forum.xda-developers.com/member.php?u=6652564");
    }

    public void gitHub(View view) {
        Utils.webPage(this, "https://github.com/Lennoard");
    }

    public void email(View view) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"lennoardrai@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        try {
            startActivity(Intent.createChooser(i, "Enviar email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Falha ao enviar email", Toast.LENGTH_SHORT).show();
        }
    }
}

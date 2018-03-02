package com.androidvip.bookshelf.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidvip.bookshelf.BuildConfig;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.util.Utils;

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView version = findViewById(R.id.about_version);
        version.setText("v" + BuildConfig.VERSION_NAME);
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
            startActivity(Intent.createChooser(i, getString(R.string.send_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.send_email_failure, Toast.LENGTH_SHORT).show();
        }
    }

    public void lic(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_web, null);

        builder.setCancelable(true);
        builder.setTitle(R.string.open_source_licenses);
        builder.setView(dialogView);

        WebView webView = dialogView.findViewById(R.id.webview_dialog);
        webView.loadUrl("file:///android_res/raw/lic.html");
        builder.show();
    }
}

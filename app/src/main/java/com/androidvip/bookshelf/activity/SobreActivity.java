package com.androidvip.bookshelf.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class SobreActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
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

    public void lic(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_web);
        dialog.setCancelable(true);

        final TextView txt = dialog.findViewById(R.id.dialog_text);
        final WebView webView = dialog.findViewById(R.id.webview_dialog);
        final ProgressBar pb = dialog.findViewById(R.id.pb_web_dialog);

        txt.setText(R.string.licencas_open_source);

        final SwipeRefreshLayout swipeLayout = dialog.findViewById(R.id.swipeToRefresh);
        swipeLayout.setColorSchemeResources(R.color.colorAccent);
        swipeLayout.setOnRefreshListener(webView::reload);
        webView.loadUrl("file:///android_res/raw/lic.html");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                swipeLayout.setRefreshing(false);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                pb.setProgress(progress);
                if (progress == 100) {
                    pb.setVisibility(View.GONE);
                    swipeLayout.setRefreshing(false);
                } else
                    pb.setVisibility(View.VISIBLE);
            }

        });
        dialog.show();
    }
}

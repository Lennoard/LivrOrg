package com.androidvip.bookshelf.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.adapter.VolumeAdapter;
import com.androidvip.bookshelf.util.Utils;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.model.Volume;

import java.util.ArrayList;
import java.util.List;

public class Pesquisar extends AppCompatActivity {
    RecyclerView.Adapter mAdapter;
    private RecyclerView rv;
    private List<Volume> volumesLista = new ArrayList<>();
    private JsonFactory jsonFactory;
    private SwipeRefreshLayout swipeLayout;
    private String prefixo = "intitle:";
    private String queryAtual = "";
    private int checkedItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesquisar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeLayout = findViewById(R.id.swipe_rv_pesquisar);
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeLayout.setOnRefreshListener(this::atualizarLista);
        jsonFactory = JacksonFactory.getDefaultInstance();

        atualizarLista();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pesquisar_por) {
            String[] array = getResources().getStringArray(R.array.pesquisar_por_array);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.pesquisar_por)
                    .setSingleChoiceItems(array, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: prefixo = "intitle:";  break;
                                case 1: prefixo = "inauthor:"; break;
                                case 2: prefixo = "isbn:";     break;
                            }
                            set(dialog, which);
                        }

                        private void set(DialogInterface dialog, int checkedItem) {
                            Pesquisar.this.checkedItem = checkedItem;
                            atualizarLista();
                            dialog.dismiss();
                        }
                    }).show();
        }
        return true;
    }

    private void atualizarLista() {
        if (!queryAtual.equals("")) {
            swipeLayout.setRefreshing(true);
            new Thread(() -> {
                try {
                    volumesLista = Utils.pesquisarLivros(jsonFactory, prefixo + queryAtual);
                } catch (Exception ignored) {

                }
                runOnUiThread(() -> {
                    swipeLayout.setRefreshing(false);
                    configurarRecyclerView();
                });
            }).start();
        }
    }

    private void configurarRecyclerView() {
        if (rv != null) {
            mAdapter = new VolumeAdapter(this, volumesLista);
            rv.setAdapter(mAdapter);
        } else {
            rv = findViewById(R.id.rv_pesquisar);
            mAdapter = new VolumeAdapter(this, volumesLista);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(mLayoutManager);
            rv.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pesquisar, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_pesquisar).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryAtual = query;
                atualizarLista();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

}

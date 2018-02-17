package com.androidvip.bookshelf.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.adapter.LivroAdapter;
import com.androidvip.bookshelf.model.Livro;
import com.androidvip.bookshelf.model.Livro_;
import com.androidvip.bookshelf.util.K;
import com.androidvip.bookshelf.util.Utils;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.List;

import io.objectbox.Box;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    RecyclerView rv;
    RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout swipeLayout;
    private Box<Livro> livroBox;
    private int idNavAtual;
    private Snackbar snackNet;
    private SharedPreferences sp;
    private ActionBar actionBar;

    private BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
                if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    onStart();
                    snackNet.dismiss();
                } else
                    snackNet.show();
            }
        }
    };

    // TODO: 13/02/18 add manual
    // TODO: 13/02/18 localização
    // TODO: 13/02/18 licences
    // TODO: 14/02/2018 tags

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.app_name) + ": " + getString(R.string.estado_leitura_lendo));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PesquisarActivity.class);
            intent.putExtra("add", true);
            startActivity(intent);
        });

        idNavAtual = R.id.nav_lendo;

        registerReceiver(netReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        configurarDrawer(toolbar);

        swipeLayout = findViewById(R.id.swipe_rv_main);
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeLayout.setOnRefreshListener(this::onStart);

        showTapTarget(toolbar, fab);

        snackNet = Snackbar.make(findViewById(R.id.cl), R.string.erro_sem_conexao, Snackbar.LENGTH_INDEFINITE);
        if (!Utils.isOnline(this))
            snackNet.show();
    }

    @Override
    protected void onStart() {
        livroBox = ((App) getApplication()).getBoxStore().boxFor(Livro.class);
        swipeLayout.setRefreshing(true);
        trocarNavItems(idNavAtual);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(netReceiver);
        } catch (Exception ignored){}
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_sobre:
                startActivity(new Intent(this, SobreActivity.class));
                break;
            case R.id.action_log_out:
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit().putBoolean("logado", false).apply();
                i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        trocarNavItems(item.getItemId());
        return true;
    }

    private void showTapTarget(Toolbar toolbar, FloatingActionButton fab) {
        if (!sp.getBoolean(K.PREF.TAP_TARGET_MAIN, false)) {
            new TapTargetSequence(this)
                    .continueOnCancel(true)
                    .targets(
                            TapTarget.forToolbarNavigationIcon(toolbar, "Menu", "Toque aqui para abrir o menu com seu catálogo organizado").id(1),
                            TapTarget.forView(fab, "Adicionar", "Toque aqui para procurar e adicionar um livro")
                                    .tintTarget(false)
                                    .id(2)
                    ).listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            sp.edit().putBoolean(K.PREF.TAP_TARGET_MAIN, true).apply();
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {

                        }
            }).start();
        }
    }

    private void trocarNavItems(int itemId) {
        switch (itemId) {
            case R.id.nav_lendo:
                idNavAtual = R.id.nav_lendo;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_LENDO));
                actionBar.setTitle(R.string.estado_leitura_lendo);
                break;
            case R.id.nav_lista_desejos:
                idNavAtual = R.id.nav_lista_desejos;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_DESEJADO));
                actionBar.setTitle(R.string.estado_leitura_desejo);
                break;
            case R.id.nav_em_espera:
                idNavAtual = R.id.nav_em_espera;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_EM_ESPERA));
                actionBar.setTitle(R.string.estado_leitura_em_espera);
                break;
            case R.id.nav_desistencias:
                idNavAtual = R.id.nav_desistencias;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_DESISTIDO));
                actionBar.setTitle(R.string.desistencias);
                break;
            case R.id.nav_finalizados:
                idNavAtual = R.id.nav_finalizados;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_FINALIZADO));
                actionBar.setTitle(R.string.finalizados);
                break;
            case R.id.nav_favoritos:
                idNavAtual = R.id.nav_favoritos;
                configurarRecyclerView(filtrarLivroFavoritos());
                actionBar.setTitle(R.string.favoritos);
                break;
            case R.id.nav_pesquisar:
                startActivity(new Intent(this, PesquisarActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    private List<Livro> filtrarLivroPorEstadoLeitura(int estadoLeitura){
       return livroBox.query().equal(Livro_.estadoLeitura, estadoLeitura).build().find();
    }

    private List<Livro> filtrarLivroFavoritos(){
        return livroBox.query().equal(Livro_.favorito, true).build().find();
    }

    private void configurarDrawer(Toolbar toolbar) {
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_lendo);
    }

    private void configurarRecyclerView(List<Livro> lista) {
        if (rv != null) {
            mAdapter = new LivroAdapter(this, lista, false);
            rv.setAdapter(mAdapter);
        } else {
            rv = findViewById(R.id.rv_main);
            mAdapter = new LivroAdapter(this, lista, false);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(mLayoutManager);
            rv.setAdapter(mAdapter);
        }
        swipeLayout.setRefreshing(false);
    }

}

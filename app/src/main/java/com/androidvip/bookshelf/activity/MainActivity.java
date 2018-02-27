package com.androidvip.bookshelf.activity;

import android.app.SearchManager;
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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.adapter.LivroAdapter;
import com.androidvip.bookshelf.model.Book;
import com.androidvip.bookshelf.model.Livro_;
import com.androidvip.bookshelf.util.K;
import com.androidvip.bookshelf.util.Utils;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    RecyclerView rv;
    RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout swipeLayout;
    private Box<Book> bookBox;
    private List<Book> currentList;
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

    // TODO: 13/02/18 localização
    // TODO: 20/02/18 icon filtrar por
    // TODO: 20/02/2018 add ic_me git

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
        bookBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);
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

        MenuItem itemPesquisar = menu.findItem(R.id.action_filtrar);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) itemPesquisar.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        itemPesquisar.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        configurarRecyclerView(filtrarPorMatch(query));
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.length() > 3) {
                            configurarRecyclerView(filtrarPorMatch(newText));
                            return true;
                        }
                        return false;
                    }
                });
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                configurarRecyclerView(currentList);
                return true;
            }
        });
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
                            TapTarget.forToolbarNavigationIcon(toolbar, getString(R.string.menu), getString(R.string.tap_target_menu)).id(1),
                            TapTarget.forView(fab, getString(R.string.add), getString(R.string.tap_target_add))
                                    .tintTarget(false)
                                    .cancelable(false)
                                    .id(2)
                    ).listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            sp.edit().putBoolean(K.PREF.TAP_TARGET_MAIN, true).apply();
                            Intent intent = new Intent(MainActivity.this, PesquisarActivity.class);
                            intent.putExtra("add", true);
                            startActivity(intent);
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
                configurarRecyclerView(filtrarPorEstadoLeitura(Book.STATE_READING));
                actionBar.setTitle(R.string.estado_leitura_lendo);
                break;
            case R.id.nav_lista_desejos:
                idNavAtual = R.id.nav_lista_desejos;
                configurarRecyclerView(filtrarPorEstadoLeitura(Book.STATE_WISH));
                actionBar.setTitle(R.string.estado_leitura_desejo);
                break;
            case R.id.nav_em_espera:
                idNavAtual = R.id.nav_em_espera;
                configurarRecyclerView(filtrarPorEstadoLeitura(Book.STATE_ON_HOLD));
                actionBar.setTitle(R.string.estado_leitura_em_espera);
                break;
            case R.id.nav_desistencias:
                idNavAtual = R.id.nav_desistencias;
                configurarRecyclerView(filtrarPorEstadoLeitura(Book.STATE_DROPPED));
                actionBar.setTitle(R.string.desistencias);
                break;
            case R.id.nav_finalizados:
                idNavAtual = R.id.nav_finalizados;
                configurarRecyclerView(filtrarPorEstadoLeitura(Book.STATE_FINISHED));
                actionBar.setTitle(R.string.finalizados);
                break;
            case R.id.nav_favoritos:
                idNavAtual = R.id.nav_favoritos;
                configurarRecyclerView(filtrarPorFavorito());
                actionBar.setTitle(R.string.favoritos);
                break;
            case R.id.nav_pesquisar:
                startActivity(new Intent(this, PesquisarActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    private List<Book> filtrarPorEstadoLeitura(int estadoLeitura){
        List<Book> l = bookBox.query().equal(Livro_.estadoLeitura, estadoLeitura).build().find();
        currentList = l;
        return l;
    }

    private List<Book> filtrarPorFavorito(){
        List<Book> l = bookBox.query().equal(Livro_.favorito, true).build().find();
        currentList = l;
        return l;
    }

    private List<Book> filtrarPorMatch(String match) {
        List<Book> l = new ArrayList<>();
        for (Book book : currentList)
            if (book.getTitle().toLowerCase().contains(match.toLowerCase()) ||
                    Utils.notNull(book.getTags().toLowerCase(), "").contains(match.toLowerCase()) ||
                    Utils.notNull(book.getAuthors().toLowerCase(), "").contains(match.toLowerCase()))
                l.add(book);
        return l;
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

    private void configurarRecyclerView(List<Book> lista) {
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

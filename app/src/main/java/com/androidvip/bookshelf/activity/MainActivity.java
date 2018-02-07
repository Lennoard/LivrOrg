package com.androidvip.bookshelf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.adapter.LivroAdapter;
import com.androidvip.bookshelf.model.Livro;
import com.androidvip.bookshelf.model.Livro_;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    RecyclerView rv;
    RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout swipeLayout;
    private Box<Livro> livroBox;
    private int idNavAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name) + ": " + getString(R.string.estado_leitura_lendo));

        idNavAtual = R.id.nav_lendo;

        configurarDrawer(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PesquisarActivity.class);
            intent.putExtra("add", true);
            startActivity(intent);
        });

        swipeLayout = findViewById(R.id.swipe_rv_main);
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeLayout.setOnRefreshListener(this::onStart);
    }

    @Override
    protected void onStart() {
        livroBox = ((App) getApplication()).getBoxStore().boxFor(Livro.class);
        swipeLayout.setRefreshing(true);
        trocarNavItems(idNavAtual);
        super.onStart();
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

    private void trocarNavItems(int itemId) {
        switch (itemId) {
            case R.id.nav_lendo:
                idNavAtual = R.id.nav_lendo;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_LENDO));
                getSupportActionBar().setTitle(R.string.estado_leitura_lendo);
                break;
            case R.id.nav_lista_desejos:
                idNavAtual = R.id.nav_lista_desejos;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_DESEJADO));
                getSupportActionBar().setTitle(R.string.estado_leitura_desejo);
                break;
            case R.id.nav_em_espera:
                idNavAtual = R.id.nav_em_espera;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_EM_ESPERA));
                getSupportActionBar().setTitle(R.string.estado_leitura_em_espera);
                break;
            case R.id.nav_desistencias:
                idNavAtual = R.id.nav_desistencias;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_DESISTIDO));
                getSupportActionBar().setTitle(R.string.desistencias);
                break;
            case R.id.nav_finalizados:
                idNavAtual = R.id.nav_finalizados;
                configurarRecyclerView(filtrarLivroPorEstadoLeitura(Livro.ESTADO_FINALIZADO));
                getSupportActionBar().setTitle(R.string.finalizados);
                break;
            case R.id.nav_favoritos:
                idNavAtual = R.id.nav_favoritos;
                configurarRecyclerView(filtrarLivroFavoritos());
                getSupportActionBar().setTitle(R.string.favoritos);
                break;
            case R.id.nav_pesquisar:
                startActivity(new Intent(this, PesquisarActivity.class));
                break;
        }
        if (drawer.isDrawerOpen(drawer))
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

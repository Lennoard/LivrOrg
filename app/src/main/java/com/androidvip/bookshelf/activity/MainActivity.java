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
import com.androidvip.bookshelf.model.Comentario;
import com.androidvip.bookshelf.model.Livro;

import io.objectbox.Box;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    RecyclerView rv;
    RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout swipeLayout;
    private Box<Livro> livroBox;
    private Box<Comentario> comentarioBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        swipeLayout.setRefreshing(true);
        livroBox = ((App) getApplication()).getBoxStore().boxFor(Livro.class);
        comentarioBox = ((App) getApplication()).getBoxStore().boxFor(Comentario.class);
        configurarRecyclerView();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_pesquisar:
                startActivity(new Intent(this, PesquisarActivity.class));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void configurarDrawer(Toolbar toolbar) {
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void configurarRecyclerView() {
        if (rv != null) {
            mAdapter = new LivroAdapter(this, livroBox, false);
            rv.setAdapter(mAdapter);
        } else {
            rv = findViewById(R.id.rv_main);
            mAdapter = new LivroAdapter(this, livroBox, false);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(mLayoutManager);
            rv.setAdapter(mAdapter);
        }
        swipeLayout.setRefreshing(false);
    }

}

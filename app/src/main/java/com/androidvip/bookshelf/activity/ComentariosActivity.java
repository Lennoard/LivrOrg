package com.androidvip.bookshelf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidvip.bookshelf.App;
import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.adapter.ComentarioAdapter;
import com.androidvip.bookshelf.model.Comentario;
import com.androidvip.bookshelf.model.Comentario_;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

public class ComentariosActivity extends AppCompatActivity {
    RecyclerView rv;
    RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout swipeLayout;
    private List<Comentario> lista;
    private long livroId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null) {
            livroId = intent.getLongExtra("livroId", 0);
            if (livroId <= 0) {
                Toast.makeText(this, R.string.comentarios_erro, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.comentarios_erro, Toast.LENGTH_LONG).show();
            finish();
        }

        lista = new ArrayList<>();

        swipeLayout = findViewById(R.id.swipe_rv_comentarios);
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeLayout.setOnRefreshListener(this::onStart);

        FloatingActionButton fab = findViewById(R.id.fab_comentarios);
        fab.setOnClickListener(view -> {
            Intent i = new Intent(ComentariosActivity.this, ComentariosDetalhesActivity.class);
            i.putExtra("livroId", livroId);
            startActivity(i);
        });

        rv = findViewById(R.id.rv_comentarios);
        mAdapter = new ComentarioAdapter(this, lista);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        Box<Comentario> comentarioBox = ((App) getApplication()).getBoxStore().boxFor(Comentario.class);
        swipeLayout.setRefreshing(true);

        lista = comentarioBox.query().equal(Comentario_.livroId, livroId).build().find();

        configurarRecyclerView(lista);
        swipeLayout.setRefreshing(false);
        super.onStart();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return true;
    }

    private void configurarRecyclerView(List<Comentario> lista) {
        if (rv != null) {
            mAdapter = new ComentarioAdapter(this, lista);
            rv.setAdapter(mAdapter);
        } else {
            rv = findViewById(R.id.rv_comentarios);
            mAdapter = new ComentarioAdapter(this, lista);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            rv.setHasFixedSize(true);
            rv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
            rv.setLayoutManager(mLayoutManager);
            rv.setAdapter(mAdapter);
        }
        swipeLayout.setRefreshing(false);
    }

}

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
import com.androidvip.bookshelf.adapter.CommentAdapter;
import com.androidvip.bookshelf.model.Comment;
import com.androidvip.bookshelf.model.Comment_;
import com.androidvip.bookshelf.util.K;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

public class CommentActivity extends AppCompatActivity {
    RecyclerView rv;
    RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout swipeLayout;
    private List<Comment> commentList;
    private long bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null) {
            bookId = intent.getLongExtra(K.EXTRA_BOOK_ID_DB, 0);
            if (bookId <= 0) {
                // This activity shows goToComments of a book. We could not receive
                // a valid book id, therefore there is nothing else to do here
                Toast.makeText(this, R.string.comments_error, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.comments_error, Toast.LENGTH_LONG).show();
            finish();
        }

        commentList = new ArrayList<>();

        swipeLayout = findViewById(R.id.swipe_rv_comments);
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeLayout.setOnRefreshListener(this::onStart);

        FloatingActionButton fab = findViewById(R.id.fab_comments);
        fab.setOnClickListener(view -> {
            Intent i = new Intent(CommentActivity.this, CommentDetailsActivity.class);
            i.putExtra(K.EXTRA_BOOK_ID_DB, bookId);
            startActivity(i);
        });
        
    }

    @Override
    protected void onStart() {
        Box<Comment> commentBox = ((App) getApplication()).getBoxStore().boxFor(Comment.class);
        commentList = commentBox.query().equal(Comment_.bookId, bookId).build().find();
        setUpRecyclerView(commentList);
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

    private void setUpRecyclerView(List<Comment> commentList) {
        swipeLayout.setRefreshing(true);
        if (rv != null) {
            mAdapter = new CommentAdapter(this, commentList);
            rv.setAdapter(mAdapter);
        } else {
            rv = findViewById(R.id.rv_comments);
            mAdapter = new CommentAdapter(this, commentList);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            rv.setHasFixedSize(true);
            rv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
            rv.setLayoutManager(mLayoutManager);
            rv.setAdapter(mAdapter);
        }
        swipeLayout.setRefreshing(false);
    }
}

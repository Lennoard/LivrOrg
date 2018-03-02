package com.androidvip.bookshelf.activity;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.androidvip.bookshelf.R;
import com.androidvip.bookshelf.adapter.VolumeAdapter;
import com.androidvip.bookshelf.util.K;
import com.androidvip.bookshelf.util.Utils;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.api.services.books.model.Volume;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    RecyclerView.Adapter mAdapter;
    private RecyclerView rv;
    private List<Volume> volumeList = new ArrayList<>();
    private SwipeRefreshLayout swipeLayout;
    private String queryPrefix = "intitle:";
    private String currentQuery = "";
    private int checkedItem = 0;
    private boolean isAddActivity = false;
    private Snackbar snackNet;
    private SharedPreferences prefs;

    private BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ImageView offline = findViewById(R.id.details_img_offline);
            RecyclerView recyclerView = findViewById(R.id.rv_search);
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
                if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    snackNet.dismiss();
                    offline.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    snackNet.show();
                    offline.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        if (intent != null)
            isAddActivity = intent.getBooleanExtra(K.EXTRA_IS_ADD_ACTIVITY, false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setUpToolBar(toolbar);

        swipeLayout = findViewById(R.id.swipe_rv_search);
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeLayout.setOnRefreshListener(() -> refreshList(true));

        refreshList(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        snackNet = Snackbar.make(findViewById(R.id.cl), R.string.error_no_connection, Snackbar.LENGTH_INDEFINITE);
        if (!Utils.isOnline(this))
            snackNet.show();

        registerReceiver(netReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(netReceiver);
        } catch (Exception ignored){}
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isAddActivity)
            getMenuInflater().inflate(R.menu.book_add, menu);
        else
            getMenuInflater().inflate(R.menu.book_search, menu);

        setUpSearch(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setUpMenuItemAction(item);
        return true;
    }

    private void setUpToolBar(Toolbar toolbar) {
        if (!prefs.getBoolean(K.PREF.TAP_TARGET_SEARCH, false)) {
            toolbar.inflateMenu(isAddActivity ? R.menu.book_add : R.menu.book_search);
            toolbar.setTitle(isAddActivity ? R.string.add : R.string.search);
            showTapTarget(toolbar);
            toolbar.setOnMenuItemClickListener(item -> {
                setUpMenuItemAction(item);
                return true;
            });
            setUpSearch(toolbar.getMenu());
        } else {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(isAddActivity ? R.string.add : R.string.search);
        }
    }

    private void setUpSearch(Menu menu) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                refreshList(true);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 12) {
                    currentQuery = newText;
                    refreshList(false);
                    return true;
                }
                return false;
            }
        });
    }

    private void setUpMenuItemAction(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_search_by:
                String[] array = getResources().getStringArray(R.array.search_by_array);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.search_by)
                        .setSingleChoiceItems(array, checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        queryPrefix = "intitle:";
                                        item.setIcon(R.drawable.ic_action_title);
                                        break;
                                    case 1:
                                        queryPrefix = "inauthor:";
                                        item.setIcon(R.drawable.ic_action_author);
                                        break;
                                    case 2:
                                        queryPrefix = "isbn:";
                                        item.setIcon(R.drawable.ic_action_isbn);
                                        break;
                                }
                                set(dialog, which);
                            }
                            private void set(DialogInterface dialog, int checkedItem) {
                                SearchActivity.this.checkedItem = checkedItem;
                                refreshList(true);
                                dialog.dismiss();
                            }
                        }).show();
                break;

            case R.id.action_manually_add:
                startActivity(new Intent(this, ManualAddActivity.class));
                break;
        }
    }

    private void showTapTarget(Toolbar toolbar) {
        if (!prefs.getBoolean(K.PREF.TAP_TARGET_SEARCH, false)) {
            new TapTargetSequence(this)
                    .continueOnCancel(true)
                    .targets(
                            TapTarget.forToolbarMenuItem(toolbar, R.id.action_search,
                                    getString(R.string.search), getString(R.string.tap_target_search, new String(Character.toChars(0x1F50D)))).id(1),
                            TapTarget.forToolbarMenuItem(toolbar, R.id.action_search_by,
                                    getString(R.string.search_by), getString(R.string.tap_target_search_by)).id(2)
                    ).listener(new TapTargetSequence.Listener() {
                @Override
                public void onSequenceFinish() {
                    prefs.edit().putBoolean(K.PREF.TAP_TARGET_SEARCH, true).apply();
                    setUpToolBar(toolbar);
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
    
    private void setUpRecyclerView() {
        if (rv != null) {
            mAdapter = new VolumeAdapter(this, volumeList);
            rv.setAdapter(mAdapter);
        } else {
            rv = findViewById(R.id.rv_search);
            mAdapter = new VolumeAdapter(this, volumeList);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(mLayoutManager);
            rv.setAdapter(mAdapter);
        }
    }

    private void refreshList(boolean fromUser) {
        if (!currentQuery.equals("")) {
            swipeLayout.setRefreshing(fromUser);
            new Thread(() -> {
                try {
                    volumeList = Utils.searchVolumes(queryPrefix + currentQuery);
                } catch (Exception ignored) {}
                runOnUiThread(() -> {
                    swipeLayout.setRefreshing(false);
                    setUpRecyclerView();
                    if (volumeList == null && fromUser) {
                        Utils.hideKeyboard(SearchActivity.this);
                        Snackbar.make(findViewById(R.id.cl), R.string.search_no_book_found, Snackbar.LENGTH_LONG).show();
                    }
                });
            }).start();
        }
    }
}

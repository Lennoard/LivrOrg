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
import com.androidvip.bookshelf.adapter.BookAdapter;
import com.androidvip.bookshelf.model.Book;
import com.androidvip.bookshelf.model.Book_;
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
    private int currentNavId;
    private Snackbar snackNet;
    private SharedPreferences prefs;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.app_name) + ": " + getString(R.string.reading_state_reading));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra(K.EXTRA_IS_ADD_ACTIVITY, true);
            startActivity(intent);
        });

        currentNavId = R.id.nav_reading;

        registerReceiver(netReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        setUpDrawer(toolbar);

        swipeLayout = findViewById(R.id.swipe_rv_main);
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeLayout.setOnRefreshListener(this::onStart);

        showTapTarget(toolbar, fab);

        snackNet = Snackbar.make(findViewById(R.id.cl), R.string.error_no_connection, Snackbar.LENGTH_INDEFINITE);
        if (!Utils.isOnline(this))
            snackNet.show();
    }

    @Override
    protected void onStart() {
        bookBox = ((App) getApplication()).getBoxStore().boxFor(Book.class);
        swipeLayout.setRefreshing(true);
        switchNavItems(currentNavId);
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

        // Set up search
        MenuItem searchMenuItem = menu.findItem(R.id.action_filter);

        // Get the search service (using this activity's context)
        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    // The search field is now visible, good opportunity to set up a query listener
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            // User has pressed the search/submit button on his/her input method,
                            // and we received the query, update the list accordingly
                            setUpRecyclerView(filterBooksByMatch(query));
                            return true;
                        }
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            // User has started to type his/her query
                            if (newText.length() > 4) {
                                // We now received the query, update the list accordingly
                                setUpRecyclerView(filterBooksByMatch(newText));
                                return true;
                            }
                            // Return false, we are not interested in queries that are no larger than 5 characters
                            return false;
                        }
                    });
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // User has clicked on the back button, give him/her the previous list back
                    setUpRecyclerView(currentList);
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.action_log_out:
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit().putBoolean(K.PREF.LOGGED_IN, false).apply();
                i = new Intent(this, LoginActivity.class);
                startActivity(i);
                // Finish this activity in case of the user attempts to come back
                finish();
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
        switchNavItems(item.getItemId());
        return true;
    }

    private void showTapTarget(Toolbar toolbar, FloatingActionButton fab) {
        if (!prefs.getBoolean(K.PREF.TAP_TARGET_MAIN, false)) {
            new TapTargetSequence(this)
                    .continueOnCancel(true)
                    .targets(
                            TapTarget.forToolbarNavigationIcon(toolbar, getString(R.string.menu), getString(R.string.tap_target_drawer_button)).id(1),
                            TapTarget.forView(fab, getString(R.string.add), getString(R.string.tap_target_add))
                                    .tintTarget(false)
                                    .cancelable(false)
                                    .id(2)
                    ).listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            prefs.edit().putBoolean(K.PREF.TAP_TARGET_MAIN, true).apply();
                            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                            intent.putExtra(K.EXTRA_IS_ADD_ACTIVITY, true);
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

    private void switchNavItems(int itemId) {
        switch (itemId) {
            case R.id.nav_reading:
                currentNavId = R.id.nav_reading;
                setUpRecyclerView(filterBooksByReadingState(Book.STATE_READING));
                actionBar.setTitle(R.string.reading_state_reading);
                break;
            case R.id.nav_wish:
                currentNavId = R.id.nav_wish;
                setUpRecyclerView(filterBooksByReadingState(Book.STATE_WISH));
                actionBar.setTitle(R.string.reading_state_wish);
                break;
            case R.id.nav_on_hold:
                currentNavId = R.id.nav_on_hold;
                setUpRecyclerView(filterBooksByReadingState(Book.STATE_ON_HOLD));
                actionBar.setTitle(R.string.reading_state_on_hold);
                break;
            case R.id.nav_dropped:
                currentNavId = R.id.nav_dropped;
                setUpRecyclerView(filterBooksByReadingState(Book.STATE_DROPPED));
                actionBar.setTitle(R.string.reading_state_dropped);
                break;
            case R.id.nav_finished:
                currentNavId = R.id.nav_finished;
                setUpRecyclerView(filterBooksByReadingState(Book.STATE_FINISHED));
                actionBar.setTitle(R.string.reading_state_finished);
                break;
            case R.id.nav_favorites:
                currentNavId = R.id.nav_favorites;
                setUpRecyclerView(filterBooksByFavorite());
                actionBar.setTitle(R.string.favorites);
                break;
            case R.id.nav_search:
                startActivity(new Intent(this, SearchActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    private List<Book> filterBooksByReadingState(int readingState){
        List<Book> l = bookBox.query().equal(Book_.readingState, readingState).build().find();
        currentList = l;
        return l;
    }

    private List<Book> filterBooksByFavorite(){
        List<Book> l = bookBox.query().equal(Book_.favorite, true).build().find();
        currentList = l;
        return l;
    }

    private List<Book> filterBooksByMatch(String match) {
        List<Book> l = new ArrayList<>();
        for (Book book : currentList) {
            // Safe
            String titleSearch = book.getTitle().toLowerCase();
            // Not safe, must check for nullity, in this case, the default values are random strings
            // rather than "" because the user might actually hit the search button with an empty query
            String tagsSearch = Utils.notNull(book.getTags(), Utils.randomString(6)).toLowerCase();
            String authorsSearch = Utils.notNull(book.getAuthors(), Utils.randomString(6)).toLowerCase();
            if (titleSearch.contains(match.toLowerCase()) || tagsSearch.contains(match.toLowerCase()) || authorsSearch.contains(match.toLowerCase()))
                l.add(book);
        }
        return l;
    }

    private void setUpDrawer(Toolbar toolbar) {
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_reading);
    }

    private void setUpRecyclerView(List<Book> lista) {
        if (rv != null) {
            mAdapter = new BookAdapter(this, lista, false);
            rv.setAdapter(mAdapter);
        } else {
            rv = findViewById(R.id.rv_main);
            mAdapter = new BookAdapter(this, lista, false);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(mLayoutManager);
            rv.setAdapter(mAdapter);
        }
        swipeLayout.setRefreshing(false);
    }

}

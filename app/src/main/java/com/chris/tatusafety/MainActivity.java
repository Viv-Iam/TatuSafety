package com.chris.tatusafety;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ListView list;
    CustomListAdapter adapter;
    ArrayList<Report> data;
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        list = (ListView) findViewById(R.id.List);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        data = new ArrayList<>();
        adapter = new CustomListAdapter(MainActivity.this, data);
        list.setAdapter(adapter);
        fetch();
        Intent i = new Intent(this, SyncService.class);
        this.startService(i);
        Toast.makeText(this, "Syncing...", Toast.LENGTH_SHORT).show();


        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent report = new Intent(MainActivity.this, NewReportActivity.class);
                startActivity(report);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //todo: swipe refresh layout
        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("REFRESH", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        list.setAdapter(null);
                        data = new ArrayList<>();
                        adapter = new CustomListAdapter(MainActivity.this, data);
                        list.setAdapter(adapter);
                        fetch();
                        Intent i = new Intent(MainActivity.this, SyncService.class);
                        MainActivity.this.startService(i);
                        swipeRefresh.setRefreshing(false );

                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation list item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_explore) {
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
            // Handle the camera action
        } else if (id == R.id.nav_report) {
            Intent newReport = new Intent(this, NewReportActivity.class);
            startActivity(newReport);

        } else if (id == R.id.nav_findMe) {
            Intent findMe = new Intent(this, FindMeActivity.class);
            startActivity(findMe);

        } else if (id == R.id.nav_history) {
            Intent history = new Intent(this, HistoryActivity.class);
            startActivity(history);

        } else if (id == R.id.nav_preferences) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_profile) {
            Intent profile = new Intent(this, AccountActivity.class);
            startActivity(profile);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    private void fetch() {
        String url = "http://www.thebigzoo.co.ke/tatusafety/fetch.php";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Connection failed!", Toast.LENGTH_SHORT).show();
                Log.d("FAILED", throwable.getMessage() + s);

            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                Log.d("SUCCESS", s);
                try {
                    JSONArray array = new JSONArray(s);
                    for (int x = 0; x < array.length(); x++) {

                        JSONObject obj = array.getJSONObject(x);
                        String road = obj.getString("road");
                        String sacco = obj.getString("sacco");
                        String date = obj.getString("date");
                        String time = obj.getString("time");
                        Report r = new Report(date, time, road, sacco);
                        data.add(r);

                    }
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }
}

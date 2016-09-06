package com.crypta.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crypta.R;
import com.crypta.dropbox.DropboxClientFactory;
import com.crypta.util.PicassoClient;
import com.dropbox.core.v2.users.FullAccount;


/**
 * Activity that shows information about the currently logged in user
 */
public class UserActivity extends DropboxActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(),
                        ChooseProviderActivity.class);
                startActivity(it);
            }
        });

   /*     setContentView(R.layout.activity_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        Button loginButton = (Button)findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.startOAuth2Authentication(UserActivity.this, getString(R.string.app_key));
            }
        });

        Button filesButton = (Button)findViewById(R.id.files_button);
        filesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(FilesActivity.getIntent(UserActivity.this, ""));
            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasToken()) {
            SharedPreferences prefs = getSharedPreferences("provider-tokens", Context.MODE_PRIVATE);
            String accessToken = prefs.getString("dropbox-access-token", null);
            DropboxClientFactory.init(accessToken);
            PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
            loadData();
            Intent intent = new Intent(UserActivity.this, FilesActivity.class);
            startActivity(intent);
            System.out.println("HELLO FROM USER ACTIVITY: " + super.getToken());

        } else {
            ((TextView) findViewById(R.id.noProviderTextView)).setText("No Providers Added");
            if (!drawer.isDrawerOpen(GravityCompat.START)) {

                drawer.openDrawer(GravityCompat.START);
            }
        }
    }

    @Override
    protected void loadData() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                ((TextView) findViewById(R.id.userEmail)).setText(result.getEmail());
                ((TextView) findViewById(R.id.userName)).setText(result.getName().getDisplayName());
                StringBuilder s = new StringBuilder();
                s.append(result.getName().getFamiliarName().charAt(0)).append(result.getName().getSurname().charAt(0));
                CharSequence sequence = s.subSequence(0, 2);
                ((TextView) findViewById(R.id.userInitials)).setText(sequence);
            }

            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details.", e);
            }
        }).execute();
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_new_provider) {
            Intent it = new Intent(getApplicationContext(),
                    ChooseProviderActivity.class);
            startActivity(it);
        } else if (id == R.id.nav_settings) {
            //launch Settings Activity
            Intent intent = new Intent(UserActivity.this, PreferencesActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

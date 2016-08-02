package com.crypta.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.crypta.R;
import com.crypta.dropbox.DropboxClientFactory;
import com.crypta.adapter.Provider;
import com.crypta.adapter.ProviderAdapter;
import com.crypta.util.PicassoClient;
import com.dropbox.core.android.Auth;
import com.dropbox.core.android.AuthActivity;

import java.util.ArrayList;
import java.util.List;

public class ChooseProviderActivity extends AppCompatActivity {


    private ProviderAdapter mProviderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_provider);
        Toolbar toolbar = (Toolbar) findViewById(R.id.include);
        toolbar.setTitle("Select your provider...");
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.provider_list);
        //ListView list_view = (ListView)findViewById(R.id.list_view);

        mProviderAdapter = new ProviderAdapter(PicassoClient.getPicasso(), new ProviderAdapter.Callback() {
            @Override
            public void onItemClicked(final Provider provider) {
                //try adding a provider, check if provider is already added
                switch (provider.getTitle()) {
                    case "Dropbox":
                        SharedPreferences prefs = getSharedPreferences("provider-tokens", MODE_PRIVATE);
                        String accessToken = prefs.getString("dropbox-access-token", null);
                        if (accessToken == null) {
                            Auth.startOAuth2Authentication(ChooseProviderActivity.this, getString(R.string.app_key));
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Dropbox already added! Choose another provider!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }


                    default:
                }

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mProviderAdapter);

        Provider provider = new Provider("Dropbox");

        List<Provider> myList = new ArrayList<Provider>();
        myList.add(provider);
        mProviderAdapter.setFiles(myList);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getDropboxAccessToken();
    }

    protected void getDropboxAccessToken() {
        if (AuthActivity.result != null) {
            String accessToken = Auth.getOAuth2Token();
            SharedPreferences prefs = getSharedPreferences("provider-tokens", MODE_PRIVATE);
            prefs.edit().putString("dropbox-access-token", accessToken).commit();
            DropboxClientFactory.reinitialize(accessToken);
            Intent intent = new Intent(ChooseProviderActivity.this, UserActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}

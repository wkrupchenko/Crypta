package com.crypta.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.crypta.dropbox.DropboxClientFactory;
import com.crypta.util.PicassoClient;


public abstract class DropboxActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("provider-tokens", MODE_PRIVATE);
        String accessToken = prefs.getString("dropbox-access-token", null);
        if (accessToken == null) {

        } else {
            initAndLoadData(accessToken);
        }
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
        loadData();
    }

    protected abstract void loadData();

    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences("provider-tokens", MODE_PRIVATE);
        String accessToken = prefs.getString("dropbox-access-token", null);
        return accessToken != null;
    }

    protected String getToken() {
        SharedPreferences prefs = getSharedPreferences("provider-tokens", MODE_PRIVATE);
        String accessToken = prefs.getString("dropbox-access-token", null);
        return accessToken;
    }
}

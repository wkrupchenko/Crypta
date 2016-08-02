package com.crypta.activities;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.SearchResult;

/**
 * Async task to list items in a folder
 */
class SearchFileTask extends AsyncTask<String, Void, SearchResult> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public SearchFileTask(DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(SearchResult result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onFilesFound(result);
        }
    }

    @Override
    protected SearchResult doInBackground(String... params) {
        String query = params[0];
        try {
            return mDbxClient.files().search("", query);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }

    public interface Callback {
        void onFilesFound(SearchResult result);

        void onError(Exception e);
    }
}

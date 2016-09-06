package com.crypta.activities;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;

class CopyFileTask extends AsyncTask<String, Void, Metadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    CopyFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Metadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onCopySuccess(result);
        }
    }

    @Override
    protected Metadata doInBackground(String... params) {
        String filePath = params[0];
        String remoteFolderPath = params[1];

        try {
            return mDbxClient.files().copy(filePath, remoteFolderPath);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }

    public interface Callback {
        void onCopySuccess(Metadata result);

        void onError(Exception e);
    }
}

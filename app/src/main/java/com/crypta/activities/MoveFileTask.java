package com.crypta.activities;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;

/**
 * Async task to upload a file to a directory
 */
class MoveFileTask extends AsyncTask<String, Void, Metadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    MoveFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
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
            mCallback.onMoveSuccess(result);
        }
    }

    @Override
    protected Metadata doInBackground(String... params) {
        String filePath = params[0];
        String remoteFolderPath = params[1];

        try {
            return mDbxClient.files().move(filePath, remoteFolderPath);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }

    public interface Callback {
        void onMoveSuccess(Metadata result);

        void onError(Exception e);
    }
}

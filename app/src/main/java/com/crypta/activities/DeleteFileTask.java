package com.crypta.activities;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 */
class DeleteFileTask extends AsyncTask<FileMetadata, Void, Metadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    DeleteFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Metadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDeleteComplete(result);
        }
    }

    @Override
    protected Metadata doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        try {

            Metadata deletedMdata = null;

            deletedMdata = mDbxClient.files().delete(metadata.getPathLower());

            return deletedMdata;
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }

    public interface Callback {
        void onDeleteComplete(Metadata result);

        void onError(Exception e);
    }
}

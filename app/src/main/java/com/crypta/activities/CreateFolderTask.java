package com.crypta.activities;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;

class CreateFolderTask extends AsyncTask<String, Void, FolderMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    CreateFolderTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FolderMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onCreateSuccess(result);
        }
    }

    @Override
    protected FolderMetadata doInBackground(String... params) {
        String folder_name = params[0];
        String remoteFolderPath = params[1];
        try {

            if (folder_name != null && folder_name.length() > 0) {

                final FolderMetadata folder;

                // Create the  folder.
                folder = mDbxClient.files().createFolder(remoteFolderPath + "/" + folder_name);

                return folder;
            }
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }

    public interface Callback {
        void onCreateSuccess(FolderMetadata result);

        void onError(Exception e);
    }
}

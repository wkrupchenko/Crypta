package com.crypta.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.crypta.R;
import com.crypta.dropbox.DropboxClientFactory;
import com.crypta.adapter.FilesAdapter;
import com.crypta.util.PicassoClient;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

public class ItemMoveActivity extends DropboxActivity {

    public final static String EXTRA_PATH = "FilesActivity_Path";
    private static final String TAG = ItemMoveActivity.class.getName();
    private String mPath;
    private FilesAdapter mFilesAdapter;

    private FileMetadata mSelectedFile;

    public static Intent getIntent(Context context, String path) {
        Intent filesIntent = new Intent(context, ItemMoveActivity.class);
        filesIntent.putExtra(ItemMoveActivity.EXTRA_PATH, path);
        return filesIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra(EXTRA_PATH);
        mPath = path == null ? "" : path;
        setContentView(R.layout.activity_item_move);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        final String activityClass = extras.getString("activity_class");
        if (activityClass != null && activityClass.equals("com.crypta.activities.FilesActivity")) {

            final String file_path = extras.getString("file_path");
            final String file_name = extras.getString("file_name");
            SharedPreferences prefs = getSharedPreferences("utils", MODE_PRIVATE);
            prefs.edit().putString("file_path", file_path).commit();
            prefs.edit().putString("file_name", file_name).commit();

        }

        Button fab = (Button) findViewById(R.id.move_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveItem();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_move_files_list);

        mFilesAdapter = new FilesAdapter(PicassoClient.getPicasso(), new FilesAdapter.Callback() {
            @Override
            public void onFolderClicked(FolderMetadata folder) {
                startActivity(ItemMoveActivity.getIntent(ItemMoveActivity.this, folder.getPathLower()));
            }

            @Override
            public void onFileClicked(final FileMetadata file) {

            }

        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mFilesAdapter);

        mSelectedFile = null;
    }

    @Override
    protected void loadData() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading");
        dialog.show();

        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                dialog.dismiss();

                mFilesAdapter.setFiles(result.getEntries());
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to list folder.", e);
                Toast.makeText(ItemMoveActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(mPath);

    }

    protected void moveItem() {

        final ProgressDialog dialog = new ProgressDialog(this);

        SharedPreferences prefs = getSharedPreferences("utils", MODE_PRIVATE);
        final String file_path = prefs.getString("file_path", null);
        final String file_name = prefs.getString("file_name", null);

        if (file_path != null && file_name != null) {

        final AsyncTask task = new MoveFileTask(ItemMoveActivity.this, DropboxClientFactory.getClient(), new MoveFileTask.Callback() {
                @Override
                public void onMoveSuccess(Metadata result) {
                    dialog.dismiss();

                    if (result != null) {
                        //refresh listview
                        loadData();
                        Toast.makeText(ItemMoveActivity.this,
                                "Item successfully moved to " + mPath,
                                Toast.LENGTH_SHORT)
                                .show();
                        Intent intent = new Intent(ItemMoveActivity.this, FilesActivity.class);
                        intent.putExtra("FilesActivity_Path", mPath);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }

                @Override
                public void onError(Exception e) {
                    dialog.dismiss();

                    Log.e(TAG, "Failed to move item!", e);
                    Toast.makeText(ItemMoveActivity.this,
                            "Failed to move item!",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }).execute(file_path, mPath + "/" + file_name);

            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage("Moving item...");
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {

                        if (task != null) {
                            task.cancel(true);
                            if (task.isCancelled()) {
                                Intent intent = new Intent(ItemMoveActivity.this, FilesActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(ItemMoveActivity.this, "Cannot cancel this task!", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            dialog.show();

        }

    }
}

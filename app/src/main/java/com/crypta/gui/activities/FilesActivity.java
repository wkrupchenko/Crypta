package com.crypta.gui.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crypta.R;
import com.crypta.dropbox.DropboxClientFactory;
import com.crypta.gui.adapter.FilesAdapter;
import com.crypta.gui.fragments.EncryptionDialogFragment;
import com.crypta.gui.fragments.NewFolderDialogFragment;
import com.crypta.util.PicassoClient;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Activity that displays the content of a path in dropbox and lets users navigate folders,
 * and upload/download files
 */
public class FilesActivity extends DropboxActivity implements NavigationView.OnNavigationItemSelectedListener, NewFolderDialogFragment.NoticeDialogListener {
    public final static String EXTRA_PATH = "FilesActivity_Path";
    private static final String TAG = FilesActivity.class.getName();
    private static final int PICKFILE_REQUEST_CODE = 1;
    public static int marker = -1;
    final int ENC_REQUEST_CODE = 2;
    private String selectedFile = "";
    private String mPath;
    private FilesAdapter mFilesAdapter;

    private FileMetadata mSelectedFile;

    public static Intent getIntent(Context context, String path) {
        Intent filesIntent = new Intent(context, FilesActivity.class);
        filesIntent.putExtra(FilesActivity.EXTRA_PATH, path);
        return filesIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String path = getIntent().getStringExtra(EXTRA_PATH);
        mPath = path == null ? "" : path;

        /*setContentView(R.layout.activity_files);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setNavigationIcon(R.drawable.ic_show_user_profile);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);*/

        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                new BottomSheet.Builder(FilesActivity.this)
                        .setSheet(R.menu.menu_files_activity_actions)
                        .setTitle("Actions")
                        .setListener(new BottomSheetListener() {
                            @Override
                            public void onSheetShown(BottomSheet bottomSheet) {

                            }

                            @Override
                            public void onSheetItemSelected(BottomSheet bottomSheet, MenuItem menuItem) {

                                if (menuItem.getItemId() == R.id.upload) {

                                    performWithPermissions(FileAction.UPLOAD);
                                } else if (menuItem.getItemId() == R.id.new_folder) {

                                    NewFolderDialogFragment dialog = new NewFolderDialogFragment();
                                    dialog.show(getFragmentManager(), "New Folder Dialog Fragment");

                                } else {

                                }

                            }

                            @Override
                            public void onSheetDismissed(BottomSheet bottomSheet, int i) {

                            }
                        })
                        .show();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.files_list);
        //ListView list_view = (ListView)findViewById(R.id.list_view);


        mFilesAdapter = new FilesAdapter(PicassoClient.getPicasso(), new FilesAdapter.Callback() {
            @Override
            public void onFolderClicked(FolderMetadata folder) {
                startActivity(FilesActivity.getIntent(FilesActivity.this, folder.getPathLower()));
            }

            @Override
            public void onFileClicked(final FileMetadata file) {
                mSelectedFile = file;
                new BottomSheet.Builder(FilesActivity.this)
                        .setSheet(R.menu.menu_file_actions)
                        .setTitle("File Actions")
                        .setListener(new BottomSheetListener() {
                            @Override
                            public void onSheetShown(BottomSheet bottomSheet) {

                            }

                            @Override
                            public void onSheetItemSelected(BottomSheet bottomSheet, MenuItem menuItem) {

                                if (menuItem.getItemId() == R.id.download) {
                                    performWithPermissions(FileAction.DOWNLOAD);
                                } else if (menuItem.getItemId() == R.id.delete) {

                                        deleteFile(file);

                                } else if (menuItem.getItemId() == R.id.share) {

                                } else if (menuItem.getItemId() == R.id.move) {
                                    Intent intent = new Intent(FilesActivity.this, ItemMoveActivity.class);
                                    intent.putExtra("file_path", file.getPathLower());
                                    intent.putExtra("file_name", file.getName());
                                    intent.putExtra("activity_class", FilesActivity.this.getClass().getCanonicalName());
                                    startActivity(intent);
                                } else {
                                }

                            }

                            @Override
                            public void onSheetDismissed(BottomSheet bottomSheet, int i) {

                            }
                        })
                        .show();
            }

        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mFilesAdapter);

       /* ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_item, new ArrayList<String>() {
            {
                add("Dropbox");
                add("Tresorit");
                add("Google Drive");
            }
        });
        list_view.setAdapter(adapter);*/

        mSelectedFile = null;
    }

    @Override
    public void onNewFolderDialogPositiveClick(DialogFragment dialog) {
        Dialog dialogView = dialog.getDialog();
        String foldername = ((EditText) dialogView.findViewById(R.id.editText_new_folder_dialog)).getText().toString();
        createNewFolder(foldername);
    }

    @Override
    public void onNewFolderDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.provider_listview_appbar, menu);

      /*  if (hasToken()) {
            MenuItem item = menu.findItem(R.id.spinner);
            Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.provider_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.select_dialog_multichoice);

            spinner.setAdapter(adapter);
            spinner.setVisibility(View.VISIBLE);
        }*/

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchDataInList(query);
        }

    }
    private void launchFilePicker() {
        // Launch intent to pick file for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                selectedFile = data.getData().toString();
                showEncryptDialog(data);
                // This is the result of a call to launchFilePicker
                //uploadFile(data.getData().toString());
            }
        }
        if (requestCode == ENC_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String pwd = data.getStringExtra("pwd");

                if (pwd != null && pwd.length() > 0) {
                    Log.i("THE PASSWORD IS", pwd);

                    try {
                        encryptFile(selectedFile);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            }
        }
    }

    void showEncryptDialog(Intent data) {
        EncryptionDialogFragment newFragment = EncryptionDialogFragment.newInstance(
                R.string.label_encrypt_dialog, data);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void doPositiveClick(Intent data) {
        Log.i("FragmentAlertDialog", "Positive click!");
        Intent it = new Intent(getApplicationContext(),
                EncryptionActivity.class);
        startActivityForResult(it, ENC_REQUEST_CODE);
    }

    public void doNegativeClick(Intent data) {
        Log.i("FragmentAlertDialog", "Negative click!");
        uploadFile(data.getData().toString());
    }

    @Override
    public void onRequestPermissionsResult(int actionCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        FileAction action = FileAction.fromCode(actionCode);

        boolean granted = true;
        for (int i = 0; i < grantResults.length; ++i) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Log.w(TAG, "User denied " + permissions[i] +
                        " permission to perform file action: " + action);
                granted = false;
                break;
            }
        }

        if (granted) {
            performAction(action);
        } else {
            switch (action) {
                case UPLOAD:
                    Toast.makeText(this,
                            "Can't upload file: read access denied. " +
                                    "Please grant storage permissions to use this functionality.",
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case DOWNLOAD:
                    Toast.makeText(this,
                            "Can't download file: write access denied. " +
                                    "Please grant storage permissions to use this functionality.",
                            Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }
    }

    private void performAction(FileAction action) {
        switch (action) {
            case UPLOAD:
                launchFilePicker();
                break;
            case DOWNLOAD:
                if (mSelectedFile != null) {
                    downloadFile(mSelectedFile);
                } else {
                    Log.e(TAG, "No file selected to download.");
                }
                break;
            default:
                Log.e(TAG, "Can't perform unhandled file action: " + action);
        }
    }

    protected void sortFilesByName(FilesAdapter adapter){

        class CompareByName implements Comparator<Metadata> {
            @Override
            public int compare(Metadata a, Metadata b) {
              return a.getPathLower().compareTo(b.getPathLower());
            }
        }

       if (adapter != null){

           List<Metadata> mFiles = new ArrayList<Metadata>(adapter.getFiles());

           if (mFiles != null) {
               Collections.sort(mFiles, new CompareByName());
               mFilesAdapter.setFiles(mFiles);
           }
       }

    }

    @Override
    protected void loadData() {

        final ProgressDialog dialog = new ProgressDialog(this);

        final AsyncTask task = new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                dialog.dismiss();

                mFilesAdapter.setFiles(result.getEntries());
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to list folder.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(mPath);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading");
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {

                    if (task != null) {
                        task.cancel(true);
                    } else {
                        Toast.makeText(FilesActivity.this,"Cannot cancel this task!", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();

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

    protected void createNewFolder(String name) {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Creating Folder...");
        dialog.show();

        new CreateFolderTask(FilesActivity.this, DropboxClientFactory.getClient(), new CreateFolderTask.Callback() {
            @Override
            public void onCreateSuccess(FolderMetadata result) {
                dialog.dismiss();

                if (result != null) {
                    //refresh listview
                    loadData();
                    Toast.makeText(FilesActivity.this,
                            "Folder " + result.getName().toString() + " created!",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to create folder.", e);
                Toast.makeText(FilesActivity.this,
                        "Failed to create folder",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(name, mPath);
    }

    protected void searchDataInList(final String name) {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Searching...");
        dialog.show();

        final List<Metadata> mFiles = new ArrayList<Metadata>();

        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                dialog.dismiss();

                for (Metadata data : result.getEntries()) {
                    if (data.getName().contains(name)) {

                        mFiles.add(data);

                    }
                }

                if (mFiles.size() > 0) {

                    mFilesAdapter.setFiles(mFiles);
                } else {
                    Toast.makeText(FilesActivity.this,
                            "File or folder not found!",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to list folder.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(mPath);

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

    private void downloadFile(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(true);
        dialog.setMessage("Downloading");
        dialog.show();

        new DownloadFileTask(FilesActivity.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();

                if (result != null) {
                    String extension = "";

                    int i = result.getAbsolutePath().lastIndexOf('.');
                    if (i >= 0) {
                        extension = result.getAbsolutePath().substring(i + 1);
                    }
                    if (extension.equals("aes")) {

                        try {
                            decryptFile(result);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        viewFileInExternalApp(result);
                    }

                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to download file.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    private void viewFileInExternalApp(File result) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = result.getName().substring(result.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);

        intent.setDataAndType(Uri.fromFile(result), type);

        // Check for a handler first to avoid a crash
        PackageManager manager = getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            startActivity(intent);
        }
    }

    private void encryptFile(String fileUri) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Encrypting file...");
        dialog.show();

        new EncryptTask(this, new EncryptTask.Callback() {
            @Override
            public void onEncryptSuccess(String filepath) {
                dialog.dismiss();
                //upload if successfully encrypted
                uploadFile(filepath);
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to encrypt file.", e);
                Toast.makeText(FilesActivity.this,
                        "Failed to encrypt file!",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(fileUri);
    }

    private void decryptFile(File file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Decrypting file...");
        dialog.show();

        new DecryptTask(this, new DecryptTask.Callback() {
            @Override
            public void onDecryptSuccess(String filepath) {
                dialog.dismiss();
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to decrypt file.", e);
                Toast.makeText(FilesActivity.this,
                        "Failed to decrypt file!",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);
    }

    private void uploadFile(String fileUri) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();

        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                dialog.dismiss();

                String message = result.getName() + " size " + result.getSize() + " modified " +
                        DateFormat.getDateTimeInstance().format(result.getClientModified());
                Toast.makeText(FilesActivity.this, message, Toast.LENGTH_SHORT)
                        .show();

                // Reload the folder
                loadData();
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to upload file.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(fileUri, mPath);
    }

    private void deleteFile(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(true);
        dialog.setMessage("Deleting file from cloud...");
        dialog.show();

        new DeleteFileTask(FilesActivity.this, DropboxClientFactory.getClient(), new DeleteFileTask.Callback() {
            @Override
            public void onDeleteComplete(Metadata result) {
                dialog.dismiss();

                if (result != null) {
                    //refresh listview
                    loadData();
                    Toast.makeText(FilesActivity.this,
                            "File " + result.getName().toString() + " deleted...",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to delete file...", e);
                Toast.makeText(FilesActivity.this,
                        "Failed to delete file...",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    private void performWithPermissions(final FileAction action) {
        if (hasPermissionsForAction(action)) {
            performAction(action);
            return;
        }

        if (shouldDisplayRationaleForAction(action)) {
            new AlertDialog.Builder(this)
                    .setMessage("This app requires storage access to download and upload files.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissionsForAction(action);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            requestPermissionsForAction(action);
        }
    }

    private boolean hasPermissionsForAction(FileAction action) {
        for (String permission : action.getPermissions()) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldDisplayRationaleForAction(FileAction action) {
        for (String permission : action.getPermissions()) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    private void requestPermissionsForAction(FileAction action) {
        ActivityCompat.requestPermissions(
                this,
                action.getPermissions(),
                action.getCode()
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences pref = getSharedPreferences("crypta", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        editor.putString("LastLoggedIn", format.format(Calendar.getInstance().getTime()));
        editor.commit();

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = getSharedPreferences("crypta", MODE_PRIVATE);
        String date = pref.getString("LastLoggedIn", null);
        if (date != null) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date d1 = format.parse(date);
                Date d2 = format.parse(format.format(Calendar.getInstance().getTime()));
                long diff = d2.getTime() - d1.getTime();
                long diffMinutes = diff / (60 * 1000) % 60;
                if (diffMinutes > 1) {
                    diffMinutes = 0;
//                    Intent it = new Intent(getApplicationContext(),
//                            SignInActivity.class);
                    //startActivity(it);
                }
                System.out.println("TIME" + diffMinutes);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //Set Dropbox Navigation Button in activity_provider_drawer menu
        // <group android:checkableBehavior="single"


        SharedPreferences prefs = getSharedPreferences("provider-tokens", MODE_PRIVATE);
        String accessToken = prefs.getString("dropbox-access-token", null);

        if (accessToken != null) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            Menu menu = navigationView.getMenu();

            if (menu.findItem(20) == null) {

                menu.add(R.id.group_id, 20, 2, "Dropbox").setIcon(R.drawable.dropbox_logo).setEnabled(true);

            }


        }

    }

    @Override
    public void onRestart() {
        super.onRestart();
        SharedPreferences prefs = getSharedPreferences("provider-tokens", MODE_PRIVATE);
        String accessToken = prefs.getString("dropbox-access-token", null);
        if (accessToken == null) {
            Intent intent = new Intent(FilesActivity.this, UserActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FilesActivity.this, SignInActivity.class);
        startActivity(intent);

//        moveTaskToBack(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort) {
            sortFilesByName(mFilesAdapter);
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

        } else if (id == 10) {
            //if Provider is Dropbox load files to FilesActivity
            Intent intent = new Intent(FilesActivity.this, UserActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_settings) {
            //launch Settings Activity
            Intent i = new Intent(FilesActivity.this, PreferencesActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private enum FileAction {
        DELETE(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        DOWNLOAD(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        UPLOAD(Manifest.permission.READ_EXTERNAL_STORAGE);

        private static final FileAction[] values = values();

        private final String[] permissions;

        FileAction(String... permissions) {
            this.permissions = permissions;
        }

        public static FileAction fromCode(int code) {
            if (code < 0 || code >= values.length) {
                throw new IllegalArgumentException("Invalid FileAction code: " + code);
            }
            return values[code];
        }

        public int getCode() {
            return ordinal();
        }

        public String[] getPermissions() {
            return permissions;
        }
    }

}

package com.crypta.gui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.crypta.R;

/**
 * Created by D064343 on 20.05.2016.
 */
public class NewFolderDialogFragment extends DialogFragment {

    NoticeDialogListener mListener;
    private Intent data;

    public static NewFolderDialogFragment newInstance(int title, Intent data) {
        NewFolderDialogFragment frag = new NewFolderDialogFragment();
        Bundle args = new Bundle();
        args.putInt("Create New folder", title);
        frag.setArguments(args);
        frag.data = data;
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_layout, null))
                // Add action buttons
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onNewFolderDialogPositiveClick(NewFolderDialogFragment.this);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onNewFolderDialogNegativeClick(NewFolderDialogFragment.this);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public interface NoticeDialogListener {
        void onNewFolderDialogPositiveClick(DialogFragment dialog);

        void onNewFolderDialogNegativeClick(DialogFragment dialog);
    }

}

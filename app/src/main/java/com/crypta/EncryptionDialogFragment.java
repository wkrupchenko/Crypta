package com.crypta;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.content.Intent;

/**
 * Created by D064343 on 20.05.2016.
 */
public class EncryptionDialogFragment extends DialogFragment {

    private Intent data;

    public static EncryptionDialogFragment newInstance(int title, Intent data) {
        EncryptionDialogFragment frag = new EncryptionDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        frag.data = data;
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        final Intent data = this.data;
       return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(R.string.label_encrypt,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((FilesActivity)getActivity()).doPositiveClick(data);
                            }
                        }
                )
                .setNegativeButton(R.string.label_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((FilesActivity)getActivity()).doNegativeClick(data);
                            }
                        }
                )
                .create();
    }
}

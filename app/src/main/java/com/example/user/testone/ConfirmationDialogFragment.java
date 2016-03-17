package com.example.user.testone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by theone on 4/11/15.
 */
public class ConfirmationDialogFragment extends DialogFragment {

    public static ConfirmationDialogFragment newInstance(String currentFile, String newFile) {
        ConfirmationDialogFragment frag = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString("current_file", currentFile);
        args.putString("new_file", newFile);
        frag.setArguments(args);
        return frag;
    }

    /*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String file = getArguments().getString("current_name");
        final String newfile = getArguments().getString("new_file");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Document: \"" + file  + "\" has been modified, save changes?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((MainActivity)getActivity()).doPositiveClick(newfile);
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        ((MainActivity)getActivity()).doNegativeClick(newfile);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
    */
}

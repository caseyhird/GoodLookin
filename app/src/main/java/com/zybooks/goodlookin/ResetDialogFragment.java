package com.zybooks.goodlookin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

// Dialogs from zybooks 4.3
public class ResetDialogFragment extends DialogFragment {
    public interface OnResetSelectedListener {
        void onResetSelectedClick(Boolean reset);
    }

    private OnResetSelectedListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.reset_title)
                .setMessage(R.string.reset_message)
                .setPositiveButton(R.string.reset_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Yes
                        // Return to main activity
                        mListener.onResetSelectedClick(true);
                    }
                })
                .setNegativeButton(R.string.reset_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked No
                        // Do nothing
                        mListener.onResetSelectedClick(false);
                    }
                })
                .create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnResetSelectedListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}

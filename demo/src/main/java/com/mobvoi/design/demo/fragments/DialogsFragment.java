package com.mobvoi.design.demo.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;

import com.ticwear.design.demo.R;

import mobvoi.design.app.AlertDialog;

/**
 * Created by tankery on 1/12/16.
 *
 * fragment for dialogs
 */
public class DialogsFragment extends ListFragment {

    static {
        initData(new int[]{
                R.string.category_dialog_notify,
                R.string.category_dialog_confirm,
                R.string.category_dialog_choose,
        });
    }

    @Override
    public void onTitleClicked(View view, @StringRes int titleResId) {
        DialogFragment fragment = createDialogFragment(view.getContext(), titleResId);
        if (fragment != null) {
            fragment.show(getChildFragmentManager(), view.getContext().getString(titleResId));
        }
    }

    private DialogFragment createDialogFragment(final Context context, int resId) {
        DialogFragment dialogFragment = null;
        switch (resId) {
            case R.string.category_dialog_notify:
                break;
            case R.string.category_dialog_confirm:
                dialogFragment = new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        return new AlertDialog.Builder(context)
                                .setTitle(R.string.category_dialog_confirm)
                                .setMessage(R.string.cheese_content)
                                .setPositiveButtonIcon(R.drawable.ic_btn_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                    }
                };
                break;
            case R.string.category_dialog_choose:
                break;
        }

        return dialogFragment;
    }

}

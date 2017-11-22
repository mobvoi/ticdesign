package ticwear.design.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import ticwear.design.app.AlertDialog;

public class SwitchConfirmPreference extends SwitchPreference {

    private String dialogMsg;

    public SwitchConfirmPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        if (isChecked()) {
            showSwitchPrefDialog();
        } else {
            super.onClick();
        }
    }

    private void showSwitchPrefDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(dialogMsg)
                .setPositiveButtonIcon(ticwear.design.R.drawable.tic_ic_btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SwitchConfirmPreference.super.onClick();
                    }
                })
                .setNegativeButtonIcon(ticwear.design.R.drawable.tic_ic_btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public void setDialogMessage(String msg) {
        dialogMsg = msg;
    }
}

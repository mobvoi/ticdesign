package com.mobvoi.design.demo.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import com.ticwear.design.demo.R;

import java.util.Calendar;

import ticwear.design.app.AlertDialog;
import ticwear.design.app.NumberPickerDialog;

/**
 * Created by tankery on 1/12/16.
 *
 * fragment for dialogs
 */
public class DialogsFragment extends ListFragment {

    static {
        initData(new int[]{
//                R.string.category_dialog_notify,
                R.string.category_dialog_confirm,
                R.string.category_dialog_choose,
                R.string.category_dialog_number_picker,
                R.string.category_dialog_time_picker,
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
                dialogFragment = new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        return new AlertDialog.Builder(context)
                                .setTitle(R.string.category_dialog_choose)
                                .setMessage(R.string.cheese_content)
                                .setPositiveButtonIcon(R.drawable.ic_btn_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setNeutralButtonIcon(R.drawable.ic_about, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                    }
                };
                break;
            case R.string.category_dialog_number_picker:
                dialogFragment = new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        return new NumberPickerDialog.Builder(context)
                                .minValue(0)
                                .maxValue(20)
                                .defaultValue(5)
                                .build();
                    }
                };
                break;
            case R.string.category_dialog_time_picker:
                dialogFragment = new TimePickerFragment();
                break;
        }

        return dialogFragment;
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }
    }
}

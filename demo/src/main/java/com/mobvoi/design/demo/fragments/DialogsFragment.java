package com.mobvoi.design.demo.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Toast;

import com.ticwear.design.demo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ticwear.design.app.AlertDialog;
import ticwear.design.app.DatetimePickerDialog;
import ticwear.design.app.NumberPickerDialog;

/**
 * Created by tankery on 1/12/16.
 *
 * fragment for dialogs
 */
public class DialogsFragment extends ListFragment {

    @Override
    protected int[] getItemTitles() {
        return new int[]{
//                R.string.category_dialog_notify,
                R.string.category_dialog_confirm,
                R.string.category_dialog_choose,
                R.string.category_dialog_number_picker,
                R.string.category_dialog_time_picker,
                R.string.category_dialog_date_picker,
                R.string.category_dialog_datetime_picker,
        };
    }

    @Override
    public void onTitleClicked(View view, @StringRes int titleResId) {
        Dialog dialog = createDialog(view.getContext(), titleResId);
        if (dialog != null) {
            dialog.show();
        }
    }

    private Dialog createDialog(final Context context, int resId) {
        Dialog dialog = null;
        switch (resId) {
            case R.string.category_dialog_notify:
                break;
            case R.string.category_dialog_confirm:
                dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.category_dialog_confirm)
                        .setMessage(R.string.cheese_content)
                        .setPositiveButtonIcon(R.drawable.ic_btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                break;
            case R.string.category_dialog_choose:
                dialog = new AlertDialog.Builder(context)
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
                break;
            case R.string.category_dialog_number_picker:
                dialog = new NumberPickerDialog.Builder(context)
                        .minValue(0)
                        .maxValue(20)
                        .defaultValue(5)
                        .valuePickedlistener(new NumberPickerDialog.OnValuePickedListener() {
                            @Override
                            public void onValuePicked(NumberPickerDialog dialog, int value) {
                                Toast.makeText(dialog.getContext(), "Picked value " + value,
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .create();
                break;
            case R.string.category_dialog_time_picker: {
                dialog = new DatetimePickerDialog.Builder(getActivity())
                        .defaultValue(Calendar.getInstance())
                        .disableDatePicker()
                        .listener(new DatetimePickerDialog.OnCalendarSetListener() {
                            @Override
                            public void onCalendarSet(DatetimePickerDialog dialog, Calendar calendar) {
                                Toast.makeText(dialog.getContext(), "Picked time: " +
                                                SimpleDateFormat.getTimeInstance().format(calendar.getTime()),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        })
                        .create();
                break;
            }
            case R.string.category_dialog_date_picker: {
                dialog = new DatetimePickerDialog.Builder(getActivity())
                        .defaultValue(Calendar.getInstance())
                        .disableTimePicker()
                        .listener(new DatetimePickerDialog.OnCalendarSetListener() {
                            @Override
                            public void onCalendarSet(DatetimePickerDialog dialog, Calendar calendar) {
                                Toast.makeText(dialog.getContext(), "Picked date: " +
                                                SimpleDateFormat.getDateInstance().format(calendar.getTime()),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        })
                        .create();
                break;
            }
            case R.string.category_dialog_datetime_picker: {
                dialog = new DatetimePickerDialog.Builder(getActivity())
                        .defaultValue(Calendar.getInstance())
                        .listener(new DatetimePickerDialog.OnCalendarSetListener() {
                            @Override
                            public void onCalendarSet(DatetimePickerDialog dialog, Calendar calendar) {
                                Toast.makeText(dialog.getContext(), "Picked datetime: " +
                                                SimpleDateFormat.getDateTimeInstance().format(calendar.getTime()),
                                        Toast.LENGTH_LONG)
                                .show();
                            }
                        })
                        .create();
                break;
            }
        }

        return dialog;
    }

}

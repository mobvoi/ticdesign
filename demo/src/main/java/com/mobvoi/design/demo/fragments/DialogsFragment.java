/*
 * Copyright (c) 2016 Mobvoi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobvoi.design.demo.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Toast;

import com.mobvoi.design.demo.data.Cheeses;
import com.ticwear.design.demo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ticwear.design.app.AlertDialog;
import ticwear.design.app.DatetimePickerDialog;
import ticwear.design.app.NumberPickerDialog;
import ticwear.design.utils.WindowUtils;

/**
 * Created by tankery on 1/12/16.
 *
 * fragment for dialogs
 */
public class DialogsFragment extends ListFragment {

    static final String TAG = "TicDialogs";

    private int[] standardDialogIds;
    private int[] valuePickerIds;
    private int[] listChoiceIds;
    private CharSequence[] standardDialogs;
    private CharSequence[] valuePickers;
    private CharSequence[] listChoices;

    @Override
    protected int[] getItemTitles() {
        return new int[]{
                R.string.category_dialog_standard,
                R.string.category_dialog_value_picker,
                R.string.category_dialog_choice,
                R.string.category_dialog_text_only,
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        standardDialogIds = new int[] {
                R.string.category_dialog_no_title,
                R.string.category_dialog_confirm,
                R.string.category_dialog_choose,
                R.string.category_dialog_delay_confirm,
                R.string.category_dialog_long,
        };
        valuePickerIds = new int[] {
                R.string.category_dialog_number_picker,
                R.string.category_dialog_time_picker,
                R.string.category_dialog_date_picker,
                R.string.category_dialog_datetime_picker,
        };
        listChoiceIds = new int[] {
                R.string.category_dialog_single_selection,
                R.string.category_dialog_single_choice,
                R.string.category_dialog_multiple_choice,
        };

        standardDialogs = new CharSequence[standardDialogIds.length];
        for (int i = 0; i < standardDialogIds.length; i++) {
            standardDialogs[i] = getResources().getString(standardDialogIds[i]);
        }
        valuePickers = new CharSequence[valuePickerIds.length];
        for (int i = 0; i < valuePickerIds.length; i++) {
            valuePickers[i] = getResources().getString(valuePickerIds[i]);
        }
        listChoices = new CharSequence[listChoiceIds.length];
        for (int i = 0; i < listChoiceIds.length; i++) {
            listChoices[i] = getResources().getString(listChoiceIds[i]);
        }
    }

    @Override
    public void onTitleClicked(View view, @StringRes int titleResId) {
        Dialog dialog = createDialog(view.getContext(), titleResId);
        showDialogIfNeed(dialog);
    }

    private Dialog createStandardDialog(final Context context, @StringRes int resId) {
        Dialog dialog = null;
        switch (resId) {
            case R.string.category_dialog_no_title:
                dialog = new AlertDialog.Builder(context, R.style.Theme_Ticwear_Dialog_Alert_SameButtonStyle)
                        .setMessage(R.string.text_short_content)
                        .setPositiveButtonIcon(ticwear.design.R.drawable.tic_ic_btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButtonIcon(ticwear.design.R.drawable.tic_ic_btn_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                break;
            case R.string.category_dialog_confirm:
                dialog = new AlertDialog.Builder(context)
                        .setIcon(R.drawable.ic_reset)
                        .setMessage(R.string.text_short_content)
                        .setPositiveButtonIcon(ticwear.design.R.drawable.tic_ic_btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                break;
            case R.string.category_dialog_choose:
                dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.category_dialog_confirm)
                        .setMessage(R.string.text_short_content)
                        .setPositiveButtonIcon(ticwear.design.R.drawable.tic_ic_btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButtonIcon(ticwear.design.R.drawable.tic_ic_btn_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                break;
            case R.string.category_dialog_delay_confirm:
                dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.category_dialog_delay_confirm)
                        .setMessage(R.string.text_dialog_delay_confirm)
                        .setPositiveButtonIcon(ticwear.design.R.drawable.tic_ic_btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Toast.makeText(context, "Positive clicked", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButtonIcon(ticwear.design.R.drawable.tic_ic_btn_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Toast.makeText(context, "Negative clicked", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setDelayConfirmAction(DialogInterface.BUTTON_POSITIVE, 5000)
                        .create();
                break;
            case R.string.category_dialog_long:
                dialog = new AlertDialog.Builder(context, R.style.Theme_Ticwear_Dialog_Alert_Compact)
                        .setTitle(R.string.category_dialog_choose)
                        .setMessage(R.string.text_long_content)
                        .setPositiveButtonIcon(ticwear.design.R.drawable.tic_ic_btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButtonIcon(ticwear.design.R.drawable.tic_ic_btn_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                break;
        }

        return dialog;
    }

    private Dialog createValuePickerDialog(Context context, @StringRes int resId) {
        Dialog dialog = null;
        switch (resId) {
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
                dialog = new DatetimePickerDialog.Builder(context)
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
                dialog = new DatetimePickerDialog.Builder(context)
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
                dialog = new DatetimePickerDialog.Builder(context)
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

    private Dialog createListChoiceDialog(Context context, @StringRes int resId) {
        Dialog dialog = null;
        final String[] listItems = Cheeses.getRandomCheesesList();
        switch (resId) {
            case R.string.category_dialog_single_selection:
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.category_dialog_single_selection)
                        .setItems(listItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getActivity(), "Picked: " + listItems[which],
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .create();
                break;
            case R.string.category_dialog_single_choice: {
                class SelectionHolder {
                    public int which;
                }
                final SelectionHolder selectionHolder = new SelectionHolder();
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.category_dialog_single_choice)
                        .setSingleChoiceItems(listItems, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectionHolder.which = which;
                            }
                        })
                        .setPositiveButtonIcon(ticwear.design.R.drawable.tic_ic_btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                Toast.makeText(getActivity(), "Picked: " + listItems[selectionHolder.which],
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .create();
                break;
            }
            case R.string.category_dialog_multiple_choice: {
                final List<Integer> selection = new ArrayList<>();
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.category_dialog_multiple_choice)
                        .setMultiChoiceItems(listItems, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    selection.add(which);
                                } else {
                                    selection.remove((Integer) which);
                                }
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                String message = "Picked item:\n";
                                for (int which : selection) {
                                    message += listItems[which] + ";\n";
                                }
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
                break;
            }
        }

        return dialog;
    }

    private Dialog createDialog(final Context context, int resId) {
        Dialog dialog = null;
        switch (resId) {
            case R.string.category_dialog_standard: {
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.category_dialog_standard)
                        .setSingleChoiceItems(standardDialogs, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dialog dlg = createStandardDialog(getActivity(), standardDialogIds[which]);
                                showDialogIfNeed(dlg);
                            }
                        })
                        .create();
                break;
            }
            case R.string.category_dialog_value_picker: {
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.category_dialog_value_picker)
                        .setSingleChoiceItems(valuePickers, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dialog dlg = createValuePickerDialog(getActivity(), valuePickerIds[which]);
                                showDialogIfNeed(dlg);
                            }
                        })
                        .create();
                break;
            }
            case R.string.category_dialog_choice: {
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.category_dialog_choice)
                        .setSingleChoiceItems(listChoices, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dialog dlg = createListChoiceDialog(getActivity(), listChoiceIds[which]);
                                showDialogIfNeed(dlg);
                            }
                        })
                        .create();
                break;
            }
            case R.string.category_dialog_text_only: {
                dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_simple_scroll_view);
            }
        }

        return dialog;
    }

    private static void showDialogIfNeed(final Dialog dlg) {
        if (dlg != null) {
            WindowUtils.clipToScreenShape(dlg.getWindow());
            dlg.show();
        }
    }

}

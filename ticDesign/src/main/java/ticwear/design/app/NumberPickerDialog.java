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

package ticwear.design.app;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import ticwear.design.R;
import ticwear.design.widget.FloatingActionButton;
import ticwear.design.widget.NumberPicker;

/**
 * Simple dialog for {@link NumberPicker}.
 *
 * Created by tankery on 3/7/16.
 */
public class NumberPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {

    public interface OnValuePickedListener {
        void onValuePicked(NumberPickerDialog dialog, int value);
    }

    public interface OnValuePickCancelListener {
        void onValuePickCancelled(NumberPickerDialog dialog);
    }

    private NumberPicker numberPicker;
    private OnValuePickedListener onValuePickedListener;
    private OnValuePickCancelListener onValuePickCancelListener;
    private FloatingActionButton buttonPositive;

    public NumberPickerDialog(Context context, @StyleRes int themeResId, CharSequence title,
                              OnValuePickedListener onValuePickedListener,
                              OnValuePickCancelListener onValuePickCancelListener,
                              int minValue, int maxValue, int defaultValue,
                              String[] displayedValues) {
        super(context, resolveDialogTheme(context, themeResId));

        // Use getContext to use wrapper context.
        context = getContext();

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_number_picker, null);
        setView(view);

        numberPicker = (NumberPicker) view.findViewById(R.id.tic_numberPicker);
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);
        if (defaultValue != Integer.MIN_VALUE) {
            numberPicker.setValue(defaultValue);
        }
        numberPicker.setDisplayedValues(displayedValues);

        this.onValuePickedListener = onValuePickedListener;
        this.onValuePickCancelListener = onValuePickCancelListener;

        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        setButton(BUTTON_POSITIVE, getContext().getDrawable(R.drawable.tic_ic_btn_ok), this);
    }

    @StyleRes
    static int resolveDialogTheme(Context context, @StyleRes int resId) {
        if (resId == 0) {
            final TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.datePickerDialogTheme, outValue, true);
            return outValue.resourceId;
        } else {
            return resId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buttonPositive = (FloatingActionButton) getWindow().findViewById(R.id.iconButton1);

        numberPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker view, @ScrollState int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    showButtonsDelayed();
                } else {
                    minimizeButtons();
                }
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == BUTTON_POSITIVE) {
            if (onValuePickedListener != null) {
                onValuePickedListener.onValuePicked(this, numberPicker.getValue());
            }
        } else {
            if (onValuePickCancelListener != null) {
                onValuePickCancelListener.onValuePickCancelled(this);
            }
        }
    }

    public NumberPicker getNumberPicker() {
        return numberPicker;
    }

    public static class Builder {

        private Context context;
        private int themeResId;

        private CharSequence title;

        private OnValuePickedListener onValuePickedListener;
        private OnValuePickCancelListener onValuePickCancelListener;

        private int minValue;
        private int maxValue;
        private int defaultValue;

        String[] displayedValues;

        public Builder(Context context) {
            this.context = context;
            this.themeResId = 0;
            this.minValue = 0;
            this.maxValue = 0;
            this.defaultValue = Integer.MIN_VALUE;
        }

        public Builder valuePickedlistener(OnValuePickedListener onValuePickedListener) {
            this.onValuePickedListener = onValuePickedListener;
            return this;
        }

        public Builder pickCancellistener(OnValuePickCancelListener onValuePickCancelListener) {
            this.onValuePickCancelListener = onValuePickCancelListener;
            return this;
        }

        public Builder theme(@StyleRes int resId) {
            this.themeResId = resolveDialogTheme(context, resId);
            return this;
        }

        public Builder title(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder minValue(int value) {
            this.minValue = value;
            return this;
        }

        public Builder maxValue(int value) {
            this.maxValue = value;
            return this;
        }

        public Builder defaultValue(int value) {
            this.defaultValue = value;
            return this;
        }

        public Builder displayedValues(String[] displayedValues) {
            this.displayedValues = displayedValues;
            return this;
        }

        public NumberPickerDialog create() {
            return new NumberPickerDialog(context, themeResId, title,
                    onValuePickedListener, onValuePickCancelListener,
                    minValue, maxValue, defaultValue, displayedValues);
        }

        public NumberPickerDialog show() {
            final NumberPickerDialog dialog = create();
            dialog.show();
            return dialog;
        }

        static int resolveDialogTheme(Context context, int resId) {
            if (resId == 0) {
                final TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.alertDialogTheme, outValue, true);
                return outValue.resourceId;
            } else {
                return resId;
            }
        }

    }

}

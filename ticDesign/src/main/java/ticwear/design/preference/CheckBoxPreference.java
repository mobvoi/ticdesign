/*
 * Copyright (C) 2016 Mobvoi Inc.
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ticwear.design.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Checkable;

import ticwear.design.R;

/**
 * A {@link Preference} that provides checkbox widget
 * functionality.
 * <p>
 * This preference will store a boolean into the SharedPreferences.
 */
public class CheckBoxPreference extends TwoStatePreference {

    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CheckBoxPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CheckBoxPreference, defStyleAttr, defStyleRes);
        setSummaryOn(a.getString(R.styleable.CheckBoxPreference_android_summaryOn));
        setSummaryOff(a.getString(R.styleable.CheckBoxPreference_android_summaryOff));
        setDisableDependentsState(a.getBoolean(
                R.styleable.CheckBoxPreference_android_disableDependentsState, false));
        a.recycle();

        mViewHolderCreator = new ViewHolderCreator() {
            @Override
            public Preference.ViewHolder create(@NonNull ViewGroup parent,
                                                @LayoutRes int layoutResId,
                                                @LayoutRes int widgetLayoutResId) {
                return new ViewHolder(parent, layoutResId, widgetLayoutResId);
            }
        };
    }

    public CheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkBoxPreferenceStyle);
    }

    public CheckBoxPreference(Context context) {
        this(context, null);
    }

    static class ViewHolder extends TwoStatePreference.ViewHolder {

        protected Checkable checkboxView;

        public ViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId,
                          @LayoutRes int widgetLayoutResId) {
            super(parent, layoutResId, widgetLayoutResId);
            checkboxView = findViewById(android.R.id.checkbox);
        }

        @Override
        @CallSuper
        public void bind(@NonNull PreferenceViewHolder.PreferenceData preferenceData) {
            super.bind(preferenceData);
            if (checkboxView != null) {
                checkboxView.setChecked(((PreferenceData) preferenceData).isChecked);
            }
        }

    }
}

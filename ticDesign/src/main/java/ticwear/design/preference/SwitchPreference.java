/*
 * Copyright (C) 2016 Mobvoi Inc.
 * Copyright (C) 2010 The Android Open Source Project
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
import android.widget.CompoundButton;
import android.widget.Switch;

import ticwear.design.R;

/**
 * A {@link Preference} that provides a two-state toggleable option.
 * <p>
 * This preference will store a boolean into the SharedPreferences.
 */
public class SwitchPreference extends TwoStatePreference {
    private final Listener mListener = new Listener();

    // Switch text for on and off states
    private CharSequence mSwitchOn;
    private CharSequence mSwitchOff;

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }

            SwitchPreference.this.setChecked(isChecked);
        }
    }

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     * @param defStyleAttr An attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     * @param defStyleRes A resource identifier of a style resource that
     *        supplies default values for the view, used only if
     *        defStyleAttr is 0 or can not be found in the theme. Can be 0
     *        to not look for defaults.
     */
    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SwitchPreference, defStyleAttr, defStyleRes);
        setSummaryOn(a.getString(R.styleable.SwitchPreference_android_summaryOn));
        setSummaryOff(a.getString(R.styleable.SwitchPreference_android_summaryOff));
        setSwitchTextOn(a.getString(
                R.styleable.SwitchPreference_android_switchTextOn));
        setSwitchTextOff(a.getString(
                R.styleable.SwitchPreference_android_switchTextOff));
        setDisableDependentsState(a.getBoolean(
                R.styleable.SwitchPreference_android_disableDependentsState, false));
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

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     * @param defStyleAttr An attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     */
    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     */
    public SwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.switchPreferenceStyle);
    }

    /**
     * Construct a new SwitchPreference with default style options.
     *
     * @param context The Context that will style this preference
     */
    public SwitchPreference(Context context) {
        this(context, null);
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param onText Text to display in the on state
     */
    public void setSwitchTextOn(CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param offText Text to display in the off state
     */
    public void setSwitchTextOff(CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }

    /**
     * @return The text that will be displayed on the switch widget in the on state
     */
    public CharSequence getSwitchTextOn() {
        return mSwitchOn;
    }

    /**
     * @return The text that will be displayed on the switch widget in the off state
     */
    public CharSequence getSwitchTextOff() {
        return mSwitchOff;
    }

    static class ViewHolder extends TwoStatePreference.ViewHolder {

        protected Checkable checkableView;

        public ViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId,
                          @LayoutRes int widgetLayoutResId) {
            super(parent, layoutResId, widgetLayoutResId, new PreferenceData());
            checkableView = findViewById(R.id.switchWidget);
        }

        @Override
        @CallSuper
        public void bindPreferenceToData(@NonNull Preference preference) {
            super.bindPreferenceToData(preference);

            PreferenceData myData = (PreferenceData) data;
            SwitchPreference switchPreference = (SwitchPreference) preference;
            myData.switchOn = switchPreference.mSwitchOn;
            myData.switchOff = switchPreference.mSwitchOff;
            myData.listener = switchPreference.mListener;
        }

        @Override
        @CallSuper
        public void bind(@NonNull PreferenceViewHolder.PreferenceData preferenceData) {
            super.bind(preferenceData);
            if (checkableView != null) {
                if (checkableView instanceof CompoundButton) {
                    final CompoundButton switchView = (CompoundButton) checkableView;
                    switchView.setOnCheckedChangeListener(null);
                }

                PreferenceData myData = (PreferenceData) preferenceData;

                checkableView.setChecked(myData.isChecked);

                if (checkableView instanceof Switch) {
                    final Switch switchView = (Switch) checkableView;
                    switchView.setTextOn(myData.switchOn);
                    switchView.setTextOff(myData.switchOff);
                }

                if (checkableView instanceof CompoundButton) {
                    final CompoundButton switchView = (CompoundButton) checkableView;
                    switchView.setOnCheckedChangeListener(myData.listener);
                }
            }
        }

        static class PreferenceData extends TwoStatePreference.ViewHolder.PreferenceData {

            Listener listener;

            // Switch text for on and off states
            CharSequence switchOn;
            CharSequence switchOff;
        }

    }
}

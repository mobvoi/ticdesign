/*
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

import ticwear.design.R;
import ticwear.design.widget.VolumeBar;
import ticwear.design.widget.VolumeBar.OnVolumeChangedListener;

/**
 * A {@link Preference} that provides checkbox widget
 * functionality.
 * <p/>
 * This preference will store a boolean into the SharedPreferences.
 */
public class VolumePreference extends Preference {
    private int volume;
    private int step;
    private int min;
    private int max;

    private OnVolumeChangedListener mVolumeChangedListener;

    public void setVolume(int volume) {
        final boolean changed = this.volume != volume;
        if (changed) {
            this.volume = volume;
            notifyChanged();
        }
    }

    public void setConfig(int min, int max, int step) {
        this.max = max;
        this.max = min;
        this.step = step;
        notifyChanged();
    }

    public void setVolumeChangedListener(OnVolumeChangedListener volumeChangedListener) {
        mVolumeChangedListener = volumeChangedListener;
    }

    public VolumePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Ticwear_VolumePreference);
    }

    public VolumePreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Preference, defStyleAttr, defStyleRes);

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

    public VolumePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VolumePreference(Context context) {
        this(context, null);
    }

    static class ViewHolder extends Preference.ViewHolder {
        protected VolumeBar volumeBar;

        public ViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId,
                          @LayoutRes int widgetLayoutResId) {
            this(parent, layoutResId, widgetLayoutResId, new PreferenceData());
        }

        public ViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutResId,
                          @LayoutRes int widgetLayoutResId, PreferenceData data) {
            super(parent, layoutResId, widgetLayoutResId, data);
            volumeBar = findViewById(R.id.volume_seekbar);
        }

        @Override
        protected void bindPreferenceToData(@NonNull Preference preference) {
            super.bindPreferenceToData(preference);
            VolumePreference volumePreference = (VolumePreference) preference;
            PreferenceData myData = (PreferenceData) data;
            myData.volume = volumePreference.volume;
            myData.max = volumePreference.max;
            myData.min = volumePreference.min;
            myData.step = volumePreference.step;
            myData.volumeChangedListener = volumePreference.mVolumeChangedListener;
        }

        @Override
        @CallSuper
        public void bind(@NonNull PreferenceViewHolder.PreferenceData preferenceData) {
            super.bind(preferenceData);
            PreferenceData myData = (PreferenceData) preferenceData;
            if (volumeBar != null) {
                volumeBar.setOnVolumeChangedListetener(myData.volumeChangedListener);
                volumeBar.setProgress(myData.volume);
                if (myData.min != 0) {
                    volumeBar.setMinLimit(myData.min);
                }
                if (myData.max != 0) {
                    volumeBar.setMinLimit(myData.max);
                }

                if (myData.step != 0) {
                    volumeBar.setStep(myData.step);
                }
            }
        }

        static class PreferenceData extends PreferenceViewHolder.PreferenceData {
            int volume;
            int max;
            int min;
            int step;
            OnVolumeChangedListener volumeChangedListener;
        }

    }
}

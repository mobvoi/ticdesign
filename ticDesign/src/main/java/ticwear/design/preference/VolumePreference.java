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
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
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

    private OnVolumeChangedListener mInternalVolumeChangedListener;
    private OnVolumeChangedListener mVolumeChangeListener;


    public void setVolume(int volume) {
        final boolean changed = this.volume != volume;
        if (changed) {
            this.volume = volume;
            persistInt(this.volume);
            notifyChanged();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setVolume(getPersistedInt(0));
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }
        final SavedState myState = new SavedState(superState);
        myState.volume = volume;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setVolume(myState.volume);
    }

    static class SavedState extends BaseSavedState {
        int volume;

        public SavedState(Parcel source) {
            super(source);
            volume = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(volume);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }


    public void setVolumeChangedListener(OnVolumeChangedListener volumeChangedListener) {
        mVolumeChangeListener = volumeChangedListener;
    }

    public VolumePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Ticwear_VolumePreference);
        mInternalVolumeChangedListener = new OnVolumeChangedListener() {
            @Override
            public void onVolumeChanged(VolumeBar volumeBar, int progress, boolean fromUser) {
                volume = volumeBar.getProgress();
                if (fromUser) {
                    persistInt(volume);
                }
                setVolume(volumeBar.getProgress());
                if (mVolumeChangeListener != null) {
                    mVolumeChangeListener.onVolumeChanged(volumeBar, progress, fromUser);
                }
            }
        };
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
            myData.volumeChangedListener = volumePreference.mInternalVolumeChangedListener;
        }

        @Override
        @CallSuper
        public void bind(@NonNull PreferenceViewHolder.PreferenceData preferenceData) {
            super.bind(preferenceData);
            PreferenceData myData = (PreferenceData) preferenceData;
            if (volumeBar != null) {
                volumeBar.setOnVolumeChangedListetener(myData.volumeChangedListener);
                volumeBar.setProgress(myData.volume);
            }
        }

        protected static class PreferenceData extends PreferenceViewHolder.PreferenceData {
            protected int volume;
            protected OnVolumeChangedListener volumeChangedListener;
        }

    }
}

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

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An adapter that returns the {@link Preference} contained in this group.
 * In most cases, this adapter should be the base class for any custom
 * adapters
 *
 * @hide
 */
public class PreferenceGroupAdapter
        extends RecyclerView.Adapter<Preference.ViewHolder>
        implements Preference.OnPreferenceChangeInternalListener {

    private static final String TAG = "PreferenceGroupAdapter";

    private OnPreferenceItemClickListener mOnPreferenceItemClickListener;

    /**
     * The group that we are providing data from.
     */
    private PreferenceGroup mPreferenceGroup;

    /**
     * Maps a position into this adapter -> {@link Preference}. These
     * {@link Preference}s don't have to be direct children of this
     * {@link PreferenceGroup}, they can be grand children or younger)
     */
    private List<Preference> mPreferenceList;

    /**
     * List of unique Preference and its subclasses' names. This is used to find
     * out how many types of views this adapter can return. Once the count is
     * returned, this cannot be modified (since the ListView only checks the
     * count once--when the adapter is being set). We will not recycle views for
     * Preference subclasses seen after the count has been returned.
     */
    private ArrayList<PreferenceLayout> mPreferenceLayouts;

    private PreferenceLayout mTempPreferenceLayout = new PreferenceLayout();

    /**
     * Blocks the mPreferenceClassNames from being changed anymore.
     */
    private boolean mHasReturnedViewTypeCount = false;

    private volatile boolean mIsSyncing = false;

    private Handler mHandler = new Handler();

    private Runnable mSyncRunnable = new Runnable() {
        public void run() {
            syncMyPreferences();
        }
    };

    public void setOnPreferenceItemClickListener(OnPreferenceItemClickListener listener) {
        this.mOnPreferenceItemClickListener = listener;
    }

    private static class PreferenceLayout implements Comparable<PreferenceLayout> {
        private int resId;
        private int widgetResId;
        private String name;
        private Preference.ViewHolderCreator viewHolderCreator;

        public int compareTo(@NonNull PreferenceLayout other) {
            int compareNames = name.compareTo(other.name);
            if (compareNames == 0) {
                if (resId == other.resId) {
                    if (widgetResId == other.widgetResId) {
                        return 0;
                    } else {
                        return widgetResId - other.widgetResId;
                    }
                } else {
                    return resId - other.resId;
                }
            } else {
                return compareNames;
            }
        }
    }

    public PreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
        mPreferenceGroup = preferenceGroup;

        // TODO: bug that when PrefScreen open a dialog, listener Adapter for upper level children
        // (set in flattenPreferenceGroup) will be override by new listener for current level here.
        // To fix this, maybe we should use two listener (one for data change of children, another
        // for hierarchy change of pref-screen), and set hierarchy listener here, and data listener
        // in flattenPreferenceGroup.

        // If this group gets or loses any children, let us know
        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mPreferenceList = new ArrayList<>();
        mPreferenceLayouts = new ArrayList<>();

        syncMyPreferences();

        setHasStableIds(true);
    }

    private void syncMyPreferences() {
        synchronized (this) {
            if (mIsSyncing) {
                return;
            }

            mIsSyncing = true;
        }

        List<Preference> newPreferenceList = new ArrayList<Preference>(mPreferenceList.size());
        flattenPreferenceGroup(newPreferenceList, mPreferenceGroup);
        mPreferenceList = newPreferenceList;

        notifyDataSetChanged();

        synchronized (this) {
            mIsSyncing = false;
            notifyAll();
        }
    }

    private void flattenPreferenceGroup(List<Preference> preferences, PreferenceGroup group) {
        // TODO: shouldn't always?
        group.sortPreferences();

        final int groupSize = group.getPreferenceCount();
        for (int i = 0; i < groupSize; i++) {
            final Preference preference = group.getPreference(i);

            preferences.add(preference);

            if (!mHasReturnedViewTypeCount) {
                addPreferenceClassName(preference);
            }

            if (preference instanceof PreferenceGroup) {
                final PreferenceGroup preferenceAsGroup = (PreferenceGroup) preference;
                if (preferenceAsGroup.isOnSameScreenAsChildren()) {
                    flattenPreferenceGroup(preferences, preferenceAsGroup);
                }
            }

            preference.setOnPreferenceChangeInternalListener(this);
        }
    }

    /**
     * Creates a string that includes the preference name, layout id and widget layout id.
     * If a particular preference type uses 2 different resources, they will be treated as
     * different view types.
     */
    private PreferenceLayout createPreferenceLayout(Preference preference, PreferenceLayout in) {
        PreferenceLayout pl = in != null ? in : new PreferenceLayout();
        pl.name = preference.getClass().getName();
        pl.resId = preference.getLayoutResource();
        pl.widgetResId = preference.getWidgetLayoutResource();
        pl.viewHolderCreator = preference.mViewHolderCreator;
        return pl;
    }

    private void addPreferenceClassName(Preference preference) {
        final PreferenceLayout pl = createPreferenceLayout(preference, null);
        int insertPos = Collections.binarySearch(mPreferenceLayouts, pl);

        // Only insert if it doesn't exist (when it is negative).
        if (insertPos < 0) {
            // Convert to insert index
            insertPos = insertPos * -1 - 1;
            mPreferenceLayouts.add(insertPos, pl);
        }
    }

    @Override
    public int getItemCount() {
        return mPreferenceList.size();
    }

    public Preference getItem(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return mPreferenceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= getItemCount()) return ListView.INVALID_ROW_ID;
        return this.getItem(position).getId();
    }

    @Override
    public void onPreferenceChange(Preference preference) {
        notifyDataSetChanged();
    }

    @Override
    public void onPreferenceHierarchyChange(Preference preference) {
        mHandler.removeCallbacks(mSyncRunnable);
        mHandler.post(mSyncRunnable);
    }

    @Override
    public Preference.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType < 0 || viewType >= mPreferenceLayouts.size()) {
            throw new RuntimeException("viewType " + viewType + " should contains in synced layouts.");
        }
        PreferenceLayout preferenceLayout = mPreferenceLayouts.get(viewType);
        if (preferenceLayout.viewHolderCreator != null) {
            return preferenceLayout.viewHolderCreator.create(parent,
                    preferenceLayout.resId, preferenceLayout.widgetResId);
        } else {
            return new Preference.ViewHolder(parent,
                    preferenceLayout.resId, preferenceLayout.widgetResId);
        }
    }

    @Override
    public void onBindViewHolder(final Preference.ViewHolder holder, int position) {
        final Preference preference = this.getItem(position);
        holder.bindPreference(preference);
        if (preference.getShouldDisableView()) {
            holder.setEnabled(preference.isEnabled());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPreferenceItemClickListener != null) {
                    mOnPreferenceItemClickListener.onPreferenceItemClick(preference);
                }
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        if (!mHasReturnedViewTypeCount) {
            mHasReturnedViewTypeCount = true;
        }

        final Preference preference = this.getItem(position);

        mTempPreferenceLayout = createPreferenceLayout(preference, mTempPreferenceLayout);

        int viewType = Collections.binarySearch(mPreferenceLayouts, mTempPreferenceLayout);
        if (viewType < 0) {
            // This is a class that was seen after we returned the count, so
            // don't recycle it.
            return Adapter.IGNORE_ITEM_VIEW_TYPE;
        } else {
            return viewType;
        }
    }

    public interface OnPreferenceItemClickListener {
        void onPreferenceItemClick(@NonNull Preference preference);
    }

}

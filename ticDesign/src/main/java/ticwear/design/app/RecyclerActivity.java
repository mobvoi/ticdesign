/*
 * Copyright (C) 2016 Mobvoi Inc.
 * Copyright (C) 2006 The Android Open Source Project
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

package ticwear.design.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;

import ticwear.design.R;
import ticwear.design.widget.FocusableLinearLayoutManager;
import ticwear.design.widget.TicklableRecyclerView;

/**
 * An activity that displays a list of items by binding to a data source such as
 * an array or Cursor, and exposes event handlers when the user selects an item.
 * <p>
 * ListActivity hosts a {@link TicklableRecyclerView} object that can
 * be bound to different data sources, typically either an array or a Cursor
 * holding query results.
 */
public class RecyclerActivity extends Activity {
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected RecyclerView.Adapter mAdapter;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected RecyclerView mList;

    private Handler mHandler = new Handler();
    private boolean mFinishedStart = false;

    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };

    /**
     * Ensures the list view has been created before Activity restores all
     * of the view states.
     *
     *@see Activity#onRestoreInstanceState(Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    /**
     * @see Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mRequestFocus);
        super.onDestroy();
    }

    /**
     * Updates the screen state (current list and other views) when the
     * content changes.
     *
     * @see Activity#onContentChanged()
     */
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mList = (TicklableRecyclerView) findViewById(android.R.id.list);
        mList.setLayoutManager(new FocusableLinearLayoutManager(this));
        if (mList == null) {
            throw new RuntimeException(
                    "Your content must have a RecyclerView whose id attribute is " +
                    "'android.R.id.list'");
        }
        if (mFinishedStart) {
            setListAdapter(mAdapter);
        }
        mHandler.post(mRequestFocus);
        mFinishedStart = true;
    }

    /**
     * Provide the cursor for the list view.
     */
    public void setListAdapter(RecyclerView.Adapter adapter) {
        synchronized (this) {
            ensureList();
            mAdapter = adapter;
            mList.setAdapter(adapter);
        }
    }

    /**
     * Get the activity's list view widget.
     */
    public RecyclerView getListView() {
        ensureList();
        return mList;
    }

    /**
     * Get the ListAdapter associated with this activity's ListView.
     */
    public RecyclerView.Adapter getListAdapter() {
        return mAdapter;
    }

    private void ensureList() {
        if (mList != null) {
            return;
        }
        setContentView(R.layout.list_content_ticklable);

    }

}

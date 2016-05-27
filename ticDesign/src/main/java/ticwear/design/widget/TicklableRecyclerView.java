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

package ticwear.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.mobvoi.ticwear.view.SidePanelEventDispatcher;

import ticwear.design.R;

@TargetApi(20)
@CoordinatorLayout.DefaultBehavior(TicklableRecyclerViewBehavior.class)
public class TicklableRecyclerView extends RecyclerView
        implements SidePanelEventDispatcher {

    static final String TAG = "TicklableRV";

    /**
     * {@link LayoutManager} for focus state.
     */
    @Nullable
    private TicklableLayoutManager mTicklableLayoutManager;

    private boolean mSkipNestedScroll;

    public TicklableRecyclerView(Context context) {
        this(context, null);
    }

    public TicklableRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TicklableRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TicklableRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        setHasFixedSize(true);
        setOverScrollMode(OVER_SCROLL_NEVER);

        mSkipNestedScroll = false;

        if (!isInEditMode() && getItemAnimator() != null) {
            long defaultAnimDuration = context.getResources()
                    .getInteger(R.integer.design_anim_list_item_state_change);
            long itemAnimDuration = defaultAnimDuration / 4;
            getItemAnimator().setMoveDuration(itemAnimDuration);
        }
    }

    /**
     * Set a new adapter to provide child views on demand.
     *
     * @param adapter new adapter that should be instance of {@link TicklableRecyclerView.Adapter}
     */
    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (mTicklableLayoutManager == null || mTicklableLayoutManager.validAdapter(adapter)) {
            super.setAdapter(adapter);
        } else {
            throw new IllegalArgumentException("Adapter is invalid for current TicklableLayoutManager.");
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);

        if (isInEditMode()) {
            return;
        }

        if (mTicklableLayoutManager == layout) {
            return;
        }

        if (mTicklableLayoutManager != null) {
            mTicklableLayoutManager.setTicklableRecyclerView(null);
        }
        if (!(layout instanceof TicklableLayoutManager)) {
            Log.w(TAG, "To let TicklableRecyclerView support complex tickle events," +
                    " let LayoutManager implements TicklableLayoutManager.");
            mTicklableLayoutManager = null;
            return;
        }

        TicklableLayoutManager ticklableLayoutManager = (TicklableLayoutManager) layout;
        if (ticklableLayoutManager.validAdapter(getAdapter())) {
            mTicklableLayoutManager = (TicklableLayoutManager) layout;
            mTicklableLayoutManager.setTicklableRecyclerView(this);
        } else {
            Log.w(TAG, "To let TicklableRecyclerView support complex tickle events," +
                    " make sure your Adapter is compat with TicklableLayoutManager.");
            mTicklableLayoutManager = null;
        }
    }

    public boolean interceptPreScroll() {
        return mTicklableLayoutManager != null && mTicklableLayoutManager.interceptPreScroll();
    }

    public boolean useScrollAsOffset() {
        return mTicklableLayoutManager != null && mTicklableLayoutManager.useScrollAsOffset();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return (mTicklableLayoutManager != null && mTicklableLayoutManager.dispatchTouchEvent(ev)) ||
                super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchSidePanelEvent(MotionEvent ev, @NonNull SuperCallback superCallback) {
        if (mTicklableLayoutManager == null) {
            return dispatchTouchEvent(ev) || superCallback.superDispatchTouchSidePanelEvent(ev);
        }

        return mTicklableLayoutManager.dispatchTouchSidePanelEvent(ev) ||
                superCallback.superDispatchTouchSidePanelEvent(ev);

    }

    public int getScrollOffset() {
        return mTicklableLayoutManager != null ? mTicklableLayoutManager.getScrollOffset() : 0;
    }

    /**
     * Update offset to scroll.
     *
     * This will calculate the delta of previous offset and new offset, then apply it to scroll.
     *
     * @param scrollOffset new offset to scroll.
     *
     * @return the unconsumed offset.
     */
    public int updateScrollOffset(int scrollOffset) {
        return mTicklableLayoutManager != null ?
                mTicklableLayoutManager.updateScrollOffset(scrollOffset) : scrollOffset;
    }

    public void scrollBySkipNestedScroll(int x, int y) {
        mSkipNestedScroll = true;
        scrollBy(x, y);
        mSkipNestedScroll = false;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return !mSkipNestedScroll && super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return !mSkipNestedScroll && super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return !mSkipNestedScroll && super.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return !mSkipNestedScroll && super.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedPrePerformAccessibilityAction(int action, Bundle arguments) {
        return !mSkipNestedScroll && super.dispatchNestedPrePerformAccessibilityAction(action, arguments);
    }
}

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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Enhanced LayoutManager that support focused status.
 *
 * Created by tankery on 4/13/16.
 */
class FocusLayoutHelper {

    private final TicklableRecyclerView mTicklableRecyclerView;
    private final RecyclerView.LayoutManager mLayoutManager;

    private final GestureDetector mGestureDetector;


    FocusLayoutHelper(@NonNull TicklableRecyclerView ticklableRecyclerView, @NonNull RecyclerView.LayoutManager layoutManager) {

        this.mTicklableRecyclerView = ticklableRecyclerView;
        this.mLayoutManager = layoutManager;

        OnGestureListener mOnGestureListener = new OnGestureListener();
        mGestureDetector = new GestureDetector(ticklableRecyclerView.getContext(), mOnGestureListener);

    }

    public void destroy() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.clearFocus();
        }
    }

    public boolean dispatchTouchSidePanelEvent(MotionEvent ev) {
        mTicklableRecyclerView.onTouchEvent(ev);
        mGestureDetector.onTouchEvent(ev);
        return true;    // return true to skip event dispatch to children.
    }

    public boolean interceptPreScroll() {
        View firstChild = getChildAt(0);
        // first child is on top of list.
        return firstChild != null &&
                mTicklableRecyclerView.getChildAdapterPosition(firstChild) <= 0 &&
                firstChild.getTop() >= mTicklableRecyclerView.getPaddingTop();
    }

    int getVerticalPadding() {
        if (getChildCount() > 0) {
            int height = ViewPropertiesHelper.getAdjustedHeight(mTicklableRecyclerView);
            int itemHeight = getCentralItemHeight();
            return (height - itemHeight) / 2;
        } else {
            return 0;
        }
    }

    void onScrollStateChanged(int state) {
        if (getChildCount() > 0) {
            final View child = getChildAt(findCenterViewIndex());
            if (state == RecyclerView.SCROLL_STATE_IDLE) {
                child.requestFocus();
            } else {
                child.clearFocus();
            }
        }
    }

    int getCentralItemHeight() {
        if (getChildCount() > 0) {
            int index = findCenterViewIndex();
            View child = getChildAt(index);
            return child.getHeight();
        } else {
            return 0;
        }
    }

    /**
     * Find a view closest to center, return its index (relative the children views)
     *
     * @return children index of the central view.
     */
    int findCenterViewIndex() {
        int count = getChildCount();
        int index = RecyclerView.NO_POSITION;
        int closest = Integer.MAX_VALUE;
        int centerY = getCenterYPos();

        for (int i = 0; i < count; ++i) {
            View child = getChildAt(i);
            int childCenterY = mTicklableRecyclerView.getTop() + ViewPropertiesHelper.getCenterYPos(child);
            int distance = Math.abs(centerY - childCenterY);
            if (distance < closest) {
                closest = distance;
                index = i;
            }
        }

        if (index == RecyclerView.NO_POSITION) {
            throw new IllegalStateException("Can\'t find central view.");
        } else {
            return index;
        }
    }

    private int getCenterYPos() {
        return ViewPropertiesHelper.getCenterYPos(mTicklableRecyclerView);
    }

    private int getChildCount() {
        return mLayoutManager.getChildCount();
    }

    private View getChildAt(int index) {
        return mLayoutManager.getChildAt(index);
    }

    private class OnGestureListener extends SimpleOnGestureListener {

        private static final long SAFE_PRESS_DELAY = 60;

        private final Runnable mConfirmPressRunnable = new Runnable() {
            @Override
            public void run() {
                confirmPress();
            }
        };

        private boolean mPressConfirmed;
        private View mTargetView;
        private float mHotspotX;
        private float mHotspotY;

        @Override
        public boolean onDown(MotionEvent e) {
            mPressConfirmed = false;
            if (getChildCount() > 0) {
                int centerIndex = findCenterViewIndex();
                View child = getChildAt(centerIndex);
                mHotspotX = e.getX() - child.getX();
                mHotspotY = e.getY() - child.getY();
                mTargetView = child;
                // The default show press delay is too quick, so we use our own delay duration.
                mTargetView.postDelayed(mConfirmPressRunnable, SAFE_PRESS_DELAY);
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            cancelPressIfNeed();

            // By scroll another time, we got a multiplier scroll speed.
            if (mLayoutManager.canScrollVertically()) {
                int dx = Math.round(distanceX);
                int dy = Math.round(distanceY);
                mTicklableRecyclerView.scrollBySkipNestedScroll(dx, dy);
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            cancelPressIfNeed();
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            stopPressConfirm(mTargetView);
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mTargetView != null && mPressConfirmed) {
                mTargetView.performClick();
            }
            cancelPressIfNeed();
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            cancelPressIfNeed();
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            cancelPressIfNeed();
        }

        private void confirmPress() {
            if (mTargetView != null) {
                mPressConfirmed = true;
                startPressIfPossible(mTargetView, mHotspotX, mHotspotY);
            }
        }

        private void startPressIfPossible(View view, float x, float y) {
            stopPressConfirm(view);
            if (view != null) {
                view.drawableHotspotChanged(x, y);
                view.setPressed(true);
            }
        }

        private void cancelPressIfNeed() {
            stopPressConfirm(mTargetView);
            if (mTargetView != null) {
                mTargetView.setPressed(false);
                mTargetView = null;
                mPressConfirmed = false;
            }
        }

        private void stopPressConfirm(View view) {
            if (view != null) {
                view.removeCallbacks(mConfirmPressRunnable);
            }
        }

    }
}

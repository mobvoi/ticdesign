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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ScrollView;

/**
 * Created by tankery on 3/16/16.
 *
 * A ScrollView can set a {@link OnScrollListener}
 */
public class SubscribedScrollView extends ScrollView {

    /**
     * The last scroll state reported to clients through {@link OnScrollListener}.
     */
    private int mLastScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    private OnScrollListener mOnScrollListener;

    private final FlingChecker mFlingChecker = new FlingChecker();
    private boolean isInFling = false;

    public SubscribedScrollView(Context context) {
        super(context);
    }

    public SubscribedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubscribedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SubscribedScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(this, l, t, oldl, oldt);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                removeCallbacks(mFlingChecker);
                reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isInFling) {
                    removeCallbacks(mFlingChecker);
                    mFlingChecker.run();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void fling(int velocityY) {
        reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
        isInFling = true;
        mFlingChecker.run();
        super.fling(velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        boolean handled = super.onNestedFling(target, velocityX, velocityY, consumed);
        if (handled) {
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
        }
        return handled;
    }

    /**
     * Fires an "on scroll state changed" event to the registered
     * {@link android.widget.AbsListView.OnScrollListener}, if any. The state change
     * is fired only if the specified state is different from the previously known state.
     *
     * @param newState The new scroll state.
     */
    void reportScrollStateChange(int newState) {
        if (newState != mLastScrollState) {
            if (mOnScrollListener != null) {
                mLastScrollState = newState;
                mOnScrollListener.onScrollStateChanged(this, newState);
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when the scroll view
     * has been scrolled.
     *
     * @see android.widget.AbsListView.OnScrollListener
     */
    public interface OnScrollListener {

        int SCROLL_STATE_IDLE = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

        int SCROLL_STATE_TOUCH_SCROLL = AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

        int SCROLL_STATE_FLING = AbsListView.OnScrollListener.SCROLL_STATE_FLING;

        /**
         * Callback method to be invoked while the list view or grid view is being scrolled. If the
         * view is being scrolled, this method will be called before the next frame of the scroll is
         * rendered. In particular, it will be called before any calls to
         * {@link Adapter#getView(int, View, ViewGroup)}.
         *
         * @param view The view whose scroll state is being reported
         *
         * @param scrollState The current scroll state. One of
         * {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
         */
        void onScrollStateChanged(SubscribedScrollView view, int scrollState);

        /**
         * Callback method to be invoked when the list or grid has been scrolled. This will be
         * called after the scroll has completed
         *
         * @param view The view whose scroll state is being reported
         * @param l Current horizontal scroll origin.
         * @param t Current vertical scroll origin.
         * @param oldl Previous horizontal scroll origin.
         * @param oldt Previous vertical scroll origin.
         */
        void onScroll(SubscribedScrollView view, int l, int t, int oldl, int oldt);
    }

    private class FlingChecker implements Runnable {

        // in milliseconds, same as fling checker in AbsListView
        private static final int FLYWHEEL_TIMEOUT = 40;

        private int mPreviousPosition = Integer.MIN_VALUE;

        @Override
        public void run() {
            removeCallbacks(this);

            int position = getScrollY();
            // TODO: there is a chance to make new position equal to old, but still in fling.
            if (mPreviousPosition == position) {
                isInFling = false;
                reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            } else {
                mPreviousPosition = getScrollY();
                postDelayed(this, FLYWHEEL_TIMEOUT);
            }
        }
    }

}

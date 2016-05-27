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

import android.support.annotation.CallSuper;
import android.support.v4.view.ViewCompat;
import android.util.Log;

import ticwear.design.DesignConfig;

/**
 * A runnable for scrolling check when fling.
 *
 * Created by tankery on 5/8/16.
 */
class ScrollViewFlingChecker implements Runnable {

    static final String TAG = "SVFlingChecker";

    private static final long IDLE_SCROLL_DURATION = 600;
    private static final int SCROLL_AXES_ALL = ViewCompat.SCROLL_AXIS_HORIZONTAL | ViewCompat.SCROLL_AXIS_VERTICAL;

    private ViewScrollingStatusAccessor mScrollingViewAccessor;

    private long mLastCheckTime;
    private int mLastScrollX;
    private int mLastScrollY;
    private float mVelocityX;
    private float mVelocityY;

    private long mNoneScrollingTime;

    public ScrollViewFlingChecker(ViewScrollingStatusAccessor accessor) {
        mScrollingViewAccessor = accessor;
        reset();
    }

    @CallSuper
    public void reset() {
        mNoneScrollingTime = IDLE_SCROLL_DURATION;
        mLastCheckTime = 0;
        mLastScrollX = Integer.MAX_VALUE;
        mLastScrollY = Integer.MAX_VALUE;
        mVelocityX = 0;
        mVelocityY = 0;
    }

    public boolean isValid() {
        return mScrollingViewAccessor != null && mScrollingViewAccessor.isValid();
    }

    public boolean isStarted() {
        return mLastCheckTime > 0;
    }

    @Override
    public final void run() {
        runCheck();
    }

    /**
     * Check if still scrolling.
     */
    @CallSuper
    protected boolean runCheck() {
        if (!isValid()) {
            reset();
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long duration = mLastCheckTime > 0 ? currentTime - mLastCheckTime : 0;
        mLastCheckTime = currentTime;

        if (isStarted() && mLastScrollY != Integer.MAX_VALUE) {
            mVelocityY = (float) (mScrollingViewAccessor.computeVerticalScrollOffset() - mLastScrollY) * 1000 / duration;
        }
        if (isStarted() && mLastScrollX != Integer.MAX_VALUE) {
            mVelocityX = (float) (mScrollingViewAccessor.computeHorizontalScrollOffset() - mLastScrollX) * 1000 / duration;
        }

        if (DesignConfig.DEBUG_COORDINATOR) {
            Log.v(TAG, "runCheck, current " + mScrollingViewAccessor.computeVerticalScrollOffset() +
                    ", last " + mLastScrollY + ", duration " + duration +
                    ", velocity " + mVelocityY + ", view " + mScrollingViewAccessor);
        }

        boolean scrollingFinished = scrollingFinished(SCROLL_AXES_ALL);
        if (scrollingFinished && (mNoneScrollingTime -= duration) > 0) {
            reset();
            return false;
        }

        return true;
    }

    private boolean scrollingFinished(int scrollAxes) {
        if ((scrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0) {
            int scrollY = mScrollingViewAccessor.computeVerticalScrollOffset();
            if (mLastScrollY == Integer.MAX_VALUE || scrollY != mLastScrollY) {
                mLastScrollY = scrollY;
                return false;
            }
        }

        if ((scrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0) {
            int scrollX = mScrollingViewAccessor.computeHorizontalScrollOffset();
            if (mLastScrollX == Integer.MAX_VALUE || scrollX != mLastScrollX) {
                mLastScrollX = scrollX;
                return false;
            }
        }

        return true;
    }

    public float getVelocityX() {
        return mVelocityX;
    }

    public float getVelocityY() {
        return mVelocityY;
    }
}

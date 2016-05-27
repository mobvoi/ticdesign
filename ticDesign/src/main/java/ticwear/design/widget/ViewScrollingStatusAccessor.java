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

import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;

import ticwear.design.DesignConfig;

/**
 * Access view's computeVerticalScrollXXX() use reflection.
 *
 * Created by tankery on 5/7/16.
 */
public class ViewScrollingStatusAccessor {

    static final String TAG = "ViewScrollSA";

    private View mScrollingView;
    private View mFailedAccessView;

    private static final int INDEX_VERTICAL_RANGE = 0;
    private static final int INDEX_VERTICAL_OFFSET = 1;
    private static final int INDEX_VERTICAL_EXTENT = 2;
    private static final int INDEX_HORIZONTAL_RANGE = 3;
    private static final int INDEX_HORIZONTAL_OFFSET = 4;
    private static final int INDEX_HORIZONTAL_EXTENT = 5;
    private static final int COMPUTE_METHOD_COUNT = 6;

    private final static String CLASS_NAME_VIEW = "android.view.View";
    private final static String[] METHOD_NAMES = {
            "computeVerticalScrollRange",
            "computeVerticalScrollOffset",
            "computeVerticalScrollExtent",
            "computeHorizontalScrollRange",
            "computeHorizontalScrollOffset",
            "computeHorizontalScrollExtent",
    };
    private final Method[] mComputeScrollMethods = new Method[COMPUTE_METHOD_COUNT];

    public ViewScrollingStatusAccessor() {
    }

    public void attach(View target) {
        if (target == mScrollingView) {
            return;
        }

        mScrollingView = target;
        mFailedAccessView = null;
        ensureMethods();
    }

    public boolean isValid() {
        return ensureMethods();
    }

    public int computeVerticalScrollRange() {
        ensureMethods();
        return invoke(INDEX_VERTICAL_RANGE, mScrollingView.getHeight());
    }

    public int computeVerticalScrollOffset() {
        ensureMethods();
        return invoke(INDEX_VERTICAL_OFFSET, mScrollingView.getScrollY());
    }

    public int computeVerticalScrollExtent() {
        ensureMethods();
        return invoke(INDEX_VERTICAL_EXTENT, mScrollingView.getHeight());
    }

    public int computeHorizontalScrollRange() {
        ensureMethods();
        return invoke(INDEX_HORIZONTAL_RANGE, mScrollingView.getWidth());
    }

    public int computeHorizontalScrollOffset() {
        ensureMethods();
        return invoke(INDEX_HORIZONTAL_OFFSET, mScrollingView.getScrollX());
    }

    public int computeHorizontalScrollExtent() {
        ensureMethods();
        return invoke(INDEX_HORIZONTAL_EXTENT, mScrollingView.getWidth());
    }

    private boolean ensureMethods() {
        if (mScrollingView == null) {
            return false;
        }

        if (!hasEmptyMethod()) {
            return true;
        }

        if (mScrollingView == mFailedAccessView) {
            return false;
        }

        try {
            Class<?> viewClass = Class.forName(CLASS_NAME_VIEW);

            for (int i = 0; i < mComputeScrollMethods.length; i++) {
                mComputeScrollMethods[i] = viewClass.getDeclaredMethod(METHOD_NAMES[i]);
                mComputeScrollMethods[i].setAccessible(true);
            }

            return true;

        } catch (Exception ex) {
            if (DesignConfig.DEBUG) {
                Log.w(TAG, "Failed to access methods for view", ex);
            }

            mFailedAccessView = mScrollingView;

            return false;
        }

    }

    private boolean hasEmptyMethod() {
        boolean empty = false;
        for (Method mComputeScrollMethod : mComputeScrollMethods) {
            if (mComputeScrollMethod == null) {
                empty = true;
            }
        }
        return empty;
    }

    private int invoke(int index, int fallback) {
        Method method = mComputeScrollMethods[index];

        if (method == null) {
            return fallback;
        }

        try {
            return (Integer) method.invoke(mScrollingView);
        } catch (Throwable ex) {
            return fallback;
        }
    }

}

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

import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Use nested scroll to control the appbar's visibility.
 *
 * Created by tankery on 4/23/16.
 */
class AppBarScrollController {

    private final int[] mScrollOffsets = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private boolean mAppBarChanging = false;

    private View mScrollingView;

    AppBarScrollController(View scrollingView) {
        mScrollingView = scrollingView;
    }

    void hideAppBar() {
        mAppBarChanging = true;
        mScrollingView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        mScrollingView.dispatchNestedPreScroll(0, mScrollingView.getHeight(), mScrollConsumed, mScrollOffsets);
//        mScrollingView.dispatchNestedScroll(0, getHeight() - mScrollConsumed[1], 0, 0, mScrollOffsets);
        mScrollingView.stopNestedScroll();
        mAppBarChanging = false;
    }

    void showAppBar() {
        mAppBarChanging = true;
        mScrollingView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        mScrollingView.dispatchNestedScroll(0, 0, -mScrollConsumed[0], -mScrollConsumed[1], mScrollOffsets);
        mScrollingView.stopNestedScroll();
        mAppBarChanging = false;
    }

    boolean isAppBarChanging() {
        return mAppBarChanging;
    }

}

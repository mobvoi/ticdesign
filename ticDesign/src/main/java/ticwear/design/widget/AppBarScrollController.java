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

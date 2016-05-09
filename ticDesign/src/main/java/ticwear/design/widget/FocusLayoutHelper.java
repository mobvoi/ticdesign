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

    private final TicklableListView mTicklableListView;
    private final RecyclerView.LayoutManager mLayoutManager;

    private final GestureDetector mGestureDetector;


    FocusLayoutHelper(@NonNull TicklableListView ticklableListView, @NonNull RecyclerView.LayoutManager layoutManager) {

        this.mTicklableListView = ticklableListView;
        this.mLayoutManager = layoutManager;

        OnGestureListener mOnGestureListener = new OnGestureListener();
        mGestureDetector = new GestureDetector(ticklableListView.getContext(), mOnGestureListener);

    }

    public void destroy() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.clearFocus();
        }
    }

    public boolean dispatchTouchSidePanelEvent(MotionEvent ev) {
        mTicklableListView.onTouchEvent(ev);
        mGestureDetector.onTouchEvent(ev);
        return true;    // return true to skip event dispatch to children.
    }

    public boolean interceptPreScroll() {
        View firstChild = getChildAt(0);
        // first child is on top of list.
        return firstChild != null &&
                mTicklableListView.getChildAdapterPosition(firstChild) <= 0 &&
                firstChild.getTop() >= mTicklableListView.getPaddingTop();
    }

    int getVerticalPadding() {
        if (getChildCount() > 0) {
            int height = ViewPropertiesHelper.getAdjustedHeight(mTicklableListView);
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
            int childCenterY = mTicklableListView.getTop() + ViewPropertiesHelper.getCenterYPos(child);
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
        return ViewPropertiesHelper.getCenterYPos(mTicklableListView);
    }

    private int getChildCount() {
        return mLayoutManager.getChildCount();
    }

    private View getChildAt(int index) {
        return mLayoutManager.getChildAt(index);
    }

    private class OnGestureListener extends SimpleOnGestureListener {

        private static final long RIPPLE_SAFE_TO_SHOW_DELAY = 100;

        private final Runnable mShowRippleRunnable = new Runnable() {
            @Override
            public void run() {
                startPressIfPossible(mTargetView, mHotspotX, mHotspotY);
            }
        };

        private View mTargetView;
        private float mHotspotX;
        private float mHotspotY;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            cancelPressIfNeed();

            // By scroll another time, we got a multiplier scroll speed.
            if (mLayoutManager.canScrollVertically()) {
                int dx = Math.round(distanceX);
                int dy = Math.round(distanceY);
                mTicklableListView.scrollBySkipNestedScroll(dx, dy);
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            cancelPressIfNeed();
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            cancelPressIfNeed();
            if (getChildCount() > 0) {
                int centerIndex = findCenterViewIndex();
                View child = getChildAt(centerIndex);
                mHotspotX = e.getX() - child.getX();
                mHotspotY = e.getY() - child.getY();
                mTargetView = child;
                // The default show press delay is too quick, so we use our own delay duration.
                mTargetView.postDelayed(mShowRippleRunnable, RIPPLE_SAFE_TO_SHOW_DELAY);
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            startPressIfPossible(mTargetView, mHotspotX, mHotspotY);
            if (mTargetView != null) {
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

        private void startPressIfPossible(View view, float x, float y) {
            stopDelayShowPress(view);
            if (view != null) {
                view.drawableHotspotChanged(x, y);
                view.setPressed(true);
            }
        }

        private void cancelPressIfNeed() {
            stopDelayShowPress(mTargetView);
            if (mTargetView != null) {
                mTargetView.setPressed(false);
                mTargetView = null;
            }
        }

        private void stopDelayShowPress(View view) {
            if (view != null) {
                view.removeCallbacks(mShowRippleRunnable);
            }
        }

    }
}

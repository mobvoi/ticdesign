package ticwear.design.widget;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
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

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];


    FocusLayoutHelper(@NonNull TicklableListView ticklableListView, @NonNull RecyclerView.LayoutManager layoutManager) {

        this.mTicklableListView = ticklableListView;
        this.mLayoutManager = layoutManager;

        mGestureDetector = new GestureDetector(ticklableListView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (getChildCount() > 0) {
                    int centerIndex = findCenterViewIndex();
                    View child = getChildAt(centerIndex);
                    child.performClick();
                    float x = e.getX() - child.getX();
                    float y = e.getY() - child.getY();
                    forceRippleAnimation(child, x, y);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!mLayoutManager.canScrollVertically()) {
                    return false;
                }
                int dx = (int) distanceX;
                int dy = (int) distanceY;
                if (mTicklableListView.dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset)) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                }
                mTicklableListView.scrollBy(dx, dy);
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (!mLayoutManager.canScrollVertically()) {
                    return false;
                }
                mTicklableListView.fling((int) -velocityX, (int) -velocityY);
                return true;
            }

        });

    }

    private void forceRippleAnimation(View view, float x, float y)
    {
        Drawable background = view.getBackground();

        if(background instanceof RippleDrawable && Build.VERSION.SDK_INT >= 21)
        {
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setHotspot(x, y);
            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

            Handler handler = new Handler();

            handler.postDelayed(new Runnable()
            {
                @Override public void run()
                {
                    rippleDrawable.setState(new int[]{});
                }
            }, 200);
        }
    }

    public boolean dispatchTouchSidePanelEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mTicklableListView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            mTicklableListView.stopNestedScroll();
        }
        return mGestureDetector.onTouchEvent(ev);
    }

    public boolean interceptPreScroll() {
        View firstChild = getChildAt(0);
        // first child is on top of list.
        return firstChild != null &&
                mTicklableListView.getChildAdapterPosition(firstChild) <= 0 &&
                firstChild.getTop() >= mTicklableListView.getPaddingTop();
    }

    void animateToCenter() {
        int index = findCenterViewIndex();
        View child = getChildAt(index);
        int scrollToMiddle = getCenterYPos() - (child.getTop() + child.getBottom()) / 2;
        mTicklableListView.smoothScrollBy(0, -scrollToMiddle);
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
        if (state == RecyclerView.SCROLL_STATE_IDLE && getChildCount() > 0) {
            animateToCenter();
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
}

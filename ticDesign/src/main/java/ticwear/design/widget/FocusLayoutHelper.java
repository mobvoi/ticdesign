package ticwear.design.widget;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
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
    private final RecyclerView.OnItemTouchListener mOnItemTouchListener;


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
        });

        mOnItemTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                return child != null && mGestureDetector.onTouchEvent(e);
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        };
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

    void animateToCenter() {
        int index = findCenterViewIndex();
        View child = getChildAt(index);
        int scrollToMiddle = getCenterYPos() - (child.getTop() + child.getBottom()) / 2;
        mTicklableListView.smoothScrollBy(0, -scrollToMiddle);
    }


    void init() {
        mTicklableListView.addOnItemTouchListener(mOnItemTouchListener);
    }

    void destroy() {
        mTicklableListView.removeOnItemTouchListener(mOnItemTouchListener);
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
        int index = findCenterViewIndex();
        View child = getChildAt(index);
        return child.getHeight();
    }

    int findCenterViewIndex() {
        int index = mTicklableListView.findCenterViewIndex();
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

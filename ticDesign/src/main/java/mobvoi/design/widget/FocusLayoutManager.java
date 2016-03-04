package mobvoi.design.widget;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import hugo.weaving.DebugLog;

/**
 * Created by tankery on 2/17/16.
 *
 * Layout manager for ticklable list view.
 */
class FocusLayoutManager extends TicklableListView.TicklableLayoutManager {
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;

    /**
     * First visible item's adapter position.
     */
    private int mFirstPosition;
    private boolean mPushFirstHigher;
    private boolean mUseOldViewTop;

    private RecyclerView.SmoothScroller mSmoothScroller;
    private RecyclerView.SmoothScroller mDefaultSmoothScroller;

    FocusLayoutManager(TicklableListView ticklableListView) {
        super(ticklableListView);

        mUseOldViewTop = true;

        ViewConfiguration vc = ViewConfiguration.get(ticklableListView.getContext());
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
    }


    int getTopViewMaxTop() {
        return getCentralViewTop();
    }

    int getItemHeight() {
        return ViewPropertiesHelper.getAdjustedHeight(ticklableListView) / 3 + 1;
    }

    public int getCentralViewTop() {
        return getPaddingTop() + getItemHeight();
    }

    public void animateToCenter() {
        int index = findCenterViewIndex();
        View child = getChildAt(index);
        int scrollToMiddle = getCentralViewTop() - child.getTop();
        ticklableListView.smoothScrollBy(0, -scrollToMiddle);
    }


    int findCenterViewIndex() {
        int index = ticklableListView.findCenterViewIndex();
        if (index == RecyclerView.NO_POSITION) {
            throw new IllegalStateException("Can\'t find central view.");
        } else {
            return index;
        }
    }

    private void notifyChildrenAboutProximity(boolean animate) {
        ticklableListView.notifyChildrenAboutProximity(findCenterViewIndex(), animate);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int parentBottom = getHeight() - getPaddingBottom();
        int oldTop = getCentralViewTop();
        if (mUseOldViewTop && getChildCount() > 0) {
            int centerIndex = findCenterViewIndex();
            int centerPosition = getChildLayoutPosition(centerIndex);

            // Try to find a view with valid position. (by search from center to both sides.)
            if (centerPosition == RecyclerView.NO_POSITION) {
                for (int spread = 1; centerIndex + spread < getChildCount() || centerIndex - spread >= 0; ++spread) {
                    // search next index (bottom side)
                    int nextIndex = centerIndex + spread;
                    centerPosition = getChildLayoutPosition(nextIndex);
                    if (centerPosition != RecyclerView.NO_POSITION) {
                        centerIndex = nextIndex;
                        break;
                    }

                    // search prev index (up side)
                    int prevIndex = centerIndex - spread;
                    centerPosition = getChildLayoutPosition(prevIndex);
                    if (centerPosition != RecyclerView.NO_POSITION) {
                        centerIndex = prevIndex;
                        break;
                    }
                }
            }

            if (centerPosition == RecyclerView.NO_POSITION) {
                oldTop = getChildAt(0).getTop();

                mFirstPosition = Math.min(mFirstPosition, Math.max(state.getItemCount(), 1));
            } else {
                oldTop = getChildAt(centerIndex).getTop();

                int firstPosition = centerPosition;
                while (oldTop > getPaddingTop() && firstPosition > 0) {
                    --firstPosition;
                    oldTop -= getItemHeight();
                }

                if (firstPosition == 0 && oldTop > getCentralViewTop()) {
                    oldTop = getCentralViewTop();
                }

                mFirstPosition = firstPosition;
            }
        } else if (mPushFirstHigher) {
            oldTop = getCentralViewTop() - getItemHeight();
        }

        performLayoutChildren(recycler, state, parentBottom, oldTop);

        mUseOldViewTop = true;
        mPushFirstHigher = false;
    }

    private int getChildLayoutPosition(int index) {
        View view = getChildAt(index);
        return view == null ? RecyclerView.NO_POSITION : getPosition(view);
    }

    private void performLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state, int parentBottom, int top) {
        detachAndScrapAttachedViews(recycler);
        performLayoutMultipleChildren(recycler, state, parentBottom, top);

        if (getChildCount() > 0) {
            notifyChildrenAboutProximity(false);
        }
    }

    private void performLayoutMultipleChildren(RecyclerView.Recycler recycler, RecyclerView.State state, int parentBottom, int top) {
        int left = getPaddingLeft();
        int right = getWidth() - getPaddingRight();
        int count = state.getItemCount();

        int bottom;
        for (int i = 0; getFirstPosition() + i < count && top < parentBottom; top = bottom) {
            View v = recycler.getViewForPosition(getFirstPosition() + i);
            addView(v, i);
            measureThirdView(v);
            bottom = top + getItemHeight();
            v.layout(left, top, right, bottom);
            ++i;
        }

    }

    private void measureView(View v, int height) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) v.getLayoutParams();
        int widthSpec = getChildMeasureSpec(getWidth(), getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width, canScrollHorizontally());
        int heightSpec = getChildMeasureSpec(getHeight(), getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, height, canScrollVertically());
        v.measure(widthSpec, heightSpec);
    }

    private void measureThirdView(View v) {
        measureView(v, (int) (1.0F + (float) getHeight() / 3.0F));
    }

    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(-1, -2);
    }

    // TODO: Consider if we should always scroll vertically.
    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    @DebugLog
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }

        int scrolled = 0;
        if (dy < 0) {
            scrolled = scrollDownVerticallyBy(dy, recycler);
        } else if (dy > 0) {
            scrolled = scrollUpVerticallyBy(dy, recycler, state);
        }

        recycleViewsOutOfBounds(recycler);
        notifyChildrenAboutProximity(true);
        return scrolled;
    }

    private int scrollDownVerticallyBy(int dy, RecyclerView.Recycler recycler) {
        int scrolled = 0;
        int scrollBy;

        while (scrolled > dy) {
            View firstChild = getChildAt(0);
            int topViewMaxTop;
            int childTop = firstChild.getTop();
            if (getFirstPosition() <= 0) {
                mPushFirstHigher = false;
                topViewMaxTop = getTopViewMaxTop();
                scrollBy = Math.min(scrolled - dy, topViewMaxTop - childTop);
                scrolled -= scrollBy;
                offsetChildrenVertical(scrollBy);
                break;
            }

            topViewMaxTop = Math.max(-childTop, 0);
            scrollBy = Math.min(scrolled - dy, topViewMaxTop);
            scrolled -= scrollBy;
            offsetChildrenVertical(scrollBy);
            if (getFirstPosition() <= 0 || scrolled <= dy) {
                break;
            }

            --mFirstPosition;
            View v = recycler.getViewForPosition(getFirstPosition());
            addView(v, 0);
            measureThirdView(v);
            int top = childTop - getItemHeight();
            v.layout(getPaddingLeft(), top, getWidth() - getPaddingRight(), childTop);
        }

        return scrolled;
    }

    private int scrollUpVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrolled = 0;
        int scrollBy;

        while (scrolled < dy) {
            View lastChild = getChildAt(getChildCount() - 1);
            int childBottom = lastChild.getBottom();
            if (getLastPosition() >= state.getItemCount()) {
                scrollBy = Math.max(-dy + scrolled, getTopViewMaxTop() - childBottom);
                scrolled -= scrollBy;
                offsetChildrenVertical(scrollBy);
                break;
            }

            scrollBy = Math.max(childBottom - getHeight(), 0);
            int scrollBy1 = -Math.min(dy - scrolled, scrollBy);
            scrolled -= scrollBy1;
            offsetChildrenVertical(scrollBy1);
            if (scrolled >= dy) {
                break;
            }

            View v = recycler.getViewForPosition(getLastPosition());
            addView(v);
            measureThirdView(v);
            int bottom = childBottom + getItemHeight();
            v.layout(getPaddingLeft(), childBottom, getWidth() - getPaddingRight(), bottom);
        }
        return scrolled;
    }

    @Override
    public void scrollToPosition(int position) {
        mUseOldViewTop = false;
        if (position > 0) {
            mFirstPosition = position - 1;
            mPushFirstHigher = true;
        } else {
            mFirstPosition = position;
            mPushFirstHigher = false;
        }

        requestLayout();
    }

    public void setCustomSmoothScroller(RecyclerView.SmoothScroller smoothScroller) {
        mSmoothScroller = smoothScroller;
    }

    public void clearCustomSmoothScroller() {
        mSmoothScroller = null;
    }

    public RecyclerView.SmoothScroller getDefaultSmoothScroller(RecyclerView recyclerView) {
        if (mDefaultSmoothScroller == null) {
            mDefaultSmoothScroller = new SmoothScroller(recyclerView.getContext(), this);
        }

        return mDefaultSmoothScroller;
    }

    private RecyclerView.SmoothScroller getNonNullScroller(RecyclerView recyclerView) {
        RecyclerView.SmoothScroller scroller = mSmoothScroller;
        if (scroller == null) {
            scroller = getDefaultSmoothScroller(recyclerView);
        }
        return scroller;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller scroller = getNonNullScroller(recyclerView);

        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    @Override
    @DebugLog
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return computeScrollOffset(state);
    }

    private int computeScrollOffset(RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        View firstChild = getChildAt(0);
        int position = ticklableListView.getChildAdapterPosition(firstChild);
        return position * getItemHeight() - firstChild.getTop();
    }

    @Override
    public void onDataSetChanged(final TicklableListView ticklableListView) {
        ticklableListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ticklableListView.removeOnLayoutChangeListener(this);
                if (getChildCount() > 0) {
                    animateToCenter();
                }
            }
        });
    }

    @Override
    public boolean onFling(TicklableListView ticklableListView, int velocityY) {
        int index = findCenterViewIndex();
        int currentPosition = ticklableListView.getChildAdapterPosition(getChildAt(index));
        if ((currentPosition != 0 || velocityY >= 0) && (currentPosition != getItemCount() - 1 || velocityY <= 0)) {
            if (Math.abs(velocityY) < mMinFlingVelocity) {
                return false;
            }

            velocityY = Math.max(Math.min(velocityY, mMaxFlingVelocity), -mMaxFlingVelocity);

            Scroller scroller = new Scroller(ticklableListView.getContext(), null, true);
            scroller.fling(0, 0, 0, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            int finalY = scroller.getFinalY();

            int delta = finalY / getItemHeight();
            if (delta == 0) {
                delta = velocityY > 0 ? 1 : -1;
            }

            int finalPosition = Math.max(0, Math.min(getItemCount() - 1, currentPosition + delta));
            ticklableListView.smoothScrollToPosition(finalPosition);
            return true;
        }

        return false;
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            animateToCenter();
        }
    }

    private void recycleViewsOutOfBounds(RecyclerView.Recycler recycler) {
        int childCount = getChildCount();
        boolean foundFirst = false;
        int first = 0;
        int last = 0;

        // Find first/last visible child index
        for (int i = 0; i < childCount; ++i) {
            View v = getChildAt(i);
            if (v.hasFocus() || viewInParentRect(v)) {
                if (!foundFirst) {
                    first = i;
                    foundFirst = true;
                }

                last = i;
            }
        }

        // Recycle views out of bounds in bottom
        for (int i = childCount - 1; i > last; --i) {
            removeAndRecycleViewAt(i, recycler);
        }

        // Recycle views out of bounds in top
        for (int i = first - 1; i >= 0; --i) {
            removeAndRecycleViewAt(i, recycler);
        }

        if (getChildCount() == 0) {
            mFirstPosition = 0;
        } else if (first > 0) {
            mPushFirstHigher = true;
            mFirstPosition += first;
        }

    }

    private boolean viewInParentRect(View v) {
        return v.getRight() >= 0 && v.getLeft() <= getWidth() && v.getBottom() >= 0 && v.getTop() <= getHeight();
    }

    public int getFirstPosition() {
        return mFirstPosition;
    }

    public int getLastPosition() {
        return mFirstPosition + getChildCount();
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
    }

    private static class SmoothScroller extends LinearSmoothScroller {
        private static final float MILLISECONDS_PER_INCH = 100.0F;
        private final FocusLayoutManager mLayoutManager;

        public SmoothScroller(Context context, FocusLayoutManager manager) {
            super(context);
            mLayoutManager = manager;
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return 100.0F / (float) displayMetrics.densityDpi;
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + boxEnd) / 2 - (viewStart + viewEnd) / 2;
        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return targetPosition < mLayoutManager.getFirstPosition() ? new PointF(0.0F, -1.0F) : new PointF(0.0F, 1.0F);
        }
    }
}

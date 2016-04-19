package ticwear.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mobvoi.ticwear.view.SidePanelEventDispatcher;

import ticwear.design.R;

@TargetApi(20)
@CoordinatorLayout.DefaultBehavior(TicklableListViewBehavior.class)
public class TicklableListView extends RecyclerView implements SidePanelEventDispatcher {

    static final String TAG = "TicklableLV";

    /**
     * {@link LayoutManager} for focus state.
     */
    private final FocusableLinearLayoutManager mFocusableLayoutManager;

    private int mFocusedPadding;

    /**
     * To make-sure we have focus change when coordinate with {@link AppBarLayout},
     * We should use a scroll to mock the offset.
     */
    private int mScrollOffset;
    private static final int INVALID_SCROLL_OFFSET = Integer.MAX_VALUE;

    private boolean mSkipNestedScroll;

    private final PointF mTempPoint = new PointF();

    public TicklableListView(Context context) {
        this(context, null);
    }

    public TicklableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TicklableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TicklableListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        int defaultFocusedPadding = isInEditMode() ?
                0 : getResources().getDimensionPixelOffset(R.dimen.tic_list_focused_padding_ticwear);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TicklableListView, defStyleAttr, defStyleRes);
        mFocusedPadding = a.getDimensionPixelOffset(
                R.styleable.TicklableListView_tic_focusedStatePadding,
                defaultFocusedPadding);
        a.recycle();

        setHasFixedSize(true);
        setOverScrollMode(OVER_SCROLL_NEVER);

        mFocusableLayoutManager = new FocusableLinearLayoutManager(this);
        super.setLayoutManager(mFocusableLayoutManager);
        resetLayoutManagerState(false, null);

        mScrollOffset = INVALID_SCROLL_OFFSET;
        mSkipNestedScroll = false;

        if (getItemAnimator() != null) {
            long defaultAnimDuration = context.getResources()
                    .getInteger(R.integer.design_anim_list_item_state_change);
            long itemAnimDuration = defaultAnimDuration / 4;
//            getItemAnimator().setChangeDuration(itemAnimDuration);
            getItemAnimator().setMoveDuration(itemAnimDuration);
        }
    }

    @Override
    public LinearLayoutManager getLayoutManager() {
        return (LinearLayoutManager) super.getLayoutManager();
    }

    /**
     * Set a new adapter to provide child views on demand.
     *
     * @param adapter new adapter that should be instance of {@link TicklableListView.Adapter}
     */
    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(this, adapter.getItemViewType(0));
            if (!(viewHolder instanceof FocusableLinearLayoutManager.ViewHolder) && !isInEditMode()) {
                throw new IllegalArgumentException("adapter's ViewHolder should be instance of TicklableListView.ViewHolder");
            }
        }
        super.setAdapter(adapter);
    }

    @Override
    public int getBaseline() {
        if (getChildCount() != 0) {
            View centerChild = getChildAt(findCenterViewIndex());
            int centerChildBaseline = centerChild == null ? -1 : centerChild.getBaseline();
            if (centerChildBaseline != -1) {
                return getPaddingTop() + centerChild.getTop() + centerChildBaseline;
            }
        }

        return super.getBaseline();
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
        int centerY = ViewPropertiesHelper.getCenterYPos(this);

        for (int i = 0; i < count; ++i) {
            View child = getChildAt(i);
            int childCenterY = getTop() + ViewPropertiesHelper.getCenterYPos(child);
            int distance = Math.abs(centerY - childCenterY);
            if (distance < closest) {
                closest = distance;
                index = i;
            }
        }

        return index;
    }

    private void resetLayoutManagerState(boolean toFocus, @Nullable PointF touchPoint) {
        final boolean preInFocusState = isInFocusState();

        // Save current scroll position.
//        int position = saveCurrentScrollPosition(toFocus, touchPoint);

        mFocusableLayoutManager.setInFocusState(toFocus);

        // Restore offset
        mFocusableLayoutManager.setScrollResetting(true);
        if (!preInFocusState && toFocus) {
            mScrollOffset = getTop();
            setTop(0);
            scrollBy(0, -mScrollOffset);
        } else if (preInFocusState && !toFocus) {
            if (mScrollOffset != INVALID_SCROLL_OFFSET) {
                setTop(mScrollOffset);
                scrollBy(0, mScrollOffset);
                mScrollOffset = INVALID_SCROLL_OFFSET;
            }
        }
        mFocusableLayoutManager.setScrollResetting(false);

        if (getAdapter() != null) {
            getAdapter().notifyDataSetChanged();
        }

//        restoreScrollPosition(position);
    }

    private int saveCurrentScrollPosition(boolean toFocus, @Nullable PointF touchPoint) {

        // Then record the current scroll position.
        int position = NO_POSITION;
        if (getChildCount() > 0) {

            if (touchPoint != null) {
                View child = findChildViewUnder(touchPoint.x, touchPoint.y);
                int touchPosition = getChildAdapterPosition(child);
                position = Math.max(0, touchPosition - 1);

                Log.i(TAG, "save position for " + (toFocus ? "focus" : "normal") +
                        ": touched " + touchPosition + ", pos " + position);
            } else {
                int centerIndex = findCenterViewIndex();
                // When hole first child is visible, we what to scroll to it, instead of second item.
                boolean useCenterIndex = toFocus && !firstChildAllVisible();
                // If in focus state, get child position in center, or, get child position in top.
                int index = useCenterIndex ? centerIndex : Math.max(0, centerIndex - 1);
                position = getChildAdapterPosition(getChildAt(index));

                Log.i(TAG, "save position for " + (toFocus ? "focus" : "normal") +
                        ": center " + centerIndex + ", index " + index + ", pos " + position);
            }
        }
        return position;
    }

    private boolean firstChildAllVisible() {
        View firstChild = getChildAt(0);
        return firstChild != null &&
                getChildAdapterPosition(firstChild) == 0 &&
                firstChild.getTop() >= getPaddingTop();

    }

    private void restoreScrollPosition(final int position) {
        if (position != NO_POSITION) {
            // Restore scroll position.
            scrollToPosition(position);
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        throw new IllegalStateException("Can't customized the layout manager for TicklableListView.");
    }

    public boolean isInFocusState() {
        return mFocusableLayoutManager.isInFocusState();
    }

    public int getFocusedPadding() {
        return mFocusedPadding;
    }

    public void setFocusedPadding(int focusedPadding) {
        this.mFocusedPadding = focusedPadding;
        mFocusableLayoutManager.setVerticalPadding(focusedPadding);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        // Fix touch offset according to scroll-offset
        // When exit focus state, scroll of this view will transfer to offset.
        // So we must calculate the offset change into touch event.
        if (isInFocusState() && mScrollOffset != INVALID_SCROLL_OFFSET &&
                e.getAction() == MotionEvent.ACTION_DOWN) {
            e.offsetLocation(0, -mScrollOffset);
        }
        mTempPoint.set(e.getX(), e.getY());
        exitFocusStateIfNeed(mTempPoint);
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchSidePanelEvent(MotionEvent ev, @NonNull SuperCallback superCallback) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean previousInFocus = isInFocusState();
                mTempPoint.set(ev.getX(), ev.getY());
                enterFocusStateIfNeed(mTempPoint);
                // Fix touch offset according to scroll-offset
                // When enter focus state, offset of this view will transfer to scroll.
                // So we must calculate the offset change into touch event.
                if (!previousInFocus && mScrollOffset != INVALID_SCROLL_OFFSET) {
                    ev.offsetLocation(0, mScrollOffset);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getHandler().postDelayed(exitFocusStateRunnable,
                        // after this period of time without focus state action (side panel event),
                        // we should exit focus state.
                        getResources().getInteger(R.integer.design_time_action_idle_timeout));
                break;
        }
        super.dispatchTouchEvent(ev);
        return superCallback.superDispatchTouchSidePanelEvent(ev);
    }

    @Override
    public FocusableLinearLayoutManager.ViewHolder getChildViewHolder(View child) {
        return (FocusableLinearLayoutManager.ViewHolder) super.getChildViewHolder(child);
    }

    @Override
    protected void onDetachedFromWindow() {
        getHandler().removeCallbacks(exitFocusStateRunnable);
        super.onDetachedFromWindow();
    }


    private void enterFocusStateIfNeed(@Nullable PointF touchPoint) {
        getHandler().removeCallbacks(exitFocusStateRunnable);
        if (isInFocusState()) {
            return;
        }

        resetLayoutManagerState(true, touchPoint);
    }

    private void exitFocusStateIfNeed() {
        exitFocusStateIfNeed(null);
    }

    private void exitFocusStateIfNeed(@Nullable PointF touchPoint) {
        getHandler().removeCallbacks(exitFocusStateRunnable);
        if (!isInFocusState()) {
            return;
        }

        resetLayoutManagerState(false, touchPoint);
    }

    private Runnable exitFocusStateRunnable = new Runnable() {
        @Override
        public void run() {
            exitFocusStateIfNeed();
        }
    };

    public int getScrollOffset() {
        return mScrollOffset == INVALID_SCROLL_OFFSET ? 0 : mScrollOffset;
    }

    /**
     * Update offset to scroll.
     *
     * This will calculate the delta of previous offset and new offset, then apply it to scroll.
     *
     * @param scrollOffset new offset to scroll.
     *
     * @return the unconsumed offset.
     *
     * TODO: scroll offset seems only work with focused linear layout, maybe we should move the logic to there.
     */
    public int updateScrollOffset(int scrollOffset) {
        if (this.mScrollOffset == scrollOffset) {
            return 0;
        }

        if (this.mScrollOffset == INVALID_SCROLL_OFFSET) {
            int curScrollOffset = -computeVerticalScrollOffset();
            if (curScrollOffset >= 0 && scrollOffset >= 0) {
                this.mScrollOffset = Math.min(scrollOffset, curScrollOffset);
            } else if (curScrollOffset <= 0 && scrollOffset <= 0) {
                this.mScrollOffset = Math.max(scrollOffset, curScrollOffset);
            } else {
                this.mScrollOffset = 0;
            }
        }

        int delta = scrollOffset - this.mScrollOffset;
        int scroll = -delta;

        int pre = computeVerticalScrollOffset();
        // Temporary disable nested scrolling.
        mSkipNestedScroll = true;
        scrollBy(0, scroll);
        mSkipNestedScroll = false;
        int real = computeVerticalScrollOffset() - pre;

        this.mScrollOffset -= real;

        return scroll - real;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return !mSkipNestedScroll && super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

}

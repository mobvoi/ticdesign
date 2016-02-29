package com.mobvoi.design.wearable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mobvoi.design.wearable.widget.CoordinatorLayout;
import com.mobvoi.design.wearable.widget.TicklableListViewBehavior;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

@TargetApi(20)
@CoordinatorLayout.DefaultBehavior(TicklableListViewBehavior.class)
public class TicklableListView extends RecyclerView {

    static final String TAG = "TicklableListView";

    /**
     * after this period of time without focus state action (side panel event), we should exit
     * focus state.
     */
    private static final long FOCUS_ACTION_IDLE_TIMEOUT = 2000l;

    /**
     * Invalid focus state
     */
    public static final int FOCUS_STATE_INVALID = -1;

    /**
     * Focus state on normal (not tickled).
     */
    public static final int FOCUS_STATE_NORMAL = 0;

    /**
     * Focus state on central, means the item is focused when tickled.
     */
    public static final int FOCUS_STATE_CENTRAL = 1;

    /**
     * Focus state on non central, means the item is not focused when tickled.
     */
    public static final int FOCUS_STATE_NON_CENTRAL = 2;

    private final List<TicklableListView.OnCentralPositionChangedListener> mOnCentralPositionChangedListeners;
    private final AdapterDataObserver mObserver;

    /**
     * Flag to indicate if we are in normal state or focus state.
     */
    private boolean inFocusState;
    /**
     * {@link LayoutManager} for normal state.
     */
    private LayoutManager normalLayoutManager;
    /**
     * {@link LayoutManager} for focus state.
     */
    private LayoutManager focusLayoutManager;

    private int previousCentral;

    /**
     * To make-sure we have focus change when coordinate with {@link com.mobvoi.design.wearable.widget.AppBarLayout},
     * We should use a scroll to mock the offset.
     */
    private int scrollOffset;
    private static final int INVALID_SCROLL_OFFSET = Integer.MAX_VALUE;

    private boolean skipNestedScroll;

    public TicklableListView(Context context) {
        this(context, null);
    }

    public TicklableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TicklableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOnCentralPositionChangedListeners = new ArrayList<>();
        mObserver = new AdapterDataObserver() {
            public void onChanged() {
                TicklableLayoutManager layoutManager = getTicklableLayoutManager();
                if (layoutManager != null) {
                    layoutManager.onDataSetChanged(TicklableListView.this);
                }
            }
        };
        setHasFixedSize(true);
        setOverScrollMode(OVER_SCROLL_NEVER);

        inFocusState = false;
        resetLayoutManager();

        previousCentral = NO_POSITION;

        scrollOffset = INVALID_SCROLL_OFFSET;
        skipNestedScroll = false;
    }

    /**
     * Set a new adapter to provide child views on demand.
     *
     * @param adapter new adapter that should be instance of {@link TicklableListView.Adapter}
     */
    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null && !(adapter instanceof Adapter)) {
            throw new IllegalArgumentException("adapter should be instance of TicklableListView.Adapter");
        }
        RecyclerView.Adapter currentAdapter = getAdapter();
        if (currentAdapter != null) {
            currentAdapter.unregisterAdapterDataObserver(mObserver);
        }

        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
        }

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

    public boolean isInFocusState() {
        return inFocusState;
    }

    private void resetLayoutManager() {
        LayoutManager layoutManager;
        if (inFocusState) {
            if (focusLayoutManager == null) {
                focusLayoutManager = new FocusLayoutManager(this);
            }
            layoutManager = focusLayoutManager;
        } else {
            if (normalLayoutManager == null) {
                normalLayoutManager = new LinearLayoutManager(getContext());
            }
            layoutManager = normalLayoutManager;
        }

        // Save current scroll position.
        int position = saveCurrentScrollPosition();

        super.setLayoutManager(layoutManager);

        restoreScrollPosition(position);
    }

    private int saveCurrentScrollPosition() {
        // First reset the scroll offset
        if (scrollOffset != INVALID_SCROLL_OFFSET) {
            // NOTE: the offset is opposite to scroll
            scrollBy(0, scrollOffset);
        }

        // Then record the current scroll position.
        int position = NO_POSITION;
        if (getChildCount() > 0) {
            int centerIndex = findCenterViewIndex();
            // If in focus state, get child position in center, or, get child position in top.
            int index = inFocusState ? centerIndex : Math.max(0, centerIndex - 1);
            position = getChildAdapterPosition(getChildAt(index));
        }
        return position;
    }

    @DebugLog
    private void restoreScrollPosition(int position) {
        if (position != NO_POSITION) {
            // Restore scroll position.
            scrollToPosition(position);

            // Restore scroll offset
            if (scrollOffset != INVALID_SCROLL_OFFSET) {
                if (inFocusState) {
                    scrollBy(0, -scrollOffset);
                } else {
                    scrollOffset = INVALID_SCROLL_OFFSET;
                }
            }
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        throw new IllegalStateException("Don't set raw layout manager, use " +
                "setNormalLayoutManager & setFocusLayoutManager instead.");
    }

    public void setNormalLayoutManager(@Nullable LayoutManager layout) {
        normalLayoutManager = layout;
        resetLayoutManager();
    }

    public void setFocusLayoutManager(@Nullable LayoutManager layout) {
        focusLayoutManager = layout;
        resetLayoutManager();
    }

    /**
     * Adds a listener that will be called when the central item of the list changes.
     */
    public void addOnCentralPositionChangedListener(TicklableListView.OnCentralPositionChangedListener listener) {
        mOnCentralPositionChangedListeners.add(listener);
    }

    /**
     * Removes a listener that would be called when the central item of the list changes.
     */
    public void removeOnCentralPositionChangedListener(TicklableListView.OnCentralPositionChangedListener listener) {
        mOnCentralPositionChangedListeners.remove(listener);
    }

    /**
     * Clear all listeners that listening the central item of the list changes event.
     */
    public void clearOnCentralPositionChangedListener() {
        mOnCentralPositionChangedListeners.clear();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        exitFocusStateIfNeed();
        return super.dispatchTouchEvent(e);
    }

    @SuppressWarnings("unused")
    public boolean dispatchTouchSidePanelEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                enterFocusStateIfNeed();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getHandler().postDelayed(exitFocusStateRunnable, FOCUS_ACTION_IDLE_TIMEOUT);
                break;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if (getChildCount() == 0) {
            return false;
        }

        TicklableLayoutManager layoutManager = getTicklableLayoutManager();
        if (layoutManager != null && layoutManager.onFling(this, velocityY)) {
            return true;
        }

        return super.fling(velocityX, velocityY);
    }

    @Nullable
    private TicklableLayoutManager getTicklableLayoutManager() {
        TicklableLayoutManager layoutManager = null;
        if (getLayoutManager() instanceof TicklableLayoutManager) {
            layoutManager = (TicklableLayoutManager) getLayoutManager();
        }
        return layoutManager;
    }

    @Override
    public TicklableListView.ViewHolder getChildViewHolder(View child) {
        return (TicklableListView.ViewHolder) super.getChildViewHolder(child);
    }

    @Override
    public void onChildAttachedToWindow(View child) {
        // Set child focus state right after its attached to window,
        // to avoid wrong state displaying on screen.
        if (isInFocusState()) {
            notifyChildFocusStateChanged(FOCUS_STATE_NON_CENTRAL, false, child);
        } else {
            notifyChildFocusStateChanged(FOCUS_STATE_NORMAL, false, child);
        }
    }

    void notifyChildrenAboutProximity(int centerIndex, boolean animate) {
        childrenFocusStateChanged(centerIndex, animate);
    }

    void notifyChildrenExitFocusState(boolean animate) {
        childrenFocusStateChanged(NO_POSITION, animate);
    }

    void childrenFocusStateChanged(int centerIndex, boolean animate) {
        for (int index = 0; index < getChildCount(); ++index) {
            View view = getChildAt(index);
            int focusState = FOCUS_STATE_NORMAL;
            if (centerIndex != NO_POSITION) {
                focusState = index == centerIndex ?
                        TicklableListView.FOCUS_STATE_CENTRAL :
                        TicklableListView.FOCUS_STATE_NON_CENTRAL;
            }
            notifyChildFocusStateChanged(focusState, animate, view);
        }

        int centerPosition = centerIndex == NO_POSITION ?
                NO_POSITION : getChildAdapterPosition(getChildAt(centerIndex));

        if (centerPosition != previousCentral) {
            for (TicklableListView.OnCentralPositionChangedListener listener : mOnCentralPositionChangedListeners) {
                listener.onCentralPositionChanged(centerPosition);
            }

            previousCentral = centerPosition;
        }
    }

    private void notifyChildFocusStateChanged(int focusState, boolean animate, View view) {
        ViewHolder viewHolder = getChildViewHolder(view);

        // Only call focus state change once.
        if (viewHolder.prevFocusState != focusState) {
            viewHolder.onFocusStateChanged(focusState, animate);
            viewHolder.prevFocusState = focusState;
        }
    }


    private void enterFocusStateIfNeed() {
        getHandler().removeCallbacks(exitFocusStateRunnable);
        if (inFocusState) {
            return;
        }

        inFocusState = true;
        resetLayoutManager();
    }

    private void exitFocusStateIfNeed() {
        getHandler().removeCallbacks(exitFocusStateRunnable);
        if (!inFocusState) {
            return;
        }

        inFocusState = false;
        resetLayoutManager();

        notifyChildrenExitFocusState(true);
    }

    private Runnable exitFocusStateRunnable = new Runnable() {
        @Override
        public void run() {
            exitFocusStateIfNeed();
        }
    };

    @DebugLog
    public int getScrollOffset() {
        return scrollOffset == INVALID_SCROLL_OFFSET ? 0 : scrollOffset;
    }

    /**
     * Update offset to scroll.
     *
     * This will calculate the delta of previous offset and new offset, then apply it to scroll.
     *
     * @param scrollOffset new offset to scroll.
     *
     * @return the unconsumed offset.
     */
    @DebugLog
    public int updateScrollOffset(int scrollOffset) {
        if (this.scrollOffset == scrollOffset) {
            return 0;
        }

        if (this.scrollOffset == INVALID_SCROLL_OFFSET) {
            int curScrollOffset = -computeVerticalScrollOffset();
            if (curScrollOffset >= 0 && scrollOffset >= 0) {
                this.scrollOffset = Math.min(scrollOffset, curScrollOffset);
            } else if (curScrollOffset <= 0 && scrollOffset <= 0) {
                this.scrollOffset = Math.max(scrollOffset, curScrollOffset);
            } else {
                this.scrollOffset = 0;
            }
        }

        int delta = scrollOffset - this.scrollOffset;
        int scroll = -delta;

        int pre = computeVerticalScrollOffset();
        // Temporary disable nested scrolling.
        skipNestedScroll = true;
        scrollBy(0, scroll);
        skipNestedScroll = false;
        int real = computeVerticalScrollOffset() - pre;

        this.scrollOffset -= real;

        return scroll - real;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return !skipNestedScroll && super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    /**
     * Created by tankery on 2/17/16.
     *
     * Layout manager specific for ticklable list view.
     *
     * This layout manager will handle more interactions than regular recycler-view's layout manager.
     * So its sub-classes can have more flexible implements.
     */
    abstract static class TicklableLayoutManager extends RecyclerView.LayoutManager {

        protected TicklableListView ticklableListView;

        TicklableLayoutManager(TicklableListView ticklableListView) {
            this.ticklableListView = ticklableListView;
        }

        /**
         * Notify that data-set is changed.
         */
        public void onDataSetChanged(TicklableListView ticklableListView) {
        }

        /**
         * handle the fling event
         * @param velocityY Initial vertical velocity in pixels per second
         * @return true if the fling was consumed by us, false to pass the fling to container view.
         */
        public boolean onFling(TicklableListView ticklableListView, int velocityY) {
            return false;
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private int prevFocusState;

        public ViewHolder(View itemView) {
            super(itemView);
            prevFocusState = FOCUS_STATE_INVALID;
        }

        protected void onFocusStateChanged(int focusState, boolean animate) {
            if (itemView instanceof TicklableListView.OnFocusStateChangedListener) {
                TicklableListView.OnFocusStateChangedListener item = (TicklableListView.OnFocusStateChangedListener) itemView;
                item.onFocusStateChanged(focusState, animate);
            } else {
                applyDefaultAnimate(focusState, animate);
            }
        }

        private void applyDefaultAnimate(int focusState, boolean animate) {
            float scale = 1.0f;
            float alpha = 1.0f;
            switch (focusState) {
                case FOCUS_STATE_NORMAL:
                    break;
                case FOCUS_STATE_CENTRAL:
                    scale = 1.1f;
                    alpha = 1.0f;
                    break;
                case FOCUS_STATE_NON_CENTRAL:
                    scale = 0.9f;
                    alpha = 0.6f;
                    break;
            }
            if (animate) {
                itemView.animate()
                        .setDuration(200)
                        .alpha(alpha)
                        .scaleX(scale)
                        .scaleY(scale);
            } else {
                itemView.setScaleX(scale);
                itemView.setScaleY(scale);
                itemView.setAlpha(alpha);
            }
        }
    }

    public abstract static class Adapter<VH extends ViewHolder>
            extends RecyclerView.Adapter<VH> {
    }

    /**
     * An OnCentralPositionChangedListener can be set on a TicklableListView to receive messages
     * when a central position changed event has occurred on that TicklableListView when tickled.
     *
     * @see TicklableListView#addOnCentralPositionChangedListener(OnCentralPositionChangedListener)
     */
    public interface OnCentralPositionChangedListener {

        /**
         * Callback method to be invoked when TicklableListView's central item changed.
         *
         * @param position The adapter position of the central item, can be {@link #NO_POSITION}.
         *                 If is {@link #NO_POSITION}, means the tickle state is changed to normal,
         *                 so there is no central item.
         */
        void onCentralPositionChanged(int position);
    }

    /**
     * An listener that receive messages for focus state change.
     *
     * @see #FOCUS_STATE_NORMAL
     * @see #FOCUS_STATE_CENTRAL
     * @see #FOCUS_STATE_NON_CENTRAL
     */
    public interface OnFocusStateChangedListener {

        /**
         * Item's focus state has changed.
         * @param focusState state of focus. can be {@link #FOCUS_STATE_NORMAL},
         *                   {@link #FOCUS_STATE_CENTRAL}, {@link #FOCUS_STATE_NON_CENTRAL}
         * @param animate interact with animation?
         */
        void onFocusStateChanged(int focusState, boolean animate);
    }

}

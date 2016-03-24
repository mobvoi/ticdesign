package ticwear.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import ticwear.design.R;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

;

@TargetApi(20)
@CoordinatorLayout.DefaultBehavior(TicklableListViewBehavior.class)
public class TicklableListView extends RecyclerView {

    static final String TAG = "TicklableListView";

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

    /**
     * Denotes that an integer parameter, field or method return value is expected
     * to be a focus state value (e.g. {@link #FOCUS_STATE_CENTRAL}).
     */
    @Documented
    @Retention(SOURCE)
    @Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
    @IntDef({FOCUS_STATE_INVALID, FOCUS_STATE_NORMAL, FOCUS_STATE_CENTRAL, FOCUS_STATE_NON_CENTRAL})
    public @interface FocusState {}

    private final List<TicklableListView.OnCentralPositionChangedListener> mOnCentralPositionChangedListeners;

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
     * To make-sure we have focus change when coordinate with {@link AppBarLayout},
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
        if (adapter != null) {
            RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(this, adapter.getItemViewType(0));
            if (!(viewHolder instanceof ViewHolder) && !isInEditMode()) {
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

        final boolean preInFocusState = getLayoutManager() == focusLayoutManager;

        // Save current scroll position.
        int position = saveCurrentScrollPosition();

        super.setLayoutManager(layoutManager);

        restoreScrollPosition(position, preInFocusState);
    }

    private int saveCurrentScrollPosition() {

        // Then record the current scroll position.
        int position = NO_POSITION;
        if (getChildCount() > 0) {
            int centerIndex = findCenterViewIndex();
            // When hole first child is visible, we what to scroll to it, instead of second item.
            boolean useCenterIndex = inFocusState && !firstChildAllVisible();
            // If in focus state, get child position in center, or, get child position in top.
            int index = useCenterIndex ? centerIndex : Math.max(0, centerIndex - 1);
            position = getChildAdapterPosition(getChildAt(index));
        }
        return position;
    }

    private boolean firstChildAllVisible() {
        View firstChild = getChildAt(0);
        return firstChild != null &&
                getChildAdapterPosition(firstChild) == 0 &&
                firstChild.getTop() >= getPaddingTop();

    }

    private void restoreScrollPosition(int position, boolean preInFocusState) {
        // Restore offset
        if (!preInFocusState && inFocusState) {
            scrollOffset = getTop();
            setTop(0);
        } else if (preInFocusState && !inFocusState) {
            if (scrollOffset != INVALID_SCROLL_OFFSET) {
                setTop(scrollOffset);
                scrollOffset = INVALID_SCROLL_OFFSET;
            }
        }

        if (position != NO_POSITION) {
            // Restore scroll position.
            scrollToPosition(position);
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
        // Fix touch offset according to scroll-offset
        // When exit focus state, scroll of this view will transfer to offset.
        // So we must calculate the offset change into touch event.
        if (isInFocusState() && scrollOffset != INVALID_SCROLL_OFFSET &&
                e.getAction() == MotionEvent.ACTION_DOWN) {
            e.offsetLocation(0, -scrollOffset);
        }
        exitFocusStateIfNeed();
        return super.dispatchTouchEvent(e);
    }

    @SuppressWarnings("unused")
    public boolean dispatchTouchSidePanelEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean previousInFocus = isInFocusState();
                enterFocusStateIfNeed();
                // Fix touch offset according to scroll-offset
                // When enter focus state, offset of this view will transfer to scroll.
                // So we must calculate the offset change into touch event.
                if (!previousInFocus && scrollOffset != INVALID_SCROLL_OFFSET) {
                    ev.offsetLocation(0, scrollOffset);
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
        // TODO: should return super.dispatchXXX, so we may need a interface for side-panel event?
        return true;
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
        child.setClickable(true);
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

            // Child not focus can not click.
            view.setClickable(focusState != FOCUS_STATE_NON_CENTRAL);

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
        if (isInEditMode()) {
            return;
        }
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @FocusState
        private int prevFocusState;

        public ViewHolder(View itemView) {
            super(itemView);
            prevFocusState = FOCUS_STATE_INVALID;
        }

        /**
         * Focus state of view bind to this ViewHolder is changed.
         *
         * @param focusState new focus state of view.
         * @param animate should apply a animate for this change? If not, just change
         *                the view immediately.
         */
        protected void onFocusStateChanged(@FocusState int focusState, boolean animate) {
            if (itemView instanceof TicklableListView.OnFocusStateChangedListener) {
                TicklableListView.OnFocusStateChangedListener item = (TicklableListView.OnFocusStateChangedListener) itemView;
                item.onFocusStateChanged(focusState, animate);
            } else {
                applyDefaultAnimate(focusState, animate);
            }
        }

        private void applyDefaultAnimate(@FocusState int focusState, boolean animate) {
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
        void onFocusStateChanged(@FocusState int focusState, boolean animate);
    }

}

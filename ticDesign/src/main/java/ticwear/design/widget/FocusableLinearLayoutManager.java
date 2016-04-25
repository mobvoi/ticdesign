package ticwear.design.widget;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator.ItemAnimatorFinishedListener;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ticwear.design.DesignConfig;
import ticwear.design.R;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Enhanced LayoutManager that support focused status.
 *
 * Created by tankery on 4/13/16.
 */
public class FocusableLinearLayoutManager extends LinearLayoutManager {

    static final String TAG = "FocusableLLM";

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

    private final TicklableListView mTicklableListView;
    private final FocusLayoutHelper mFocusLayoutHelper;

    private ScrollVelocityTracker mScrollVelocityTracker;

    private boolean mInFocusState;
    private boolean mScrollResetting;
    private final FocusStateRequest mFocusStateRequest = new FocusStateRequest();

    private final List<OnCentralPositionChangedListener> mOnCentralPositionChangedListeners;
    private int mPreviousCentral;

    /**
     * To make-sure we have focus change when coordinate with {@link AppBarLayout},
     * We should use a scroll to mock the offset.
     */
    private int mScrollOffset;
    private static final int INVALID_SCROLL_OFFSET = Integer.MAX_VALUE;

    private final AppBarScrollController mAppBarScrollController;

    public FocusableLinearLayoutManager(@NonNull  TicklableListView ticklableListView) {
        super(ticklableListView.getContext(), VERTICAL, false);

        mTicklableListView = ticklableListView;
        mFocusLayoutHelper = new FocusLayoutHelper(ticklableListView, this);
        mInFocusState = false;

        mOnCentralPositionChangedListeners = new ArrayList<>();
        mPreviousCentral = RecyclerView.NO_POSITION;

        mScrollOffset = INVALID_SCROLL_OFFSET;

        mAppBarScrollController = new AppBarScrollController(mTicklableListView);

        setInFocusState(false);
    }

    /**
     * Adds a listener that will be called when the central item of the list changes.
     */
    public void addOnCentralPositionChangedListener(OnCentralPositionChangedListener listener) {
        mOnCentralPositionChangedListeners.add(listener);
    }

    /**
     * Removes a listener that would be called when the central item of the list changes.
     */
    public void removeOnCentralPositionChangedListener(OnCentralPositionChangedListener listener) {
        mOnCentralPositionChangedListeners.remove(listener);
    }

    /**
     * Clear all listeners that listening the central item of the list changes event.
     */
    public void clearOnCentralPositionChangedListener() {
        mOnCentralPositionChangedListeners.clear();
    }

    private void setInFocusState(boolean toFocus) {
        if (mInFocusState == toFocus) {
            return;
        }

        mInFocusState = toFocus;

        // Restore offset
        restoreOffset(toFocus);

        // Set flag so we will request focus state change on next layout.
        // Or, we will notify immediately.
        mFocusStateRequest.notifyOnNextLayout = true;

        if (mInFocusState) {
            mFocusLayoutHelper.init();
        } else {
            mFocusLayoutHelper.destroy();
        }
        if (getChildCount() > 0) {
            if (mInFocusState) {
                requestNotifyChildrenAboutProximity(true);
            } else {
                requestNotifyChildrenAboutExit(true);
            }
        }

        requestSimpleAnimationsInNextLayout();


        if (mTicklableListView.getAdapter() != null) {
            mTicklableListView.getAdapter().notifyDataSetChanged();
        }
    }

    private void restoreOffset(boolean toFocus) {
        mScrollResetting = true;
        if (toFocus) {
            mScrollOffset = mTicklableListView.getTop();
            mTicklableListView.offsetTopAndBottom(-mScrollOffset);
            mTicklableListView.scrollBy(0, -mScrollOffset);
        } else {
            if (mScrollOffset != INVALID_SCROLL_OFFSET) {
                mTicklableListView.offsetTopAndBottom(mScrollOffset);
                mTicklableListView.scrollBy(0, mScrollOffset);
                mScrollOffset = INVALID_SCROLL_OFFSET;
            }
        }
        mScrollResetting = false;
    }

    public boolean isInFocusState() {
        return mInFocusState;
    }

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
     */
    public int updateScrollOffset(int scrollOffset) {
        if (mAppBarScrollController.isAppBarChanging()) {
            int delta = scrollOffset - this.mScrollOffset;
            this.mScrollOffset = scrollOffset;
            return delta;
        }
        if (this.mScrollOffset == scrollOffset) {
            return 0;
        }

        if (this.mScrollOffset == INVALID_SCROLL_OFFSET) {
            int curScrollOffset = -mTicklableListView.computeVerticalScrollOffset();
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

        int pre = mTicklableListView.computeVerticalScrollOffset();
        // Temporary disable nested scrolling.
        mTicklableListView.scrollBySkipNestedScroll(0, scroll);
        int real = mTicklableListView.computeVerticalScrollOffset() - pre;

        this.mScrollOffset -= real;

        return scroll - real;
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);

        if (mInFocusState) {
            mFocusLayoutHelper.init();
        }
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        if (mInFocusState) {
            mFocusLayoutHelper.destroy();
        }

        getHandler().removeCallbacks(exitFocusStateRunnable);

        super.onDetachedFromWindow(view, recycler);
    }

    @Override
    public void onLayoutChildren(Recycler recycler, State state) {
        super.onLayoutChildren(recycler, state);

        if (mFocusStateRequest.notifyOnNextLayout) {
            mFocusStateRequest.notifyOnNextLayout = false;
            // If the notify has a animation, we should make sure the notify is later than
            // RecyclerView's animation. So they will not conflict.
            if (mFocusStateRequest.animate) {
                postOnAnimation(new Runnable() {
                    @Override
                    public void run() {
                        // We wait for animation time begin and notify on next main loop,
                        // So we can sure the notify is follow the state change animation.
                        notifyAfterLayoutOnNextMainLoop();
                    }
                });
            } else {
                requestNotifyChildrenStateChanged(mFocusStateRequest);
            }
        }
    }

    private void notifyAfterLayoutOnNextMainLoop() {
        mTicklableListView.post(new Runnable() {
            @Override
            public void run() {
                requestNotifyChildrenStateChanged(mFocusStateRequest);
            }
        });
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean getClipToPadding() {
        return !mInFocusState && super.getClipToPadding();
    }

    @Override
    public int getPaddingTop() {
        return mInFocusState ?
                mFocusLayoutHelper.getVerticalPadding() :
                super.getPaddingTop();
    }

    @Override
    public int getPaddingBottom() {
        return mInFocusState ?
                mFocusLayoutHelper.getVerticalPadding() :
                super.getPaddingBottom();
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        onScrollVerticalBy(dy);
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (mInFocusState) {
            mFocusLayoutHelper.onScrollStateChanged(state);
        } else {
            super.onScrollStateChanged(state);
        }
    }

    private void onScrollVerticalBy(int dy) {
        if (mScrollVelocityTracker == null) {
            int itemHeight = mFocusLayoutHelper.getCentralItemHeight();
            mScrollVelocityTracker =
                    new ScrollVelocityTracker(mTicklableListView.getContext(), itemHeight);
        }

        boolean scrollFast = mScrollVelocityTracker.addScroll(dy);

        if (getChildCount() > 0) {
            if (mInFocusState) {
                requestNotifyChildrenAboutProximity(!scrollFast);
            } else {
                requestNotifyChildrenAboutExit(false);
            }
        }
    }


    public boolean dispatchTouchEvent(MotionEvent e) {
        exitFocusStateIfNeed(e);

        return false;
    }

    public boolean dispatchTouchSidePanelEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                enterFocusStateIfNeed(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getHandler().postDelayed(exitFocusStateRunnable,
                        // after this period of time without focus state action (side panel event),
                        // we should exit focus state.
                        mTicklableListView.getResources().getInteger(R.integer.design_time_action_idle_timeout));
                break;
        }
        return mInFocusState && mFocusLayoutHelper.dispatchTouchSidePanelEvent(ev);
    }


    private void enterFocusStateIfNeed(@Nullable MotionEvent ev) {
        getHandler().removeCallbacks(exitFocusStateRunnable);
        if (isInFocusState()) {
            return;
        }

        setInFocusState(true);

        // Fix touch offset according to scroll-offset
        // When enter focus state, offset of this view will transfer to scroll.
        // So we must calculate the offset change into touch event.
        if (ev != null && mScrollOffset != INVALID_SCROLL_OFFSET &&
                ev.getAction() == MotionEvent.ACTION_DOWN) {
            ev.offsetLocation(0, mScrollOffset);
        }
    }

    private void exitFocusStateIfNeed() {
        exitFocusStateIfNeed(null);
    }

    private void exitFocusStateIfNeed(@Nullable MotionEvent ev) {
        getHandler().removeCallbacks(exitFocusStateRunnable);
        if (!isInFocusState()) {
            return;
        }

        // Fix touch offset according to scroll-offset
        // When exit focus state, scroll of this view will transfer to offset.
        // So we must calculate the offset change into touch event.
        if (ev != null && mScrollOffset != INVALID_SCROLL_OFFSET &&
                ev.getAction() == MotionEvent.ACTION_DOWN) {
            ev.offsetLocation(0, -mScrollOffset);
        }

        setInFocusState(false);
    }

    private Runnable exitFocusStateRunnable = new Runnable() {
        @Override
        public void run() {
            exitFocusStateIfNeed();
        }
    };

    void requestNotifyChildrenAboutProximity(boolean animate) {
        int centerViewIndex = mFocusLayoutHelper.findCenterViewIndex();
        requestNotifyChildrenStateChanged(centerViewIndex, animate);
    }

    void requestNotifyChildrenAboutExit(boolean animate) {
        requestNotifyChildrenStateChanged(RecyclerView.NO_POSITION, animate);
    }

    private void requestNotifyChildrenStateChanged(int centerIndex, boolean animate) {
        if (mScrollResetting) {
            return;
        }

        mFocusStateRequest.centerIndex = centerIndex;
        mFocusStateRequest.animate = animate;
        requestNotifyChildrenStateChanged(mFocusStateRequest);
    }

    private void requestNotifyChildrenStateChanged(final FocusStateRequest request) {
        if (request.notifyOnNextLayout) {
            return;
        }

        boolean isRunning = mTicklableListView.getItemAnimator().isRunning(
                new ItemAnimatorFinishedListener() {
                    @Override
                    public void onAnimationsFinished() {
                        notifyChildrenStateChanged(request);
                    }
                });

        if (DesignConfig.DEBUG_RECYCLER_VIEW) {
            Log.v(TAG, "request state changed with item anim running? " + isRunning);
        }
    }

    private void notifyChildrenStateChanged(FocusStateRequest request) {
        for (int index = 0; index < getChildCount(); ++index) {
            View view = getChildAt(index);
            int focusState = FOCUS_STATE_NORMAL;
            if (request.centerIndex != RecyclerView.NO_POSITION) {
                focusState = index == request.centerIndex ?
                        FOCUS_STATE_CENTRAL :
                        FOCUS_STATE_NON_CENTRAL;
            }

            final boolean animate = view.isShown() && request.animate;
            notifyChildFocusStateChanged(focusState, animate, view);
        }

        notifyOnCentralPositionChanged(request.centerIndex);
    }

    private void notifyChildFocusStateChanged(int focusState, boolean animate, View view) {
        ViewHolder viewHolder = mTicklableListView.getChildViewHolder(view);

        // Only call focus state change once.
        if (viewHolder.prevFocusState != focusState) {
            viewHolder.onFocusStateChanged(focusState, animate);
            viewHolder.prevFocusState = focusState;
            view.setClickable(focusState != FOCUS_STATE_NON_CENTRAL);
        }
    }

    private void notifyOnCentralPositionChanged(int centerIndex) {
        int centerPosition = centerIndex == RecyclerView.NO_POSITION ?
                RecyclerView.NO_POSITION : mTicklableListView.getChildAdapterPosition(getChildAt(centerIndex));

        if (centerPosition != mPreviousCentral) {
            for (OnCentralPositionChangedListener listener : mOnCentralPositionChangedListeners) {
                listener.onCentralPositionChanged(centerPosition);
            }

            mPreviousCentral = centerPosition;
        }
    }

    public Handler getHandler() {
        return mTicklableListView.getHandler();
    }

    private static class FocusStateRequest {
        public boolean notifyOnNextLayout;
        public int centerIndex;
        public boolean animate;

        public FocusStateRequest() {
            notifyOnNextLayout = false;
        }

        @Override
        public String toString() {
            return "FocusStateRequest@" + hashCode() + "{" +
                    "notifyOnNext " + notifyOnNextLayout +
                    ", center " + centerIndex +
                    ", animate " + animate +
                    "}";
        }
    }

    /**
     * Denotes that an integer parameter, field or method return value is expected
     * to be a focus state value (e.g. {@link #FOCUS_STATE_CENTRAL}).
     */
    @Documented
    @Retention(SOURCE)
    @Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
    @IntDef({FOCUS_STATE_INVALID, FOCUS_STATE_NORMAL, FOCUS_STATE_CENTRAL, FOCUS_STATE_NON_CENTRAL})
    public @interface FocusState {}

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

    private static class ScrollVelocityTracker {

        private long mLastScrollTime = -1;
        private final float mFastScrollVelocity;

        ScrollVelocityTracker(@NonNull Context context, int itemHeight) {
            long animationTime = context.getResources()
                    .getInteger(R.integer.design_anim_list_item_state_change);
            mFastScrollVelocity = 1.5f * itemHeight / animationTime;
        }

        public boolean addScroll(int dy) {
            boolean scrollFast = false;

            long currentTime = System.currentTimeMillis();
            if (mLastScrollTime > 0) {
                long duration = currentTime - mLastScrollTime;
                float velocity = (float) Math.abs(dy) / duration;
                if (velocity > mFastScrollVelocity) {
                    scrollFast = true;
                }
            }
            mLastScrollTime = currentTime;


            return scrollFast;
        }

    }

    /**
     * An OnCentralPositionChangedListener can be set on a TicklableListView to receive messages
     * when a central position changed event has occurred on that TicklableListView when tickled.
     *
     * @see #addOnCentralPositionChangedListener(OnCentralPositionChangedListener)
     */
    public interface OnCentralPositionChangedListener {

        /**
         * Callback method to be invoked when TicklableListView's central item changed.
         *
         * @param position The adapter position of the central item, can be {@link RecyclerView#NO_POSITION}.
         *                 If is {@link RecyclerView#NO_POSITION}, means the tickle state is changed to normal,
         *                 so there is no central item.
         */
        void onCentralPositionChanged(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @FocusState
        private int prevFocusState;

        private final long defaultAnimDuration;

        public ViewHolder(View itemView) {
            super(itemView);
            prevFocusState = FOCUS_STATE_INVALID;
            defaultAnimDuration = itemView.getContext().getResources()
                    .getInteger(R.integer.design_anim_list_item_state_change);
        }

        /**
         * Focus state of view bind to this ViewHolder is changed.
         *
         * @param focusState new focus state of view.
         * @param animate should apply a animate for this change? If not, just change
         *                the view immediately.
         */
        protected void onFocusStateChanged(@FocusState int focusState, boolean animate) {

            if (DesignConfig.DEBUG_RECYCLER_VIEW) {
                Log.d(TAG, getLogPrefix() + "focus state to " + focusState + ", animate " + animate + getLogSuffix());
            }

            if (itemView instanceof OnFocusStateChangedListener) {
                OnFocusStateChangedListener item = (OnFocusStateChangedListener) itemView;
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
                case FOCUS_STATE_INVALID:
                    throw new RuntimeException("focusState in invalidate!");
            }

            itemView.animate().cancel();
            if (animate) {
                itemView.animate()
                        .setDuration(defaultAnimDuration)
                        .alpha(alpha)
                        .scaleX(scale)
                        .scaleY(scale)
                        .start();
            } else {
                itemView.setScaleX(scale);
                itemView.setScaleY(scale);
                itemView.setAlpha(alpha);
            }
        }

        public long getDefaultAnimDuration() {
            return defaultAnimDuration;
        }

        protected final String getLogPrefix() {
            int layoutPosition = getLayoutPosition();
            int adapterPosition = getAdapterPosition();

            return String.format(Locale.getDefault(),
                    "<%d,%d %8x,%8x>: ",
                    adapterPosition, layoutPosition,
                    hashCode(), itemView.hashCode());
        }

        protected final String getLogSuffix() {
            return " with " + this + ", view " + itemView;
        }
    }
}

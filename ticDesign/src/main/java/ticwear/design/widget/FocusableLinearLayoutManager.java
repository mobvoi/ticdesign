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

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemAnimator.ItemAnimatorFinishedListener;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

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
public class FocusableLinearLayoutManager extends LinearLayoutManager
        implements TicklableLayoutManager {

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

    private final Context mContext;
    private final Handler mUiHandler;

    @Nullable
    private TicklableRecyclerView mTicklableRecyclerView;
    @Nullable
    private FocusLayoutHelper mFocusLayoutHelper;

    private ScrollVelocityTracker mScrollVelocityTracker;

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

    public FocusableLinearLayoutManager(Context context) {
        super(context, VERTICAL, false);

        mContext = context;
        mUiHandler = new Handler();

        mOnCentralPositionChangedListeners = new ArrayList<>();
        mPreviousCentral = RecyclerView.NO_POSITION;

        mScrollOffset = INVALID_SCROLL_OFFSET;

        mAppBarScrollController = new AppBarScrollController(mTicklableRecyclerView);

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
        boolean inFocus = mFocusLayoutHelper != null;
        if (inFocus == toFocus) {
            return;
        }

        if (toFocus && mTicklableRecyclerView != null) {
            mFocusLayoutHelper = new FocusLayoutHelper(mTicklableRecyclerView, this);
        } else if (mFocusLayoutHelper != null) {
            mFocusLayoutHelper.destroy();
            mFocusLayoutHelper = null;
        }

        // Restore offset
        restoreOffset();

        // Set flag so we will request focus state change on next layout.
        // Or, we will notify immediately.
        mFocusStateRequest.notifyOnNextLayout = true;

        if (getChildCount() > 0) {
            if (mFocusLayoutHelper != null) {
                requestNotifyChildrenAboutFocus();
            } else {
                requestNotifyChildrenAboutExit();
            }
        }

        requestSimpleAnimationsInNextLayout();


        if (mTicklableRecyclerView != null && mTicklableRecyclerView.getAdapter() != null) {
            mTicklableRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void restoreOffset() {
        if (mTicklableRecyclerView == null)
            return;

        mScrollResetting = true;
        if (mFocusLayoutHelper != null) {
            mScrollOffset = mTicklableRecyclerView.getTop();
            mTicklableRecyclerView.offsetTopAndBottom(-mScrollOffset);
            mTicklableRecyclerView.scrollBy(0, -mScrollOffset);
        } else {
            if (mScrollOffset != INVALID_SCROLL_OFFSET) {
                mTicklableRecyclerView.offsetTopAndBottom(mScrollOffset);
                mTicklableRecyclerView.scrollBy(0, mScrollOffset);
                mScrollOffset = INVALID_SCROLL_OFFSET;
            }
        }
        mScrollResetting = false;
    }

    @Override
    public void setTicklableRecyclerView(TicklableRecyclerView ticklableRecyclerView) {
        mTicklableRecyclerView = ticklableRecyclerView;
    }

    @Override
    public boolean validAdapter(Adapter adapter) {
        if (adapter != null) {
            RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(mTicklableRecyclerView,
                    adapter.getItemViewType(0));
            if (!(viewHolder instanceof ViewHolder)) {
                String msg = "adapter's ViewHolder should be instance of FocusableLinearLayoutManager.ViewHolder";
                if (DesignConfig.DEBUG) {
                    throw new IllegalArgumentException(msg);
                } else {
                    Log.w(TAG, msg);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean interceptPreScroll() {
        return mFocusLayoutHelper != null && mFocusLayoutHelper.interceptPreScroll();
    }

    @Override
    public boolean useScrollAsOffset() {
        return mFocusLayoutHelper != null;
    }

    @Override
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
     * @return the unconsumed offset (that needs to appending on raw offset).
     */
    @Override
    public int updateScrollOffset(int scrollOffset) {
        if (mTicklableRecyclerView == null ||
                this.mScrollOffset == INVALID_SCROLL_OFFSET ||
                mAppBarScrollController.isAppBarChanging()) {
            this.mScrollOffset = scrollOffset;
            return 0;
        }

        if (this.mScrollOffset == scrollOffset) {
            return 0;
        }

        int delta = scrollOffset - this.mScrollOffset;
        int scroll = -delta;

        int pre = mTicklableRecyclerView.computeVerticalScrollOffset();
        // Temporary disable nested scrolling.
        mTicklableRecyclerView.scrollBySkipNestedScroll(0, scroll);
        int real = mTicklableRecyclerView.computeVerticalScrollOffset() - pre;

        this.mScrollOffset -= real;

        return scrollOffset - this.mScrollOffset;
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
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
        mUiHandler.post(new Runnable() {
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
        return mFocusLayoutHelper == null && super.getClipToPadding();
    }

    @Override
    public int getPaddingTop() {
        return mFocusLayoutHelper != null ?
                mFocusLayoutHelper.getVerticalPadding() :
                super.getPaddingTop();
    }

    @Override
    public int getPaddingBottom() {
        return mFocusLayoutHelper != null ?
                mFocusLayoutHelper.getVerticalPadding() :
                super.getPaddingBottom();
    }

    @Override
    public boolean canScrollVertically() {
        return getChildCount() > 0;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrolled = super.scrollVerticallyBy(dy, recycler, state);

        if (scrolled == dy) {
            onScrollVerticalBy(dy);
        }

        return scrolled;
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (mFocusLayoutHelper != null) {
            mFocusLayoutHelper.onScrollStateChanged(state);
        } else {
            super.onScrollStateChanged(state);
        }
    }

    private void onScrollVerticalBy(int dy) {
        if (mScrollVelocityTracker == null && mFocusLayoutHelper != null) {
            int itemHeight = mFocusLayoutHelper.getCentralItemHeight();
            mScrollVelocityTracker =
                    new ScrollVelocityTracker(mContext, itemHeight);
        }

        boolean scrollFast = mScrollVelocityTracker != null &&
                mScrollVelocityTracker.addScroll(dy);

        if (getChildCount() > 0) {
            if (mFocusLayoutHelper != null) {
                requestNotifyChildrenAboutScroll(!scrollFast);
            } else {
                requestNotifyChildrenAboutExit();
            }
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        exitFocusStateIfNeed(e);

        return false;
    }

    @Override
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
                        mContext.getResources().getInteger(R.integer.design_time_action_idle_timeout));
                break;
        }
        return mFocusLayoutHelper != null && mFocusLayoutHelper.dispatchTouchSidePanelEvent(ev);
    }


    private void enterFocusStateIfNeed(@Nullable MotionEvent ev) {
        getHandler().removeCallbacks(exitFocusStateRunnable);
        if (mFocusLayoutHelper != null) {
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
        if (mFocusLayoutHelper == null) {
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

    void requestNotifyChildrenAboutScroll(boolean animate) {
        if (mScrollResetting || mFocusLayoutHelper == null) {
            return;
        }

        mFocusStateRequest.centerIndex = mFocusLayoutHelper.findCenterViewIndex();
        mFocusStateRequest.animate = animate;
        mFocusStateRequest.scroll = true;
        requestNotifyChildrenStateChanged(mFocusStateRequest);
    }

    void requestNotifyChildrenAboutFocus() {
        if (mScrollResetting || mFocusLayoutHelper == null) {
            return;
        }

        mFocusStateRequest.centerIndex = mFocusLayoutHelper.findCenterViewIndex();
        mFocusStateRequest.animate = true;
        mFocusStateRequest.scroll = false;
        requestNotifyChildrenStateChanged(mFocusStateRequest);
    }

    void requestNotifyChildrenAboutExit() {
        if (mScrollResetting) {
            return;
        }

        mFocusStateRequest.centerIndex = RecyclerView.NO_POSITION;
        mFocusStateRequest.animate = true;
        mFocusStateRequest.scroll = false;
        requestNotifyChildrenStateChanged(mFocusStateRequest);
    }

    private void requestNotifyChildrenStateChanged(final FocusStateRequest request) {
        if (request.notifyOnNextLayout) {
            return;
        }

        boolean isRunning = mTicklableRecyclerView != null &&
                mTicklableRecyclerView.getItemAnimator().isRunning(
                        new ItemAnimatorFinishedListener() {
                            @Override
                            public void onAnimationsFinished() {
                                if (mTicklableRecyclerView != null) {
                                    notifyChildrenStateChanged(mTicklableRecyclerView, request);
                                }
                            }
                        });

        if (DesignConfig.DEBUG_RECYCLER_VIEW) {
            Log.v(TAG, "request state changed with item anim running? " + isRunning);
        }
    }

    private void notifyChildrenStateChanged(@NonNull TicklableRecyclerView listView,
                                            FocusStateRequest request) {
        int top = ViewPropertiesHelper.getTop(listView);
        int bottom = ViewPropertiesHelper.getBottom(listView);
        int center = ViewPropertiesHelper.getCenterYPos(listView);

        for (int index = 0; index < getChildCount(); ++index) {
            View view = getChildAt(index);
            int focusState = FOCUS_STATE_NORMAL;
            if (request.centerIndex != RecyclerView.NO_POSITION) {
                focusState = index == request.centerIndex ?
                        FOCUS_STATE_CENTRAL :
                        FOCUS_STATE_NON_CENTRAL;
            }

            final boolean animateStateChange = view.isShown() && request.animate;
            notifyChildFocusStateChanged(listView, focusState, animateStateChange, view);

            if (focusState == FOCUS_STATE_NORMAL) {
                continue;
            }

            int childCenter = ViewPropertiesHelper.getCenterYPos(view);
            int halfChildHeight = view.getHeight() / 2;

            float progress = getCentralProgress(top + halfChildHeight, bottom - halfChildHeight, center, childCenter);
            ViewHolder viewHolder = (ViewHolder) listView.getChildViewHolder(view);
            final boolean animateProgressChange = view.isShown() && request.animate && !request.scroll;
            notifyChildProgressUpdated(viewHolder, progress, animateProgressChange);
        }

        notifyOnCentralPositionChanged(listView, request.centerIndex);
    }

    private float getCentralProgress(int top, int bottom, int center, int childCenter) {
        if (childCenter < top) {
            childCenter = top;
        }

        if (childCenter > bottom) {
            childCenter = bottom;
        }

        float progress;
        if (childCenter > center) {
            progress = (float) (bottom - childCenter) / (bottom - center);
        } else {
            progress = (float) (childCenter - top) / (center - top);
        }
        return progress;
    }

    private void notifyChildProgressUpdated(ViewHolder viewHolder, float progress, boolean animate) {
        long defaultDuration = viewHolder.getDefaultAnimDuration();
        long duration;

        // We have a animation in progress.
        if (viewHolder.animationStartTime > 0) {
            long timePassed = System.currentTimeMillis() - viewHolder.animationStartTime;
            viewHolder.animationPlayedTime += timePassed;

            if (viewHolder.animationPlayedTime >= defaultDuration) {
                // animation end.
                viewHolder.animationStartTime = 0;
                viewHolder.animationPlayedTime = 0;
                duration = animate ? defaultDuration : 0;
            } else {
                // animation in progress, always play the rest duration.
                duration = animate ?
                        defaultDuration :
                        defaultDuration - viewHolder.animationPlayedTime;
            }
        } else {
            duration = animate ? defaultDuration : 0;
            if (animate) {
                // If we update progress with animation and no anim before,
                // we enter the animation mode with a start time set.
                viewHolder.animationStartTime = System.currentTimeMillis();
                viewHolder.animationPlayedTime = 0;
            }
        }
        viewHolder.onCentralProgressUpdated(progress, duration);
    }

    private void notifyChildFocusStateChanged(@NonNull TicklableRecyclerView listView,
                                              int focusState, boolean animate, View view) {
        ViewHolder viewHolder = (ViewHolder) listView.getChildViewHolder(view);

        // Only call focus state change once.
        if (viewHolder.prevFocusState != focusState) {
            if (focusState == FOCUS_STATE_NORMAL) {
                viewHolder.itemView.setFocusable(false);
                viewHolder.itemView.setFocusableInTouchMode(false);
            } else {
                viewHolder.itemView.setFocusable(true);
                viewHolder.itemView.setFocusableInTouchMode(true);
            }
            viewHolder.onFocusStateChanged(focusState, animate);
            viewHolder.prevFocusState = focusState;
            view.setClickable(focusState != FOCUS_STATE_NON_CENTRAL);
        }
    }

    private void notifyOnCentralPositionChanged(@NonNull TicklableRecyclerView listView,
                                                int centerIndex) {
        int centerPosition = centerIndex == RecyclerView.NO_POSITION ?
                RecyclerView.NO_POSITION : listView.getChildAdapterPosition(getChildAt(centerIndex));

        if (centerPosition != mPreviousCentral) {
            for (OnCentralPositionChangedListener listener : mOnCentralPositionChangedListeners) {
                listener.onCentralPositionChanged(centerPosition);
            }

            mPreviousCentral = centerPosition;
        }
    }

    public Handler getHandler() {
        return mUiHandler;
    }

    private static class FocusStateRequest {
        public boolean notifyOnNextLayout;
        public int centerIndex;
        public boolean animate;
        public boolean scroll;

        public FocusStateRequest() {
            notifyOnNextLayout = false;
            centerIndex = -1;
            animate = false;
            scroll = false;
        }

        @Override
        public String toString() {
            return "FocusStateRequest@" + hashCode() + "{" +
                    "notifyOnNext " + notifyOnNextLayout +
                    ", center " + centerIndex +
                    ", animate " + animate +
                    ", scroll " + scroll +
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

    public interface OnCentralProgressUpdatedListener {

        /**
         * When we are in focus state, item will be notified by the progress of going central.
         *
         * @param progress progress from edge to center. The value is in [0, 1],
         *                 1 means right on the view's center, 0 means on view's edge.
         * @param animateDuration animate duration to that progress, if 0, means
         *                        no animate at all.
         */
        void onCentralProgressUpdated(float progress, long animateDuration);
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
     * An OnCentralPositionChangedListener can be set on a TicklableRecyclerView to receive messages
     * when a central position changed event has occurred on that TicklableRecyclerView when tickled.
     *
     * @see #addOnCentralPositionChangedListener(OnCentralPositionChangedListener)
     */
    public interface OnCentralPositionChangedListener {

        /**
         * Callback method to be invoked when TicklableRecyclerView's central item changed.
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

        private long animationStartTime;
        private long animationPlayedTime;

        private final long defaultAnimDuration;
        private final Interpolator focusInterpolator;

        public ViewHolder(View itemView) {
            super(itemView);
            prevFocusState = FOCUS_STATE_INVALID;
            animationStartTime = 0;
            animationPlayedTime = 0;
            defaultAnimDuration = itemView.getContext().getResources()
                    .getInteger(R.integer.design_anim_list_item_state_change);
            focusInterpolator = new AccelerateDecelerateInterpolator();
        }

        /**
         * When we are in focus state, item will be notified by the progress of going central.
         * Override this method to do more smooth animation.
         *
         * @param progress progress from edge to center. The value is in [0, 1],
         *                 1 means right on the view's center, 0 means on view's edge.
         * @param animateDuration animate duration to that progress, if 0, means
         */
        protected void onCentralProgressUpdated(float progress, long animateDuration) {
            if (itemView instanceof OnCentralProgressUpdatedListener) {
                OnCentralProgressUpdatedListener item = (OnCentralProgressUpdatedListener) itemView;
                item.onCentralProgressUpdated(progress, animateDuration);
            } else {
                float scaleMin = 1.0f;
                float scaleMax = 1.1f;
                float alphaMin = 0.6f;
                float alphaMax = 1.0f;

                float scale = scaleMin + (scaleMax - scaleMin) * progress;
                float alphaProgress = getFocusInterpolator().getInterpolation(progress);
                float alpha = alphaMin + (alphaMax - alphaMin) * alphaProgress;
                transform(scale, alpha, animateDuration);
            }
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
                if (focusState == FOCUS_STATE_NORMAL) {
                    transform(1.0f, 1.0f, animate ? getDefaultAnimDuration() : 0);
                }
            }
        }

        private void transform(float scale, float alpha, long duration) {
            itemView.animate().cancel();
            if (duration > 0) {
                itemView.animate()
                        .setDuration(duration)
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

        /**
         * If the transform needs a animation, use this duration for default.
         */
        public long getDefaultAnimDuration() {
            return defaultAnimDuration;
        }

        /**
         * get the interpolator for focus usage, use the interpolator to interpolation
         * the progress, so you can get a more obvious focus effect.
         *
         * @see #onCentralProgressUpdated
         */
        public Interpolator getFocusInterpolator() {
            return focusInterpolator;
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

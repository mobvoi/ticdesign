package ticwear.design.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    private final List<OnCentralPositionChangedListener> mOnCentralPositionChangedListeners;
    private int mPreviousCentral;

    public FocusableLinearLayoutManager(@NonNull  TicklableListView ticklableListView) {
        super(ticklableListView.getContext(), VERTICAL, false);

        mTicklableListView = ticklableListView;
        mFocusLayoutHelper = new FocusLayoutHelper(ticklableListView, this);
        mInFocusState = false;

        mOnCentralPositionChangedListeners = new ArrayList<>();
        mPreviousCentral = RecyclerView.NO_POSITION;
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

    public void setInFocusState(boolean inFocusState) {
        if (mInFocusState == inFocusState) {
            return;
        }

        this.mInFocusState = inFocusState;

        if (mInFocusState) {
            mFocusLayoutHelper.init();
            requestNotifyChildrenAboutProximity(false);
        } else {
            mFocusLayoutHelper.destroy();
            requestNotifyChildrenAboutExit(false);
        }

        requestSimpleAnimationsInNextLayout();
    }

    public boolean isInFocusState() {
        return mInFocusState;
    }

    public void setScrollResetting(boolean scrollResetting) {
        this.mScrollResetting = scrollResetting;
    }

    public void setVerticalPadding(int verticalPadding) {
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

        super.onDetachedFromWindow(view, recycler);
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

    void requestNotifyChildrenAboutProximity(boolean animate) {
        requestNotifyChildrenAboutProximity(mFocusLayoutHelper.findCenterViewIndex(), animate);
    }

    void requestNotifyChildrenAboutExit(boolean animate) {
        requestNotifyChildrenAboutProximity(RecyclerView.NO_POSITION, animate);
    }

    private void requestNotifyChildrenAboutProximity(int centerIndex, boolean animate) {
        if (mScrollResetting) {
            return;
        }

        for (int index = 0; index < getChildCount(); ++index) {
            View view = getChildAt(index);
            int focusState = FOCUS_STATE_NORMAL;
            if (centerIndex != RecyclerView.NO_POSITION) {
                focusState = index == centerIndex ?
                        FOCUS_STATE_CENTRAL :
                        FOCUS_STATE_NON_CENTRAL;
            }

            notifyChildFocusStateChanged(focusState, view.isShown() && animate, view);
        }

        notifyOnCentralPositionChanged(centerIndex);
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

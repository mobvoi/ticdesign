/*
 * Copyright (C) 2016 Mobvoi Inc.
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ticwear.design.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ticwear.design.DesignConfig;
import ticwear.design.R;

/**
 * AppBarLayout is a vertical {@link LinearLayout} which implements many of the features of
 * material designs app bar concept, namely scrolling gestures.
 * <p>
 * Children should provide their desired scrolling behavior through
 * {@link LayoutParams#setScrollFlags(int)} and the associated layout xml attribute:
 * {@code app:tic_layout_scrollFlags}.
 *
 * <p>
 * This view depends heavily on being used as a direct child within a {@link CoordinatorLayout}.
 * If you use AppBarLayout within a different {@link ViewGroup}, most of it's functionality will
 * not work.
 * <p>
 * AppBarLayout also requires a separate scrolling sibling in order to know when to scroll.
 * The binding is done through the {@link ScrollingViewBehavior} behavior class, meaning that you
 * should set your scrolling view's behavior to be an instance of {@link ScrollingViewBehavior}.
 * A string resource containing the full class name is available.
 *
 * <pre>
 * &lt;ticwear.design.widget.CoordinatorLayout
 *         xmlns:android=&quot;http://schemas.android.com/apk/res/android&quot;
 *         xmlns:app=&quot;http://schemas.android.com/apk/res-auto&quot;
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;android.support.v4.widget.NestedScrollView
 *             android:layout_width=&quot;match_parent&quot;
 *             android:layout_height=&quot;match_parent&quot;
 *             app:tic_layout_behavior=&quot;@string/tic_appbar_scrolling_view_behavior&quot;&gt;
 *
 *         &lt;!-- Your scrolling content --&gt;
 *
 *     &lt;/android.support.v4.widget.NestedScrollView&gt;
 *
 *     &lt;ticwear.design.widget.AppBarLayout
 *             android:layout_height=&quot;wrap_content&quot;
 *             android:layout_width=&quot;match_parent&quot;&gt;
 *
 *         &lt;android.support.v7.widget.Toolbar
 *                 ...
 *                 app:tic_layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *         &lt;ticwear.design.widget.TabLayout
 *                 ...
 *                 app:tic_layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *     &lt;/ticwear.design.widget.AppBarLayout&gt;
 *
 * &lt;/ticwear.design.widget.CoordinatorLayout&gt;
 * </pre>
 *
 * @see <a href="http://www.google.com/design/spec/layout/structure.html#structure-app-bar">
 *     http://www.google.com/design/spec/layout/structure.html#structure-app-bar</a>
 */
@CoordinatorLayout.DefaultBehavior(AppBarLayout.Behavior.class)
public class AppBarLayout extends LinearLayout {

    private static final String TAG = "ABL";

    private static final int PENDING_ACTION_NONE = 0x0;
    private static final int PENDING_ACTION_EXPANDED = 0x1;
    private static final int PENDING_ACTION_COLLAPSED = 0x2;
    private static final int PENDING_ACTION_ANIMATE_ENABLED = 0x4;

    /**
     * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
     * offset changes.
     */
    public interface OnOffsetChangedListener {
        /**
         * Called when the {@link AppBarLayout}'s layout offset has been changed. This allows
         * child views to implement custom behavior based on the offset (for instance pinning a
         * view at a certain y value).
         *
         * @param appBarLayout the {@link AppBarLayout} which offset has changed
         * @param verticalOffset the vertical offset for the parent {@link AppBarLayout}, in px
         */
        void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset);
    }

    private static final int INVALID_SCROLL_RANGE = -1;
    private static final int INVALID_VIEW_HEIGHT = -1;

    private int mTotalScrollRange = INVALID_SCROLL_RANGE;
    private int mDownPreScrollRange = INVALID_SCROLL_RANGE;
    private int mDownScrollRange = INVALID_SCROLL_RANGE;

    private boolean mShouldConsumePreScroll = false;

    boolean mHaveChildWithInterpolator;
    boolean mHaveChildWithResistance;

    private float mTargetElevation;

    private int mPendingAction = PENDING_ACTION_NONE;

    private WindowInsetsCompat mLastInsets;

    private final List<OnOffsetChangedListener> mListeners;

    public AppBarLayout(Context context) {
        this(context, null);
    }

    public AppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppBarLayout,
                0, R.style.Widget_Ticwear_AppBarLayout);
        mTargetElevation = a.getDimensionPixelSize(R.styleable.AppBarLayout_android_elevation, 0);
        setBackgroundDrawable(a.getDrawable(R.styleable.AppBarLayout_android_background));
        if (a.hasValue(R.styleable.AppBarLayout_tic_expanded)) {
            setExpanded(a.getBoolean(R.styleable.AppBarLayout_tic_expanded, false));
        }
        a.recycle();

        // Use the bounds view outline provider so that we cast a shadow, even without a background
        setOutlineProvider(ViewOutlineProvider.BOUNDS);

        mListeners = new ArrayList<>();

        ViewCompat.setElevation(this, mTargetElevation);

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        if (isShown()) {
                            setWindowInsets(insets);
                            return insets.consumeSystemWindowInsets();
                        } else {
                            return insets;
                        }
                    }
                });
    }

    /**
     * Add a listener that will be called when the offset of this {@link AppBarLayout} changes.
     *
     * @param listener The listener that will be called when the offset changes.]
     *
     * @see #removeOnOffsetChangedListener(OnOffsetChangedListener)
     */
    public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Remove the previously added {@link OnOffsetChangedListener}.
     *
     * @param listener the listener to remove.
     */
    public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        invalidateScrollRanges();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        invalidateScrollRanges();

        mHaveChildWithInterpolator = false;
        mHaveChildWithResistance = false;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
            final Interpolator interpolator = childLp.getScrollInterpolator();
            final float resistanceFactor = childLp.getScrollResistanceFactor();

            if (interpolator != null) {
                mHaveChildWithInterpolator = true;
                if (mHaveChildWithResistance) {
                    break;
                }
            }

            if (resistanceFactor > 0 &&
                    overScrollBounceEnabled(childLp)) {
                mHaveChildWithResistance = true;
                if (mHaveChildWithInterpolator) {
                    break;
                }
            }
        }
    }

    private static boolean overScrollBounceEnabled(LayoutParams childLp) {
        return (childLp.getScrollFlags() & LayoutParams.FLAG_OVERSCROLL) == LayoutParams.FLAG_OVERSCROLL;
    }

    private void invalidateScrollRanges() {
        // Invalidate the scroll ranges
        mTotalScrollRange = INVALID_SCROLL_RANGE;
        mDownPreScrollRange = INVALID_SCROLL_RANGE;
        mDownScrollRange = INVALID_SCROLL_RANGE;
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation != VERTICAL) {
            throw new IllegalArgumentException("AppBarLayout is always vertical and does"
                    + " not support horizontal orientation");
        }
        super.setOrientation(orientation);
    }

    /**
     * Sets whether this {@link AppBarLayout} is expanded or not, animating if it has already
     * been laid out.
     *
     * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a
     * direct child of a {@link CoordinatorLayout}.</p>
     *
     * @param expanded true if the layout should be fully expanded, false if it should
     *                 be fully collapsed
     *
     * @attr ref R.styleable#AppBarLayout_expanded
     */
    public void setExpanded(boolean expanded) {
        setExpanded(expanded, ViewCompat.isLaidOut(this));
    }

    /**
     * Sets whether this {@link AppBarLayout} is expanded or not.
     *
     * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a
     * direct child of a {@link CoordinatorLayout}.</p>
     *
     * @param expanded true if the layout should be fully expanded, false if it should
     *                 be fully collapsed
     * @param animate Whether to animate to the new state
     *
     * @attr ref R.styleable#AppBarLayout_expanded
     */
    public void setExpanded(boolean expanded, boolean animate) {
        mPendingAction = (expanded ? PENDING_ACTION_EXPANDED : PENDING_ACTION_COLLAPSED)
                | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0);
        requestLayout();
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs, isInEditMode());
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LinearLayout.LayoutParams) {
            return new LayoutParams((LinearLayout.LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    private boolean hasChildWithInterpolator() {
        return mHaveChildWithInterpolator;
    }

    public boolean hasChildWithResistance() {
        return mHaveChildWithResistance;
    }

    /**
     * Returns the scroll range of all children.
     *
     * @return the scroll range in px
     */
    public final int getTotalScrollRange() {
        if (mTotalScrollRange != INVALID_SCROLL_RANGE) {
            return mTotalScrollRange;
        }

        int range = 0;
        int currentTop = getTop();
        boolean allScroll = true;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int childBottom = getTop() + child.getBottom();
            final int flags = lp.mScrollFlags;

            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                // We're set to scroll so add the child's height
                range += childBottom - currentTop;
                currentTop = childBottom;

                if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // For a collapsing scroll, we to take the collapsed height into account.
                    // We also break straight away since later views can't scroll beneath
                    // us
                    range -= ViewCompat.getMinimumHeight(child);
                    allScroll = false;
                    break;
                }
            } else {
                allScroll = false;
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
        if (allScroll) {
            range += getBottom() - currentTop;
        }
        return mTotalScrollRange = Math.max(0, range - getTopInset());
    }

    private boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    /**
     * Return the scroll range when scrolling up from a nested pre-scroll.
     */
    private int getUpNestedPreScrollRange() {
        return getTotalScrollRange();
    }

    /**
     * Return the scroll range when scrolling down from a nested pre-scroll.
     */
    private int getDownNestedPreScrollRange() {
        return getDownNestedPreScrollRange(false);
    }

    private int getDownNestedPreScrollRange(boolean consumePreScroll) {
        if (consumePreScroll != mShouldConsumePreScroll) {
            mDownPreScrollRange = INVALID_SCROLL_RANGE;
            mShouldConsumePreScroll = consumePreScroll;
        }
        if (mDownPreScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return mDownPreScrollRange;
        }

        int range = 0;
        int currentBottom = getBottom();
        boolean allScroll = true;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int flags = lp.mScrollFlags;

            if (consumePreScroll || (flags & LayoutParams.FLAG_QUICK_RETURN) == LayoutParams.FLAG_QUICK_RETURN) {
                final int childTop = getTop() + child.getTop() - lp.topMargin;
                // First take the margin into account
                // The view has the quick return flag combination...
                if ((flags & LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0) {
                    // If they're set to enter collapsed, use the minimum height
                    int collapsedTop = getTop() + child.getBottom() - ViewCompat.getMinimumHeight(child);
                    range += currentBottom - collapsedTop;
                } else {
                    // Else use the full height
                    range += currentBottom - childTop;
                }
                currentBottom = childTop;
            } else if (range > 0) {
                // If we've hit an non-quick return scrollable view, and we've already hit a
                // quick return view, return now
                allScroll = false;
                break;
            } else {
                allScroll = false;
            }
        }
        if (allScroll) {
            range += currentBottom - getTop();
        }
        return mDownPreScrollRange = range;
    }

    /**
     * Return the scroll range when scrolling down from a nested scroll.
     */
    private int getDownNestedScrollRange() {
        if (mDownScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return mDownScrollRange;
        }

        int range = 0;
        int currentTop = getTop();
        boolean allScroll = true;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int childBottom = getTop() + child.getBottom();

            final int flags = lp.mScrollFlags;

            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                // We're set to scroll so add the child's height
                range += childBottom - currentTop;
                currentTop = childBottom;

                if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // For a collapsing exit scroll, we to take the collapsed height into account.
                    // We also break the range straight away since later views can't scroll
                    // beneath us
                    range -= ViewCompat.getMinimumHeight(child) + getTopInset();
                    allScroll = false;
                    break;
                }
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                allScroll = false;
                break;
            }
        }
        if (allScroll) {
            range += getBottom() - currentTop;
        }
        return mDownScrollRange = Math.max(0, range);
    }

    final int getMinimumHeightForVisibleOverlappingContent() {
        final int topInset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight != 0) {
            // If this layout has a min height, use it (doubled)
            return (minHeight * 2) + topInset;
        }

        // Otherwise, we'll use twice the min height of our last child
        final int childCount = getChildCount();
        return childCount >= 1
                ? (ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) * 2) + topInset
                : 0;
    }

    /**
     * Set the elevation value to use when this {@link AppBarLayout} should be elevated
     * above content.
     * <p>
     * This method does not do anything itself. A typical use for this method is called from within
     * an {@link OnOffsetChangedListener} when the offset has changed in such a way to require an
     * elevation change.
     *
     * @param elevation the elevation value to use.
     *
     * @see ViewCompat#setElevation(View, float)
     */
    public void setTargetElevation(float elevation) {
        mTargetElevation = elevation;
    }

    /**
     * Returns the elevation value to use when this {@link AppBarLayout} should be elevated
     * above content.
     */
    public float getTargetElevation() {
        return mTargetElevation;
    }

    private int getPendingAction() {
        return mPendingAction;
    }

    private void resetPendingAction() {
        mPendingAction = PENDING_ACTION_NONE;
    }

    private int getTopInset() {
        return mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
    }

    private void setWindowInsets(WindowInsetsCompat insets) {
        // Invalidate the total scroll range...
        mTotalScrollRange = INVALID_SCROLL_RANGE;
        mLastInsets = insets;

        // Now dispatch them to our children
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            insets = ViewCompat.dispatchApplyWindowInsets(child, insets);
            if (insets.isConsumed()) {
                break;
            }
        }
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {

        /** @hide */
        @IntDef(flag=true, value={
                SCROLL_FLAG_SCROLL,
                SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
                SCROLL_FLAG_ENTER_ALWAYS,
                SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED,
                SCROLL_FLAG_SNAP,
                SCROLL_FLAG_OVER_SCROLL_BOUNCE
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface ScrollFlags {}

        /**
         * The view will be scroll in direct relation to scroll events. This flag needs to be
         * set for any of the other flags to take effect. If any sibling views
         * before this one do not have this flag, then this value has no effect.
         */
        public static final int SCROLL_FLAG_SCROLL = 0x1;

        /**
         * When exiting (scrolling off screen) the view will be scrolled until it is
         * 'collapsed'. The collapsed height is defined by the view's minimum height.
         *
         * @see ViewCompat#getMinimumHeight(View)
         * @see View#setMinimumHeight(int)
         */
        public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 0x2;

        /**
         * When entering (scrolling on screen) the view will scroll on any downwards
         * scroll event, regardless of whether the scrolling view is also scrolling. This
         * is commonly referred to as the 'quick return' pattern.
         */
        public static final int SCROLL_FLAG_ENTER_ALWAYS = 0x4;

        /**
         * An additional flag for 'enterAlways' which modifies the returning view to
         * only initially scroll back to it's collapsed height. Once the scrolling view has
         * reached the end of it's scroll range, the remainder of this view will be scrolled
         * into view. The collapsed height is defined by the view's minimum height.
         *
         * @see ViewCompat#getMinimumHeight(View)
         * @see View#setMinimumHeight(int)
         */
        public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 0x8;

        /**
         * Upon a scroll ending, if the view is only partially visible then it will be snapped
         * and scrolled to it's closest edge. For example, if the view only has it's bottom 25%
         * displayed, it will be scrolled off screen completely. Conversely, if it's bottom 75%
         * is visible then it will be scrolled fully into view.
         */
        public static final int SCROLL_FLAG_SNAP = 0x10;

        /**
         * Use when overScrollBounce is set, represent the scroll resistance. The value is between
         * [0, 1], the view is harder to change if the factor is smaller. When factor is 1, there
         * is no resistance effect, scroll follow the touch, when is 0, the view is stuck and can't
         * be stretching. The resistance effect is defined by
         * {@link R.attr#tic_layout_scrollResistanceFactor}.
         */
        public static final int SCROLL_FLAG_OVER_SCROLL_BOUNCE = 0x20;

        /**
         * Internal flags which allows quick checking features
         */
        static final int FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS;
        static final int FLAG_SNAP = SCROLL_FLAG_SCROLL | SCROLL_FLAG_SNAP;
        static final int FLAG_OVERSCROLL = SCROLL_FLAG_SCROLL | SCROLL_FLAG_OVER_SCROLL_BOUNCE;

        int mScrollFlags = SCROLL_FLAG_SCROLL;
        Interpolator mScrollInterpolator;

        float mScrollResistanceFactor;
        int mScrollOffsetLimit;
        int mOverScrollOriginalHeight = INVALID_VIEW_HEIGHT;

        public LayoutParams(Context c, AttributeSet attrs, boolean isInEditMode) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.AppBarLayout_LayoutParams);
            mScrollFlags = a.getInt(R.styleable.AppBarLayout_LayoutParams_tic_layout_scrollFlags, 0);
            if (a.hasValue(R.styleable.AppBarLayout_LayoutParams_tic_layout_scrollInterpolator)) {
                int resId = a.getResourceId(
                        R.styleable.AppBarLayout_LayoutParams_tic_layout_scrollInterpolator, 0);
                mScrollInterpolator = android.view.animation.AnimationUtils.loadInterpolator(
                        c, resId);
            }

            float defaultFactor;
            int defaultOffsetLimit;
            if (isInEditMode) {
                defaultFactor = 0;
                defaultOffsetLimit = Integer.MAX_VALUE;
            } else {
                TypedValue typedValue = new TypedValue();
                c.getResources().getValue(R.integer.design_factor_over_scroll_bounce, typedValue, true);
                defaultFactor = typedValue.getFloat();
                defaultOffsetLimit = c.getResources().getDimensionPixelOffset(R.dimen.design_over_scroll_limit);
            }
            mScrollResistanceFactor = MathUtils.constrain(
                    a.getFloat(R.styleable.AppBarLayout_LayoutParams_tic_layout_scrollResistanceFactor,
                            defaultFactor),
                    0, 1);
            mScrollOffsetLimit = a.getDimensionPixelOffset(
                    R.styleable.AppBarLayout_LayoutParams_tic_layout_overScrollLimit,
                    defaultOffsetLimit);

            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LinearLayout.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            mScrollFlags = source.mScrollFlags;
            mScrollInterpolator = source.mScrollInterpolator;
            mScrollResistanceFactor = source.mScrollResistanceFactor;
            mScrollOffsetLimit = source.mScrollOffsetLimit;
        }

        /**
         * Set the scrolling flags.
         *
         * @param flags bitwise int of {@link #SCROLL_FLAG_SCROLL},
         *             {@link #SCROLL_FLAG_EXIT_UNTIL_COLLAPSED}, {@link #SCROLL_FLAG_ENTER_ALWAYS},
         *             {@link #SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED}, {@link #SCROLL_FLAG_SNAP } and
         *             {@link #SCROLL_FLAG_OVER_SCROLL_BOUNCE}.
         *
         * @see #getScrollFlags()
         *
         * @attr ref R.styleable#AppBarLayout_LayoutParams_tic_layout_scrollFlags
         */
        public void setScrollFlags(@ScrollFlags int flags) {
            mScrollFlags = flags;
        }

        /**
         * Returns the scrolling flags.
         *
         * @see #setScrollFlags(int)
         *
         * @attr ref R.styleable#AppBarLayout_LayoutParams_tic_layout_scrollFlags
         */
        @ScrollFlags
        public int getScrollFlags() {
            return mScrollFlags;
        }

        /**
         * Set the interpolator to when scrolling the view associated with this
         * {@link LayoutParams}.
         *
         * @param interpolator the interpolator to use, or null to use normal 1-to-1 scrolling.
         *
         * @attr ref R.styleable#AppBarLayout_LayoutParams_tic_layout_scrollInterpolator
         * @see #getScrollInterpolator()
         */
        public void setScrollInterpolator(Interpolator interpolator) {
            mScrollInterpolator = interpolator;
        }

        /**
         * Returns the {@link Interpolator} being used for scrolling the view associated with this
         * {@link LayoutParams}. Null indicates 'normal' 1-to-1 scrolling.
         *
         * @attr ref R.styleable#AppBarLayout_LayoutParams_tic_layout_scrollInterpolator
         * @see #setScrollInterpolator(Interpolator)
         */
        public Interpolator getScrollInterpolator() {
            return mScrollInterpolator;
        }


        /**
         * Returns the resistance factor being used for over-scroll resistance effect, the value is
         * between [0, 1] 1 indicates 'normal' 1-to-1 scrolling, 0 indicates no scrolling at all.
         *
         * @attr ref R.styleable#AppBarLayout_LayoutParams_tic_layout_scrollResistanceFactor
         * @see #setScrollResistanceFactor(float)
         */
        public float getScrollResistanceFactor() {
            return mScrollResistanceFactor;
        }


        /**
         * Set the resistance factor used for over-scroll resistance effect associated with this
         * {@link LayoutParams}.
         *
         * @param resistanceFactor the factor of resistance, the value is between [0, 1],
         *                         1 indicates 'normal' 1-to-1 scrolling, 0 indicates no scrolling
         *                         at all.
         *
         * @attr ref R.styleable#AppBarLayout_LayoutParams_tic_layout_scrollResistanceFactor
         * @see #getScrollResistanceFactor()
         */
        public void setScrollResistanceFactor(float resistanceFactor) {
            this.mScrollResistanceFactor = MathUtils.constrain(resistanceFactor, 0, 1);
        }

        /**
         * Returns the limit factor being used for over-scroll offset limit.
         *
         * @attr ref R.styleable#AppBarLayout_LayoutParams_tic_layout_overScrollLimit
         * @see #setScrollOffsetLimit(int)
         */
        public int getScrollOffsetLimit() {
            return mScrollOffsetLimit;
        }


        /**
         * Set the over scroll limit when overScrollBounce is set.
         * {@link LayoutParams}.
         *
         * @param offsetLimit the over scroll limit. default value is defined in
         *                    {@link R.dimen#design_over_scroll_limit}.
         *
         * @attr ref R.styleable#AppBarLayout_LayoutParams_tic_layout_overScrollLimit
         * @see #getScrollOffsetLimit()
         */
        public void setScrollOffsetLimit(int offsetLimit) {
            this.mScrollOffsetLimit = MathUtils.constrain(offsetLimit, 0, 1);
        }
    }

    /**
     * The default {@link Behavior} for {@link AppBarLayout}. Implements the necessary nested
     * scroll handling with offsetting.
     */
    public static class Behavior extends HeaderBehavior<AppBarLayout> {
        private static final int ANIMATE_OFFSET_DIPS_PER_SECOND = 300;
        private static final int INVALID_POSITION = -1;

        /**
         * Callback to allow control over any {@link AppBarLayout} dragging.
         */
        public static abstract class DragCallback {
            /**
             * Allows control over whether the given {@link AppBarLayout} can be dragged or not.
             *
             * <p>Dragging is defined as a direct touch on the AppBarLayout with movement. This
             * call does not affect any nested scrolling.</p>
             *
             * @return true if we are in a position to scroll the AppBarLayout via a drag, false
             *         if not.
             */
            public abstract boolean canDrag(@NonNull AppBarLayout appBarLayout);
        }

        private int mOffsetDelta;
        private int mOverScrollDelta;

        // Track over-scroll down event when scroll, to avoid set offset when pre-scroll.
        private boolean mSiblingOverScrollDown;

        private boolean mSkipNestedPreScroll;
        private boolean mWasFlung;

        private ValueAnimator mAnimator;

        private int mOffsetToChildIndexOnLayout = INVALID_POSITION;
        private boolean mOffsetToChildIndexOnLayoutIsMinHeight;
        private float mOffsetToChildIndexOnLayoutPerc;

        private WeakReference<View> mLastNestedScrollingChildRef;
        private DragCallback mOnDragCallback;

        int mOverScrollOriginalHeight = INVALID_VIEW_HEIGHT;

        private ScrollViewFlingChecker mAppBarFlingChecker;

        public Behavior() {}

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout abl,
                View directTargetChild, View target, int nestedScrollAxes) {
            // Return true if we're nested scrolling vertically, and we have scrollable children
            // and the scrolling view is big enough to scroll
            final boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                    && abl.hasScrollableChildren()
                    && parent.getHeight() - directTargetChild.getHeight() <= abl.getHeight();

            if (started && mAnimator != null) {
                // Cancel any offset animation
                mAnimator.cancel();
            } else {
                for (int i = 0, count = abl.getChildCount(); i < count; i++) {
                    View child = abl.getChildAt(i);
                    final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                    if (overScrollBounceEnabled(childLp)) {
                        if (childLp.mOverScrollOriginalHeight == INVALID_VIEW_HEIGHT) {
                            childLp.mOverScrollOriginalHeight = child.getMeasuredHeight();
                        }
                    }
                }
                if (abl.hasChildWithResistance() &&
                        mOverScrollOriginalHeight == INVALID_VIEW_HEIGHT) {
                    mOverScrollOriginalHeight = abl.getMeasuredHeight();
                }
            }

            // A new nested scroll has started so clear out the previous ref
            mLastNestedScrollingChildRef = null;

            mSiblingOverScrollDown = false;
            // Reset over-scroll offset
            // TODO: reset according to current height & original height diff.
            mOverScrollDelta = 0;

            return started;
        }

        @Override
        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                View target, int dx, int dy, int[] consumed) {
            // Nested scroll only handle when dy < 0, so we only skip pre-scroll when dy < 0;
            boolean shouldSkipNestedPreScroll = mSkipNestedPreScroll && dy < 0;
            if (dy != 0 && !shouldSkipNestedPreScroll) {
                int min, max;
                if (dy < 0) {
                    // We're scrolling down
                    min = -child.getTotalScrollRange();
                    max = min + child.getDownNestedPreScrollRange(
                            consumePreScroll(coordinatorLayout, target));
                } else {
                    // We're scrolling up
                    min = -child.getUpNestedPreScrollRange();
                    max = 0;
                }
                consumed[1] = scroll(coordinatorLayout, child, dy, min, max);
            }
        }

        private boolean consumePreScroll(CoordinatorLayout coordinatorLayout, View target) {
            ViewGroup.LayoutParams lp = target.getLayoutParams();
            if (lp instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) lp).getBehavior();
                if (behavior != null) {
                    return behavior.requestInterceptPreScroll(coordinatorLayout);
                }
            }
            return false;
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                View target, int dxConsumed, int dyConsumed,
                int dxUnconsumed, int dyUnconsumed, int[] consumed) {

            if (dyUnconsumed < 0) {
                mSiblingOverScrollDown = true;
                // If the scrolling view is scrolling down but not consuming, it's probably be at
                // the top of it's content
                consumed[1] = scroll(coordinatorLayout, child, dyUnconsumed,
                        -child.getDownNestedScrollRange(), 0);
                // Set the expanding flag so that onNestedPreScroll doesn't handle any events
                mSkipNestedPreScroll = true;
            } else {
                // As we're no longer handling nested scrolls, reset the skip flag
                mSkipNestedPreScroll = false;
            }
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout abl,
                View target) {
            if (!snapToZeroOffsetIfNeeded(coordinatorLayout, abl) && !mWasFlung) {
                // If we haven't been flung then let's see if the current view has been set to snap
                snapToChildIfNeeded(coordinatorLayout, abl);
            }

            // Reset the flags
            mSkipNestedPreScroll = false;
            mWasFlung = false;
            // Keep a reference to the previous nested scrolling child
            mLastNestedScrollingChildRef = new WeakReference<>(target);
        }

        @Override
        public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                                        View target, float velocityX, float velocityY) {

            // If we're scrolling up and the child also consumed the fling. We'll fake scroll
            // upto our 'collapsed' offset
            if (velocityY < 0) {
                // We're scrolling down
                final int targetScroll = -child.getTotalScrollRange()
                        + child.getDownNestedPreScrollRange(
                        consumePreScroll(coordinatorLayout, target));
                if (getTopBottomOffsetForScrollingSibling() < targetScroll) {
                    // If we're currently not expanded more than the target scroll, we'll
                    // animate a fling
                    animateOffsetTo(coordinatorLayout, child, targetScroll);
                }
            } else {
                // We're scrolling up
                final int targetScroll = -child.getUpNestedPreScrollRange();
                if (getTopBottomOffsetForScrollingSibling() > targetScroll) {
                    // If we're currently not expanded less than the target scroll, we'll
                    // animate a fling
                    animateOffsetTo(coordinatorLayout, child, targetScroll);
                }
            }

            // don't handle the pre-fling to let target fling.
            return false;
        }

        @Override
        public boolean onNestedFling(final CoordinatorLayout coordinatorLayout,
                final AppBarLayout child, View target, float velocityX, float velocityY,
                boolean consumed) {
            boolean flung = false;

            if (!consumed || consumePreScroll(coordinatorLayout, target)) {
                // It hasn't consumed by target or should consumed by ourselves, fling with full scroll
                flung = fling(coordinatorLayout, child, -child.getTotalScrollRange(),
                        0, -velocityY);
            }

            mWasFlung = flung;
            return flung;
        }

        /**
         * Set a callback to control any {@link AppBarLayout} dragging.
         *
         * @param callback the callback to use, or {@code null} to use the default behavior.
         */
        public void setDragCallback(@Nullable DragCallback callback) {
            mOnDragCallback = callback;
        }

        private void animateOffsetTo(final CoordinatorLayout coordinatorLayout,
                final AppBarLayout child, final int offset) {
            final int currentOffset = getTopBottomOffsetForScrollingSibling();
            animateOffsetTo(coordinatorLayout, child, currentOffset, offset);
        }

        private void animateOffsetTo(final CoordinatorLayout coordinatorLayout,
                final AppBarLayout child, final int currentOffset, final int offset) {
            if (currentOffset == offset) {
                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.cancel();
                }
                return;
            }

            if (mAnimator == null) {
                mAnimator = new ValueAnimator();
                mAnimator.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        setHeaderTopBottomOffset(coordinatorLayout, child,
                                (int) animator.getAnimatedValue());
                    }
                });
            } else {
                mAnimator.cancel();
            }

            // Set the duration based on the amount of dips we're travelling in
            final float distanceDp = Math.abs(currentOffset - offset) /
                    coordinatorLayout.getResources().getDisplayMetrics().density;
            mAnimator.setDuration(Math.round(distanceDp * 1000 / ANIMATE_OFFSET_DIPS_PER_SECOND));

            mAnimator.setIntValues(currentOffset, offset);
            mAnimator.start();
        }

        private View getChildOnOffset(AppBarLayout abl, final int offset) {
            for (int i = 0, count = abl.getChildCount(); i < count; i++) {
                View child = abl.getChildAt(i);
                if (child.getTop() <= -offset && child.getBottom() >= -offset) {
                    return child;
                }
            }
            return null;
        }

        @Override
        boolean snapToZeroOffsetIfNeeded(CoordinatorLayout coordinatorLayout, AppBarLayout abl) {
            if (needSnapToZero()) {
                int deltaHeight = 0;
                if (abl.hasChildWithResistance() && mOverScrollOriginalHeight != INVALID_VIEW_HEIGHT) {
                    deltaHeight = abl.getMeasuredHeight() - mOverScrollOriginalHeight;
                }
                // TODO: adjust animation duration for long distance snap
                animateOffsetTo(coordinatorLayout, abl, deltaHeight, 0);
                return true;
            }

            return false;
        }

        private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, AppBarLayout abl) {
            final int offset = getTopBottomOffsetForScrollingSibling();
            final View offsetChild = getChildOnOffset(abl, offset);
            if (offsetChild != null) {
                final LayoutParams lp = (LayoutParams) offsetChild.getLayoutParams();
                if ((lp.getScrollFlags() & LayoutParams.FLAG_SNAP) == LayoutParams.FLAG_SNAP) {
                    // We're set the snap, so animate the offset to the nearest edge
                    int childTop = -offsetChild.getTop();
                    int childBottom = -offsetChild.getBottom();

                    // If the view is set only exit until it is collapsed, we'll abide by that
                    if ((lp.getScrollFlags() & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
                            == LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) {
                        childBottom += ViewCompat.getMinimumHeight(offsetChild);
                    }

                    final int newOffset = offset < (childBottom + childTop) / 2
                            ? childBottom : childTop;
                    animateOffsetTo(coordinatorLayout, abl,
                            MathUtils.constrain(newOffset, -abl.getTotalScrollRange(), 0));
                }
            }
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, AppBarLayout abl,
                int layoutDirection) {
            boolean handled = super.onLayoutChild(parent, abl, layoutDirection);

            final int pendingAction = abl.getPendingAction();
            if (pendingAction != PENDING_ACTION_NONE) {
                final boolean animate = (pendingAction & PENDING_ACTION_ANIMATE_ENABLED) != 0;
                if ((pendingAction & PENDING_ACTION_COLLAPSED) != 0) {
                    final int offset = -abl.getUpNestedPreScrollRange();
                    if (animate) {
                        animateOffsetTo(parent, abl, offset);
                    } else {
                        setHeaderTopBottomOffset(parent, abl, offset);
                    }
                } else if ((pendingAction & PENDING_ACTION_EXPANDED) != 0) {
                    if (animate) {
                        animateOffsetTo(parent, abl, 0);
                    } else {
                        setHeaderTopBottomOffset(parent, abl, 0);
                    }
                }
            } else if (mOffsetToChildIndexOnLayout >= 0) {
                View child = abl.getChildAt(mOffsetToChildIndexOnLayout);
                int offset = -child.getBottom();
                if (mOffsetToChildIndexOnLayoutIsMinHeight) {
                    offset += ViewCompat.getMinimumHeight(child);
                } else {
                    offset += Math.round(child.getHeight() * mOffsetToChildIndexOnLayoutPerc);
                }
                setTopAndBottomOffset(offset);
            }

            // Finally reset any pending states
            abl.resetPendingAction();
            mOffsetToChildIndexOnLayout = INVALID_POSITION;

            // Make sure we update the elevation
            dispatchOffsetUpdates(abl);

            return handled;
        }

        @Override
        boolean canDragView(AppBarLayout view) {
            if (mOnDragCallback != null) {
                // If there is a drag callback set, it's in control
                return mOnDragCallback.canDrag(view);
            }

            // Else we'll use the default behaviour of seeing if it can scroll down
            if (mLastNestedScrollingChildRef != null) {
                // If we have a reference to a scrolling view, check it
                final View scrollingView = mLastNestedScrollingChildRef.get();
                return scrollingView != null && scrollingView.isShown()
                        && !ViewCompat.canScrollVertically(scrollingView, -1);
            } else {
                // Otherwise we assume that the scrolling view hasn't been scrolled and can drag.
                return true;
            }
        }

        @Override
        int getMaxDragOffset(AppBarLayout view) {
            return -view.getDownNestedScrollRange();
        }

        @Override
        int getScrollRangeForDragFling(AppBarLayout view) {
            return view.getTotalScrollRange();
        }

        @Override
        int setHeaderTopBottomOffset(CoordinatorLayout coordinatorLayout,
                AppBarLayout appBarLayout, int targetOffset, int minOffset, int maxOffset) {
            final int curOffset = getTopBottomOffsetForScrollingSibling();
            int consumed = 0;
            final int newOffset = MathUtils.constrain(targetOffset, minOffset, maxOffset);

            final boolean overScrollDown = targetOffset > 0 && newOffset >= 0;
            // sibling is in over-scroll mode (not pre-scroll) and appbar is in over-scroll
            // (all content have been offset out), we now can do over-scroll stretch effect.
            final boolean overScrollStretch = overScrollDown && mSiblingOverScrollDown &&
                    appBarLayout.hasChildWithResistance();

            if (overScrollStretch) {
                resistanceSizeChange(appBarLayout, targetOffset);
                consumed = - (targetOffset - mOverScrollDelta);

                // Update the stored sibling offset
                mOverScrollDelta = targetOffset - newOffset;
            } else if (minOffset != 0 && curOffset >= minOffset
                    && curOffset <= maxOffset) {
                // If we have some scrolling range, and we're currently within the min and max
                // offsets, calculate a new offset
                if (curOffset != newOffset) {
                    final int interpolatedOffset = appBarLayout.hasChildWithInterpolator()
                            ? interpolateOffset(appBarLayout, newOffset)
                            : newOffset;

                    boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);

                    // Update how much dy we have consumed
                    consumed = curOffset - newOffset;
                    // Update the stored sibling offset
                    mOffsetDelta = newOffset - interpolatedOffset;

                    if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
                        // If the offset hasn't changed and we're using an interpolated scroll
                        // then we need to keep any dependent views updated. CoL will do this for
                        // us when we move, but we need to do it manually when we don't (as an
                        // interpolated scroll may finish early).
                        coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
                    }

                    // Dispatch the updates to any listeners
                    dispatchOffsetUpdates(appBarLayout);
                }
                mOverScrollDelta = 0;
            }

            return consumed;
        }

        private void dispatchOffsetUpdates(AppBarLayout layout) {
            final List<OnOffsetChangedListener> listeners = layout.mListeners;

            // Iterate backwards through the list so that most recently added listeners
            // get the first chance to decide
            for (int i = 0, z = listeners.size(); i < z; i++) {
                final OnOffsetChangedListener listener = listeners.get(i);
                if (listener != null) {
                    listener.onOffsetChanged(layout, getTopAndBottomOffset());
                }
            }
        }

        private int interpolateOffset(AppBarLayout layout, final int offset) {
            final int absOffset = Math.abs(offset);

            for (int i = 0, z = layout.getChildCount(); i < z; i++) {
                final View child = layout.getChildAt(i);
                final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                final Interpolator interpolator = childLp.getScrollInterpolator();

                if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
                    if (interpolator != null) {
                        int childScrollableHeight = 0;
                        final int flags = childLp.getScrollFlags();
                        if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                            // We're set to scroll so add the child's height plus margin
                            childScrollableHeight += child.getHeight() + childLp.topMargin
                                    + childLp.bottomMargin;

                            if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                                // For a collapsing scroll, we to take the collapsed height
                                // into account.
                                childScrollableHeight -= ViewCompat.getMinimumHeight(child);
                            }
                        }

                        if (ViewCompat.getFitsSystemWindows(child)) {
                            childScrollableHeight -= layout.getTopInset();
                        }

                        if (childScrollableHeight > 0) {
                            final int offsetForView = absOffset - child.getTop();
                            final int interpolatedDiff = Math.round(childScrollableHeight *
                                    interpolator.getInterpolation(
                                            offsetForView / (float) childScrollableHeight));

                            return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
                        }
                    }

                    // If we get to here then the view on the offset isn't suitable for interpolated
                    // scrolling. So break out of the loop
                    break;
                }
            }

            return offset;
        }

        private int resistanceSizeChange(AppBarLayout layout, final int offset) {
            if (offset <= 0)
                return offset;

            float maxResistanceFactor = 0;
            float totalResistanceFactor = 0;
            int maxOffsetLimit = 0;
            for (int i = 0, z = layout.getChildCount(); i < z; i++) {
                final View child = layout.getChildAt(i);
                final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                final float factor = childLp.getScrollResistanceFactor();
                final int offsetLimit = childLp.getScrollOffsetLimit();
                totalResistanceFactor += factor;
                maxResistanceFactor = Math.max(factor, maxResistanceFactor);
                maxOffsetLimit = Math.max(offsetLimit, maxOffsetLimit);
            }
            final int limitedOffset = Math.min(maxOffsetLimit, offset);
            int totalOffset = 0;
            for (int i = 0, z = layout.getChildCount(); i < z; i++) {
                final View child = layout.getChildAt(i);
                final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                if (overScrollBounceEnabled(childLp)) {
                    final float factor = childLp.getScrollResistanceFactor();
                    final float averagedFactor = maxResistanceFactor * factor / totalResistanceFactor;
                    final int factoredOffset = (int) (averagedFactor * limitedOffset);

                    totalOffset+= factoredOffset;

                    childLp.height = childLp.mOverScrollOriginalHeight + factoredOffset;
                    child.setLayoutParams(childLp);
                }
            }

            ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
            layoutParams.height = mOverScrollOriginalHeight + totalOffset;
            layout.setLayoutParams(layoutParams);

            return totalOffset;
        }

        @Override
        int getTopBottomOffsetForScrollingSibling() {
            return getTopAndBottomOffset() + mOffsetDelta;
        }

        @Override
        int getOverScrollOffset() {
            return mOverScrollDelta;
        }

        @Override
        public Parcelable onSaveInstanceState(CoordinatorLayout parent, AppBarLayout appBarLayout) {
            final Parcelable superState = super.onSaveInstanceState(parent, appBarLayout);
            final int offset = getTopAndBottomOffset();

            // Try and find the first visible child...
            for (int i = 0, count = appBarLayout.getChildCount(); i < count; i++) {
                View child = appBarLayout.getChildAt(i);
                final int visBottom = child.getBottom() + offset;

                if (child.getTop() + offset <= 0 && visBottom >= 0) {
                    final SavedState ss = new SavedState(superState);
                    ss.firstVisibleChildIndex = i;
                    ss.firstVisibileChildAtMinimumHeight =
                            visBottom == ViewCompat.getMinimumHeight(child);
                    ss.firstVisibileChildPercentageShown = visBottom / (float) child.getHeight();
                    return ss;
                }
            }

            // Else we'll just return the super state
            return superState;
        }

        @Override
        public void onRestoreInstanceState(CoordinatorLayout parent, AppBarLayout appBarLayout,
                Parcelable state) {
            if (state instanceof SavedState) {
                final SavedState ss = (SavedState) state;
                super.onRestoreInstanceState(parent, appBarLayout, ss.getSuperState());
                mOffsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
                mOffsetToChildIndexOnLayoutPerc = ss.firstVisibileChildPercentageShown;
                mOffsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibileChildAtMinimumHeight;
            } else {
                super.onRestoreInstanceState(parent, appBarLayout, state);
                mOffsetToChildIndexOnLayout = INVALID_POSITION;
            }
        }

        protected static class SavedState extends BaseSavedState {
            int firstVisibleChildIndex;
            float firstVisibileChildPercentageShown;
            boolean firstVisibileChildAtMinimumHeight;

            public SavedState(Parcel source, ClassLoader loader) {
                super(source);
                firstVisibleChildIndex = source.readInt();
                firstVisibileChildPercentageShown = source.readFloat();
                firstVisibileChildAtMinimumHeight = source.readByte() != 0;
            }

            public SavedState(Parcelable superState) {
                super(superState);
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(firstVisibleChildIndex);
                dest.writeFloat(firstVisibileChildPercentageShown);
                dest.writeByte((byte) (firstVisibileChildAtMinimumHeight ? 1 : 0));
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
                        @Override
                        public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                            return new SavedState(source, loader);
                        }

                        @Override
                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    });
        }
    }

    /**
     * Behavior which should be used by {@link View}s which can scroll vertically and support
     * nested scrolling to automatically scroll any {@link AppBarLayout} siblings.
     */
    public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {

        private static final String TAG = AppBarLayout.TAG + ".ScrollingVB";

        private static final int INVALID_PADDING = Integer.MIN_VALUE;

        private int mOverlayTop;
        private int mAdditionalOffset;

        private View mScrollingView;
        private int mOriginalPaddingTop = INVALID_PADDING;
        private int mOriginalPaddingBottom = INVALID_PADDING;

        public ScrollingViewBehavior() {}

        public ScrollingViewBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.ScrollingViewBehavior_Params);
            mOverlayTop = a.getDimensionPixelSize(
                    R.styleable.ScrollingViewBehavior_Params_tic_behavior_overlapTop, 0);
            a.recycle();
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
            // We depend on any AppBarLayouts
            return dependency instanceof AppBarLayout;
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
            // First lay out the child as normal
            super.onLayoutChild(parent, child, layoutDirection);

            if (mScrollingView == null) {
                mScrollingView = findScrollingView(child);
            }

            if (mOriginalPaddingTop == INVALID_PADDING) {
                mOriginalPaddingTop = child.getPaddingTop();
            }
            if (mOriginalPaddingBottom == INVALID_PADDING) {
                mOriginalPaddingBottom = child.getPaddingBottom();
            }

            updateOffset(parent, child);
            return true;
        }

        private View findScrollingView(View root) {
            if (root.canScrollVertically(1) || root.canScrollVertically(-1)) {
                return root;
            }

            if (root instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) root;
                View highestChild = null;
                List<View> largerChildren = new ArrayList<>(1);
                largerChildren.clear();

                for (int i = 0, c = parent.getChildCount(); i < c; i++) {
                    View child = parent.getChildAt(i);

                    // We only interested in the view is visible.
                    if (!child.isShown()) {
                        continue;
                    }

                    // We only interested in the larger view.
                    largerChildren.add(child);
                    if (highestChild != null && child.getHeight() < highestChild.getHeight()) {
                        largerChildren.remove(child);
                    } else {
                        highestChild = child;
                    }
                }

                for (View largerChild : largerChildren) {
                    View scrolling = findScrollingView(largerChild);
                    if (scrolling != null) {
                        return scrolling;
                    }
                }
            }

            return null;
        }

        public View getScrollingView() {
            return mScrollingView;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, View child,
                View dependency) {
            updateOffset(parent, child, dependency);
            return false;
        }

        void setScrollOffset(CoordinatorLayout parent, View child, int additionalOffset) {
            mAdditionalOffset = additionalOffset;
            updateOffset(parent, child);
        }

        private void updateOffset(CoordinatorLayout parent, View child) {
            // Now offset us correctly to be in the correct position. This is important for things
            // like activity transitions which rely on accurate positioning after the first layout.
            final List<View> dependencies = parent.getDependencies(child);
            boolean updated = false;
            for (int i = 0, z = dependencies.size(); i < z; i++) {
                if (updateOffset(parent, child, dependencies.get(i))) {
                    // If we updated the offset, break out of the loop now
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                updateChildOffset(parent, child, mAdditionalOffset);
            }
        }

        private boolean updateOffset(CoordinatorLayout parent, View child, View dependency) {
            final CoordinatorLayout.Behavior behavior =
                    ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
            if (behavior instanceof Behavior) {
                // Offset the child so that it is below the app-bar (with any overlap)
                final int offset = ((Behavior) behavior).getTopBottomOffsetForScrollingSibling();
                final int dependencyHeight = dependency.getHeight() + offset
                        - getOverlapForOffset(dependency, offset);
                final int totalOffset;
                if (dependency.getVisibility() == GONE) {
                    totalOffset = mAdditionalOffset;
                } else {
                    totalOffset = dependencyHeight + mAdditionalOffset;
                }
                updateChildOffset(parent, child, totalOffset);
                return true;
            }
            return false;
        }

        private void updateChildOffset(CoordinatorLayout parent, View child, int totalOffset) {
            setTopAndBottomOffset(totalOffset);

            if (totalOffset != 0) {
                int left = child.getPaddingLeft();
                int top = child.getPaddingTop();
                int right = child.getPaddingRight();
                int bottom = child.getPaddingBottom();

                // Set a additional padding for the content at top/bottom that can't scroll, so
                // they won't be pushed out of the screen.
                boolean canScroll = mScrollingView != null &&
                        mScrollingView.canScrollVertically(totalOffset);
                boolean overScreen = child.getBottom() > parent.getHeight() || child.getTop() < 0;
                boolean alreadyAddedSpace = top != mOriginalPaddingTop || bottom != mOriginalPaddingBottom;
                boolean needAdditionalSpace = alreadyAddedSpace || (!canScroll && overScreen);

                if (DesignConfig.DEBUG_COORDINATOR) {
                    Log.v(TAG, "update child offset to " + totalOffset + ", child canScroll " + canScroll +
                            ", overScreen " + overScreen + ", alreadyAdd " + alreadyAddedSpace +
                            ", needAdd " + needAdditionalSpace + ", child " + child);
                }

                if (needAdditionalSpace && totalOffset < 0) {
                    top = mOriginalPaddingTop == INVALID_PADDING ?
                            -totalOffset : mOriginalPaddingTop - totalOffset;
                } else if (needAdditionalSpace && totalOffset > 0) {
                    bottom = mOriginalPaddingBottom == INVALID_PADDING ?
                            totalOffset : mOriginalPaddingBottom + totalOffset;
                }

                if (needAdditionalSpace) {
                    child.setPadding(left, top, right, bottom);
                }
            }
        }

        private int getOverlapForOffset(final View dependency, final int offset) {
            if (mOverlayTop != 0 && dependency instanceof AppBarLayout) {
                final AppBarLayout abl = (AppBarLayout) dependency;
                final int totalScrollRange = abl.getTotalScrollRange();
                final int preScrollDown = abl.getDownNestedPreScrollRange();

                if (preScrollDown != 0 && (totalScrollRange + offset) <= preScrollDown) {
                    // If we're in a pre-scroll down. Don't use the offset at all.
                    return 0;
                } else {
                    final int availScrollRange = totalScrollRange - preScrollDown;
                    if (availScrollRange != 0) {
                        // Else we'll use a interpolated ratio of the overlap, depending on offset
                        final float percScrolled = offset / (float) availScrollRange;
                        return MathUtils.constrain(
                                Math.round((1f + percScrolled) * mOverlayTop), 0, mOverlayTop);
                    }
                }
            }
            return mOverlayTop;
        }

        /**
         * Set the distance that this view should overlap any {@link AppBarLayout}.
         *
         * @param overlayTop the distance in px
         */
        public void setOverlayTop(int overlayTop) {
            mOverlayTop = overlayTop;
        }

        /**
         * Returns the distance that this view should overlap any {@link AppBarLayout}.
         */
        public int getOverlayTop() {
            return mOverlayTop;
        }

        @Override
        View findFirstDependency(List<View> views) {
            for (int i = 0, z = views.size(); i < z; i++) {
                View view = views.get(i);
                if (view instanceof AppBarLayout) {
                    return view;
                }
            }
            return null;
        }

        @Override
        int getScrollRange(View v) {
            if (v instanceof AppBarLayout) {
                return ((AppBarLayout) v).getTotalScrollRange();
            } else {
                return super.getScrollRange(v);
            }
        }
    }
}

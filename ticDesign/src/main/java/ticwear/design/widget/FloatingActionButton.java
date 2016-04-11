/*
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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import ticwear.design.R;
import ticwear.design.widget.FloatingActionButtonAnimator.InternalVisibilityChangedListener;

/**
 * Floating action buttons are used for a special type of promoted action. They are distinguished
 * by a circled icon floating above the UI and have special motion behaviors related to morphing,
 * launching, and the transferring anchor point.
 *
 * <p>Floating action buttons come in two sizes: the default and the mini. The size can be
 * controlled with the {@code fabSize} attribute.</p>
 *
 * <p>As this class descends from {@link ImageView}, you can control the icon which is displayed
 * via {@link #setImageDrawable(Drawable)}.</p>
 *
 * <p>The background color of this view defaults to the your theme's {@code colorAccent}. If you
 * wish to change this at runtime then you can do so via
 * {@link #setBackgroundTintList(ColorStateList)}.</p>
 *
 * @attr ref android.support.design.R.styleable#FloatingActionButton_fabSize
 */
@CoordinatorLayout.DefaultBehavior(FloatingActionButton.Behavior.class)
public class FloatingActionButton extends VisibilityAwareImageButton {

    private static final String LOG_TAG = "FloatingActionButton";

    /**
     * Callback to be invoked when the visibility of a FloatingActionButton changes.
     */
    public abstract static class OnVisibilityChangedListener {
        /**
         * Called when a FloatingActionButton has been
         * {@link #show(OnVisibilityChangedListener) shown}.
         *
         * @param fab the FloatingActionButton that was shown.
         */
        public void onShown(FloatingActionButton fab) {}

        /**
         * Called when a FloatingActionButton has been
         * {@link #hide(OnVisibilityChangedListener) hidden}.
         *
         * @param fab the FloatingActionButton that was hidden.
         */
        public void onHidden(FloatingActionButton fab) {}

        /**
         * Called when a FloatingActionButton has been
         * {@link #minimize(OnVisibilityChangedListener) minimize}.
         *
         * @param fab the FloatingActionButton that was minimum.
         */
        public void onMinimum(FloatingActionButton fab) {}
    }

    // These values must match those in the attrs declaration
    private static final int SIZE_MINI = 1;
    private static final int SIZE_NORMAL = 0;

    private int mSize;
    private int mImagePadding;
    private Rect mTouchArea;

    private Drawable mShapeDrawable;
    private RippleDrawable mRippleDrawable;

    // Padding for circular progress bar.
    private final Rect mProgressPadding;

    private final FloatingActionButtonAnimator mImpl;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_Ticwear_FloatingActionButton);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mProgressPadding = new Rect();
        mTouchArea = new Rect();

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.FloatingActionButton, defStyleAttr, defStyleRes);
        int rippleColor = a.getColor(R.styleable.FloatingActionButton_tic_rippleColor, 0);
        mSize = a.getInt(R.styleable.FloatingActionButton_tic_fabSize, SIZE_NORMAL);
        final float pressedTranslationZ = a.getDimension(
                R.styleable.FloatingActionButton_tic_pressedTranslationZ, 0f);
        a.recycle();

        mImpl = new FloatingActionButtonAnimator(this);

        final int maxImageSize = (int) getResources().getDimension(R.dimen.tic_design_fab_image_size);
        mImagePadding = (getSizeDimension() - maxImageSize) / 2;
        setPadding(mImagePadding, mImagePadding, mImagePadding, mImagePadding);

        mShapeDrawable = createShapeDrawable();
        mRippleDrawable = new RippleDrawable(ColorStateList.valueOf(rippleColor),
                mShapeDrawable, null);

        super.setBackgroundDrawable(mRippleDrawable);

        mImpl.setPressedTranslationZ(pressedTranslationZ);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int preferredSize = getSizeDimension();

        final int w = resolveAdjustedSize(preferredSize, widthMeasureSpec);
        final int h = resolveAdjustedSize(preferredSize, heightMeasureSpec);

        // As we want to stay circular, we set both dimensions to be the
        // smallest resolved dimension
        final int d = Math.min(w, h);

        // We add the shadow's padding to the measured dimension
        setMeasuredDimension(
                d + mProgressPadding.left + mProgressPadding.right,
                d + mProgressPadding.top + mProgressPadding.bottom);
    }

    /**
     * Set the ripple color for this {@link FloatingActionButton}.
     * <p>
     * When running on devices with KitKat or below, we draw a fill rather than a ripple.
     *
     * @param color ARGB color to use for the ripple.
     */
    public void setRippleColor(@ColorInt int color) {
        mRippleDrawable.setColor(ColorStateList.valueOf(color));
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        Log.e(LOG_TAG, "Setting a custom background is not supported.");
    }

    @Override
    public void setBackgroundResource(int resid) {
        Log.e(LOG_TAG, "Setting a custom background is not supported.");
    }

    @Override
    public void setBackgroundColor(int color) {
        Log.e(LOG_TAG, "Setting a custom background is not supported.");
    }

    @Override
    public void setBackground(Drawable background) {
        Log.e(LOG_TAG, "Setting a custom background is not supported.");
    }

    Drawable createShapeDrawable() {
        ShapeDrawable d = new ShapeDrawable();
        d.setShape(new OvalShape());
        d.getPaint().setColor(Color.WHITE);
        return d;
    }

    /**
     * Shows the button.
     * <p>This method will animate the button show if the view has already been laid out.</p>
     */
    public void show() {
        show(null);
    }

    /**
     * Shows the button.
     * <p>This method will animate the button show if the view has already been laid out.</p>
     *
     * @param listener the listener to notify when this view is shown
     */
    public void show(@Nullable final OnVisibilityChangedListener listener) {
        show(listener, true);
    }

    private void show(OnVisibilityChangedListener listener, boolean fromUser) {
        mImpl.show(wrapOnVisibilityChangedListener(listener), fromUser);
    }

    /**
     * Hides the button.
     * <p>This method will animate the button hide if the view has already been laid out.</p>
     */
    public void hide() {
        hide(null);
    }

    /**
     * Hides the button.
     * <p>This method will animate the button hide if the view has already been laid out.</p>
     *
     * @param listener the listener to notify when this view is hidden
     */
    public void hide(@Nullable OnVisibilityChangedListener listener) {
        hide(listener, true);
    }

    private void hide(@Nullable OnVisibilityChangedListener listener, boolean fromUser) {
        mImpl.hide(wrapOnVisibilityChangedListener(listener), fromUser);
    }

    /**
     * Minimize the button.
     * <p>This method will animate the button minimize.</p>
     */
    public void minimize() {
        minimize(null);
    }

    /**
     * Minimize the button.
     * <p>This method will animate the button minimize.</p>
     *
     * @param listener the listener to notify when this view is minimize
     */
    public void minimize(@Nullable OnVisibilityChangedListener listener) {
        minimize(listener, true);
    }

    private void minimize(@Nullable OnVisibilityChangedListener listener, boolean fromUser) {
        mImpl.minimize(wrapOnVisibilityChangedListener(listener), fromUser);
    }

    @Nullable
    private InternalVisibilityChangedListener wrapOnVisibilityChangedListener(
            @Nullable final OnVisibilityChangedListener listener) {
        if (listener == null) {
            return null;
        }

        return new InternalVisibilityChangedListener() {
            @Override
            public void onShown() {
                listener.onShown(FloatingActionButton.this);
            }

            @Override
            public void onHidden() {
                listener.onHidden(FloatingActionButton.this);
            }

            @Override
            public void onMinimum() {
                listener.onMinimum(FloatingActionButton.this);
            }
        };
    }

    final int getSizeDimension() {
        switch (mSize) {
            case SIZE_MINI:
                return getResources().getDimensionPixelSize(R.dimen.tic_design_fab_size_mini);
            case SIZE_NORMAL:
            default:
                return getResources().getDimensionPixelSize(R.dimen.tic_design_fab_size_normal);
        }
    }

    /**
     * Return in {@code rect} the bounds of the actual floating action button content in view-local
     * coordinates. This is defined as anything within any visible shadow.
     *
     * @return true if this view actually has been laid out and has a content rect, else false.
     */
    public boolean getContentRect(@NonNull Rect rect) {
        if (ViewCompat.isLaidOut(this)) {
            rect.set(0, 0, getWidth(), getHeight());
            rect.left += mProgressPadding.left;
            rect.top += mProgressPadding.top;
            rect.right -= mProgressPadding.right;
            rect.bottom -= mProgressPadding.bottom;
            return true;
        } else {
            return false;
        }
    }

    private static int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                // Parent says we can be as big as we want. Just don't be larger
                // than max size imposed on ourselves.
                result = desiredSize;
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(desiredSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //noinspection SimplifiableIfStatement
        if (ev.getAction() == MotionEvent.ACTION_DOWN &&
                getContentRect(mTouchArea) &&
                !mTouchArea.contains((int) ev.getX(), (int) ev.getY())) {
            return false;
        }

        return super.onTouchEvent(ev);
    }

    /**
     * Behavior designed for use with {@link FloatingActionButton} instances. It's main function
     * is to move {@link FloatingActionButton} views so that any displayed {@link View}s do
     * not cover them.
     */
    public static class Behavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

        private Rect mTmpRect;

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child,
                                              View dependency) {
            if (dependency instanceof AppBarLayout) {
                // If we're depending on an AppBarLayout we will show/hide it automatically
                // if the FAB is anchored to the AppBarLayout
                updateFabVisibility(parent, (AppBarLayout) dependency, child);
            }
            return false;
        }

        private boolean updateFabVisibility(CoordinatorLayout parent,
                                            AppBarLayout appBarLayout, FloatingActionButton child) {
            final CoordinatorLayout.LayoutParams lp =
                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            if (lp.getAnchorId() != appBarLayout.getId()) {
                // The anchor ID doesn't match the dependency, so we won't automatically
                // show/hide the FAB
                return false;
            }

            if (child.getUserSetVisibility() != VISIBLE) {
                // The view isn't set to be visible so skip changing it's visibility
                return false;
            }

            if (mTmpRect == null) {
                mTmpRect = new Rect();
            }

            // First, let's get the visible rect of the dependency
            final Rect rect = mTmpRect;
            ViewGroupUtils.getDescendantRect(parent, appBarLayout, rect);

            if (rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
                // If the anchor's bottom is below the seam, we'll animate our FAB out
                child.hide(null, false);
            } else {
                // Else, we'll animate our FAB back in
                child.show(null, false);
            }
            return true;
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionButton child,
                                     int layoutDirection) {
            // First, lets make sure that the visibility of the FAB is consistent
            final List<View> dependencies = parent.getDependencies(child);
            for (int i = 0, count = dependencies.size(); i < count; i++) {
                final View dependency = dependencies.get(i);
                if (dependency instanceof AppBarLayout
                        && updateFabVisibility(parent, (AppBarLayout) dependency, child)) {
                    break;
                }
            }
            // Now let the CoordinatorLayout lay out the FAB
            parent.onLayoutChild(child, layoutDirection);
            // Now offset it if needed
            offsetIfNeeded(parent, child);
            return true;
        }

        /**
         * Pre-Lollipop we use padding so that the shadow has enough space to be drawn. This method
         * offsets our layout position so that we're positioned correctly if we're on one of
         * our parent's edges.
         */
        private void offsetIfNeeded(CoordinatorLayout parent, FloatingActionButton fab) {
            final Rect padding = fab.mProgressPadding;

            if (padding.centerX() > 0 && padding.centerY() > 0) {
                final CoordinatorLayout.LayoutParams lp =
                        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();

                int offsetTB = 0, offsetLR = 0;

                if (fab.getRight() >= parent.getWidth() - lp.rightMargin) {
                    // If we're on the left edge, shift it the right
                    offsetLR = padding.right;
                } else if (fab.getLeft() <= lp.leftMargin) {
                    // If we're on the left edge, shift it the left
                    offsetLR = -padding.left;
                }
                if (fab.getBottom() >= parent.getBottom() - lp.bottomMargin) {
                    // If we're on the bottom edge, shift it down
                    offsetTB = padding.bottom;
                } else if (fab.getTop() <= lp.topMargin) {
                    // If we're on the top edge, shift it up
                    offsetTB = -padding.top;
                }

                fab.offsetTopAndBottom(offsetTB);
                fab.offsetLeftAndRight(offsetLR);
            }
        }

    }
}

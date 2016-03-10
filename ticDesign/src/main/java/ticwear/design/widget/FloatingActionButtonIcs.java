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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

class FloatingActionButtonIcs extends FloatingActionButtonImpl {

    Drawable mShapeDrawable;
    Drawable mRippleDrawable;
    CircularBorderDrawable mBorderDrawable;

    private float mElevation;
    private float mPressedTranslationZ;
    private int mAnimationDuration;

    private StateListAnimator mStateListAnimator;

    ShadowDrawableWrapper mShadowDrawable;

    private enum AnimState {
        Idle,
        Hiding,
        Minimizing
    }
    private AnimState mAnimState = AnimState.Idle;

    FloatingActionButtonIcs(VisibilityAwareImageButton view,
            ShadowViewDelegate shadowViewDelegate) {
        super(view, shadowViewDelegate);

        mAnimationDuration = view.getResources().getInteger(android.R.integer.config_shortAnimTime);

        mStateListAnimator = new StateListAnimator();
        mStateListAnimator.setTarget(view);

        // Elevate with translationZ when pressed or focused
        mStateListAnimator.addState(PRESSED_ENABLED_STATE_SET,
                setupAnimation(new ElevateToTranslationZAnimation()));
        mStateListAnimator.addState(FOCUSED_ENABLED_STATE_SET,
                setupAnimation(new ElevateToTranslationZAnimation()));
        // Reset back to elevation by default
        mStateListAnimator.addState(EMPTY_STATE_SET,
                setupAnimation(new ResetElevationAnimation()));
    }

    @Override
    void setBackgroundDrawable(ColorStateList backgroundTint,
                               PorterDuff.Mode backgroundTintMode, int rippleColor, int borderWidth) {
        // Now we need to tint the original background with the tint, using
        // an InsetDrawable if we have a border width
        mShapeDrawable = DrawableCompat.wrap(createShapeDrawable());
        DrawableCompat.setTintList(mShapeDrawable, backgroundTint);
        if (backgroundTintMode != null) {
            DrawableCompat.setTintMode(mShapeDrawable, backgroundTintMode);
        }

        // Now we created a mask Drawable which will be used for touch feedback.
        GradientDrawable touchFeedbackShape = createShapeDrawable();

        // We'll now wrap that touch feedback mask drawable with a ColorStateList. We do not need
        // to inset for any border here as LayerDrawable will nest the padding for us
        mRippleDrawable = DrawableCompat.wrap(touchFeedbackShape);
        DrawableCompat.setTintList(mRippleDrawable, createColorStateList(rippleColor));
        DrawableCompat.setTintMode(mRippleDrawable, PorterDuff.Mode.MULTIPLY);

        final Drawable[] layers;
        if (borderWidth > 0) {
            mBorderDrawable = createBorderDrawable(borderWidth, backgroundTint);
            layers = new Drawable[] {mBorderDrawable, mShapeDrawable, mRippleDrawable};
        } else {
            mBorderDrawable = null;
            layers = new Drawable[] {mShapeDrawable, mRippleDrawable};
        }

        mShadowDrawable = new ShadowDrawableWrapper(
                mView.getResources(),
                new LayerDrawable(layers),
                mShadowViewDelegate.getRadius(),
                mElevation,
                mElevation + mPressedTranslationZ);
        mShadowDrawable.setAddPaddingForCorners(false);

        mShadowViewDelegate.setBackgroundDrawable(mShadowDrawable);

        updatePadding();
    }

    @Override
    void setBackgroundTintList(ColorStateList tint) {
        DrawableCompat.setTintList(mShapeDrawable, tint);
        if (mBorderDrawable != null) {
            mBorderDrawable.setBorderTint(tint);
        }
    }

    @Override
    void setBackgroundTintMode(PorterDuff.Mode tintMode) {
        DrawableCompat.setTintMode(mShapeDrawable, tintMode);
    }

    @Override
    void setRippleColor(int rippleColor) {
        DrawableCompat.setTintList(mRippleDrawable, createColorStateList(rippleColor));
    }

    @Override
    void setElevation(float elevation) {
        if (mElevation != elevation && mShadowDrawable != null) {
            mShadowDrawable.setShadowSize(elevation, elevation + mPressedTranslationZ);
            mElevation = elevation;
            updatePadding();
        }
    }

    @Override
    void setPressedTranslationZ(float translationZ) {
        if (mPressedTranslationZ != translationZ && mShadowDrawable != null) {
            mPressedTranslationZ = translationZ;
            mShadowDrawable.setMaxShadowSize(mElevation + translationZ);
            updatePadding();
        }
    }

    @Override
    void onDrawableStateChanged(int[] state) {
        mStateListAnimator.setState(state);
    }

    @Override
    void jumpDrawableToCurrentState() {
        mStateListAnimator.jumpToCurrentState();
    }

    @Override
    boolean requirePreDrawListener() {
        return true;
    }

    @Override
    void onPreDraw() {
        updateFromViewRotation(mView.getRotation());
    }

    @Override
    void hide(@Nullable final InternalVisibilityChangedListener listener, final boolean fromUser) {
        if (isHiding() || mView.getVisibility() != View.VISIBLE) {
            // A hide animation is in progress, or we're already hidden. Skip the call
            if (listener != null) {
                listener.onHidden();
            }
            return;
        }

        if (!ViewCompat.isLaidOut(mView) || mView.isInEditMode()) {
            mAnimState = AnimState.Idle;
            // If the view isn't laid out, or we're in the editor, don't run the animation
            mView.internalSetVisibility(View.GONE, fromUser);
            if (listener != null) {
                listener.onHidden();
            }
        } else {
            mView.animate().cancel();
            mView.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
                    .setListener(new AnimatorListenerAdapter() {
                        private boolean mCancelled;

                        @Override
                        public void onAnimationStart(Animator animation) {
                            mAnimState = AnimState.Hiding;
                            mCancelled = false;
                            mView.internalSetVisibility(View.VISIBLE, fromUser);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            mAnimState = AnimState.Idle;
                            mCancelled = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimState = AnimState.Idle;
                            if (!mCancelled) {
                                mView.internalSetVisibility(View.GONE, fromUser);
                                if (listener != null) {
                                    listener.onHidden();
                                }
                            }
                        }
                    });
        }
    }

    @Override
    void show(@Nullable final InternalVisibilityChangedListener listener, final boolean fromUser) {
        if (notShown()) {
            if (ViewCompat.isLaidOut(mView) && !mView.isInEditMode()) {
                mView.animate().cancel();
                if (mView.getVisibility() != View.VISIBLE) {
                    // If the view isn't visible currently, we'll animate it from a single pixel
                    mView.setAlpha(0f);
                    mView.setScaleY(0f);
                    mView.setScaleX(0f);
                }
                mView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(SHOW_HIDE_ANIM_DURATION)
                        .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                mAnimState = AnimState.Idle;
                                mView.internalSetVisibility(View.VISIBLE, fromUser);
                                mView.setClickable(true);
                                mView.setImageAlpha(0xFF);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (listener != null) {
                                    listener.onShown();
                                }
                            }
                        });
            } else {
                mAnimState = AnimState.Idle;
                mView.internalSetVisibility(View.VISIBLE, fromUser);
                mView.setAlpha(1f);
                mView.setScaleY(1f);
                mView.setScaleX(1f);
                mView.setClickable(true);
                mView.setImageAlpha(0xFF);
                if (listener != null) {
                    listener.onShown();
                }
            }
        }
    }

    private boolean notShown() {
        return isHiding() || isMinimizing() || mView.getVisibility() != View.VISIBLE || mView.getScaleX() != 1f;
    }

    @Override
    void minimize(@Nullable final InternalVisibilityChangedListener listener, final boolean fromUser) {
        if (isMinimizing()) {
            // A minimize animation is in progress, or we're already minimum. Skip the call
            if (listener != null) {
                listener.onMinimum();
            }
            return;
        }

        float targetScale = 0.1f;

        if (ViewCompat.isLaidOut(mView) && !mView.isInEditMode()) {
            mView.animate().cancel();
            if (mView.getVisibility() != View.VISIBLE) {
                // If the view isn't visible currently, we'll animate it from a single pixel
                mView.setAlpha(0f);
                mView.setScaleY(0f);
                mView.setScaleX(0f);
                mView.setImageAlpha(0);
            }
            mView.animate()
                    .scaleX(targetScale)
                    .scaleY(targetScale)
                    .alpha(1f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mAnimState = AnimState.Minimizing;
                            mView.internalSetVisibility(View.VISIBLE, fromUser);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimState = AnimState.Idle;
                            mView.setClickable(false);
                            mView.setImageAlpha(0);
                            if (listener != null) {
                                listener.onMinimum();
                            }
                        }
                    });
        } else {
            mAnimState = AnimState.Idle;
            mView.internalSetVisibility(View.VISIBLE, fromUser);
            mView.setAlpha(1f);
            mView.setScaleY(targetScale);
            mView.setScaleX(targetScale);
            mView.setClickable(false);
            mView.setImageAlpha(0);
            if (listener != null) {
                listener.onMinimum();
            }
        }
    }

    private boolean isHiding() {
        return mAnimState == AnimState.Hiding;
    }

    private boolean isMinimizing() {
        return mAnimState == AnimState.Minimizing;
    }

    private void updateFromViewRotation(float rotation) {
        // Offset any View rotation
        if (mShadowDrawable != null) {
            mShadowDrawable.setRotation(-rotation);
        }
        if (mBorderDrawable != null) {
            mBorderDrawable.setRotation(-rotation);
        }
    }

    private void updatePadding() {
        Rect rect = new Rect();
        mShadowDrawable.getPadding(rect);
        mShadowViewDelegate.setShadowPadding(rect.left, rect.top, rect.right, rect.bottom);
    }

    private Animation setupAnimation(Animation animation) {
        animation.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        animation.setDuration(mAnimationDuration);
        return animation;
    }

    private abstract class BaseShadowAnimation extends Animation {
        private float mShadowSizeStart;
        private float mShadowSizeDiff;

        @Override
        public void reset() {
            super.reset();

            mShadowSizeStart = mShadowDrawable.getShadowSize();
            mShadowSizeDiff = getTargetShadowSize() - mShadowSizeStart;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mShadowDrawable.setShadowSize(mShadowSizeStart + (mShadowSizeDiff * interpolatedTime));
        }

        /**
         * @return the shadow size we want to animate to.
         */
        protected abstract float getTargetShadowSize();
    }

    private class ResetElevationAnimation extends BaseShadowAnimation {
        @Override
        protected float getTargetShadowSize() {
            return mElevation;
        }
    }

    private class ElevateToTranslationZAnimation extends BaseShadowAnimation {
        @Override
        protected float getTargetShadowSize() {
            return mElevation + mPressedTranslationZ;
        }
    }

    private static ColorStateList createColorStateList(int selectedColor) {
        final int[][] states = new int[3][];
        final int[] colors = new int[3];
        int i = 0;

        states[i] = FOCUSED_ENABLED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        states[i] = PRESSED_ENABLED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        // Default enabled state
        states[i] = new int[0];
        colors[i] = Color.TRANSPARENT;
        i++;

        return new ColorStateList(states, colors);
    }
}

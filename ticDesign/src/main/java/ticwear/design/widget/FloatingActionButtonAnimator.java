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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

class FloatingActionButtonAnimator {

    static final int SHOW_HIDE_ANIM_DURATION = 200;
    final VisibilityAwareImageButton mView;

    static final int[] PRESSED_ENABLED_STATE_SET = {android.R.attr.state_pressed,
            android.R.attr.state_enabled};
    static final int[] FOCUSED_ENABLED_STATE_SET = {android.R.attr.state_focused,
            android.R.attr.state_enabled};
    static final int[] EMPTY_STATE_SET = new int[0];

    private AnimState mAnimState = AnimState.Idle;

    private Interpolator mInterpolator;

    private int mMinimizeTranslationX = 0;
    private int mMinimizeTranslationY = 0;

    FloatingActionButtonAnimator(VisibilityAwareImageButton view) {

        mView = view;

        if (!view.isInEditMode()) {
            mInterpolator = AnimationUtils.loadInterpolator(mView.getContext(),
                    android.R.interpolator.fast_out_slow_in);
        }

    }

    void setPressedTranslationZ(float translationZ) {

        StateListAnimator stateListAnimator = mView.getStateListAnimator();
        if (stateListAnimator == null) {
            stateListAnimator = new StateListAnimator();
        }

        // Animate translationZ to our value when pressed or focused
        stateListAnimator.addState(PRESSED_ENABLED_STATE_SET,
                setupAnimator(ObjectAnimator.ofFloat(mView, "translationZ", translationZ)));
        stateListAnimator.addState(FOCUSED_ENABLED_STATE_SET,
                setupAnimator(ObjectAnimator.ofFloat(mView, "translationZ", translationZ)));
        // Animate translationZ to 0 otherwise
        stateListAnimator.addState(EMPTY_STATE_SET,
                setupAnimator(ObjectAnimator.ofFloat(mView, "translationZ", 0f)));

        mView.setStateListAnimator(stateListAnimator);
    }

    void setMinimizeTranslation(int x, int y) {
        mMinimizeTranslationX = x;
        mMinimizeTranslationY = y;
    }

    private Animator setupAnimator(Animator animator) {
        animator.setInterpolator(mInterpolator);
        return animator;
    }

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
                    .translationX(mMinimizeTranslationX)
                    .translationY(mMinimizeTranslationY)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(ticwear.design.widget.AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
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

    void show(@Nullable final InternalVisibilityChangedListener listener, final boolean fromUser) {
        if (notShown()) {
            if (ViewCompat.isLaidOut(mView) && !mView.isInEditMode()) {
                mView.animate().cancel();
                if (mView.getVisibility() != View.VISIBLE) {
                    // If the view isn't visible currently, we'll animate it from a single pixel
                    mView.setScaleY(0f);
                    mView.setScaleX(0f);
                    mView.setTranslationX(mMinimizeTranslationX);
                    mView.setTranslationY(mMinimizeTranslationY);
                }
                mView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .translationX(0)
                        .translationY(0)
                        .setDuration(SHOW_HIDE_ANIM_DURATION)
                        .setInterpolator(ticwear.design.widget.AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
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
                mView.setScaleY(1f);
                mView.setScaleX(1f);
                mView.setTranslationX(0);
                mView.setTranslationY(0);
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
                mView.setScaleY(0f);
                mView.setScaleX(0f);
                mView.setTranslationX(mMinimizeTranslationX);
                mView.setTranslationY(mMinimizeTranslationY);
                mView.setImageAlpha(0);
            }
            mView.animate()
                    .scaleX(targetScale)
                    .scaleY(targetScale)
                    .translationX(mMinimizeTranslationX)
                    .translationY(mMinimizeTranslationY)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(ticwear.design.widget.AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
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
            mView.setScaleY(targetScale);
            mView.setScaleX(targetScale);
            mView.setTranslationX(mMinimizeTranslationX);
            mView.setTranslationY(mMinimizeTranslationY);
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

    private enum AnimState {
        Idle,
        Hiding,
        Minimizing
    }

    interface InternalVisibilityChangedListener {
        void onShown();
        void onHidden();
        void onMinimum();
    }

}

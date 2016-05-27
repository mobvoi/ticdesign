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

package ticwear.design.drawable;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import ticwear.design.R;
import ticwear.design.utils.ColorUtils;

/**
 * CircularProgressDrawable has support only 2 modes: indeterminate and determinate.
 * <p>
 * Porting from https://github.com/rey5137/material
 */
public class CircularProgressDrawable extends Drawable implements Animatable {

    public static final long FRAME_DURATION = 1000 / 60;

    public static final int MODE_DETERMINATE = 0;
    public static final int MODE_INDETERMINATE = 1;

    private static final int PROGRESS_STATE_HIDE = -1;
    private static final int PROGRESS_STATE_STRETCH = 0;
    private static final int PROGRESS_STATE_KEEP_STRETCH = 1;
    private static final int PROGRESS_STATE_SHRINK = 2;
    private static final int PROGRESS_STATE_KEEP_SHRINK = 3;
    private static final int RUN_STATE_STOPPED = 0;
    private static final int RUN_STATE_STARTING = 1;
    private static final int RUN_STATE_STARTED = 2;
    private static final int RUN_STATE_RUNNING = 3;
    private static final int RUN_STATE_STOPPING = 4;
    private long mLastUpdateTime;
    private long mLastProgressStateTime;
    private long mLastRunStateTime;
    private int mProgressState;
    // 初始运行状态为停止
    private int mRunState = RUN_STATE_STOPPED;
    // progressbar的paint
    private Paint mPaint;
    private RectF mRect;
    private float mStartAngle;
    // indeterminate模式中的当前弧长
    private float mSweepAngle;
    private int mStrokeColorIndex;

    private int mPadding;
    private float mInitialAngle;
    // progressbar的进度，若人为设置，自动变为determinate模式
    private float mProgressPercent;
    private float mSecondaryProgressPercent;
    // indeterminate模式中的最大弧长
    private float mMaxSweepAngle;
    // indeterminate模式中的最小弧长
    private float mMinSweepAngle;
    // progressbar的宽度
    private int mStrokeSize;
    // progressbar的颜色，若人为设置，则无tint效果
    private int[] mStrokeColors;
    private int mStrokeSecondaryColor;
    // progressbar是否反向
    private boolean mReverse;
    // progressbar转一圈的时间
    private int mRotateDuration;
    private int mTransformDuration;
    private int mKeepDuration;
    private float mInStepPercent;
    private int[] mInColors;
    // 动画开始的时间
    private int mInAnimationDuration;
    // 动画消失的时间
    private int mOutAnimationDuration;
    private int mProgressMode;
    private Interpolator mTransformInterpolator;
    // 更新动画的任务
    private final Runnable mUpdater = new Runnable() {

        @Override
        public void run() {
            update();
        }

    };
    private boolean mMutated = false;
    private PorterDuffColorFilter mTintFilter;
    // 存储当前drawable的信息
    private ProgressState mState;

    // progressbar的alpha值
    private int mAlpha;

    private CircularProgressDrawable(int padding, float initialAngle, float progressPercent, float secondaryProgressPercent, float maxSweepAngle, float minSweepAngle, int strokeSize, int[] strokeColors, int strokeSecondaryColor, boolean reverse, int rotateDuration, int transformDuration, int keepDuration, Interpolator transformInterpolator, int progressMode, int inAnimDuration, float inStepPercent, int[] inStepColors, int outAnimDuration, int alpha) {
        mPadding = padding;
        mInitialAngle = initialAngle;
        mAlpha = alpha;
        mProgressPercent = Math.min(1f, Math.max(0f, progressPercent));
//        setProgress(progressPercent);
        setSecondaryProgress(secondaryProgressPercent);
        mMaxSweepAngle = maxSweepAngle;
        mMinSweepAngle = minSweepAngle;
        mStrokeSize = strokeSize;
        mStrokeColors = strokeColors;
        mStrokeSecondaryColor = strokeSecondaryColor;
        mReverse = reverse;
        mRotateDuration = rotateDuration;
        mTransformDuration = transformDuration;
        mKeepDuration = keepDuration;
        mTransformInterpolator = transformInterpolator;
        mProgressMode = progressMode;
        mInAnimationDuration = inAnimDuration;
        mInStepPercent = inStepPercent;
        mInColors = inStepColors;
        mOutAnimationDuration = outAnimDuration;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.BUTT);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mRect = new RectF();

        mState = createConstantState(null, null);
    }

    public int getStrokeSize() {
        return mStrokeSize;
    }

    public int getProgressMode() {
        return mProgressMode;
    }

    public void setProgressMode(int mode) {
        if (mProgressMode != mode) {
            mProgressMode = mode;
            if (mode == MODE_INDETERMINATE) {
                mProgressPercent = 0;
            }
            if (mState != null) {
                mState.mBuilder.progressPercent(mProgressPercent);
                mState.mBuilder.progressMode(mode);
            }
            invalidateSelf();
        }
    }

    public float getProgress() {
        return mProgressPercent;
    }

    /**
     * 设置progressbar的进度
     *
     * @param percent 传入的progressbar的进度
     */
    public void setProgress(float percent) {
        mProgressMode = MODE_DETERMINATE;
        percent = Math.min(1f, Math.max(0f, percent));
        if (mState != null) {
            mState.mBuilder.progressMode(MODE_DETERMINATE);
            mState.mBuilder.progressPercent(percent);
        }
        if (mProgressPercent != percent) {
            mProgressPercent = percent;
        }
        if (isRunning()) {
            resetAnimation();
            invalidateSelf();
        }
        else if (mProgressPercent != 0f)
            start();
    }

    public float getSecondaryProgress() {
        return mSecondaryProgressPercent;
    }

    public void setSecondaryProgress(float percent) {
        percent = Math.min(1f, Math.max(0f, percent));
        if (mSecondaryProgressPercent != percent) {
            mSecondaryProgressPercent = percent;
            if (isRunning())
                invalidateSelf();
            else if (mSecondaryProgressPercent != 0f)
                start();
        }
    }

    //Animation: based on http://cyrilmottier.com/2012/11/27/actionbar-on-the-move/

    /**
     * 设置progressbar的透明度
     *
     * @param alpha 传入的progressbar的alpha
     */
    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        mAlpha = alpha;
        if (mState != null) {
            mState.mBuilder.mAlpha = alpha;
        }
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setTintList(ColorStateList tint) {
        mState.mTint = tint;
        mTintFilter = createTintFilter(tint, mState.mTintMode);
        invalidateSelf();
    }

    @Override
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        mState.mTintMode = tintMode;
        mTintFilter = createTintFilter(mState.mTint, tintMode);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mPaint.getColorFilter() != mTintFilter) {
            mPaint.setColorFilter(mTintFilter);
        }

        switch (mProgressMode) {
            case MODE_DETERMINATE:
                drawDeterminate(canvas);
                break;
            case MODE_INDETERMINATE:
                drawIndeterminate(canvas);
                break;
        }
    }

    @Override
    public void start() {
        start(mInAnimationDuration > 0);
    }

    @Override
    public void stop() {
        stop(mOutAnimationDuration > 0);
    }

    /**
     * 当模式为determinate时使用
     */
    private void drawDeterminate(Canvas canvas) {
        Rect bounds = getBounds();
        float radius = 0f;
        float size = 0f;

        if (mRunState == RUN_STATE_STARTING) {
            size = (float) mStrokeSize * Math.min(mInAnimationDuration, (SystemClock.uptimeMillis() - mLastRunStateTime)) / mInAnimationDuration;
            if (size > 0)
                radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize * 2 + size) / 2f;
        } else if (mRunState == RUN_STATE_STOPPING) {
            size = (float) mStrokeSize * Math.max(0, (mOutAnimationDuration - SystemClock.uptimeMillis() + mLastRunStateTime)) / mOutAnimationDuration;
            if (size > 0)
                radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize * 2 + size) / 2f;
        } else if (mRunState != RUN_STATE_STOPPED) {
            size = mStrokeSize;
            radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize) / 2f;
        }

        if (radius > 0) {
            float x = (bounds.left + bounds.right) / 2f;
            float y = (bounds.top + bounds.bottom) / 2f;

            mPaint.setStrokeWidth(size);
            mPaint.setStyle(Paint.Style.STROKE);

            if (mProgressPercent == 1f) {
                mPaint.setColor(mStrokeColors[0]);
                mPaint.setAlpha(mAlpha);
                canvas.drawCircle(x, y, radius, mPaint);
            } else if (mProgressPercent == 0f) {
                mPaint.setColor(mStrokeSecondaryColor);
                canvas.drawCircle(x, y, radius, mPaint);
            } else {
                float sweepAngle = (mReverse ? -360 : 360) * mProgressPercent;

                float sweepAngleSecond = (mReverse ? -360 : 360) * Math.max(mSecondaryProgressPercent - mProgressPercent, 0);

                mRect.set(x - radius, y - radius, x + radius, y + radius);
                mPaint.setColor(mStrokeSecondaryColor);
                canvas.drawArc(mRect, mStartAngle + sweepAngle, sweepAngleSecond, false, mPaint);

                mPaint.setColor(mStrokeColors[0]);
                mPaint.setAlpha(mAlpha);
                canvas.drawArc(mRect, mInitialAngle, sweepAngle, false, mPaint);
            }
        }
    }

    private int getIndeterminateStrokeColor() {
        if (mProgressState != PROGRESS_STATE_KEEP_SHRINK || mStrokeColors.length == 1)
            return mStrokeColors[mStrokeColorIndex];

        float value = Math.max(0f, Math.min(1f, (float) (SystemClock.uptimeMillis() - mLastProgressStateTime) / mKeepDuration));
        int prev_index = mStrokeColorIndex == 0 ? mStrokeColors.length - 1 : mStrokeColorIndex - 1;

        return ColorUtils.getMiddleColor(mStrokeColors[prev_index], mStrokeColors[mStrokeColorIndex], value);
    }

    /**
     * 模式为indeterminate时使用
     */
    private void drawIndeterminate(Canvas canvas) {
        if (mRunState == RUN_STATE_STARTING) { // 正在开始时
            Rect bounds = getBounds();
            float x = (bounds.left + bounds.right) / 2f;
            float y = (bounds.top + bounds.bottom) / 2f;
            float maxRadius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2) / 2f;

            float stepTime = 1f / (mInStepPercent * (mInColors.length + 2) + 1);
            float time = (float) (SystemClock.uptimeMillis() - mLastRunStateTime) / mInAnimationDuration;
            float steps = time / stepTime;

            float outerRadius = 0f;
            float innerRadius = 0f;

            for (int i = (int) Math.floor(steps); i >= 0; i--) {
                innerRadius = outerRadius;
                outerRadius = Math.min(1f, (steps - i) * mInStepPercent) * maxRadius;

                if (i >= mInColors.length)
                    continue;

                if (innerRadius == 0) {
                    mPaint.setColor(mInColors[i]);
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x, y, outerRadius, mPaint);
                } else if (outerRadius > innerRadius) {
                    float radius = (innerRadius + outerRadius) / 2;
                    mRect.set(x - radius, y - radius, x + radius, y + radius);

                    mPaint.setStrokeWidth(outerRadius - innerRadius);
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setColor(mInColors[i]);

                    canvas.drawCircle(x, y, radius, mPaint);
                } else
                    break;
            }

            if (mProgressState == PROGRESS_STATE_HIDE) {
                if (steps >= 1 / mInStepPercent || time >= 1) {
                    resetAnimation();
                    mProgressState = PROGRESS_STATE_STRETCH;
                }
            } else {
                float radius = maxRadius - mStrokeSize / 2f;

                mRect.set(x - radius, y - radius, x + radius, y + radius);
                mPaint.setStrokeWidth(mStrokeSize);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(getIndeterminateStrokeColor());

                mPaint.setColor(getIndeterminateStrokeColor());
                canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, mPaint);
            }
        } else if (mRunState == RUN_STATE_STOPPING) { // 正在停止时
            float size = (float) mStrokeSize * Math.max(0, (mOutAnimationDuration - SystemClock.uptimeMillis() + mLastRunStateTime)) / mOutAnimationDuration;

            if (size > 0) {
                Rect bounds = getBounds();
                float radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize * 2 + size) / 2f;
                float x = (bounds.left + bounds.right) / 2f;
                float y = (bounds.top + bounds.bottom) / 2f;

                mRect.set(x - radius, y - radius, x + radius, y + radius);
                mPaint.setStrokeWidth(size);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(getIndeterminateStrokeColor());
                canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, mPaint);
            }
        } else if (mRunState != RUN_STATE_STOPPED) {
            Rect bounds = getBounds();
            float radius = (Math.min(bounds.width(), bounds.height()) - mPadding * 2 - mStrokeSize) / 2f;
            float x = (bounds.left + bounds.right) / 2f;
            float y = (bounds.top + bounds.bottom) / 2f;

            mRect.set(x - radius, y - radius, x + radius, y + radius);
            mPaint.setStrokeWidth(mStrokeSize);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(getIndeterminateStrokeColor());
            mPaint.setAlpha(mAlpha);
            canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, mPaint);
        }
    }


    private void resetAnimation() {
        mLastUpdateTime = SystemClock.uptimeMillis();
        mLastProgressStateTime = mLastUpdateTime;
        mStartAngle = mInitialAngle;
        mStrokeColorIndex = 0;
        mSweepAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;
    }

    private void start(boolean withAnimation) {
        if (isRunning())
            return;

        resetAnimation();

        if (withAnimation) {
            mRunState = RUN_STATE_STARTING;
            mLastRunStateTime = SystemClock.uptimeMillis();
            mProgressState = PROGRESS_STATE_HIDE;
        }

        scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
        invalidateSelf();
    }

    private void stop(boolean withAnimation) {
        if (!isRunning())
            return;

        if (withAnimation) {
            mLastRunStateTime = SystemClock.uptimeMillis();
            if (mRunState == RUN_STATE_STARTED) {
                scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
                invalidateSelf();
            }
            mRunState = RUN_STATE_STOPPING;
        } else {
            mRunState = RUN_STATE_STOPPED;
            unscheduleSelf(mUpdater);
            invalidateSelf();
        }
    }

    @Override
    public boolean isRunning() {
        return mRunState != RUN_STATE_STOPPED;
    }

    @Override
    public void scheduleSelf(Runnable what, long when) {
        if (mRunState == RUN_STATE_STOPPED)
            mRunState = mInAnimationDuration > 0 ? RUN_STATE_STARTING : RUN_STATE_RUNNING;
        super.scheduleSelf(what, when);
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations()
                | mState.mChangingConfigurations;
    }

    @Override
    public ConstantState getConstantState() {
        mState.mChangingConfigurations = getChangingConfigurations();
        return mState;
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState = createConstantState(mState, null);
            mMutated = true;
        }
        return this;
    }

    ProgressState createConstantState(ConstantState state, Resources res) {
        return new ProgressState(state, this);
    }

    private void update() {
        switch (mProgressMode) {
            case MODE_DETERMINATE:
                updateDeterminate();
                break;
            case MODE_INDETERMINATE:
                updateIndeterminate();
                break;
        }
    }

    private void updateDeterminate() {
        long curTime = SystemClock.uptimeMillis();
        float rotateOffset = (curTime - mLastUpdateTime) * 360f / mRotateDuration;
        if (mReverse)
            rotateOffset = -rotateOffset;
        mLastUpdateTime = curTime;

        mStartAngle += rotateOffset;

        if (mRunState == RUN_STATE_STARTING) {
            if (curTime - mLastRunStateTime > mInAnimationDuration) {
                mRunState = RUN_STATE_RUNNING;
            }
        } else if (mRunState == RUN_STATE_STOPPING) {
            if (curTime - mLastRunStateTime > mOutAnimationDuration) {
                stop(false);
                return;
            }
        }

        if (isRunning())
            scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);

        invalidateSelf();
    }

    private void updateIndeterminate() {
        //update animation
        long curTime = SystemClock.uptimeMillis();
        float rotateOffset = (curTime - mLastUpdateTime) * 360f / mRotateDuration;
        if (mReverse)
            rotateOffset = -rotateOffset;
        mLastUpdateTime = curTime;

        switch (mProgressState) {
            case PROGRESS_STATE_STRETCH:
                if (mTransformDuration <= 0) {
                    mSweepAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;
                    mProgressState = PROGRESS_STATE_KEEP_STRETCH;
                    mStartAngle += rotateOffset;
                    mLastProgressStateTime = curTime;
                } else {
                    float value = (curTime - mLastProgressStateTime) / (float) mTransformDuration;
                    float maxAngle = mReverse ? -mMaxSweepAngle : mMaxSweepAngle;
                    float minAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;

                    mStartAngle += rotateOffset;
                    mSweepAngle = mTransformInterpolator.getInterpolation(value) * (maxAngle - minAngle) + minAngle;

                    if (value > 1f) {
                        mSweepAngle = maxAngle;
                        mProgressState = PROGRESS_STATE_KEEP_STRETCH;
                        mLastProgressStateTime = curTime;
                    }
                }
                break;
            case PROGRESS_STATE_KEEP_STRETCH:
                mStartAngle += rotateOffset;

                if (curTime - mLastProgressStateTime > mKeepDuration) {
                    mProgressState = PROGRESS_STATE_SHRINK;
                    mLastProgressStateTime = curTime;
                }
                break;
            case PROGRESS_STATE_SHRINK:
                if (mTransformDuration <= 0) {
                    mSweepAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;
                    mProgressState = PROGRESS_STATE_KEEP_SHRINK;
                    mStartAngle += rotateOffset;
                    mLastProgressStateTime = curTime;
                    mStrokeColorIndex = (mStrokeColorIndex + 1) % mStrokeColors.length;
                } else {
                    float value = (curTime - mLastProgressStateTime) / (float) mTransformDuration;
                    float maxAngle = mReverse ? -mMaxSweepAngle : mMaxSweepAngle;
                    float minAngle = mReverse ? -mMinSweepAngle : mMinSweepAngle;

                    float newSweepAngle = (1f - mTransformInterpolator.getInterpolation(value)) * (maxAngle - minAngle) + minAngle;
                    mStartAngle += rotateOffset + mSweepAngle - newSweepAngle;
                    mSweepAngle = newSweepAngle;

                    if (value > 1f) {
                        mSweepAngle = minAngle;
                        mProgressState = PROGRESS_STATE_KEEP_SHRINK;
                        mLastProgressStateTime = curTime;
                        mStrokeColorIndex = (mStrokeColorIndex + 1) % mStrokeColors.length;
                    }
                }
                break;
            case PROGRESS_STATE_KEEP_SHRINK:
                mStartAngle += rotateOffset;

                if (curTime - mLastProgressStateTime > mKeepDuration) {
                    mProgressState = PROGRESS_STATE_STRETCH;
                    mLastProgressStateTime = curTime;
                }
                break;
        }

        if (mRunState == RUN_STATE_STARTING) {
            if (curTime - mLastRunStateTime > mInAnimationDuration) {
                mRunState = RUN_STATE_RUNNING;
                if (mProgressState == PROGRESS_STATE_HIDE) {
                    resetAnimation();
                    mProgressState = PROGRESS_STATE_STRETCH;
                }
            }
        } else if (mRunState == RUN_STATE_STOPPING) {
            if (curTime - mLastRunStateTime > mOutAnimationDuration) {
                stop(false);
                return;
            }
        }

        if (isRunning())
            scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);

        invalidateSelf();
    }

    /**
     * Initializes local dynamic properties from state. This should be called
     * after significant state changes, e.g. from the One True Constructor and
     * after inflating or applying a theme.
     */
    private void initializeWithState(ProgressState state, Resources res) {
        mState = createConstantState(mState, null);
        mTintFilter = createTintFilter(state.mTint, state.mTintMode);
    }

    /**
     * Ensures the tint filter is consistent with the current tint color and
     * mode.
     */
    PorterDuffColorFilter createTintFilter(ColorStateList tint, Mode tintMode) {
        if (tint == null || tintMode == null) {
            return null;
        }

        final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
        return new PorterDuffColorFilter(color, tintMode);
    }

    static class ProgressState extends ConstantState {
        Builder mBuilder;

        ColorStateList mTint = null;
        Mode mTintMode = PorterDuff.Mode.SRC_IN;
        int mChangingConfigurations;

        public ProgressState(ConstantState orig, CircularProgressDrawable owner) {
            mBuilder = new Builder();

            if (orig != null && orig instanceof ProgressState) {
                final ProgressState origs = (ProgressState) orig;
                mBuilder = origs.mBuilder;
                mChangingConfigurations = origs.mChangingConfigurations;
            } else if (owner != null) {
                mBuilder.mPadding = owner.mPadding;
                mBuilder.mInitialAngle = owner.mInitialAngle;
                mBuilder.mProgressPercent = owner.mProgressPercent;
                mBuilder.mSecondaryProgressPercent = owner.mSecondaryProgressPercent;
                mBuilder.mMaxSweepAngle = owner.mMaxSweepAngle;
                mBuilder.mMinSweepAngle = owner.mMinSweepAngle;
                mBuilder.mStrokeSize = owner.mStrokeSize;
                mBuilder.mStrokeColors = owner.mStrokeColors;
                mBuilder.mStrokeSecondaryColor = owner.mStrokeSecondaryColor;
                mBuilder.mReverse = owner.mReverse;
                mBuilder.mRotateDuration = owner.mRotateDuration;
                mBuilder.mTransformDuration = owner.mTransformDuration;
                mBuilder.mKeepDuration = owner.mKeepDuration;
                mBuilder.mTransformInterpolator = owner.mTransformInterpolator;
                mBuilder.mProgressMode = owner.mProgressMode;
                mBuilder.mInStepPercent = owner.mInStepPercent;
                mBuilder.mInColors = owner.mInColors;
                mBuilder.mInAnimationDuration = owner.mInAnimationDuration;
                mBuilder.mOutAnimationDuration = owner.mOutAnimationDuration;
                mBuilder.mAlpha = owner.mAlpha;
            }
        }

        @Override
        public boolean canApplyTheme() {
            return super.canApplyTheme();
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            CircularProgressDrawable drawable = mBuilder.build();
            drawable.initializeWithState(this, null);
            return drawable;
        }

        @NonNull
        @Override
        public Drawable newDrawable(Resources res) {
            CircularProgressDrawable drawable = mBuilder.build();
            drawable.initializeWithState(drawable.mState, res);
            return drawable;
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }

    public static class Builder {
        private int mPadding;
        private float mInitialAngle;
        private float mProgressPercent;
        private float mSecondaryProgressPercent;
        private float mMaxSweepAngle;
        private float mMinSweepAngle;
        private int mStrokeSize;
        private int[] mStrokeColors;
        private int mStrokeSecondaryColor;
        private boolean mReverse;
        private int mRotateDuration;
        private int mTransformDuration;
        private int mKeepDuration;
        private Interpolator mTransformInterpolator;
        private int mProgressMode;
        private float mInStepPercent;
        private int[] mInColors;
        private int mInAnimationDuration;
        private int mOutAnimationDuration;

        private int mAlpha;

        public Builder() {
        }

        public Builder(Context context) {
            this(context, R.style.Widget_Ticwear_CircularProgressDrawable);
        }

        public Builder(Context context, int defStyleRes) {
            TypedArray a = context.obtainStyledAttributes(null, R.styleable.CircularProgressDrawable, 0, defStyleRes);

            strokeSize(a.getDimensionPixelSize(R.styleable.CircularProgressDrawable_tic_cpd_strokeSize, 0));
            initialAngle(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_initialAngle, 0));
            progressPercent(a.getFloat(R.styleable.CircularProgressDrawable_tic_cpd_progress, 0));
            progressMode(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_progressMode, MODE_INDETERMINATE));
            secondaryProgressPercent(a.getFloat(R.styleable.CircularProgressDrawable_tic_cpd_secondaryProgress, 0));
            maxSweepAngle(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_maxSweepAngle, 0));
            minSweepAngle(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_minSweepAngle, 0));
            reverse(a.getBoolean(R.styleable.CircularProgressDrawable_tic_cpd_reverse, false));
            rotateDuration(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_rotateDuration, context.getResources().getInteger(android.R.integer.config_longAnimTime)));
            transformDuration(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_transformDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
            keepDuration(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_keepDuration, context.getResources().getInteger(android.R.integer.config_shortAnimTime)));
            inAnimDuration(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_inAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
            progressAlpha(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_progressAlpha, 128));
            inStepPercent(a.getFloat(R.styleable.CircularProgressDrawable_tic_cpd_inStepPercent, 0.5f));
            outAnimDuration(a.getInteger(R.styleable.CircularProgressDrawable_tic_cpd_outAnimDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));

            a.recycle();
        }

        public CircularProgressDrawable build() {
            if (mStrokeColors == null)
                mStrokeColors = new int[]{0xFFFFFFFF};

            if (mInColors == null && mInAnimationDuration > 0)
                mInColors = new int[]{0xFFB5D4FF, 0xFFDEEAFC, 0xFFFAFFFE};

            if (mTransformInterpolator == null)
                mTransformInterpolator = new DecelerateInterpolator();

            return new CircularProgressDrawable(mPadding, mInitialAngle, mProgressPercent, mSecondaryProgressPercent, mMaxSweepAngle, mMinSweepAngle, mStrokeSize, mStrokeColors, mStrokeSecondaryColor, mReverse, mRotateDuration, mTransformDuration, mKeepDuration, mTransformInterpolator, mProgressMode, mInAnimationDuration, mInStepPercent, mInColors, mOutAnimationDuration, mAlpha);
        }

        public Builder padding(int padding) {
            mPadding = padding;
            return this;
        }

        public Builder initialAngle(float angle) {
            mInitialAngle = angle;
            return this;
        }

        public Builder progressPercent(float percent) {
            mProgressPercent = percent;
            return this;
        }

        public Builder secondaryProgressPercent(float percent) {
            mSecondaryProgressPercent = percent;
            return this;
        }

        public Builder maxSweepAngle(float angle) {
            mMaxSweepAngle = angle;
            return this;
        }

        public Builder minSweepAngle(float angle) {
            mMinSweepAngle = angle;
            return this;
        }

        public Builder strokeSize(int strokeSize) {
            mStrokeSize = strokeSize;
            return this;
        }

        public Builder strokeColors(int... strokeColors) {
            mStrokeColors = strokeColors;
            return this;
        }

        public Builder strokeSecondaryColor(int color) {
            mStrokeSecondaryColor = color;
            return this;
        }

        public Builder reverse(boolean reverse) {
            mReverse = reverse;
            return this;
        }

        public Builder reverse() {
            return reverse(true);
        }

        public Builder rotateDuration(int duration) {
            mRotateDuration = duration;
            return this;
        }

        public Builder transformDuration(int duration) {
            mTransformDuration = duration;
            return this;
        }

        public Builder keepDuration(int duration) {
            mKeepDuration = duration;
            return this;
        }

        public Builder transformInterpolator(Interpolator interpolator) {
            mTransformInterpolator = interpolator;
            return this;
        }

        public Builder progressMode(int mode) {
            mProgressMode = mode;
            return this;
        }

        public Builder inAnimDuration(int duration) {
            mInAnimationDuration = duration;
            return this;
        }

        public Builder inStepPercent(float percent) {
            mInStepPercent = percent;
            return this;
        }

        public Builder inStepColors(int... colors) {
            mInColors = colors;
            return this;
        }

        public Builder outAnimDuration(int duration) {
            mOutAnimationDuration = duration;
            return this;
        }

        public Builder progressAlpha(int alpha) {
            mAlpha = alpha;
            return this;
        }
    }
}
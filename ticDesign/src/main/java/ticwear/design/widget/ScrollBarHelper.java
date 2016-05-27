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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import ticwear.design.DesignConfig;
import ticwear.design.R;

/**
 * Draw round scrolbar, see {@link CoordinatorLayout} to see how to use.
 *
 * The scroll view's width and height must be match_parent(the screen size of this device).
 * Also set android:scrollbarSize="0dp" and android:scrollbars="vertical" to enable scrollbar.
 *
 * Created by goodev on 2016/4/13.
 */
public class ScrollBarHelper {

    static final String TAG = "SBHelper";

    private static final float START_ANGLE = -30.0F;
    private static final float SWEEP_ANGLE = 60.0F;
    private static final float MIN_SWEEP = 3.0F;

    private boolean mIsRound = true;
    private Paint mPaint = new Paint();
    private RectF mOval = null;
    private int mBgColor;
    private int mSweepColor;
    private final Point mOffset = new Point();

    private final float mScreenRadius;

    public ScrollBarHelper(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ScrollBar, defStyleAttr, R.style.Widget_Ticwear_ScrollBar);
        float strokeWidth = a.getDimension(R.styleable.ScrollBar_tic_scroll_bar_strokeWidth, 0f);
        float margin = a.getDimension(R.styleable.ScrollBar_tic_scroll_bar_margin, 0f);
        mBgColor = a.getColor(R.styleable.ScrollBar_tic_scroll_bar_bgColor, 0x66666666);
        mSweepColor = a.getColor(R.styleable.ScrollBar_tic_scroll_bar_sweepColor, 0xff0098e6);
        a.recycle();

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScreenRadius = metrics.widthPixels / 2f;
        float offset = margin + strokeWidth / 2f;
        float scrollBarRadius = mScreenRadius - offset;
        mOval = new RectF(offset, offset, scrollBarRadius * 2, scrollBarRadius * 2);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(strokeWidth);
    }

    public void setIsRound(boolean isRound) {
        mIsRound = isRound;
    }

    public void setViewOffset(int x, int y) {
        mOffset.set(x, y);
    }

    /**
     * Call this in hidden API onDrawVerticalScrollBar.
     *
     * @param canvas canvas from view.
     * @param range see {@link View#computeVerticalScrollRange()}
     * @param offset see {@link View#computeVerticalScrollOffset()}
     * @param extent see {@link View#computeVerticalScrollExtent()}
     * @param alpha alpha of scrollbar
     */
    public void onDrawScrollBar(Canvas canvas, int range, int offset, int extent, int alpha) {

        float strokeRadius = mPaint.getStrokeWidth() / 2;

        float extraAngle = 0;
        float offsetY = mOffset.y;
        if (offsetY > 0) {
            float targetHeight = mScreenRadius - offsetY - strokeRadius;
            double targetRadians = - Math.asin(targetHeight / mScreenRadius);
            float targetAngle = (float) (360 * targetRadians / Math.PI / 2);
            extraAngle = MathUtils.constrain(targetAngle - START_ANGLE, 0f, -START_ANGLE);
        } else if (offsetY < 0) {
            float targetHeight = mScreenRadius + offsetY - strokeRadius;
            double targetRadians = Math.asin(targetHeight / mScreenRadius);
            float targetAngle = (float) (360 * targetRadians / Math.PI / 2);
            extraAngle = MathUtils.constrain(START_ANGLE + SWEEP_ANGLE - targetAngle, 0f, START_ANGLE + SWEEP_ANGLE);
        }

        float startAngle = MathUtils.constrain(START_ANGLE + extraAngle, START_ANGLE, 0);
        float sweepAngle = MathUtils.constrain(SWEEP_ANGLE - 2 * extraAngle, 0, SWEEP_ANGLE);
        float minSweep = MIN_SWEEP * sweepAngle / SWEEP_ANGLE;

        float thumbSweep = (extent * sweepAngle) / range;
        thumbSweep = MathUtils.constrain(thumbSweep, minSweep, sweepAngle);
        float thumbRotation = (sweepAngle - thumbSweep) * (offset) / (range - extent);

        float opacity = alpha / 255f;

        if (DesignConfig.DEBUG_SCROLLBAR && offsetY != 0) {
            Log.v(TAG, "offset " + offsetY + ", extra " + extraAngle +
                    ", scrollbar (" + startAngle + "," + sweepAngle + "), with opacity " + opacity);
        }

        canvas.save();
        canvas.translate(-mOffset.x, -mOffset.y);
        if (mIsRound) {
            setColorWithOpacity(mPaint, mBgColor, opacity);
            canvas.drawArc(mOval, startAngle, sweepAngle, false, mPaint);
            setColorWithOpacity(mPaint, mSweepColor, opacity);
            canvas.rotate(thumbRotation, mOval.centerX(), mOval.centerY());
            canvas.drawArc(mOval, startAngle, thumbSweep, false, mPaint);
        } else {
            float x = mOval.right;
            setColorWithOpacity(mPaint, mBgColor, opacity);
            float startY = getY(startAngle, x);
            float length = getY(startAngle + sweepAngle, x) - startY;
            canvas.drawLine(x, startY, x, startY + length, mPaint);
            setColorWithOpacity(mPaint, mSweepColor, opacity);
            float start = startY + (thumbRotation / sweepAngle) * length;
            float end = startY + ((thumbRotation + thumbSweep) / sweepAngle) * length;
            canvas.drawLine(x, start, x, end, mPaint);
        }
        canvas.restore();

    }

    private void setColorWithOpacity(Paint paint, int color, float opacity) {
        int targetAlpha = (int) (Color.alpha(color) * opacity);
        paint.setColor(color);
        paint.setAlpha(targetAlpha);
    }

    public float getY(float angle, float x) {
        double rad = angle * Math.PI * 2 / 360f;
        return (float) (mOval.centerY() + (x - mOval.centerX()) * Math.tan(rad));
    }
}

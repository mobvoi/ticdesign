package ticwear.design.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;

import ticwear.design.R;

/**
 * Draw round scrolbar, see {@link TicklableListView} to see how to use.
 *
 * The scroll view's width and height must be match_parent(the screen size of this device).
 * Also set android:scrollbarSize="0dp" and android:scrollbars="vertical" to enable scrollbar.
 *
 * Created by goodev on 2016/4/13.
 */
public class ScrollBarHelper {
    private static final float START_ANGLE = -30.0F;
    private static final float SWEEP_ANGLE = 60.0F;
    private static final float MIN_SWEEP = 3.0F;

    private boolean mIsRound = true;
    private float mSweep = 0.0f;
    private float mRotation;
    private Paint mPaint = new Paint();
    private RectF mOval = null;
    private int mBgColor;
    private int mSweepColor;

    public ScrollBarHelper(Context context) {
        TypedArray a = context.obtainStyledAttributes(null,
                R.styleable.ScrollBar, 0, R.style.Widget_Ticwear_ScrollBar);
        float strokeWidth = a.getDimension(R.styleable.ScrollBar_tic_scroll_bar_strokeWidth, 0f);
        int margin = (int)a.getDimension(R.styleable.ScrollBar_tic_scroll_bar_margin, 0f);
        mBgColor = a.getColor(R.styleable.ScrollBar_tic_scroll_bar_bgColor, 0x66666666);
        mSweepColor = a.getColor(R.styleable.ScrollBar_tic_scroll_bar_sweepColor, 0xff0098e6);
        a.recycle();

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels - margin;
        mOval = new RectF(margin, margin, width, width);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(strokeWidth);
    }

    public void setIsRound(boolean isRound) {
        mIsRound = isRound;
    }

    /**
     *
     * @param canvas
     * @param range see {@link View#computeVerticalScrollRange()}
     * @param offset see {@link View#computeVerticalScrollOffset()}
     * @param extent see {@link View#computeVerticalScrollExtent()}
     * @param alpha
     */
    public void onDrawScrollBar(Canvas canvas, int range, int offset, int extent, int alpha) {
        mSweep = (extent * SWEEP_ANGLE) / range;
        mSweep = (mSweep < MIN_SWEEP && mSweep > 0.0f) ? MIN_SWEEP : mSweep;
        mRotation = (SWEEP_ANGLE - mSweep) * (offset) / (range - extent);
        mPaint.setAlpha(alpha);

        canvas.save();
        if (mIsRound) {
            mPaint.setColor(mBgColor);
            canvas.drawArc(mOval, START_ANGLE, SWEEP_ANGLE, false, mPaint);
            mPaint.setColor(mSweepColor);
            canvas.rotate(mRotation, mOval.centerX(), mOval.centerY());
            canvas.drawArc(mOval, START_ANGLE, mSweep, false, mPaint);
        } else {
            float x = mOval.right;
            mPaint.setColor(mBgColor);
            float startY = getY(START_ANGLE, x);
            float length = getY(START_ANGLE + SWEEP_ANGLE, x) - startY;
            canvas.drawLine(x, startY, x, startY + length, mPaint);
            mPaint.setColor(mSweepColor);
            float start = startY + (mRotation / SWEEP_ANGLE) * length;
            float end = startY + ((mRotation + mSweep) / SWEEP_ANGLE) * length;
            canvas.drawLine(x, start, x, end, mPaint);
        }
        canvas.restore();

    }

    public float getY(float angle, float x) {
        double rad = angle * Math.PI * 2 / 360f;
        return (float) (mOval.centerY() + (x - mOval.centerX()) * Math.tan(rad));
    }
}

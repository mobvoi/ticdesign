package ticwear.design.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import ticwear.design.R;

/**
 * 内置三种固定尺寸 (150*150, 120*120, 32*32)
 */
public class ClockLoadingView extends View {
    private static final int STYLE_LARGE = 1;
    private static final int STYLE_MIDDLE = 2;
    private static final int STYLE_SMALL = 3;
    private static final int STYLE_CUSTOM = 0;

    private static final int SIZE_LARGE = 150;
    private static final int SIZE_MIDDLE = 120;
    private static final int SIZE_SMALL = 32;

    private static final int BG_RADIUS_LARGE = 60;
    private static final int BG_THICK_WIDTH_LARGE = 8;
    private static final int HOUR_HAND_WIDTH_LARGE = 42;
    private static final int HOUR_HAND_HEIGHT_LARGE = 8;
    private static final int MINUTE_HAND_WIDTH_LARGE = 8;
    private static final int MINUTE_HAND_HEIGHT_LARGE = 48;

    private static final int BG_RADIUS_MIDDLE = 48;
    private static final int BG_THICK_WIDTH_MIDDLE = 8;
    private static final int HOUR_HAND_WIDTH_MIDDLE = 32;
    private static final int HOUR_HAND_HEIGHT_MIDDLE = 6;
    private static final int MINUTE_HAND_WIDTH_MIDDLE = 6;
    private static final int MINUTE_HAND_HEIGHT_MIDDLE = 38;

    private static final int BG_RADIUS_SMALL = 12;
    private static final int BG_THICK_WIDTH_SMALL = 2;
    private static final int HOUR_HAND_WIDTH_SMALL = 6;
    private static final int HOUR_HAND_HEIGHT_SMALL = 2;
    private static final int MINUTE_HAND_WIDTH_SMALL = 2;
    private static final int MINUTE_HAND_HEIGHT_SMALL = 8;

    private final int mViewColor;
    private final int mOneCycleDuration;
    private int mStyle;

    private float mBgRadius;
    private float mBgThickWidth;
    private float mHourHandWidth;
    private float mHourHandHeight;
    private float mMinuteHandWidth;
    private float mMinuteHandHeight;

    private Paint mPaint;

    private ValueAnimator valueAnimator;
    private float animatedValue;

    public ClockLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedValue colorAccent = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, colorAccent, true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockLoadingView);
        mViewColor = typedArray.getColor(R.styleable.ClockLoadingView_tic_clv_progressColor, colorAccent.data);
        mOneCycleDuration = typedArray.getInteger(R.styleable.ClockLoadingView_tic_clv_cycleDuration, 8000);
        mStyle = typedArray.getInt(R.styleable.ClockLoadingView_tic_clv_style, STYLE_CUSTOM);
        if (STYLE_LARGE == mStyle) {
            mBgRadius = BG_RADIUS_LARGE;
            mBgThickWidth = BG_THICK_WIDTH_LARGE;
            mHourHandWidth = HOUR_HAND_WIDTH_LARGE;
            mHourHandHeight = HOUR_HAND_HEIGHT_LARGE;
            mMinuteHandWidth = MINUTE_HAND_WIDTH_LARGE;
            mMinuteHandHeight = MINUTE_HAND_HEIGHT_LARGE;
        } else if (STYLE_MIDDLE == mStyle) {
            mBgRadius = BG_RADIUS_MIDDLE;
            mBgThickWidth = BG_THICK_WIDTH_MIDDLE;
            mHourHandWidth = HOUR_HAND_WIDTH_MIDDLE;
            mHourHandHeight = HOUR_HAND_HEIGHT_MIDDLE;
            mMinuteHandWidth = MINUTE_HAND_WIDTH_MIDDLE;
            mMinuteHandHeight = MINUTE_HAND_HEIGHT_MIDDLE;
        } else if (STYLE_SMALL == mStyle) {
            mBgRadius = BG_RADIUS_SMALL;
            mBgThickWidth = BG_THICK_WIDTH_SMALL;
            mHourHandWidth = HOUR_HAND_WIDTH_SMALL;
            mHourHandHeight = HOUR_HAND_HEIGHT_SMALL;
            mMinuteHandWidth = MINUTE_HAND_WIDTH_SMALL;
            mMinuteHandHeight = MINUTE_HAND_HEIGHT_SMALL;
        } else {
            mBgRadius = typedArray.getDimension(R.styleable.ClockLoadingView_tic_clv_bgRadius, BG_RADIUS_LARGE);
            mBgThickWidth = typedArray.getDimension(R.styleable.ClockLoadingView_tic_clv_bgThickWidth, BG_THICK_WIDTH_LARGE);
            mHourHandWidth = typedArray.getDimension(R.styleable.ClockLoadingView_tic_clv_hourHandWidth, HOUR_HAND_WIDTH_LARGE);
            mHourHandHeight = typedArray.getDimension(R.styleable.ClockLoadingView_tic_clv_hourHandHeight, HOUR_HAND_HEIGHT_LARGE);
            mMinuteHandWidth = typedArray.getDimension(R.styleable.ClockLoadingView_tic_clv_minuteHandWidth, MINUTE_HAND_WIDTH_LARGE);
            mMinuteHandHeight = typedArray.getDimension(R.styleable.ClockLoadingView_tic_clv_minuteHandHeight, MINUTE_HAND_HEIGHT_LARGE);
        }
        typedArray.recycle();
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mViewColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        valueAnimator = ValueAnimator.ofFloat(0, 1f);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(mOneCycleDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animatedValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public void startAnimation() {
        if (valueAnimator != null && !valueAnimator.isRunning()) {
            valueAnimator.start();
        }
    }

    public void stopAnimation() {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
            valueAnimator.end();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (STYLE_LARGE == mStyle) {
            setMeasuredDimension(SIZE_LARGE, SIZE_LARGE);
        } else if (STYLE_MIDDLE == mStyle) {
            setMeasuredDimension(SIZE_MIDDLE, SIZE_MIDDLE);
        } else if (STYLE_SMALL == mStyle) {
            setMeasuredDimension(SIZE_SMALL, SIZE_SMALL);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int halfWidth = width >> 1;
        int halfHeight = height >> 1;

        mPaint.setStrokeWidth(mBgThickWidth);
        canvas.drawCircle(halfWidth, halfHeight, mBgRadius - mBgThickWidth / 2, mPaint);
        canvas.save();

        mPaint.setStrokeWidth(mHourHandHeight);
        canvas.rotate(animatedValue * 360, halfWidth, halfHeight);
        canvas.drawLine(halfWidth, halfHeight, halfWidth + mHourHandWidth - mHourHandHeight / 2,
                halfHeight, mPaint);
        canvas.restore();

        canvas.save();
        mPaint.setStrokeWidth(mMinuteHandWidth);
        canvas.rotate(animatedValue * 4 * 360, halfWidth, halfHeight);
        canvas.drawLine(halfWidth, halfHeight, halfWidth,
                halfHeight - mMinuteHandHeight - mMinuteHandWidth / 2, mPaint);
        canvas.restore();
    }
}
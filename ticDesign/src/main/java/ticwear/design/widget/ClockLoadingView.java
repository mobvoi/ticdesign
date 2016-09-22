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

import ticwear.design.R;

public class ClockLoadingView extends View {
    private final Context mContext;
    private final int mViewColor;
    private final int mOneCycleDuration;
    private Paint mPaint;
    private int mMeasureWidth;
    private int mMeastureHeight;

    private ValueAnimator valueAnimator;
    private float animatedValue;

    public ClockLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        TypedValue colorAccent = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, colorAccent, true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockLoadingView);
        mViewColor = typedArray.getColor(R.styleable.ClockLoadingView_tic_clv_progress_color, colorAccent.data);
        mOneCycleDuration = typedArray.getInteger(R.styleable.ClockLoadingView_tic_clv_cycle_duration, 8000);
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
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
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
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == VISIBLE) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStrokeWidth(dp2px(4));
        canvas.drawCircle(mMeasureWidth / 2, mMeastureHeight / 2, dp2px(20), mPaint);
        canvas.save();

        mPaint.setStrokeWidth(dp2px(3));
        canvas.rotate(animatedValue * 4 * 360, mMeasureWidth / 2, mMeastureHeight / 2);
        canvas.drawLine(mMeasureWidth / 2, mMeastureHeight / 2, mMeasureWidth / 2,
                mMeastureHeight / 2 - dp2px(14), mPaint);
        canvas.restore();

        canvas.save();
        mPaint.setStrokeWidth(dp2px(3));
        canvas.rotate(animatedValue * 360, mMeasureWidth / 2, mMeastureHeight / 2);
        canvas.drawLine(mMeasureWidth / 2, mMeastureHeight / 2, mMeasureWidth / 2 + dp2px(11),
                mMeastureHeight / 2, mPaint);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMeastureHeight = getMeasuredHeight();
        mMeasureWidth = getMeasuredWidth();
    }

    public float dp2px(float value) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return value * density;
    }
}

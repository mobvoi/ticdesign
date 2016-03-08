package mobvoi.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import mobvoi.design.R;

/**
 * Created by tankery on 1/28/16.
 *
 * A {@link TextView} That can set text scale type like {@link android.widget.ImageView}.
 *
 * @attr ref mobvoi.design.R.styleable#ScalableTextView_scaleFactor
 */
public class ScalableTextView extends TextView {

    // Avoid allocations...
    private RectF mTempSrc = new RectF();
    private RectF mTempDst = new RectF();

    private float mTextScale = 1f;

    /**
     * A factor multiplied to text scale.
     *
     * When equals 0, there is no scale effect, when equals 1, the text scaled with view change.
     * textScale = 1f + (scale - 1f) * scaleFactor
     */
    private float mScaleFactor;

    public ScalableTextView(Context context) {
        this(context, null);
    }

    public ScalableTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public ScalableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ScalableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ScalableTextView, defStyleAttr, defStyleRes);

        mScaleFactor = a.getFloat(R.styleable.ScalableTextView_scaleFactor, 0.5f);

        a.recycle();
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.mScaleFactor = scaleFactor;
        updateDrawingMatrix();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            if (mTempSrc.isEmpty()) {
                updateContainerRect(mTempSrc, left, top, right, bottom);
            }
            updateContainerRect(mTempDst, left, top, right, bottom);

            updateDrawingMatrix();
        }
    }

    private void updateContainerRect(@NonNull RectF rect, int left, int top, int right, int bottom) {
        rect.set(0, 0, right - left, bottom - top);
    }

    /**
     * update drawing matrix with source rect & dest rect
     */
    private void updateDrawingMatrix() {
        if (mTempSrc.isEmpty() || mTempDst.isEmpty())
            return;

        if (mScaleFactor > 0) {
            int srcWidth = (int) mTempSrc.width();
            int srcHeight = (int) mTempSrc.height();

            int dstWidth = (int) mTempDst.width();
            int dstHeight = (int) mTempDst.height();

            float scale;

            // Select the larger scale.
            if (srcWidth * dstHeight > dstWidth * srcHeight) {
                scale = (float) dstHeight / (float) srcHeight;
            } else {
                scale = (float) dstWidth / (float) srcWidth;
            }

            mTextScale = 1f + (scale - 1f) * mScaleFactor;
        } else {
            mTextScale = 1f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        canvas.scale(mTextScale, mTextScale, width * 0.5f, height * 0.5f);
        super.onDraw(canvas);
    }
}

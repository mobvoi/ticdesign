package ticwear.design.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ProgressBarButton extends ImageView {

    private static final int LONG_PRESS_DELAY = 300;

    private static int mDefaultImageSize;

    private TouchListener mTouchListener;

    public ProgressBarButton(Context context) {
        super(context);
    }

    public ProgressBarButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ProgressBarButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setTouchListener(TouchListener touchListener) {
        if (mTouchListener == touchListener) {
            return;
        }

        stopLongPressUpdate();

        mTouchListener = touchListener;
    }

    private void stopLongPressUpdate() {
        removeCallbacks(mLongPressUpdateRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLongPressUpdate();
        setTouchListener(null);
        super.onDetachedFromWindow();
    }

    private final Runnable mLongPressUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTouchListener != null) {
                mTouchListener.onLongPress();
                ProgressBarButton.this.postDelayed(this, 60);
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mTouchListener == null) {
            return true;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mTouchListener.onDown();
            postDelayed(mLongPressUpdateRunnable, LONG_PRESS_DELAY);
        } else {
            stopLongPressUpdate();
            if (action == MotionEvent.ACTION_UP) {
                mTouchListener.onUp();
            }
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int padding = (height - mDefaultImageSize)/2;
        this.setPadding(padding, padding, padding, padding);
        setMeasuredDimension(width, height);
    }

    public void setDefaultImageSize(int size) {
        mDefaultImageSize = size;
    }

    public interface TouchListener {
        void onDown();

        void onUp();

        void onLongPress();
    }
}

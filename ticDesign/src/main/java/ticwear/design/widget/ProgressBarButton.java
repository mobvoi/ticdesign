package ticwear.design.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ProgressBarButton extends ImageView {
    private static final int MSG_LONG_PRESS = 1;
    private static final int DELAY_MILLIS = 60;

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

    private MyHandler mHandler;

    public void setTouchListener(TouchListener touchListener) {
        mHandler = new MyHandler();
        mTouchListener = touchListener;
    }

    public void removeTouchListener() {
        mTouchListener = null;
        if (mHandler != null) {
            mHandler.removeMessages(MSG_LONG_PRESS);
            mHandler = null;
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LONG_PRESS:
                    sendEmptyMessageDelayed(MSG_LONG_PRESS, 60);
                    ProgressBarButton.this.mTouchListener.onLongPress();
                    break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mTouchListener == null) {
            return true;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mTouchListener.onDown();
            mHandler.sendEmptyMessageDelayed(MSG_LONG_PRESS, DELAY_MILLIS);
        } else {
            if (action == MotionEvent.ACTION_UP) {
                mTouchListener.onUp();
            }
            scale(this, false);
            mHandler.removeMessages(MSG_LONG_PRESS);
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

    private void scale(View view, boolean isPressed) {
        float endScale = isPressed ? 0.9f : 1f;
        if (view != null) {
            view.animate().scaleX(endScale).scaleY(endScale).setDuration(200L)
                    .start();
        }
    }

    public interface TouchListener {
        void onDown();

        void onUp();

        void onLongPress();
    }


}

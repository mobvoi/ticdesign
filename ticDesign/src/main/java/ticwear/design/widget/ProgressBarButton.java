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

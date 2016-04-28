package ticwear.design.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import ticwear.design.R;

/**
 * 实现类似progressbar功能，
 * 点击左右有两个按钮可以调节progressbar的值，
 * 也可以点击progressbar本身去调节值
 * Created by louxiaodan on 16/4/27.
 */
public class VolumeBar extends FrameLayout {
    private ProgressBarButton mMinButton;
    private ProgressBarButton mMaxButton;

    // 当前值
    private int mProgress = 50;
    private int mProgressStart;
    // 点击按钮时变化的值
    private int mProgressStep = 10;
    // 当数值小于（大于）10时隐藏减号（加号）
    private Paint mPaint;
    // bar中图片image的半径（乘2的值为宽和高）
    private int mDrawableRadius;
    // thumb背景
    private Drawable mVolumeDrawable;
    // 无声时thumb背景
    private Drawable mNoVolumeDrawable;
    // 减号
    private Drawable mMinButtonDrawable;
    // 加号
    private Drawable mMaxButtonDrawable;
    // 背景色
    private int mBgColor;
    // 数值颜色
    private int mValueColor;
    // 前后左右的padding
    private int mTouchPaddingLeft;
    private int mTouchPaddingRight;
    private int mTouchPaddingTop;
    private int mTouchPaddingBottom;
    // 数值改变时回到监听器的onVolumeChanged()
    private OnVolumeChangedListener mListener;

    public VolumeBar(Context context) {
        this(context, null);
    }

    public VolumeBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VolumeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_Ticwear_VolumeBar);
    }

    public VolumeBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setWillNotDraw(false);
        inflater.inflate(R.layout.volume_bar_ticwear, this, true);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.VolumeBar, defStyleAttr, defStyleRes);
        mDrawableRadius = (a.getDimensionPixelSize(R.styleable.VolumeBar_tic_vb_btnImageSize, 32))/2;
        mBgColor = a.getColor(R.styleable.VolumeBar_tic_vb_bgColor, Color.RED);
        mValueColor = a.getColor(R.styleable.VolumeBar_tic_vb_valueColor, Color.GREEN);
        mTouchPaddingLeft = a.getDimensionPixelSize(R.styleable.VolumeBar_tic_vb_touchPaddingLeft, 0);
        mTouchPaddingRight = a.getDimensionPixelSize(R.styleable.VolumeBar_tic_vb_touchPaddingRight, 0);
        mTouchPaddingTop = a.getDimensionPixelSize(R.styleable.VolumeBar_tic_vb_touchPaddingTop, 0);
        mTouchPaddingBottom = a.getDimensionPixelSize(R.styleable.VolumeBar_tic_vb_touchPaddingBottom, 0);
        int thumbImageId = a.getResourceId(R.styleable.VolumeBar_tic_vb_thumbImage, 0);
        int thumbLeftImageId = a.getResourceId(R.styleable.VolumeBar_tic_vb_thumbLeftImage, 0);
        a.recycle();
        mPaint = new Paint();

        // 读取需要的背景图
        Resources.Theme t = context.getApplicationContext().getTheme();
        if (thumbImageId != 0) {
            mVolumeDrawable = getResources().getDrawable(thumbImageId, t);
            mNoVolumeDrawable = getResources().getDrawable(thumbLeftImageId, t);
        }
        mMinButtonDrawable = getResources().getDrawable(R.drawable.tic_ic_minus_32px, t);
        mMaxButtonDrawable = getResources().getDrawable(R.drawable.tic_ic_plus_32px, t);

        // 设定各按钮监听器
        mMinButton = (ProgressBarButton) findViewById(R.id.min);
        mMinButton.setDefaultImageSize(mDrawableRadius*2);
        mMinButton.setTouchListener(mMinButtonListener);

        mMaxButton = (ProgressBarButton) findViewById(R.id.max);
        mMaxButton.setTouchListener(mMaxButtonListener);
    }



    public interface OnVolumeChangedListener {
        void onVolumeChanged(VolumeBar volumeBar, int progress);
    }

    public void setOnVolumeChangedListetener (OnVolumeChangedListener listener) {
        mListener = listener;
    }

    /**
     * 设定当前值
     * @param level 当前值
     */
    public void setLevel(int level) {
        mProgress = level;
        invalidate();
    }

    /**
     * 得到但前值
     * @return 当前值
     */
    public int getLevel() {
        return mProgress;
    }

    /**
     * 设定按下按钮时改变的大小
     * @param step 改变的大小
     */
    public void setStep (int step) {
        mProgressStep = step;
    }

    /**
     * 设定隐藏按钮的门限值，当减号（加号）在门限值下（上）时隐藏
     * @param threshold 门限值
     */
    public void setHiddenImageThreshold (int threshold) {
//        mhiddenImageThreshold = threshold;
    }

    private ProgressBarButton.TouchListener mMinButtonListener = new ProgressBarButton.TouchListener() {
        @Override
        public void onDown() {
            mProgressStart = mProgress;
        }

        @Override
        public void onUp() {
            if (mProgressStart - mProgress < mProgressStep) {
                int det = Math.min(mProgressStep, mProgressStep - (mProgressStart - mProgress));
                adjustVolume(-det);
            }
        }

        @Override
        public void onLongPress() {
            adjustVolume(-1);
        }
    };

    private ProgressBarButton.TouchListener mMaxButtonListener = new ProgressBarButton.TouchListener() {
        @Override
        public void onDown() {
            mProgressStart = mProgress;
        }

        @Override
        public void onUp() {
            if (mProgress - mProgressStart < mProgressStep) {
                int det = Math.min(mProgressStep, mProgressStep - (mProgress - mProgressStart));
                adjustVolume(det);
            }
        }

        @Override
        public void onLongPress() {
            adjustVolume(1);
        }
    };

    @Override
    protected void onVisibilityChanged (@NonNull View changedView, int visibility) {
        if (visibility == View.VISIBLE) {
            mMinButton.setTouchListener(mMinButtonListener);
            mMaxButton.setTouchListener(mMaxButtonListener);
        }
        else {
            mMinButton.removeTouchListener();
            mMaxButton.removeTouchListener();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        int radius = (getHeight()-mTouchPaddingTop-mTouchPaddingBottom)/2;
        mPaint.setColor(mBgColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2*radius);
        mPaint.setAntiAlias(true);
        // 灰色背景线
        canvas.drawLine(mTouchPaddingLeft+radius, mTouchPaddingTop+radius, getWidth()-mTouchPaddingRight-radius, mTouchPaddingTop+radius, mPaint);

        // 右面半弧（圆）
        canvas.drawCircle(getWidth()-radius-mTouchPaddingRight, mTouchPaddingTop+radius, radius, mPaint);

        mPaint.setColor(mValueColor);
        // 左面半弧（圆）
        canvas.drawCircle(mTouchPaddingLeft+radius, mTouchPaddingTop+radius, radius, mPaint);

        // 取值线
        canvas.drawLine(mTouchPaddingLeft+radius, mTouchPaddingTop+radius, mTouchPaddingLeft+radius+mProgress/100.0f*(getWidth()-mTouchPaddingLeft-mTouchPaddingRight-2*radius), mTouchPaddingTop+radius, mPaint);

        // 判断是否隐藏减号
        float thumbleft = mTouchPaddingLeft+mProgress/100.0f*(getWidth()-mTouchPaddingLeft-mTouchPaddingRight-2*radius);
        float buttonRight = mTouchPaddingLeft+radius;
        if (thumbleft < buttonRight) {
            mMinButton.setImageDrawable(null);
        }
        else {
            mMinButton.setImageDrawable(mMinButtonDrawable);
        }

        // 判断是否隐藏加号
        float thumbRight = mTouchPaddingLeft+2*radius+mProgress/100.0f*(getWidth()-mTouchPaddingLeft-mTouchPaddingRight-2*radius);
        float buttonLeft = getWidth()-mTouchPaddingRight-radius;

        if (thumbRight > buttonLeft) {
            mMaxButton.setImageDrawable(null);
        }
        else {
            mMaxButton.setImageDrawable(mMaxButtonDrawable);
        }

        // thumb背景
        canvas.drawCircle(mTouchPaddingLeft+radius+mProgress/100.0f*(getWidth()-mTouchPaddingLeft-mTouchPaddingRight-2*radius), mTouchPaddingTop+radius, radius, mPaint);

        // 设定thumb图片
        Drawable thumbBg;
        if (mProgress == 0) {
            thumbBg = mNoVolumeDrawable;
        }
        else {
            thumbBg = mVolumeDrawable;
        }
        if (thumbBg != null) {
            thumbBg.setBounds((int)(mTouchPaddingLeft+radius+mProgress/100.0f*(getWidth()-mTouchPaddingLeft-mTouchPaddingRight-2*radius)-mDrawableRadius),
                    mTouchPaddingTop+radius-mDrawableRadius,
                    (int)(mTouchPaddingLeft+radius+mProgress/100.0f*(getWidth()-mTouchPaddingLeft-mTouchPaddingRight-2*radius)+mDrawableRadius),
                    mTouchPaddingTop+radius+mDrawableRadius);
            thumbBg.draw(canvas);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int radius = (getHeight()-mTouchPaddingTop-mTouchPaddingBottom)/2;
        // 当点击区域为bar身时，直接移动bar的值
        if (ev.getX() >(1.5*radius+mTouchPaddingTop) && ev.getX()<getWidth()-mTouchPaddingBottom-1.5*radius) {
            mProgress = (int)((ev.getX()-radius-mTouchPaddingLeft)/(getWidth()-2*radius-mTouchPaddingLeft-mTouchPaddingRight)*100);
            if (mProgress < 0) {
                mProgress = 0;
            }
            else if (mProgress >100) {
                mProgress = 100;
            }
            invalidate();
            return false;
        }
        return super.onTouchEvent(ev);
    }

    private void adjustVolume(int det) {
        mProgress += det;
        if (mProgress > 100) {
            mProgress = 100;
        } else if (mProgress < 0) {
            mProgress = 0;
        }
        if (mListener != null) {
            mListener.onVolumeChanged(this, mProgress);
        }
        invalidate();
    }
}

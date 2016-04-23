package ticwear.design.drawable;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;

/**
 * A layer drawable has a circular progress contains (surrounding) another drawable.
 *
 * Created by tankery on 4/23/16.
 */
public class CircularProgressContainerDrawable extends LayerDrawable {

    int mStrokeSize;

    /**
     * Create a new layer drawable with the list of specified layers.
     *
     * @param progressDrawable The surrounding progress drawable.
     * @param content Content drawable inside.
     */
    // progressbar的厚度
    public CircularProgressContainerDrawable(@NonNull Drawable content, @NonNull CircularProgressDrawable progressDrawable, int strokeSize) {
        super(new Drawable[] {content, progressDrawable});
        mStrokeSize = strokeSize;
    }

    /**
     * 设置view中progressbar的进度，若view中无progressbar，则不做任何操作
     * @param percent 传入progress的百分比
     */
    public void setProgressPercent(float percent) {
        getProgressDrawable().setProgress(percent);
    }

    /**
     * 设置progressbar的模式，determintate/indeterminate
     * @param mode  传入的模式
     */
    public void setProgressMode(int mode) {
        getProgressDrawable().setProgressMode(mode);
    }

    /**
     * 设置progressbar的透明度
     * @param alpha progressBar的透明度
     */
    public void setProgressAlpha(int alpha) {
        getProgressDrawable().setAlpha(alpha);
    }

    /**
     * 设置是否有progressbar
     * @param hasProgress false为无progressbar
     */
    public void hasProgress(boolean hasProgress) {
        if (hasProgress) {
            getProgressDrawable().start();
        }
        else {
            getProgressDrawable().stop();
        }
    }

    /**
     * 获取progressbar
     * @return container中的progressbar
     */
    public CircularProgressDrawable getProgressDrawable() {
        if (getNumberOfLayers() >1) {
            return (CircularProgressDrawable) getDrawable(1);
        }
        return null;
    }

    /**
     * 获取content(rippleDrawable)
     * @return container中的content
     */
    public Drawable getContentDrawable() {
        return getDrawable(0);
    }

    /**
     * 重写onBoundsChange，对progressbar和content分别设置bounds
     * @param bounds ccontainer的bounds
     */
    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        // 重置content(rippleDrawable)的bounds
        Rect innerRect = new Rect(bounds);
        innerRect.left += mStrokeSize;
        innerRect.top += mStrokeSize;
        innerRect.right -= mStrokeSize;
        innerRect.bottom -= mStrokeSize;
        getContentDrawable().setBounds(innerRect);
    }
}

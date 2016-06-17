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

package ticwear.design.drawable;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * A layer drawable has a circular progress contains (surrounding) another drawable.
 *
 * Created by tankery on 4/23/16.
 */
public class CircularProgressContainerDrawable extends LayerDrawable {

    private int mStrokeSize;

    private boolean mBoundsChanged = true;

    /**
     * Create a drawable with the list of specified contents inside progress bar.
     *
     * @param progressDrawable The surrounding progress drawable.
     * @param contents Content drawable layers inside.
     */
    // progressbar的厚度
    public CircularProgressContainerDrawable(@NonNull Drawable[] contents,
                                             @NonNull CircularProgressDrawable progressDrawable) {
        super(buildLayers(contents, progressDrawable));
        mStrokeSize = progressDrawable.getStrokeSize();
    }

    private static Drawable[] buildLayers(@NonNull Drawable[] contents,
                                   @NonNull CircularProgressDrawable progressDrawable) {
        Drawable[] layers = Arrays.copyOf(contents, contents.length + 1);
        layers[contents.length] = progressDrawable;
        return layers;
    }

    public CircularProgressDrawable getProgressDrawable() {
        if (getNumberOfLayers() > 0 &&
                getDrawable(getNumberOfLayers() - 1) instanceof CircularProgressDrawable) {
            return (CircularProgressDrawable) getDrawable(getNumberOfLayers() - 1);
        }
        return null;
    }

    /**
     * Get specific content drawable.
     *
     * @param index the index of drawable, same order with the contents array passed in when create.
     * @return The drawable on index if exist, or null will be return.
     */
    public Drawable getContentDrawable(int index) {
        if (getNumberOfLayers() > index + 1) {
            return getDrawable(index);
        }
        return null;
    }

    /**
     * The reason of translate the canvas here instead of add the translate to bounds,
     * is that the offsets in bounds will cause a broken ripple shape.
     */
    @Override
    public void draw(Canvas canvas) {
        if (mBoundsChanged) {
            updateBounds(getBounds());
            mBoundsChanged = false;
        }
        canvas.save();
        canvas.translate(mStrokeSize, mStrokeSize);
        for (int i = 0; i < getNumberOfLayers() - 1; i++ ) {
            Drawable contentDrawable = getContentDrawable(i);
            contentDrawable.draw(canvas);
        }
        canvas.restore();
        getProgressDrawable().draw(canvas);
    }

    /**
     * Set bounds with a additional padding for contents. And original bounds for progress.
     */
    @Override
    protected void onBoundsChange(Rect bounds) {
        mBoundsChanged = true;
        super.onBoundsChange(bounds);
    }

    @Override
    protected boolean onStateChange(int[] state) {
        mBoundsChanged = true;
        return super.onStateChange(state);
    }

    @Override
    protected boolean onLevelChange(int level) {
        mBoundsChanged = true;
        return super.onLevelChange(level);
    }

    // 1. The reason we use compat library instead of using API 23's method is that our
    //    system build toolchain is based on API 22, we can't change the compileSdkVersion.
    // 2. The reason we use custom class instead of compat library, is that the low level compat
    //    library has bug that setLayoutDirection return void, and high level compat library
    //    using an internal jar that make system is hard to use.
    //
    // @Override
    @SuppressLint("Override")
    @Keep
    public boolean onLayoutDirectionChanged(int layoutDirection) {
        boolean changed = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (int i = 0; i < getNumberOfLayers() - 1; i++) {
                changed |= DrawableCompatJellybeanMr1.setLayoutDirection(getContentDrawable(i), layoutDirection);
            }
            changed |= DrawableCompatJellybeanMr1.setLayoutDirection(getProgressDrawable(), layoutDirection);
        }

        mBoundsChanged = true;

        return changed;
    }

    private void updateBounds(Rect bounds) {
        for (int i = 0; i < getNumberOfLayers() - 1; i++ ) {
            getContentDrawable(i).setBounds(
                    bounds.left, bounds.top,
                    bounds.right - mStrokeSize * 2, bounds.bottom - mStrokeSize * 2);
        }
        getProgressDrawable().setBounds(bounds);
    }

}

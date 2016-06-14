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

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

/**
 * An drawable to clip given drawable to a shape.
 * <p/>
 * Created by tankery on 6/15/16.
 */
public class ClipCircleDrawable extends Drawable {

    private final Paint mPathPaint = new Paint();
    private final Drawable mSource;
    private final Path mClipPath = new Path();

    public ClipCircleDrawable(int color) {
        this(new ColorDrawable(color));
    }

    public ClipCircleDrawable(@NonNull Drawable drawable) {
        super();
        mSource = drawable;
        mPathPaint.setAntiAlias(true);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mSource.setColorFilter(colorFilter);
        mPathPaint.setColorFilter(colorFilter);
    }

    @Override
    public void setTintList(ColorStateList tint) {
        mSource.setTintList(tint);
    }

    @Override
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        mSource.setTintMode(tintMode);
    }

    @Override
    public int getOpacity() {
        // We always have lots of transparent pixels.
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mSource.setBounds(left, top, right, bottom);

        mClipPath.reset();
        mClipPath.addOval(left, top, right, bottom, Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();

        if (mSource instanceof ColorDrawable) {
            int color = ((ColorDrawable) mSource).getColor();
            drawWithColor(canvas, color);
        } else {
            drawWithClip(canvas, mSource);
        }

        canvas.restore();
    }

    private void drawWithColor(Canvas canvas, @ColorInt int color) {

        final ColorFilter colorFilter = mPathPaint.getColorFilter();
        if ((color >>> 24) != 0 || colorFilter != null /*|| mTintFilter != null*/) {

            // do not using filter
//            if (colorFilter == null) {
//                mPathPaint.setColorFilter(mTintFilter);
//            }

            mPathPaint.setColor(color);
            canvas.drawPath(mClipPath, mPathPaint);

            // Restore original color filter.
//            mPathPaint.setColorFilter(colorFilter);
        }
    }

    private void drawWithClip(Canvas canvas, Drawable drawable) {
        canvas.clipRect(getBounds());
        canvas.clipPath(mClipPath);

        drawable.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        mSource.setAlpha(alpha);
    }

}

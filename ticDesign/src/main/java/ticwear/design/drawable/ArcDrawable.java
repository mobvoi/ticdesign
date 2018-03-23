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

import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.Gravity;

/**
 * An arc shape drawable that draw with a semi-circle clip.
 * <p/>
 * Created by tankery on 4/8/16.
 */
public class ArcDrawable extends ClipPathDrawable {

    private int mGravity = Gravity.BOTTOM;

    public ArcDrawable(int color) {
        super(color);
    }

    public ArcDrawable(@NonNull Drawable drawable) {
        super(drawable);
    }

    public void setGravity(int gravity) {
        mGravity = gravity;
        onResetPath(getClipPath(), getBounds());
    }

    @Override
    public int getOpacity() {
        // We always have lots of transparent pixels.
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    protected void onResetPath(Path path, Rect bounds) {
        int size = Math.max(bounds.width(), bounds.height());
        float radius = size * 0.5f;
        path.reset();

        switch (mGravity) {
            case Gravity.LEFT:
                path.addCircle(bounds.right - radius, bounds.top + radius, radius, Path.Direction.CW);
                break;
            case Gravity.RIGHT:
                path.addCircle(bounds.left + radius, bounds.top + radius, radius, Path.Direction.CW);
                break;
            case Gravity.BOTTOM:
                path.addCircle(bounds.left + radius, bounds.top + radius, radius, Path.Direction.CW);
                break;
            case Gravity.TOP:
                path.addCircle(bounds.right + radius, bounds.bottom - radius, radius, Path.Direction.CW);
                break;
            default:
                break;

        }
    }

}

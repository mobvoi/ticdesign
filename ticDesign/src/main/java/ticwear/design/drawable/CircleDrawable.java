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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * An drawable to clip given drawable to a shape.
 * <p/>
 * Created by tankery on 6/15/16.
 */
public class CircleDrawable extends ClipPathDrawable {

    public CircleDrawable(int color) {
        this(new ColorDrawable(color));
    }

    public CircleDrawable(@NonNull Drawable drawable) {
        super(drawable);
    }

    @Override
    public int getOpacity() {
        // We always have lots of transparent pixels.
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    protected void onResetPath(Path path, Rect bounds) {
        path.reset();
        path.addOval(bounds.left, bounds.top, bounds.right, bounds.bottom, Path.Direction.CW);
    }

}

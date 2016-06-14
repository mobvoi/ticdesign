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

package ticwear.design.utils;

import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowInsets;

import ticwear.design.drawable.ClipCircleDrawable;

/**
 * Utilities for window.
 *
 * Created by tankery on 6/14/16.
 */
public class WindowUtils {

    /**
     * Clip given window to round.
     *
     * This method should be called before window is show.
     *
     * @param window window needs to be clipped.
     * @return If clip succeed.
     */
    public static boolean clipToScreenShape(final Window window) {
        if (window == null || window.getDecorView() == null) {
            return false;
        }

        // Record original drawable & set window to transparent to avoid window has solid color.
        final Drawable original = window.getDecorView().getBackground();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        window.getDecorView().setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Delay setting window background when shape is defined.
                Drawable background = getBackgroundDrawable(original, insets.isRound());
                window.setBackgroundDrawable(background);
                if (insets.isRound()) {
                    clipToRound(v);
                }
                return insets;
            }
        });
        window.getDecorView().requestApplyInsets();

        return true;
    }

    /**
     * Clip a view to round shape.
     */
    public static void clipToRound(@NonNull View view) {
        view.setClipToOutline(true);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
    }

    @NonNull
    private static Drawable getBackgroundDrawable(Drawable original, boolean isRound) {
        if (original == null) {
            original = new ColorDrawable(Color.BLACK);
        }
        if (isRound) {
            return new ClipCircleDrawable(original);
        } else {
            return original;
        }
    }

}

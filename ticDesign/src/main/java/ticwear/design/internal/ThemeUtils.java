/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ticwear.design.internal;

import android.content.Context;
import android.content.res.TypedArray;

import ticwear.design.R;

public class ThemeUtils {

    private static final int[] DESIGN_CHECK_ATTRS = { R.attr.tic_windowIconStyle };

    public static void checkDesignTheme(Context context) {
        TypedArray a = context.obtainStyledAttributes(DESIGN_CHECK_ATTRS);
        final boolean failed = !a.hasValue(0);
        a.recycle();
        if (failed) {
            throw new IllegalArgumentException("You need to use a Theme.Ticwear theme "
                    + "(or descendant) with the design library.");
        }
    }
}

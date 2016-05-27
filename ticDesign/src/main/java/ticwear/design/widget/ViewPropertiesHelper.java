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

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by tankery on 2/18/16.
 *
 * Helper to get view properties.
 */
class ViewPropertiesHelper {

    static int getAdjustedHeight(View v) {
        if (clipToPadding(v)) {
            return v.getHeight() - v.getPaddingBottom() - v.getPaddingTop();
        } else {
            return v.getHeight();
        }
    }

    static int getCenterYPos(View v) {
        int padding  = clipToPadding(v) ? v.getPaddingTop() : 0;
        return v.getTop() + padding + getAdjustedHeight(v) / 2;
    }

    private static boolean clipToPadding(View v) {
        return !(v instanceof ViewGroup) || ((ViewGroup) v).getClipToPadding();
    }

    static int getTop(View v) {
        return clipToPadding(v)? v.getTop() + v.getPaddingTop() : v.getTop();
    }

    static int getBottom(View v) {
        return clipToPadding(v)? v.getBottom() - v.getPaddingBottom() : v.getBottom();
    }

}

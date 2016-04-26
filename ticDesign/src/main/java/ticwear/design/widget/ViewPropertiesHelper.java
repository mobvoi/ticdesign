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

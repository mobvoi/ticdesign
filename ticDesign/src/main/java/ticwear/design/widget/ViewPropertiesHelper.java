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
        boolean clipToPadding = !(v instanceof ViewGroup) || ((ViewGroup) v).getClipToPadding();
        if (clipToPadding) {
            return v.getHeight() - v.getPaddingBottom() - v.getPaddingTop();
        } else {
            return v.getHeight();
        }
    }

    static int getCenterYPos(View v) {
        boolean clipToPadding = !(v instanceof ViewGroup) || ((ViewGroup) v).getClipToPadding();
        int padding  = clipToPadding ? v.getPaddingTop() : 0;
        return v.getTop() + padding + getAdjustedHeight(v) / 2;
    }

}

package com.mobvoi.design.wearable.view;

import android.view.View;

/**
 * Created by tankery on 2/18/16.
 *
 * Helper to get view properties.
 */
class ViewPropertiesHelper {

    static int getAdjustedHeight(View v) {
        return v.getHeight() - v.getPaddingBottom() - v.getPaddingTop();
    }

    static int getCenterYPos(View v) {
        return v.getTop() + v.getPaddingTop() + getAdjustedHeight(v) / 2;
    }

}

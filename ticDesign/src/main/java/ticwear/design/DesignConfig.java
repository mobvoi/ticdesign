package ticwear.design;

import android.os.Build;

/**
 * Library configurations, delete when we integrated the gradle into make.
 *
 * Created by tankery on 4/15/16.
 */
public class DesignConfig {

    public static final boolean DEBUG = !Build.TYPE.equals("user");

    public static final boolean DEBUG_PICKERS = false;

}

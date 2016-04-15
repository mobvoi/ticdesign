package com.mobvoi.ticwear.view;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

/**
 * Implement this to handle the dispatch of side panel tickle events.
 *
 * The tickle event dispatch will only work with ViewGroups. Other class
 * implement this interface will have no effect.
 *
 * @see SidePanelEventTarget
 *
 * Created by tankery on 4/12/16.
 */
public interface SidePanelEventDispatcher {

    /**
     * Created by tankery on 4/15/16.
     */
    interface SuperCallback {
        boolean superDispatchTouchSidePanelEvent(MotionEvent event);
    }

    /**
     * Pass the side panel tickle event down to the target view, or this
     * view if it is the target.
     *
     * @param event The tickle event.
     * @param superCallback The callback can invoke the super view's dispatch method.
     * @return true if this view group handled the dispatch, false to
     *         allow pass it down.
     */
    boolean dispatchTouchSidePanelEvent(MotionEvent event, @NonNull SuperCallback superCallback);
}

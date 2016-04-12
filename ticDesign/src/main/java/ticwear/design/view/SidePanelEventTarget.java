package ticwear.design.view;

import android.view.MotionEvent;

/**
 * Implement this interface to receive side panel tickle events.
 *
 * The tickle event will only work with Views & Activity. Other class
 * implement this interface will have no effect.
 *
 * The tickle event is similar to touch event. Normally, the x value will
 * not change and equals the screen width (or 0 when on right hand mode).
 * The y value will match the screen position and relative to the view
 * coordinator (like touch event).
 *
 * Created by tankery on 4/12/16.
 */
public interface SidePanelEventTarget {

    /**
     * Pass the side panel tickle event down to the target view, or this
     * view if it is the target.
     *
     * @param event The tickle event.
     * @return true if this view is the target, false to allow pass it down.
     *
     * @see #onTouchSidePanel(MotionEvent)
     * @see android.view.View#dispatchTouchEvent(MotionEvent)
     */
    boolean dispatchTouchSidePanelEvent(MotionEvent event);

    /**
     * Implement this method to handle side panel tickle events.
     *
     * @param event The tickle event.
     * @return True if the event was handled, false otherwise.
     *
     * @see #dispatchTouchSidePanelEvent(MotionEvent)
     * @see android.view.View#onTouchEvent(MotionEvent)
     */
    boolean onTouchSidePanel(MotionEvent event);

}

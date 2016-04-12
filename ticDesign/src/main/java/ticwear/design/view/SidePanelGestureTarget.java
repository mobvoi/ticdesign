package ticwear.design.view;

import android.view.MotionEvent;

/**
 * Implement this interface to receive side panel gesture.
 *
 * The tickle event will only work with Views & Activity. Other class
 * implement this interface will have no effect.
 *
 * @see SidePanelEventTarget
 *
 * Created by tankery on 4/12/16.
 */
public interface SidePanelGestureTarget {

    /**
     * Long press the side panel.
     * @param e undefined now, ignore it.
     * @return True if the event was handled, false otherwise.
     */
    boolean onLongPressSidePanel(MotionEvent e);

    /**
     * Scroll on side panel
     *
     * @param e1 undefined now, ignore it.
     * @param e2 undefined now, ignore it.
     * @param distanceX undefined now, ignore it.
     * @param distanceY move distance on y, positive/negative
     *                  indicates the direction.
     * @return True if the event was handled, false otherwise.
     */
    boolean onScrollSidePanel(MotionEvent e1, MotionEvent e2,
                              float distanceX, float distanceY);

    /**
     * Fling on side panel.
     *
     * @param e1 undefined now, ignore it.
     * @param e2 undefined now, ignore it.
     * @param velocityX undefined now, ignore it.
     * @param velocityY velocity on y, positive/negative
     *                  indicates the direction.
     * @return True if the event was handled, false otherwise.
     */
    boolean onFlingSidePanel(MotionEvent e1, MotionEvent e2,
                             float velocityX, float velocityY);

    /**
     * Double tap on side panel.
     *
     * @param e undefined now, ignore it.
     * @return True if the event was handled, false otherwise.
     */
    boolean onDoubleTapSidePanel(MotionEvent e);

    /**
     * Single tap on side panel.
     *
     * @param e undefined now, ignore it.
     * @return True if the event was handled, false otherwise.
     */
    boolean onSingleTapSidePanel(MotionEvent e);

}

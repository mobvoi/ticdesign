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

package com.mobvoi.ticwear.view;

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
 * @see SidePanelEventDispatcher
 *
 * Created by tankery on 4/12/16.
 */
public interface SidePanelEventTarget {

    /**
     * Implement this method to handle side panel tickle events.
     *
     * @param event The tickle event.
     * @return True if the event was handled, false otherwise.
     *
     * @see SidePanelEventDispatcher#dispatchTouchSidePanelEvent(MotionEvent, SidePanelEventDispatcher.SuperCallback)
     * @see android.view.View#onTouchEvent(MotionEvent)
     */
    boolean onTouchSidePanel(MotionEvent event);

}

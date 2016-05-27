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

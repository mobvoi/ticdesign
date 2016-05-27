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

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.MotionEvent;

/**
 * An interface for {@link LayoutManager} that can be ticklable.
 *
 * Created by tankery on 4/25/16.
 */
public interface TicklableLayoutManager {

    void setTicklableRecyclerView(TicklableRecyclerView ticklableRecyclerView);

    /**
     * Check if the adapter is valid for this layout manager.
     */
    boolean validAdapter(Adapter adapter);

    /**
     * Intercept pre scroll to quick show the AppBar.
     */
    boolean interceptPreScroll();

    /**
     * Should use scroll as offset of view.
     */
    boolean useScrollAsOffset();
    int getScrollOffset();
    int updateScrollOffset(int scrollOffset);

    /**
     * Pass touch event to LayoutManager
     */
    boolean dispatchTouchEvent(MotionEvent e);

    /**
     * Pass side panel event to LayoutManager
     */
    boolean dispatchTouchSidePanelEvent(MotionEvent ev);
}

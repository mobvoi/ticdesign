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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.mobvoi.ticwear.view.SidePanelEventDispatcher;

/**
 * A ScrollView that can be tickled.
 *
 * Created by tankery on 6/21/16.
 */
public class TicklableScrollView extends ScrollView implements SidePanelEventDispatcher {

    private SidePanelEventDispatcher mSidePanelEventDispatcher;

    public TicklableScrollView(Context context) {
        this(context, null);
    }

    public TicklableScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.scrollViewStyle);
    }

    public TicklableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TicklableScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setSidePanelEventDispatcher(new SidePanelEventDispatcher() {
            @Override
            public boolean dispatchTouchSidePanelEvent(MotionEvent event, @NonNull SuperCallback superCallback) {
                return dispatchTouchEvent(event) || superCallback.superDispatchTouchSidePanelEvent(event);
            }
        });
    }

    public void setSidePanelEventDispatcher(SidePanelEventDispatcher dispatcher) {
        this.mSidePanelEventDispatcher = dispatcher;
    }

    @Override
    public boolean dispatchTouchSidePanelEvent(MotionEvent event, @NonNull SuperCallback superCallback) {
        return mSidePanelEventDispatcher != null &&
                mSidePanelEventDispatcher.dispatchTouchSidePanelEvent(event, superCallback);
    }

}

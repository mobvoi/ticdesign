package ticwear.design.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.mobvoi.ticwear.view.SidePanelEventDispatcher;

/**
 * A FrameLayout that can be tickled.
 *
 * Created by tankery on 4/14/16.
 */
public class TicklableFrameLayout extends FrameLayout implements SidePanelEventDispatcher {

    private SidePanelEventDispatcher mSidePanelEventDispatcher;

    public TicklableFrameLayout(Context context) {
        super(context);
    }

    public TicklableFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TicklableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TicklableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

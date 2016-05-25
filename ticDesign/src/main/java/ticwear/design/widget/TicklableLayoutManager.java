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

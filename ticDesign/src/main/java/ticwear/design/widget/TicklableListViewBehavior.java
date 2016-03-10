package ticwear.design.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import hugo.weaving.DebugLog;

/**
 * Behavior which should be used by {@link TicklableListViewBehavior} which can scroll and support
 * nested scrolling to automatically scroll any {@link AppBarLayout} siblings.
 */
public class TicklableListViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    private boolean scrolling = false;
    private View hostView;

    public TicklableListViewBehavior() {
        super();
    }

    public TicklableListViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        hostView = child;
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    @DebugLog
    public boolean setTopAndBottomOffset(int offset) {
        // Avoid re-entry the scrolling path.
        if (scrolling || hostView == null) {
            return false;
        }
        scrolling = true;
        boolean done = false;
        if (hostView instanceof TicklableListView) {
            TicklableListView listView = (TicklableListView) hostView;
            if (listView.isInFocusState()) {
                done = setOffsetForFocusListView(listView, offset);
            }
        }

        if (!done) {
            done = setRawTopAndBottomOffset(offset);
        }
        scrolling = false;

        return done;
    }

    @Override
    public int getTopAndBottomOffset() {
        if (hostView instanceof TicklableListView) {
            TicklableListView listView = (TicklableListView) hostView;
            return getRawTopAndBottomOffset() + listView.getScrollOffset();
        }
        return super.getTopAndBottomOffset();
    }

    @DebugLog
    public boolean setRawTopAndBottomOffset(int offset) {
        return super.setTopAndBottomOffset(offset);
    }

    public int getRawTopAndBottomOffset() {
        return super.getTopAndBottomOffset();
    }

    /**
     * Set offset for focus list view.
     *
     * Focus list view has a little complicated offset setting path.
     *
     * When stretching, we should first try scroll to mock offset, then do real offset,
     * When releasing, first try to reduce the real offset we setting on stretch, then reduce the
     * scroll mocked offset.
     * So we first check if there is space to reduce the offset (in releasing).
     *
     * @return If we have successfully set the offset.
     */
    @DebugLog
    private boolean setOffsetForFocusListView(TicklableListView listView, int offset) {
        // If we have offset, first try to remove it
        offset = reduceRemovableOffset(offset);
        // Then, we try to do the scroll to mock a offset for focus state ticklable list-view
        int unconsumed = listView.updateScrollOffset(offset);
        // If scroll can't consume all the offset request, offset the rest part.
        offset = getRawTopAndBottomOffset() - unconsumed;

        setRawTopAndBottomOffset(offset);

        return true;
    }

    /**
     * Reduce removable raw offset of this view.
     *
     * @param newOffset new offset request
     * @return the offset can't consume (should be apply to scroll offset).
     */
    @DebugLog
    private int reduceRemovableOffset(int newOffset) {
        int currentRawOffset = getRawTopAndBottomOffset();
        // Current offset already been zero, all offset request can not consume.
        if (currentRawOffset == 0) {
            return newOffset;
        }

        int currentAllOffset = getTopAndBottomOffset();

        Log.v("Ticklable", "reduce offset, raw " + currentRawOffset + ", all " + currentAllOffset + ", new " + newOffset);

        checkOffsetValidation(currentAllOffset, currentRawOffset);

        boolean sameSign = sameSign(newOffset, currentAllOffset);
        int reduce = Math.abs(currentAllOffset) - Math.abs(newOffset);

        // offsets on same side, and new offset is little,
        // offset to the new offset until 0, return that not consumed.
        if (sameSign && reduce >= 0) {
            // the new raw offset, should been reduced until 0.
            int offset = Math.max(0, currentRawOffset - reduce);
            setRawTopAndBottomOffset(offset);
            return newOffset - offset;
        }
        // offsets on diff side, offset this view to 0. All offset request can not be consumed.
        if (!sameSign) {
            setRawTopAndBottomOffset(0);
            return newOffset;
        }

        // offsets on same side, and new offset is larger, return that not consumed.
        return newOffset - currentRawOffset;
    }

    private void checkOffsetValidation(int allOffset, int rawOffset) {
        if (Math.abs(allOffset) < Math.abs(rawOffset)) {
            throw new RuntimeException("total offset should not smaller than raw offset");
        }
        if (!sameSign(allOffset, rawOffset)) {
            throw new RuntimeException("total offset has different sign than raw offset");
        }
    }

    boolean sameSign(int x, int y)
    {
        return (x >= 0) ^ (y < 0);
    }

}
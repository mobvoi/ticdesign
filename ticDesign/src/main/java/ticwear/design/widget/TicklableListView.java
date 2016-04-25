package ticwear.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mobvoi.ticwear.view.SidePanelEventDispatcher;

import ticwear.design.R;

@TargetApi(20)
@CoordinatorLayout.DefaultBehavior(TicklableListViewBehavior.class)
public class TicklableListView extends RecyclerView implements SidePanelEventDispatcher {

    static final String TAG = "TicklableLV";

    /**
     * {@link LayoutManager} for focus state.
     */
    private final FocusableLinearLayoutManager mFocusableLayoutManager;


    private boolean mSkipNestedScroll;

    public TicklableListView(Context context) {
        this(context, null);
    }

    public TicklableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TicklableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TicklableListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        setHasFixedSize(true);
        setOverScrollMode(OVER_SCROLL_NEVER);

        mFocusableLayoutManager = new FocusableLinearLayoutManager(this);
        super.setLayoutManager(mFocusableLayoutManager);

        mSkipNestedScroll = false;

        if (!isInEditMode() && getItemAnimator() != null) {
            long defaultAnimDuration = context.getResources()
                    .getInteger(R.integer.design_anim_list_item_state_change);
            long itemAnimDuration = defaultAnimDuration / 4;
            getItemAnimator().setMoveDuration(itemAnimDuration);
        }
    }

    @Override
    public LinearLayoutManager getLayoutManager() {
        return (LinearLayoutManager) super.getLayoutManager();
    }

    /**
     * Set a new adapter to provide child views on demand.
     *
     * @param adapter new adapter that should be instance of {@link TicklableListView.Adapter}
     */
    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(this, adapter.getItemViewType(0));
            if (!(viewHolder instanceof FocusableLinearLayoutManager.ViewHolder) && !isInEditMode()) {
                throw new IllegalArgumentException("adapter's ViewHolder should be instance of FocusableLinearLayoutManager.ViewHolder");
            }
        }
        super.setAdapter(adapter);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        throw new IllegalStateException("Can't customized the layout manager for TicklableListView.");
    }

    public boolean isInFocusState() {
        return mFocusableLayoutManager.isInFocusState();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        return mFocusableLayoutManager.dispatchTouchEvent(e) ||
                super.dispatchTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchSidePanelEvent(MotionEvent ev, @NonNull SuperCallback superCallback) {
        return mFocusableLayoutManager.dispatchTouchSidePanelEvent(ev) ||
                superCallback.superDispatchTouchSidePanelEvent(ev);
    }

    @Override
    public FocusableLinearLayoutManager.ViewHolder getChildViewHolder(View child) {
        return (FocusableLinearLayoutManager.ViewHolder) super.getChildViewHolder(child);
    }

    public int getScrollOffset() {
        return mFocusableLayoutManager.getScrollOffset();
    }

    /**
     * Update offset to scroll.
     *
     * This will calculate the delta of previous offset and new offset, then apply it to scroll.
     *
     * @param scrollOffset new offset to scroll.
     *
     * @return the unconsumed offset.
     */
    public int updateScrollOffset(int scrollOffset) {
        return mFocusableLayoutManager.updateScrollOffset(scrollOffset);
    }

    public void scrollBySkipNestedScroll(int x, int y) {
        mSkipNestedScroll = true;
        scrollBy(x, y);
        mSkipNestedScroll = false;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return !mSkipNestedScroll && super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

}

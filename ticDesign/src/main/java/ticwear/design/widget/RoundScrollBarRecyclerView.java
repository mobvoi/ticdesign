package ticwear.design.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.WindowInsets;

/**
 * A recycler view display round scrollbar.
 *
 * Created by goodev on 2016/4/13.
 */
public class RoundScrollBarRecyclerView extends RecyclerView {

    private ScrollBarHelper mScrollBarHelper;
    private final int[] mTempLocation = new int[2];

    public RoundScrollBarRecyclerView(Context context) {
        this(context, null);
    }

    public RoundScrollBarRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundScrollBarRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScrollBarHelper = new ScrollBarHelper(context, attrs, defStyle);
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        super.offsetTopAndBottom(offset);
        invalidate();
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mScrollBarHelper.setIsRound(insets.isRound());
        return super.onApplyWindowInsets(insets);
    }

    @Override
    public void draw(Canvas c) {
        getLocationInWindow(mTempLocation);
        mScrollBarHelper.setViewOffset(mTempLocation[0], mTempLocation[1]);

        super.draw(c);
    }

    //@hide api @Override
    @SuppressWarnings("unused")
    protected void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        int range = computeVerticalScrollRange();
        int offset = computeVerticalScrollOffset();
        int extent = computeVerticalScrollExtent();
        if (range > extent) {
            mScrollBarHelper.onDrawScrollBar(canvas, range, offset, extent, scrollBar.getAlpha());
        }
    }

}

package com.mobvoi.design.wearable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by tankery on 1/11/16.
 *
 * ListView matches Ticwear design and support tickle interaction.
 */
public class ListView extends android.widget.ListView {

    public ListView(Context context) {
        super(context);
        init();
    }

    public ListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public ListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setNestedScrollingEnabled(true);
    }

}

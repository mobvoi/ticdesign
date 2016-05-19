package ticwear.design.widget;

import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import ticwear.design.internal.view.menu.ContextMenuBuilder;
import ticwear.design.internal.view.menu.MenuBuilder;
import ticwear.design.internal.view.menu.MenuBuilder.Callback;

/**
 *
 *
 * Created by tankery on 5/16/16.
 */
public class FloatingContextMenu implements Callback {

    private final Context mContext;

    private ContextMenuBuilder mMenuBuilder;
    private ContextMenuCreator mContextMenuCreator;
    private OnMenuSelectedListener mOnMenuSelectedListener;

    public FloatingContextMenu(Context context) {
        mContext = context;
    }

    public FloatingContextMenu setContextMenuCreator(ContextMenuCreator creator) {
        this.mContextMenuCreator = creator;
        return this;
    }

    public FloatingContextMenu setOnMenuSelectedListener(OnMenuSelectedListener listener) {
        this.mOnMenuSelectedListener = listener;
        return this;
    }

    public boolean show(View view) {
        if (mContextMenuCreator == null) {
            return false;
        }

        if (mMenuBuilder == null) {
            mMenuBuilder = new ContextMenuBuilder(mContext);
            mMenuBuilder.setCallback(this);
        } else {
            mMenuBuilder.clear();
        }

        mContextMenuCreator.onCreateContextMenu(mMenuBuilder, view);
        if (mMenuBuilder.size() > 0) {
            mMenuBuilder.show();
            return true;
        }

        return false;
    }

    public void dismiss() {
        if (mMenuBuilder != null) {
            mMenuBuilder.dismiss();
        }
    }

    @Override
    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
        return mOnMenuSelectedListener != null &&
                mOnMenuSelectedListener.onContextItemSelected(item);
    }

    public interface ContextMenuCreator {
        void onCreateContextMenu(ContextMenu menu, View v);
    }

    public interface OnMenuSelectedListener {
        boolean onContextItemSelected(MenuItem item);
    }

}

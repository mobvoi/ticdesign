package ticwear.design.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;

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

    private View mBindingView;

    private OnAttachStateChangeListener mOnAttachStateChangeListener = new OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(View v) {
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            close();
        }
    };

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

    /**
     * Bind floating context menu to view and show.
     *
     * When the view detached from window, the context menu will be dismissed.
     *
     * @param view the view to bind menu with.
     * @return if the menu success to show
     */
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
            bindView(view);
            if (mMenuBuilder.isOpen()) {
                mMenuBuilder.onItemsChanged(true);
            } else {
                mMenuBuilder.open();
            }
        } else {
            mMenuBuilder.close();
        }

        return mMenuBuilder.size() > 0;
    }

    public void close() {
        if (mMenuBuilder != null) {
            mMenuBuilder.close();
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuBuilder menu, MenuItem item) {
        return mOnMenuSelectedListener != null &&
                mOnMenuSelectedListener.onContextItemSelected(item);
    }

    @Override
    public void onMenuClosed(MenuBuilder menu) {
        unbindView();
    }

    private void bindView(View view) {
        if (mBindingView == view) {
            return;
        }

        unbindView();
        mBindingView = view;
        if (mBindingView != null) {
            mBindingView.addOnAttachStateChangeListener(mOnAttachStateChangeListener);
        }
    }

    private void unbindView() {
        if (mBindingView != null) {
            mBindingView.removeOnAttachStateChangeListener(mOnAttachStateChangeListener);
            mBindingView = null;
        }
    }

    public interface ContextMenuCreator {
        void onCreateContextMenu(ContextMenu menu, View v);
    }

    public interface OnMenuSelectedListener {
        boolean onContextItemSelected(@NonNull MenuItem item);
    }

}

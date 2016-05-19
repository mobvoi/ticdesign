package ticwear.design.internal.view.menu;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import hugo.weaving.DebugLog;
import ticwear.design.R;
import ticwear.design.app.AlertDialog;
import ticwear.design.app.AlertDialog.Builder;
import ticwear.design.internal.view.menu.MenuFloatingLayout.OnItemSelectedListener;
import ticwear.design.utils.blur.BlurBehind;

/**
 * Implement of {@link ContextMenu}
 *
 * Created by tankery on 5/13/16.
 */
public class ContextMenuBuilder extends MenuBuilder implements ContextMenu {

    private CharSequence mTitle;
    private Drawable mIcon;
    private View mHeaderView;

    private MenuFloatingLayout mMenuLayout;
    private AlertDialog mMenuDialog;

    public ContextMenuBuilder(Context context) {
        super(context);
    }

    @Override
    public ContextMenu setHeaderTitle(int titleRes) {
        mTitle = getResources().getString(titleRes);
        return this;
    }

    @Override
    public ContextMenu setHeaderTitle(CharSequence title) {
        mTitle = title;
        return this;
    }

    @Override
    public ContextMenu setHeaderIcon(int iconRes) {
        mIcon = getResources().getDrawable(iconRes, getContext().getTheme());
        return this;
    }

    @Override
    public ContextMenu setHeaderIcon(Drawable icon) {
        mIcon = icon;
        return this;
    }

    @Override
    public ContextMenu setHeaderView(View view) {
        mHeaderView = view;
        return this;
    }

    @Override
    public void clearHeader() {
        mHeaderView = null;
        mTitle = null;
        mIcon = null;
    }

    @DebugLog
    public void open() {

        mMenuDialog = new Builder(getContext(), R.style.Theme_Ticwear_Dialog_Alert_ContextMenu)
                .setCustomTitle(mHeaderView)
                .setTitle(mTitle)
                .setIcon(mIcon)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        close();
                    }
                })
                .create();

        // Use dialog context to match Ticwear theme.
        ViewGroup layout = createDialogContent(mMenuDialog.getContext());
        mMenuDialog.setView(layout);

        mMenuLayout.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(MenuItem item) {
                if (item != null) {
                    performItemAction(item);
                }
                close();
            }
        });

        onItemsChanged(true);

        final int maskColor = getResources().getColor(R.color.tic_background_mask_dark);
        final int animDurationLong = getResources().getInteger(android.R.integer.config_longAnimTime);

        mMenuDialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface di) {
                mMenuLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mMenuDialog == null) {
                            return;
                        }
                        BlurBehind.from(getContext())
                                .animate(animDurationLong)
                                .color(maskColor)
                                .sampling(2)
                                .capture(getBackgroundWindow())
                                .into(mMenuDialog.getWindow());
                    }
                }, 500);
            }
        });

        // Show dialog after blur.
        BlurBehind.from(getContext())
                .color(maskColor)
                .sampling(2)
                .capture(getBackgroundWindow())
                .preSetBackground(new Runnable() {
                    @Override
                    public void run() {
                        if (mMenuDialog != null) {
                            mMenuDialog.show();
                        }
                    }
                })
                .into(mMenuDialog.getWindow());
    }

    @NonNull
    private RelativeLayout createDialogContent(Context dialogContext) {
        // Use a RelativeLayout to wrap the menu, so we can set the height to match_parent.
        RelativeLayout layout = new RelativeLayout(dialogContext);
        layout.setGravity(Gravity.CENTER);

        mMenuLayout = (MenuFloatingLayout) LayoutInflater.from(dialogContext)
                .inflate(R.layout.menu_floating_layout_ticwear, layout, false);

        mMenuLayout.clear();

        layout.addView(mMenuLayout);
        return layout;
    }

    @Nullable
    private Window getBackgroundWindow() {
        Window window = null;
        if (getContext() instanceof Activity) {
            window = ((Activity) getContext()).getWindow();
        }
        return window;
    }

    public boolean isOpen() {
        return mMenuDialog != null;
    }

    @Override
    @DebugLog
    public void close() {
        if (!isOpen()) {
            return;
        }

        super.close();
        mMenuLayout = null;

        mMenuDialog.dismiss();
        mMenuDialog = null;
    }

    @Override
    public void onItemsChanged(boolean structureChanged) {
        super.onItemsChanged(structureChanged);
        if (mMenuLayout != null) {
            if (structureChanged) {
                mMenuLayout.clear();
                for (int i = 0; i < size(); i++) {
                    mMenuLayout.addMenuItem(getItem(i));
                }
            }
            mMenuLayout.notifyItemsChanged();
        }
    }
}

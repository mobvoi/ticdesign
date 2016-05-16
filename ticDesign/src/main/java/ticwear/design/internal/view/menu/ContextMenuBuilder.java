package ticwear.design.internal.view.menu;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import ticwear.design.R;
import ticwear.design.app.AlertDialog;
import ticwear.design.app.AlertDialog.Builder;
import ticwear.design.internal.view.menu.MenuFloatingLayout.OnItemSelectedListener;
import ticwear.design.utils.BlurBehind;
import ticwear.design.utils.BlurBehind.OnBlurFinishedCallback;

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
    private BlurBehind mBlurBehind;

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

    public void show() {

        final AlertDialog dialog = new Builder(getContext(), R.style.Theme_Ticwear_Dialog_Alert_ContextMenu)
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
        ViewGroup layout = createDialogContent(dialog.getContext());
        dialog.setView(layout);

        mMenuLayout.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(MenuItem item) {
                if (item != null) {
                    performItemAction(item);
                }
                dialog.dismiss();
            }
        });

        onItemsChanged(true);

        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface di) {
                mMenuLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateBlur(new OnBlurFinishedCallback() {
                            @Override
                            public void onBlurFinished(Bitmap blurredBitmap) {
                                changeBackground(dialog.getWindow().getDecorView(),
                                        new BitmapDrawable(getResources(), blurredBitmap));
                            }
                        });
                    }
                }, 500);
            }
        });
        // Show dialog after below.
        updateBlur(new OnBlurFinishedCallback() {
            @Override
            public void onBlurFinished(Bitmap blurredBitmap) {
                dialog.show();
                dialog.getWindow().getDecorView()
                        .setBackground(new BitmapDrawable(getResources(), blurredBitmap));
            }
        });
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

    private void updateBlur(OnBlurFinishedCallback callback) {
        if (mBlurBehind == null) {
            mBlurBehind = new BlurBehind();
        }
        Window window = null;
        if (getContext() instanceof Activity) {
            window = ((Activity) getContext()).getWindow();
        }
        mBlurBehind.prepare(window, callback);
    }

    private void changeBackground(View decorView, Drawable drawable) {
        Drawable oldDrawable = decorView.getBackground();
        if (oldDrawable instanceof  TransitionDrawable) {
            oldDrawable = ((TransitionDrawable) oldDrawable).getDrawable(1);
        }
        TransitionDrawable transitionDrawable = new TransitionDrawable(
                new Drawable[]{oldDrawable, drawable});
        decorView.setBackground(transitionDrawable);
        transitionDrawable.startTransition(500);
    }

    @Override
    public void close() {
        super.close();
        mMenuLayout = null;
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

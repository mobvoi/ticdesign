/*
 * Copyright (c) 2016 Mobvoi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ticwear.design.internal.view.menu;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

/**
 * A simple menu item implementation that only work for flat (no sub menu) and base menu.
 *
 * Created by tankery on 5/13/16.
 */
public class MenuItemImpl implements MenuItem {

    private static final String TAG = "TicMenuItemImpl";

    /** Used for the icon resource ID if this item does not have an icon */
    static final int NO_ICON = 0;

    /** The menu to which this item belongs */
    private MenuBuilder mMenu;

    private int mId;
    private int mGroupId;
    private int mOrder;
    private CharSequence mTitle;

    /** The icon's drawable which is only created as needed */
    private Drawable mIconDrawable;
    /**
     * The icon's resource ID which is used to get the Drawable when it is
     * needed (if the Drawable isn't already obtained--only one of the two is
     * needed).
     */
    private int mIconResId = NO_ICON;

    private Intent mIntent;

    private OnMenuItemClickListener mClickListener;

    MenuItemImpl(MenuBuilder menu, int group, int id, int order, CharSequence title) {
        this.mMenu = menu;
        this.mGroupId = group;
        this.mId = id;
        this.mOrder = order;
        this.mTitle = title;
    }

    @Override
    public int getItemId() {
        return mId;
    }

    @Override
    public int getGroupId() {
        return mGroupId;
    }

    @Override
    public int getOrder() {
        return mOrder;
    }

    @Override
    public MenuItem setTitle(CharSequence title) {
        mTitle = title;
        mMenu.onItemsChanged(false);
        return this;
    }

    @Override
    public MenuItem setTitle(int title) {
        mTitle = mMenu.getContext().getString(title);
        mMenu.onItemsChanged(false);
        return this;
    }

    @Override
    public CharSequence getTitle() {
        return mTitle;
    }

    @Override
    public MenuItem setTitleCondensed(CharSequence title) {
        return this;
    }

    @Override
    public CharSequence getTitleCondensed() {
        return mTitle;
    }

    @Override
    public MenuItem setIcon(Drawable icon) {
        mIconDrawable = icon;
        mIconResId = NO_ICON;
        return this;
    }

    @Override
    public MenuItem setIcon(int iconRes) {
        mIconDrawable = null;
        mIconResId = iconRes;
        mMenu.onItemsChanged(false);
        return this;
    }

    @Override
    public Drawable getIcon() {
        if (mIconDrawable != null) {
            return mIconDrawable;
        }

        if (mIconResId != NO_ICON) {
            Drawable icon =  mMenu.getContext().getDrawable(mIconResId);
            mIconResId = NO_ICON;
            mIconDrawable = icon;
            return icon;
        }

        return null;
    }

    @Override
    public MenuItem setIntent(Intent intent) {
        mIntent = intent;
        return this;
    }

    @Override
    public Intent getIntent() {
        return mIntent;
    }

    @Override
    public MenuItem setShortcut(char numericChar, char alphaChar) {
        return this;
    }

    @Override
    public MenuItem setNumericShortcut(char numericChar) {
        return this;
    }

    @Override
    public char getNumericShortcut() {
        return 0;
    }

    @Override
    public MenuItem setAlphabeticShortcut(char alphaChar) {
        return this;
    }

    @Override
    public char getAlphabeticShortcut() {
        return 0;
    }

    @Override
    public MenuItem setCheckable(boolean checkable) {
        return this;
    }

    @Override
    public boolean isCheckable() {
        return false;
    }

    @Override
    public MenuItem setChecked(boolean checked) {
        return this;
    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public MenuItem setVisible(boolean visible) {
        return this;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public MenuItem setEnabled(boolean enabled) {
        return this;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean hasSubMenu() {
        return false;
    }

    @Override
    public SubMenu getSubMenu() {
        return null;
    }

    @Override
    public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        mClickListener = menuItemClickListener;
        return this;
    }

    @Override
    public ContextMenuInfo getMenuInfo() {
        return null;
    }

    @Override
    public void setShowAsAction(int actionEnum) {

    }

    @Override
    public MenuItem setShowAsActionFlags(int actionEnum) {
        return this;
    }

    @Override
    public MenuItem setActionView(View view) {
        return this;
    }

    @Override
    public MenuItem setActionView(int resId) {
        return this;
    }

    @Override
    public View getActionView() {
        return null;
    }

    @Override
    public MenuItem setActionProvider(ActionProvider actionProvider) {
        return this;
    }

    @Override
    public ActionProvider getActionProvider() {
        return null;
    }

    @Override
    public boolean expandActionView() {
        return false;
    }

    @Override
    public boolean collapseActionView() {
        return false;
    }

    @Override
    public boolean isActionViewExpanded() {
        return false;
    }

    @Override
    public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
        return this;
    }


    /**
     * Invokes the item by calling various listeners or callbacks.
     *
     * @return true if the invocation was handled, false otherwise
     */
    public boolean invoke() {
        if (mClickListener != null &&
                mClickListener.onMenuItemClick(this)) {
            return true;
        }

        if (mMenu.dispatchMenuItemSelected(mMenu, this)) {
            return true;
        }

//        if (mItemCallback != null) {
//            mItemCallback.run();
//            return true;
//        }

        if (mIntent != null) {
            try {
                mMenu.getContext().startActivity(mIntent);
                return true;
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Can't find activity to handle intent; ignoring", e);
            }
        }

        return false;
    }
}

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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ticwear.design.R;

/**
 * A view contains a menu icon on top and title on bottom.
 *
 * Created by tankery on 5/16/16.
 */
public class MenuItemView extends LinearLayout {

    public static final int MENU_ITEM_TYPE_DEFAULT = 0;
    public static final int MENU_ITEM_TYPE_LARGE = 1;
    public static final int MENU_ITEM_TYPE_MIDDLE = 2;
    public static final int MENU_ITEM_TYPE_SMALL = 3;

    @IntDef(value = {
            MENU_ITEM_TYPE_DEFAULT,
            MENU_ITEM_TYPE_LARGE, MENU_ITEM_TYPE_MIDDLE, MENU_ITEM_TYPE_SMALL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface MenuItemType {}

    private @MenuItemType int mItemType = MENU_ITEM_TYPE_DEFAULT;

    ImageButton mImageIcon;
    TextView mTextTitle;

    public MenuItemView(Context context) {
        this(context, null);
    }

    public MenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MenuItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mImageIcon = (ImageButton) findViewById(android.R.id.icon);
        mTextTitle = (TextView) findViewById(android.R.id.title);

        mImageIcon.setScaleType(ScaleType.FIT_CENTER);
    }

    public void setIcon(Drawable icon) {
        if (mImageIcon != null) {
            mImageIcon.setImageDrawable(icon);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if (mImageIcon != null && mItemType != MENU_ITEM_TYPE_DEFAULT) {
            mImageIcon.setOnClickListener(l);
        } else {
            if (mImageIcon != null) {
                mImageIcon.setClickable(false);
            }
            super.setOnClickListener(l);
        }
    }

    public void setTitle(CharSequence title) {
        if (mTextTitle != null) {
            mTextTitle.setText(title);
        }
    }

    public void setMenuItemType(@MenuItemType int type) {
        mItemType = type;
        if (mImageIcon == null || mTextTitle == null) {
            return;
        }
        int margin;
        int textSize;
        int iconSize;
        int iconPadding;

        switch (type) {
            case MENU_ITEM_TYPE_DEFAULT:
                return;
            case MENU_ITEM_TYPE_LARGE:
                margin = getResources().getDimensionPixelOffset(R.dimen.tic_menu_item_content_margin_large);
                iconPadding = getResources().getDimensionPixelOffset(R.dimen.tic_menu_item_icon_padding_1);
                textSize = getResources().getDimensionPixelSize(R.dimen.tic_menu_item_title_size_1);
                iconSize = getResources().getDimensionPixelSize(R.dimen.tic_menu_item_icon_size_1);
                break;
            case MENU_ITEM_TYPE_MIDDLE:
                margin = getResources().getDimensionPixelOffset(R.dimen.tic_menu_item_content_margin_large);
                iconPadding = getResources().getDimensionPixelOffset(R.dimen.tic_menu_item_icon_padding_2);
                textSize = getResources().getDimensionPixelSize(R.dimen.tic_menu_item_title_size_2);
                iconSize = getResources().getDimensionPixelSize(R.dimen.tic_menu_item_icon_size_2);
                break;
            case MENU_ITEM_TYPE_SMALL:
                margin = getResources().getDimensionPixelOffset(R.dimen.tic_menu_item_content_margin_small);
                iconPadding = getResources().getDimensionPixelOffset(R.dimen.tic_menu_item_icon_padding_3);
                textSize = getResources().getDimensionPixelSize(R.dimen.tic_menu_item_title_size_3);
                iconSize = getResources().getDimensionPixelSize(R.dimen.tic_menu_item_icon_size_3);
                break;
            default:
                margin = getResources().getDimensionPixelOffset(R.dimen.tic_menu_item_content_margin_small);
                iconPadding = getResources().getDimensionPixelOffset(R.dimen.tic_menu_item_icon_padding_2);
                textSize = getResources().getDimensionPixelSize(R.dimen.tic_menu_item_title_size_2);
                iconSize = getResources().getDimensionPixelSize(R.dimen.tic_menu_item_icon_size_2);
                break;
        }

        LayoutParams layoutParams;
        mTextTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        layoutParams = (LayoutParams) mTextTitle.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.topMargin = margin;
            layoutParams.bottomMargin = margin;
            mTextTitle.setLayoutParams(layoutParams);
        }

        mImageIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        mImageIcon.setMinimumWidth(iconSize);
        mImageIcon.setMinimumHeight(iconSize);
        layoutParams = (LayoutParams) mImageIcon.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = iconSize;
            layoutParams.height = iconSize;
            layoutParams.topMargin = margin;
            layoutParams.bottomMargin = margin;
            layoutParams.leftMargin = margin;
            layoutParams.rightMargin = margin;
            mImageIcon.setLayoutParams(layoutParams);
        }
    }

}

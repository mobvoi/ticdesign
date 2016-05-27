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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import ticwear.design.R;
import ticwear.design.internal.view.menu.MenuItemView.MenuItemType;
import ticwear.design.widget.FocusableLinearLayoutManager;
import ticwear.design.widget.TicklableRecyclerView;

/**
 * Layout contains some menu items.
 *
 * Created by tankery on 5/16/16.
 */
public class MenuFloatingLayout extends LinearLayout {

    private static final int[] MENU_ITEM_TYPE_FOR_SIZE = {
            MenuItemView.MENU_ITEM_TYPE_DEFAULT,
            MenuItemView.MENU_ITEM_TYPE_LARGE,
            MenuItemView.MENU_ITEM_TYPE_MIDDLE,
            MenuItemView.MENU_ITEM_TYPE_SMALL,
            MenuItemView.MENU_ITEM_TYPE_SMALL,
    };

    private final List<MenuItem> mItems;
    private OnItemSelectedListener mOnItemSelectedListener;

    private Runnable mResetLayoutRunnable = new Runnable() {
        @Override
        public void run() {
            resetLayout();
        }
    };

    LinearLayout mMenuLinearLayout1;
    LinearLayout mMenuLinearLayout2;
    TicklableRecyclerView mMenuListLayout;

    RecyclerView.Adapter mListAdapter;

    public MenuFloatingLayout(Context context) {
        this(context, null);
    }

    public MenuFloatingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuFloatingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MenuFloatingLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mItems = new ArrayList<>(1);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mMenuLinearLayout1 = (LinearLayout) findViewById(R.id.tic_menu_linear_layout_1);
        mMenuLinearLayout2 = (LinearLayout) findViewById(R.id.tic_menu_linear_layout_2);
        mMenuListLayout = (TicklableRecyclerView) findViewById(R.id.tic_menu_list_layout);
        mMenuListLayout.setLayoutManager(new FocusableLinearLayoutManager(getContext()));
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.mOnItemSelectedListener = listener;
    }

    public void clear() {
        mItems.clear();
        notifyItemsChanged();
    }

    public void addMenuItem(MenuItem item) {
        mItems.add(item);
        notifyItemsChanged();
    }

    public void notifyItemsChanged() {
        removeCallbacks(mResetLayoutRunnable);
        post(mResetLayoutRunnable);
    }

    private void resetLayout() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemSelected(null);
            }
        });

        if (mItems.size() > 0 && mItems.size() <= 3) {
            mMenuLinearLayout1.setVisibility(VISIBLE);
            mMenuLinearLayout2.setVisibility(GONE);
            mMenuListLayout.setVisibility(GONE);

            resetLinearLayout(mMenuLinearLayout1, 0, mItems.size());
        } else if (mItems.size() == 4) {
            mMenuLinearLayout1.setVisibility(VISIBLE);
            mMenuLinearLayout2.setVisibility(VISIBLE);
            mMenuListLayout.setVisibility(GONE);

            resetLinearLayout(mMenuLinearLayout1, 0, 2);
            resetLinearLayout(mMenuLinearLayout2, 2, 2);

        } else {
            mMenuLinearLayout1.setVisibility(GONE);
            mMenuLinearLayout2.setVisibility(GONE);
            mMenuListLayout.setVisibility(VISIBLE);

            resetListLayout();
        }
    }

    private void resetLinearLayout(LinearLayout layout, int start, int count) {
        layout.removeAllViews();
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        int margin = getResources().getDimensionPixelOffset(
                count < 3 && mItems.size() > 3 ?
                        R.dimen.tic_menu_item_margin_horizontal_large :
                        R.dimen.tic_menu_item_margin_horizontal_small);
        for (int i = start; i < start + count && i < mItems.size(); i++) {
            final MenuItem item = mItems.get(i);
            MenuItemView view = createItemView(MENU_ITEM_TYPE_FOR_SIZE[mItems.size()],
                    LinearLayout.VERTICAL, margin);

            view.setIcon(item.getIcon());
            view.setTitle(item.getTitle());

            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemSelected(item);
                }
            });

            layout.addView(view);
        }
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemSelected(null);
            }
        });
    }

    private void resetListLayout() {
        if (mListAdapter == null) {
            mListAdapter = new Adapter() {

                private static final int VIEW_TYPE_NORMAL = 0;
                private static final int VIEW_TYPE_CLOSE = 1;

                @Override
                public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(getContext())
                            .inflate(viewType == VIEW_TYPE_NORMAL ?
                                    R.layout.menu_list_item_view_ticwear :
                                    R.layout.menu_list_item_close_ticwear, parent, false);
                    if (viewType == VIEW_TYPE_CLOSE) {
                        view.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                notifyItemSelected(null);
                            }
                        });
                    }
                    return new FocusableLinearLayoutManager.ViewHolder(view);
                }

                @Override
                public void onBindViewHolder(ViewHolder holder, int position) {
                    if (getItemViewType(position) == VIEW_TYPE_NORMAL) {
                        MenuItemView view = (MenuItemView) holder.itemView;
                        final MenuItem item = mItems.get(position);
                        view.setIcon(item.getIcon());
                        view.setTitle(item.getTitle());
                        view.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                notifyItemSelected(item);
                            }
                        });
                    }
                }

                @Override
                public int getItemViewType(int position) {
                    return position < mItems.size() ? VIEW_TYPE_NORMAL : VIEW_TYPE_CLOSE;
                }

                @Override
                public int getItemCount() {
                    return mItems.size() + 1;
                }
            };

            mMenuListLayout.setAdapter(mListAdapter);
            mMenuListLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemSelected(null);
                }
            });
        } else {
            mListAdapter.notifyDataSetChanged();
        }
    }

    private void notifyItemSelected(MenuItem item) {
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onItemSelected(item);
        }
    }

    private MenuItemView createItemView(@MenuItemType int type, int orientation, int margin) {
        MenuItemView view = (MenuItemView) LayoutInflater.from(getContext())
                .inflate(R.layout.menu_item_view_ticwear, this, false);
        view.setMenuItemType(type);
        view.setOrientation(orientation);
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        lp.leftMargin = margin;
        lp.rightMargin = margin;
        view.setLayoutParams(lp);
        return view;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(MenuItem item);
    }

}

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

package com.mobvoi.design.demo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ticwear.design.demo.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ticwear.design.widget.FocusableLinearLayoutManager;
import ticwear.design.widget.FocusableLinearLayoutManager.FocusState;
import ticwear.design.widget.SimpleRecyclerAdapter;

/**
 * Fragment contains a list of title can be clicked.
 *
 * Created by tankery on 3/8/16.
 */
public class ListFragment extends Fragment {

    private final static String[] fromList = {
            "title"
    };
    private final static int[] toList = {
            R.id.text1
    };
    private static List<Map<String, Object>> listData;

    private CharSequence title;

    private static List<Map<String, Object>> initData(@StringRes int[] titles) {
        if (titles == null)
            return null;

        List<Map<String, Object>> listData = new ArrayList<>(titles.length);
        for (int title : titles) {
            listData.add(createRowData(title));
        }

        return listData;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        updateTitle();
    }

    public CharSequence getTitle() {
        return title;
    }

    private void updateTitle() {
        if (textTitle != null && title != null) {
            textTitle.setText(title);
        }
    }

    @StringRes
    protected int[] getItemTitles() {
        return null;
    }

    private static Map<String, Object> createRowData(@StringRes int title) {
        Map<String, Object> map = new HashMap<>(2);
        map.put(fromList[0], title);
        return map;
    }

    @BindView(android.R.id.title)
    TextView textTitle;

    @BindView(R.id.list_sub_demo)
    RecyclerView listSubDemo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        listData = initData(getItemTitles());
        initViews();
    }

    private void initViews() {
        if (listData == null)
            return;

        SimpleRecyclerAdapter adapter = new SimpleRecyclerAdapter(getActivity(),
                listData, R.layout.list_item_simple_text1, fromList, toList);
        adapter.setViewHolderCreator(new SimpleRecyclerAdapter.ViewHolderCreator() {
            @Override
            public SimpleRecyclerAdapter.ViewHolder create(View view, int[] to) {
                return new ViewHolder(ListFragment.this, view, to);
            }
        });
        listSubDemo.setLayoutManager(new FocusableLinearLayoutManager(getActivity()));
        listSubDemo.setAdapter(adapter);

        updateTitle();
    }

    public void onTitleClicked(View view, @StringRes int titleResId) {
    }

    public boolean onTitleLongClicked(View view, @StringRes int titleResId) {
        return false;
    }

    public static class ViewHolder extends SimpleRecyclerAdapter.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private final WeakReference<ListFragment> listFragment;

        @BindView(R.id.text1)
        TextView title;

        ViewHolder(ListFragment listFragment, View view, int[] to) {
            super(view, to);
            ButterKnife.bind(this, view);
            this.listFragment = new WeakReference<>(listFragment);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ListFragment listFragment = this.listFragment.get();
            if (listFragment != null) {
                int resId = (int) getBindingData(fromList[0]);
                listFragment.onTitleClicked(v, resId);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            ListFragment listFragment = this.listFragment.get();
            if (listFragment != null) {
                int resId = (int) getBindingData(fromList[0]);
                return listFragment.onTitleLongClicked(v, resId);
            }
            return false;
        }

        @Override
        protected void onCentralProgressUpdated(float progress, long animateDuration) {
            float scaleMin = 0.9f;
            float scaleMax = 1.1f;
            float alphaMin = 0.6f;
            float alphaMax = 1.0f;

            float scale = scaleMin + (scaleMax - scaleMin) * progress;
            float alphaProgress = getFocusInterpolator().getInterpolation(progress);
            float alpha = alphaMin + (alphaMax - alphaMin) * alphaProgress;
            transform(scale, alpha, animateDuration);
        }

        @Override
        protected void onFocusStateChanged(@FocusState int focusState, boolean animate) {
            if (focusState == FocusableLinearLayoutManager.FOCUS_STATE_NORMAL) {
                float scale = 1.0f;
                float alpha = 1.0f;
                transform(scale, alpha, animate ? getDefaultAnimDuration() : 0);
            }
        }

        private void transform(float scale, float alpha, long duration) {
            title.setPivotX(0);
            title.setPivotY(title.getHeight() / 2f);
            title.animate().cancel();
            if (duration > 0) {
                title.animate()
                        .setDuration(duration)
                        .alpha(alpha)
                        .scaleX(scale)
                        .scaleY(scale)
                        .start();
            } else {
                title.setScaleX(scale);
                title.setScaleY(scale);
                title.setAlpha(alpha);
            }
        }
    }
}

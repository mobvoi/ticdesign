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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ticwear.design.demo.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import ticwear.design.widget.FloatingActionButton;
import ticwear.design.widget.SubscribedScrollView;
import ticwear.design.widget.SubscribedScrollView.OnScrollListener;

/**
 * Fragment for CoordinatorLayout demo.
 *
 * Created by tankery on 6/8/16.
 */
public class CoordinatorFragment extends Fragment {


    @Bind(R.id.coordinator_pager)
    ViewPager pagerCoordinator;
    @Bind(R.id.fab_coordinator)
    FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coordinator, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        pagerCoordinator.setAdapter(new MyPagerAdapter());
        pagerCoordinator.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    fab.show();
                } else {
                    fab.minimize();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }


    public class MyPagerAdapter extends PagerAdapter {
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View pageContent = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.coordinator_page_scroll, container, false);
            SubscribedScrollView scrollView = ButterKnife.findById(pageContent, R.id.scroll_view);
            scrollView.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrollStateChanged(SubscribedScrollView view, int scrollState) {
                    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        fab.show();
                    } else {
                        fab.minimize();
                    }
                }

                @Override
                public void onScroll(SubscribedScrollView view, int l, int t, int oldl, int oldt) {
                }
            });
            container.addView(pageContent, 0);
            return pageContent;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (object);
        }

    }
}

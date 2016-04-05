package com.mobvoi.design.demo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import ticwear.design.widget.SimpleRecyclerAdapter;
import ticwear.design.widget.TicklableListView;

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
        if (textTitle != null) {
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

    @Bind(android.R.id.title)
    TextView textTitle;

    @Bind(R.id.list_sub_demo)
    TicklableListView listSubDemo;

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
        listSubDemo.setAdapter(adapter);

        updateTitle();
    }

    public void onTitleClicked(View view, @StringRes int titleResId) {
    }

    public static class ViewHolder extends SimpleRecyclerAdapter.ViewHolder implements View.OnClickListener {

        private final WeakReference<ListFragment> listFragment;

        ViewHolder(ListFragment listFragment, View view, int[] to) {
            super(view, to);
            this.listFragment = new WeakReference<>(listFragment);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ListFragment listFragment = this.listFragment.get();
            if (listFragment != null) {
                int resId = (int) getBindingData(fromList[0]);
                listFragment.onTitleClicked(v, resId);
            }
        }

    }
}

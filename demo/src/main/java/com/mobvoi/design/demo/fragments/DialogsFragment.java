package com.mobvoi.design.demo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mobvoi.design.widget.SimpleRecyclerAdapter;
import mobvoi.design.widget.TicklableListView;
import com.ticwear.design.demo.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tankery on 1/12/16.
 *
 * fragment for dialogs
 */
public class DialogsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Bind(R.id.list_sub_demo)
    TicklableListView listSubDemo;

    private final static String[] fromList = {
            "title"
    };
    private final static int[] toList = {
            R.id.text1
    };
    private final static List<Map<String, Object>> listData = Arrays.asList(
            createRowData(R.string.category_dialog_notify),
            createRowData(R.string.category_dialog_confirm)
    );

    private static Map<String, Object> createRowData(@StringRes int title) {
        Map<String, Object> map = new HashMap<>(2);
        map.put(fromList[0], title);
        return map;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initViews();
    }

    private void initViews() {
        RecyclerView.Adapter adapter = new SimpleRecyclerAdapter(getActivity(), listData, R.layout.list_item_simple_text1, fromList, toList);
        listSubDemo.setAdapter(adapter);
    }

}

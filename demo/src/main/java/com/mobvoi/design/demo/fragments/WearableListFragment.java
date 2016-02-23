package com.mobvoi.design.demo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class WearableListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wearable_list, container, false);
    }

    @Bind(R.id.list_view)
    WearableListView listView;

    private final static String[] fromList = {
            "title",
            "subtitle"
    };
    private final static int[] toList = {
            android.R.id.text1,
            android.R.id.text2
    };
    private final static List<Map<String, Object>> listData = Arrays.asList(
            createRowData("Notify Dialog", ""),
            createRowData("Confirm Dialog", null)
    );

    private static Map<String, Object> createRowData(String title, String subtitle) {
        Map<String, Object> map = new HashMap<>(2);
        map.put(fromList[0], title);
        map.put(fromList[1], subtitle);
        return map;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initViews();
    }

    private void initViews() {

        RecyclerView.Adapter adapter = new WearableListView.Adapter() {
            @Override
            public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        };
//        RecyclerView.Adapter adapter = new SimpleAdapter(getActivity(), listData, android.R.layout.simple_list_item_2, fromList, toList);
        listView.setAdapter(adapter);
    }

}

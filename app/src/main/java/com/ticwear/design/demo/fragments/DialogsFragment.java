package com.ticwear.design.demo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.ticwear.design.demo.R;
import com.mobvoi.design.wearable.view.ListView;

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
    ListView listSubDemo;

    private final static String[] fromList = {
            "title",
            "subtitle"
    };
    private final static int[] toList = {
            R.id.text1,
            R.id.text2
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
        ListAdapter adapter = new SimpleAdapter(getActivity(), listData, R.layout.list_item_simple_text2, fromList, toList);
        listSubDemo.setAdapter(adapter);
    }

}

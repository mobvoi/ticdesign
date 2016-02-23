package com.mobvoi.design.demo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobvoi.design.wearable.view.TicklableListView;
import com.ticwear.design.demo.R;
import com.mobvoi.design.demo.data.Cheeses;
import com.mobvoi.design.demo.widgets.IconTextRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tankery on 1/12/16.
 *
 * fragment for dialogs
 */
public class TransitionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Bind(R.id.list_sub_demo)
    TicklableListView listSubDemo;

    private final static List<IconTextRecyclerViewAdapter.ListData> listData = generateContents(10);

    private static List<IconTextRecyclerViewAdapter.ListData> generateContents(int count) {
        List<IconTextRecyclerViewAdapter.ListData> listData = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int icon = Cheeses.getRandomCheeseDrawable();
            String title = Cheeses.getRandomCheeseString();
            listData.add(new IconTextRecyclerViewAdapter.ListData(icon, title));
        }

        return listData;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initViews();
    }

    private void initViews() {
        RecyclerView.Adapter adapter = new IconTextRecyclerViewAdapter(getActivity(), listData);
        listSubDemo.setAdapter(adapter);
    }

}

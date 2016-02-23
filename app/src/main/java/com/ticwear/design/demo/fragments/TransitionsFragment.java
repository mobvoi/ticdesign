package com.ticwear.design.demo.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ticwear.design.demo.ContentActivity;
import com.ticwear.design.demo.R;
import com.ticwear.design.demo.data.Cheeses;
import com.mobvoi.design.wearable.view.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

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
    ListView listSubDemo;

    private final static String[] fromList = {
            "icon",
            "title"
    };
    private final static int[] toList = {
            R.id.item_icon,
            R.id.item_text,
    };
    private final static List<Map<String, Object>> listData = generateContents(10);

    private static List<Map<String, Object>> generateContents(int count) {
        List<Map<String, Object>> listData = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int icon = Cheeses.getRandomCheeseDrawable();
            String title = Cheeses.getRandomCheeseString();
            listData.add(createRowData(icon, title));
        }

        return listData;
    }

    private static Map<String, Object> createRowData(@DrawableRes int icon, String title) {
        Map<String, Object> map = new HashMap<>(2);
        map.put(fromList[0], icon);
        map.put(fromList[1], title);
        return map;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initViews();
    }

    private void initViews() {
        ListAdapter adapter = new SimpleAdapter(getActivity(), listData, R.layout.list_item_with_icon,
                fromList, toList);
        listSubDemo.setAdapter(adapter);
    }

    @SuppressWarnings({"unchecked", "unused"})
    @OnItemClick(R.id.list_sub_demo)
    protected void onItemClick(View item, int position) {
        ImageView icon = ButterKnife.findById(item, R.id.item_icon);
        TextView title = ButterKnife.findById(item, R.id.item_text);
        Map<String, Object> data = (Map<String, Object>) listSubDemo.getItemAtPosition(position);

        Intent intent = new Intent(getActivity(), ContentActivity.class);
        intent.putExtra(getString(R.string.transition_shared_avatar), (Integer) data.get("icon"));
        intent.putExtra(getString(R.string.transition_shared_title), (String) data.get("title"));
        ContentActivity.startActivityWithOptions(getActivity(), intent, icon, title);
    }

}

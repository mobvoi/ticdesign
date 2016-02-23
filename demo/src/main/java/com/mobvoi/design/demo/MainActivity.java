package com.mobvoi.design.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ticwear.design.demo.R;
import com.mobvoi.design.demo.widgets.SimpleRecyclerViewAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    @Bind(R.id.list_sub_demo)
    RecyclerView listSubDemo;

    private final static String[] fromList = {
            "title",
            "subtitle"
    };
    private final static int[] toList = {
            R.id.text1,
            R.id.text2
    };
    private final static List<Map<String, String>> listData = Arrays.asList(
            createRowData("Dialogs", ""),
            createRowData("Widgets 1", null),
            createRowData("Widgets 2", null),
            createRowData("Widgets 3", null),
            createRowData("Widgets 4", null),
            createRowData("Showcase", null)
    );

    private static Map<String, String> createRowData(String title, String subtitle) {
        Map<String, String> map = new HashMap<>(2);
        map.put(fromList[0], title);
        map.put(fromList[1], subtitle);
        return map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        RecyclerView.Adapter adapter = new SimpleRecyclerViewAdapter(this, listData, R.layout.list_item_simple_text2, fromList, toList);
        if (listSubDemo.getLayoutManager() == null) {
            listSubDemo.setLayoutManager(new LinearLayoutManager(this));
        }
        listSubDemo.setAdapter(adapter);
    }

}

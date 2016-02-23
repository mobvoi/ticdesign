package com.ticwear.design.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.mobvoi.design.wearable.view.ListView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class MainActivity extends Activity {

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
            createRowData("Dialogs", ""),
            createRowData("Widgets", null),
            createRowData("Widgets", null),
            createRowData("Widgets", null),
            createRowData("Widgets", null),
            createRowData("Showcase", null)
    );

    private static Map<String, Object> createRowData(String title, String subtitle) {
        Map<String, Object> map = new HashMap<>(2);
        map.put(fromList[0], title);
        map.put(fromList[1], subtitle);
        return map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        ListAdapter adapter = new SimpleAdapter(this, listData, R.layout.list_item_simple_text2, fromList, toList);
        listSubDemo.setAdapter(adapter);
    }

    @SuppressWarnings("unused")
    @OnItemClick(R.id.list_sub_demo)
    protected void onItemClick(View item) {
        Intent intent = new Intent(this, DetailsActivity.class);
        TextView title = ButterKnife.findById(item, R.id.text1);
        intent.putExtra("case", title.getText().toString());
        startActivity(intent);
    }

}

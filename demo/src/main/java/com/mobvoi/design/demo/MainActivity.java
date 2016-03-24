package com.mobvoi.design.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ticwear.design.demo.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import ticwear.design.widget.SimpleRecyclerAdapter;
import ticwear.design.widget.TicklableListView;

public class MainActivity extends Activity {

    @Bind(R.id.list_sub_demo)
    RecyclerView listSubDemo;

    private final static String[] fromList = {
            "title"
    };
    private final static int[] toList = {
            R.id.text1
    };
    private final static List<Map<String, Object>> listData = Arrays.asList(
            createRowData(R.string.category_spec_title),
            createRowData(R.string.category_dialog_title),
            createRowData(R.string.category_settings_title),
            createRowData(R.string.category_widgets_title),
            createRowData(R.string.category_showcase_title),
            createRowData(R.string.app_name),
            createRowData(R.string.app_name),
            createRowData(R.string.app_name)
    );

    private static Map<String, Object> createRowData(@StringRes int title) {
        Map<String, Object> map = new HashMap<>(2);
        map.put(fromList[0], title);
        return map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        SimpleRecyclerAdapter adapter = new SimpleRecyclerAdapter(this, listData, R.layout.list_item_simple_text1, fromList, toList);
        adapter.setViewHolderCreator(new SimpleRecyclerAdapter.ViewHolderCreator() {
            @Override
            public SimpleRecyclerAdapter.ViewHolder create(View view, int[] to) {
                return new ViewHolder(view, to);
            }
        });
        listSubDemo.setAdapter(adapter);
    }

    private static class ViewHolder extends SimpleRecyclerAdapter.ViewHolder implements View.OnClickListener {

        private final long animDuration;

        ViewHolder(View view, int[] to) {
            super(view, to);
            view.setOnClickListener(this);
            animDuration = 200; // view.getResources().getInteger(android.R.integer.config_shortAnimTime);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("case", (int) getBindingData(fromList[0]));
            context.startActivity(intent);
        }

        @Override
        protected void onFocusStateChanged(int focusState, boolean animate) {
            float scale = 1;
            switch (focusState) {
                case TicklableListView.FOCUS_STATE_NORMAL:
                    scale = 1;
                    break;
                case TicklableListView.FOCUS_STATE_CENTRAL:
                    scale = 1.3f;
                    break;
                case TicklableListView.FOCUS_STATE_NON_CENTRAL:
                    scale = 0.8f;
                    break;
            }
            TextView title = ButterKnife.findById(itemView, R.id.text1);
            title.setPivotX(0);
            title.setPivotY(title.getHeight() / 2f);
            if (animate) {
                title.animate().setDuration(animDuration).scaleX(scale);
                title.animate().setDuration(animDuration).scaleY(scale);
            } else {
                title.setScaleX(scale);
                title.setScaleY(scale);
            }
        }
    }

}

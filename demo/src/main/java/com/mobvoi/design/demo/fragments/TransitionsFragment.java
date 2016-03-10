package com.mobvoi.design.demo.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobvoi.design.demo.ContentActivity;
import com.mobvoi.design.demo.data.Cheeses;
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

    private final static String[] fromList = {
            "icon",
            "title",
    };
    private final static int[] toList = {
            R.id.item_icon,
            R.id.item_text,
    };
    private final static List<Map<String, Object>> listData = generateContents(10);

    private static Map<String, Object> createRowData(@DrawableRes int icon, String title) {
        Map<String, Object> map = new HashMap<>(2);
        map.put(fromList[0], icon);
        map.put(fromList[1], title);
        return map;
    }

    private static List<Map<String, Object>> generateContents(int count) {
        List<Map<String, Object>> listData = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int icon = Cheeses.getRandomCheeseDrawable();
            String title = Cheeses.getRandomCheeseString();
            listData.add(createRowData(icon, title));
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
        SimpleRecyclerAdapter adapter = new SimpleRecyclerAdapter(getActivity(), listData, R.layout.list_item_with_icon, fromList, toList);
        adapter.setViewHolderCreator(new SimpleRecyclerAdapter.ViewHolderCreator() {
            @Override
            public SimpleRecyclerAdapter.ViewHolder create(View view, int[] to) {
                return new ViewHolder(getActivity(), view, to);
            }
        });
        listSubDemo.setAdapter(adapter);
    }


    public static class ViewHolder extends SimpleRecyclerAdapter.ViewHolder implements View.OnClickListener {

        private final long animDuration;

        private final WeakReference<Activity> hostActivity;

        @Bind(R.id.item_icon)
        ImageView imageIcon;
        @Bind(R.id.item_text)
        TextView textView;

        ViewHolder(Activity host, View view, int[] to) {
            super(view, to);
            ButterKnife.bind(this, view);
            this.hostActivity = new WeakReference<>(host);
            view.setOnClickListener(this);
            animDuration = 200; // view.getResources().getInteger(android.R.integer.config_shortAnimTime);
        }

        @Override
        public void onClick(View v) {
            Activity host = hostActivity.get();
            if (host == null) {
                return;
            }
            Intent intent = new Intent(host, ContentActivity.class);
            intent.putExtra(host.getString(R.string.transition_shared_avatar), (int) getBindingData(fromList[0]));
            intent.putExtra(host.getString(R.string.transition_shared_title), (String) getBindingData(fromList[1]));
            ContentActivity.startActivityWithOptions(host, intent, imageIcon, textView);
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
            textView.setPivotX(0);
            textView.setPivotY(textView.getHeight() / 2f);
            if (animate) {
                textView.animate().setDuration(animDuration).scaleX(scale);
                textView.animate().setDuration(animDuration).scaleY(scale);
                imageIcon.animate().setDuration(animDuration).scaleX(scale);
                imageIcon.animate().setDuration(animDuration).scaleY(scale);
            } else {
                textView.setScaleX(scale);
                textView.setScaleY(scale);
                imageIcon.setScaleX(scale);
                imageIcon.setScaleY(scale);
            }
        }
    }

}

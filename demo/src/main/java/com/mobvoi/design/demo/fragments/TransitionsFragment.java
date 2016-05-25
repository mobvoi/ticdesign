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
import ticwear.design.widget.FocusableLinearLayoutManager;
import ticwear.design.widget.FocusableLinearLayoutManager.FocusState;
import ticwear.design.widget.SimpleRecyclerAdapter;
import ticwear.design.widget.TicklableRecyclerView;

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

    @Bind(android.R.id.title)
    TextView textTitle;

    @Bind(R.id.list_sub_demo)
    TicklableRecyclerView listSubDemo;

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
        listSubDemo.setLayoutManager(new FocusableLinearLayoutManager(getActivity()));
        listSubDemo.setAdapter(adapter);

        textTitle.setText(R.string.category_showcase_title);
    }


    public static class ViewHolder extends SimpleRecyclerAdapter.ViewHolder implements View.OnClickListener {

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
        protected void onCentralProgressUpdated(float progress, long animateDuration) {
            float scaleMin = 0.8f;
            float scaleMax = 1.3f;
            float alphaMin = 0.6f;
            float alphaMax = 1.0f;

            float scale = scaleMin + (scaleMax - scaleMin) * progress;
            float alphaProgress = getFocusInterpolator().getInterpolation(progress);
            float alpha = alphaMin + (alphaMax - alphaMin) * alphaProgress;
            transform(scale, alpha, animateDuration);
        }

        @Override
        protected void onFocusStateChanged(@FocusState int focusState,
                                           boolean animate) {
            if (focusState == FocusableLinearLayoutManager.FOCUS_STATE_NORMAL) {
                transform(1f, 1f, animate ? getDefaultAnimDuration() : 0);
            }
        }

        private void transform(float scale, float alpha, long duration) {
            textView.setPivotX(0);
            textView.setPivotY(textView.getHeight() / 2f);
            if (duration > 0) {
                textView.animate()
                        .setDuration(duration)
                        .alpha(alpha)
                        .scaleX(scale)
                        .scaleY(scale);
                imageIcon.animate()
                        .setDuration(duration)
                        .alpha(alpha)
                        .scaleX(scale)
                        .scaleY(scale);
            } else {
                textView.setAlpha(alpha);
                textView.setScaleX(scale);
                textView.setScaleY(scale);
                imageIcon.setAlpha(alpha);
                imageIcon.setScaleX(scale);
                imageIcon.setScaleY(scale);
            }
        }
    }

}

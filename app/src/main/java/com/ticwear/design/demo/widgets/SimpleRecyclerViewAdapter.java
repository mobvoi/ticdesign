package com.ticwear.design.demo.widgets;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobvoi.design.wearable.view.TicklableListView;
import com.ticwear.design.demo.DetailsActivity;
import com.ticwear.design.demo.R;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class SimpleRecyclerViewAdapter extends TicklableListView.Adapter<SimpleRecyclerViewAdapter.ViewHolder> {
    private final LayoutInflater layoutInflater;
    private final List<? extends Map<String, String>> listData;
    private final int resource;
    private final String[] from;
    private final int[] to;

    public SimpleRecyclerViewAdapter(Context context, List<? extends Map<String, String>> data,
                                     @LayoutRes int resource, String[] from, @IdRes int[] to) {
        this.listData = data;
        this.resource = resource;
        this.from = from;
        this.to = to;

        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(resource, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, String> item = listData.get(position);
        for (int i = 0; i < from.length; i++) {
            TextView textView = ButterKnife.findById(holder.itemView, to[i]);
            textView.setText(item.get(from[i]));
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    public static class ViewHolder extends TicklableListView.ViewHolder implements View.OnClickListener {

        private final long animDuration;

        ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            animDuration = 200; // view.getResources().getInteger(android.R.integer.config_shortAnimTime);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(context, DetailsActivity.class);
            TextView title = ButterKnife.findById(itemView, R.id.text1);
            intent.putExtra("case", title.getText().toString());
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
            title.setPivotY(0);
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
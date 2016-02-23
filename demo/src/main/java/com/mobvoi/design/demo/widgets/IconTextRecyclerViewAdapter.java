package com.mobvoi.design.demo.widgets;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobvoi.design.wearable.view.TicklableListView;
import com.mobvoi.design.demo.ContentActivity;
import com.ticwear.design.demo.R;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class IconTextRecyclerViewAdapter extends TicklableListView.Adapter<IconTextRecyclerViewAdapter.ViewHolder> {
    private final WeakReference<Activity> hostActivity;
    private final LayoutInflater layoutInflater;
    private final List<ListData> listData;

    public IconTextRecyclerViewAdapter(Activity host, List<ListData> listData) {
        this.hostActivity = new WeakReference<>(host);
        this.listData = listData;

        layoutInflater = LayoutInflater.from(host);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(hostActivity.get(),
                layoutInflater.inflate(R.layout.list_item_with_icon, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ListData item = listData.get(position);
        holder.textView.setText(item.text);
        holder.text = item.text;
        holder.imageIcon.setImageResource(item.icon);
        holder.icon = item.icon;
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }


    public static class ListData {
        public final int icon;
        public final String text;

        public ListData(int icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }


    public static class ViewHolder extends TicklableListView.ViewHolder implements View.OnClickListener {

        private final long animDuration;

        private final WeakReference<Activity> hostActivity;

        int icon;
        String text;
        @Bind(R.id.item_icon)
        ImageView imageIcon;
        @Bind(R.id.item_text)
        TextView textView;

        ViewHolder(Activity host, View view) {
            super(view);
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
            intent.putExtra(host.getString(R.string.transition_shared_avatar), icon);
            intent.putExtra(host.getString(R.string.transition_shared_title), text);
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
            textView.setPivotY(0);
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
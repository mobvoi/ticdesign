/*
 * Copyright (c) 2016 Mobvoi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobvoi.design.demo.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.StringRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ticwear.design.demo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import ticwear.design.app.AlertDialog;
import ticwear.design.utils.ColorPalette;

/**
 * Created by tankery on 3/24/16.
 *
 * fragment for Specifications
 */
public class SpecFragment extends ListFragment {

    @Override
    protected int[] getItemTitles() {
        return new int[]{
                R.string.category_spec_text,
                R.string.category_spec_color,
                R.string.category_spec_layout,
        };
    }

    @Override
    public void onTitleClicked(View view, @StringRes int titleResId) {
        Dialog dialog = createDialog(view.getContext(), titleResId);
        if (dialog != null) {
            dialog.show();
        }
    }

    private Dialog createDialog(final Context context, int resId) {
        Dialog dialog = null;
        switch (resId) {
            case R.string.category_spec_text:
                dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.category_spec_text)
                        .setView(R.layout.dialog_spec_text)
                        .create();
                break;
            case R.string.category_spec_color:
                RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(context)
                        .inflate(R.layout.dialog_spec_color, null);
                recyclerView.setNestedScrollingEnabled(true);
                recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                recyclerView.setAdapter(new ColorAdapter(ColorPalette.ColorName.values()));

                dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.category_spec_color)
                        .setView(recyclerView)
                        .create();
                break;
            case R.string.category_spec_layout:
                break;
        }

        return dialog;
    }


    static class ColorAdapter extends RecyclerView.Adapter<ColorViewHolder> {

        private final ColorPalette.ColorName[] colorNames;

        public ColorAdapter(ColorPalette.ColorName[] names) {
            this.colorNames = names;
        }

        @Override
        public ColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ColorViewHolder(inflater.inflate(R.layout.dialog_spec_color_group, parent, false));
        }

        @Override
        public void onBindViewHolder(ColorViewHolder holder, int position) {
            holder.bind(colorNames[position]);
        }

        @Override
        public int getItemCount() {
            return colorNames.length;
        }
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {

        private final Context context;

        @BindView(R.id.color_name)
        TextView colorName;
        @BindView(R.id.color_darken)
        TextView colorDarken;
        @BindView(R.id.color_normal)
        TextView colorNormal;
        @BindView(R.id.color_lighten)
        TextView colorLighten;

        public ColorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.context = itemView.getContext();
        }

        public void bind(ColorPalette.ColorName name) {
            ColorPalette.Color color = ColorPalette.from(context).color(name);
            colorName.setText(name.colorName());
            colorName.setTextColor(color.value());
            boolean useBlack = (color.value() & 0xffffff) > (0xffffff / 2);
            int textColor = useBlack ? Color.BLACK : Color.WHITE;
            colorNormal.setText(color.rgbString());
            colorNormal.setTextColor(textColor);
            colorNormal.setBackgroundColor(color.value());
            colorDarken.setText(color.darken().rgbString());
            colorDarken.setTextColor(textColor);
            colorDarken.setBackgroundColor(color.darken().value());
            colorLighten.setText(color.lighten().rgbString());
            colorLighten.setTextColor(textColor);
            colorLighten.setBackgroundColor(color.lighten().value());
        }

    }

}

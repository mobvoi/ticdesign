package com.mobvoi.design.demo.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobvoi.design.demo.DetailsActivity;
import com.ticwear.design.demo.R;

/**
 * Created by tankery on 3/24/16.
 *
 * fragment for Specifications
 */
public class MainFragment extends ListFragment {

    @Override
    protected int[] getItemTitles() {
        return new int[]{
                R.string.category_spec_title,
                R.string.category_dialog_title,
                R.string.category_settings_title,
                R.string.category_widgets_title,
                R.string.category_menu_title,
                R.string.category_showcase_title,
                R.string.app_name,
                R.string.app_name,
                R.string.app_name,
                R.string.app_name,
                R.string.app_name,
                R.string.app_name,
                R.string.app_name,
                R.string.app_name,
                R.string.app_name,
                R.string.app_name,
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_list, container, false);
    }

    @Override
    public void onTitleClicked(View view, @StringRes int titleResId) {
        Context context = view.getContext();
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("case", titleResId);
        context.startActivity(intent);
    }

}

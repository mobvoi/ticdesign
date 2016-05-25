package com.mobvoi.design.demo.fragments;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ticwear.design.demo.R;

import ticwear.design.widget.FloatingContextMenu;
import ticwear.design.widget.FloatingContextMenu.ContextMenuCreator;
import ticwear.design.widget.FloatingContextMenu.OnMenuSelectedListener;

/**
 * A Fragment to demo the floating context menu.
 *
 * Created by tankery on 5/12/16.
 */
public class MenuFragment extends ListFragment implements OnMenuSelectedListener, ContextMenuCreator {

    @Override
    protected int[] getItemTitles() {
        return new int[]{
                R.string.category_menu_hint_1,
                R.string.category_menu_hint_2,
                R.string.category_menu_hint_3,
                R.string.category_menu_hint_4,
                R.string.category_menu_hint_5,
        };
    }

    @Override
    public boolean onTitleLongClicked(View view, @StringRes int titleResId) {
        int[] titles = getItemTitles();
        int count = 0;
        for (int i = 0; i < titles.length; i++) {
            if (titles[i] == titleResId) {
                count = i + 1;
            }
        }
        view.setTag(count);
        return new FloatingContextMenu(getActivity())
                .setContextMenuCreator(this)
                .setOnMenuSelectedListener(this)
                .show(view);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.default_hint, menu);

        int count = (Integer) v.getTag();
        for (int i = menu.size(); i > count; i--) {
            MenuItem item = menu.getItem(i - 1);
            menu.removeItem(item.getItemId());
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Toast.makeText(getActivity(), item.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }
}

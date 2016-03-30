package com.mobvoi.design.demo.fragments;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;

import com.ticwear.design.demo.R;

import ticwear.design.app.AlertDialog;
import ticwear.design.widget.FloatingActionButton;

/**
 * Created by tankery on 1/12/16.
 *
 * fragment for test
 */
public class WidgetsFragment extends ListFragment {

    @Override
    protected int[] getItemTitles() {
        return new int[]{
                R.string.category_widgets_fab,
                R.string.category_widgets_button,
                R.string.category_widgets_picker,
                R.string.category_widgets_progress,
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
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (resId) {
            case R.string.category_widgets_fab: {
                View layout = inflater.inflate(
                        R.layout.widgets_fab_scroll, null);
                final FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.fab);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fab.minimize();
                    }
                });
                layout.findViewById(R.id.text_content)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fab.show();
                            }
                        });
                dialog = new Dialog(context);
                dialog.setContentView(layout);
                break;
            }
            case R.string.category_widgets_button: {
                dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.category_widgets_button)
                        .setView(R.layout.dialog_widgets_btn_list)
                        .create();
                break;
            }
            case R.string.category_widgets_picker:
                break;
            case R.string.category_widgets_progress:
                break;
        }

        return dialog;
    }

}

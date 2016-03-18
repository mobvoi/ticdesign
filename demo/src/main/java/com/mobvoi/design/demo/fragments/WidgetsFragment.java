package com.mobvoi.design.demo.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.ticwear.design.demo.R;

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
                R.string.category_widgets_button,
                R.string.category_widgets_picker,
                R.string.category_widgets_progress,
        };
    }

    @Override
    public void onTitleClicked(View view, @StringRes int titleResId) {
        DialogFragment fragment = createDialogFragment(view.getContext(), titleResId);
        if (fragment != null) {
            fragment.show(getChildFragmentManager(), view.getContext().getString(titleResId));
        }
    }

    private DialogFragment createDialogFragment(final Context context, int resId) {
        DialogFragment dialogFragment = null;
        switch (resId) {
            case R.string.category_widgets_button:
                dialogFragment = new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        LayoutInflater inflater = (LayoutInflater)
                                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                        Dialog dialog = new Dialog(context);
                        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(layout);

                        return dialog;
                    }
                };
                break;
            case R.string.category_widgets_picker:
                break;
            case R.string.category_widgets_progress:
                break;
        }

        return dialogFragment;
    }

}

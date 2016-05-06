package com.mobvoi.design.demo.fragments;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ticwear.design.demo.R;

import ticwear.design.app.AlertDialog;
import ticwear.design.drawable.CircularProgressDrawable;
import ticwear.design.widget.FloatingActionButton;
import ticwear.design.widget.FloatingActionButton.DelayedConfirmationListener;
import ticwear.design.widget.VolumeBar;

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
                R.string.category_widgets_fab_delay,
                R.string.category_widgets_button,
                R.string.category_volume_bar,
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
            case R.string.category_widgets_fab:
                dialog = createFABDialog(context, inflater);
                break;
            case R.string.category_widgets_fab_delay: {
                dialog = createFABDelayConfirmDialog(context, inflater);
                break;
            }
            case R.string.category_widgets_button: {
                dialog = new AlertDialog.Builder(context, ticwear.design.R.style.Theme_Ticwear_Dialog_Datetime)
                        .setTitle(R.string.category_widgets_button)
                        .setView(R.layout.dialog_widgets_btn_list)
                        .create();
                break;
            }
            case R.string.category_volume_bar:
                dialog = new Dialog(context);
                View layout = inflater.inflate(
                        R.layout.widgets_volume_bar, null);
                VolumeBar vBar = (VolumeBar)layout.findViewById(R.id.volume_bar);
                final TextView tv = (TextView) layout.findViewById(R.id.volume_text);
                tv.setText(vBar.getProgress()+"");
                vBar.setOnVolumeChangedListetener(new VolumeBar.OnVolumeChangedListener() {
                    @Override
                    public void onVolumeChanged(VolumeBar volumeBar, int progress, boolean fromUser) {
                        tv.setText(progress+"");
                    }
                });
                dialog.setContentView(layout);
                break;
            case R.string.category_widgets_picker:
                break;
            case R.string.category_widgets_progress:
                break;
        }

        return dialog;
    }

    @NonNull
    private Dialog createFABDialog(Context context, LayoutInflater inflater) {
        View layout = inflater.inflate(
                R.layout.widgets_fab_scroll, null);
        final FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.fab);

        View.OnClickListener listener = new View.OnClickListener() {
            boolean isShow = true;
            @Override
            public void onClick(View v) {
                if (isShow) {
                    fab.minimize();
                } else {
                    fab.show();
                }
                isShow = !isShow;
            }
        };
        layout.findViewById(R.id.text_content)
                .setOnClickListener(listener);
        layout.setOnClickListener(listener);

        fab.setOnClickListener(new View.OnClickListener() {
            private int clickCount = 0;
            private ValueAnimator increaseAnimator = ValueAnimator.ofFloat(0, 1)
                    .setDuration(5000);

            {
                increaseAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float progress = (float) animation.getAnimatedValue();
                        fab.setProgressPercent(progress);

                        if (progress >= 1) {
                            onClick(null);
                        }
                    }
                });
            }

            @Override
            public void onClick(View v) {
                clickCount++;
                int count = clickCount % 3;
                fab.setShowProgress(count != 0);

                switch (count) {
                    case 1:
                        fab.setProgressMode(CircularProgressDrawable.MODE_DETERMINATE);
                        increaseAnimator.start();
                        break;
                    case 2:
                        increaseAnimator.cancel();
                        fab.setProgressMode(CircularProgressDrawable.MODE_INDETERMINATE);
                        fab.startProgress();
                        break;
                    default:
                        increaseAnimator.cancel();
                        break;
                }
            }
        });


        Dialog dialog = new Dialog(context);
        dialog.setContentView(layout);
        return dialog;
    }

    private Dialog createFABDelayConfirmDialog(Context context, LayoutInflater inflater) {
        View layout = inflater.inflate(
                R.layout.widgets_fab_scroll, null);
        final FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.fab);
        fab.setImageResource(ticwear.design.R.drawable.tic_ic_btn_ok);

        final TextView content = (TextView) layout.findViewById(R.id.text_content);
        content.setText(R.string.text_dialog_delay_confirm);

        Dialog dialog = new Dialog(context);
        dialog.setContentView(layout);
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                fab.startDelayConfirmation(5000, new DelayedConfirmationListener() {
                    @Override
                    public void onButtonClicked(FloatingActionButton fab) {
                        Toast.makeText(fab.getContext(), "Button clicked", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTimerFinished(FloatingActionButton fab) {
                        Toast.makeText(fab.getContext(), "Timer finished", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return dialog;
    }
}

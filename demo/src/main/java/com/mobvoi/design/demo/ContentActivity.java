package com.mobvoi.design.demo;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ticwear.design.demo.R;
import com.mobvoi.design.demo.data.Cheeses;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContentActivity extends Activity {

    @Bind(R.id.layout_container)
    ViewGroup layoutContainer;

    @Bind(R.id.image_avatar)
    ImageView imageAvatar;

    @Bind(R.id.text_title)
    TextView textTitle;
    @Bind(R.id.text_content)
    TextView textContent;

    @SuppressWarnings("unchecked")
    public static void startActivityWithOptions(Activity current, Intent intent, View avatar, View title) {
        ActivityOptions transitionActivity =
                ActivityOptions.makeSceneTransitionAnimation(current,
                        Pair.create(avatar, current.getString(R.string.transition_shared_avatar)),
                        Pair.create(title, current.getString(R.string.transition_shared_title)));
        current.startActivity(intent, transitionActivity.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);

        if (getIntent() != null) {
            int avatar = getIntent().getIntExtra(getString(R.string.transition_shared_avatar), 0);
            String title = getIntent().getStringExtra(getString(R.string.transition_shared_title));

            if (avatar > 0) {
                imageAvatar.setImageResource(avatar);
                colorize(((BitmapDrawable) imageAvatar.getDrawable()).getBitmap());
            }
            if (title != null) {
                textTitle.setText(title);
            }

            Transition transition =
                    TransitionInflater.from(this).inflateTransition(R.transition.slide_bottom);
            getWindow().setEnterTransition(transition);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.image_avatar)
    protected void onChangeCheese() {
        int iconRes = Cheeses.getRandomCheeseDrawable();
        String titleString = Cheeses.getRandomCheeseString();

        TransitionManager.beginDelayedTransition(layoutContainer, new AutoTransition());

        imageAvatar.setImageResource(iconRes);
        textTitle.setText(titleString);
        colorize(((BitmapDrawable) imageAvatar.getDrawable()).getBitmap());
    }

    private void colorize(Bitmap photo) {
        Palette palette = Palette.from(photo).generate();
        applyPalette(palette);
    }

    private void applyPalette(Palette palette) {
        int colorMuted = palette.getDarkMutedColor(getResources().getColor(android.R.color.black));
        int colorLightVibrant = palette.getLightVibrantColor(getResources().getColor(android.R.color.primary_text_dark));
        int colorVibrant = palette.getVibrantColor(getResources().getColor(android.R.color.primary_text_dark));

        getWindow().setBackgroundDrawable(new ColorDrawable(colorMuted));
        textTitle.setTextColor(colorLightVibrant);
        textContent.setTextColor(colorVibrant);
    }

}

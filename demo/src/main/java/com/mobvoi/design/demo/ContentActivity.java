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

import com.mobvoi.design.demo.data.Cheeses;
import com.ticwear.design.demo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContentActivity extends BaseActivity {

    @BindView(R.id.layout_container)
    ViewGroup layoutContainer;

    @BindView(R.id.image_avatar)
    ImageView imageAvatar;

    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.text_content)
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

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

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.mobvoi.design.demo.fragments.CoordinatorFragment;
import com.mobvoi.design.demo.fragments.DialogsFragment;
import com.mobvoi.design.demo.fragments.ListFragment;
import com.mobvoi.design.demo.fragments.MenuFragment;
import com.mobvoi.design.demo.fragments.SpecFragment;
import com.mobvoi.design.demo.fragments.TransitionsFragment;
import com.mobvoi.design.demo.fragments.WidgetsFragment;
import com.ticwear.design.demo.R;

/**
 * Created by tankery on 1/12/16.
 *
 * Activity for details.
 */
public class DetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment detailFragment;
        int titleRes = getIntent().getIntExtra("case", -1);
        switch (titleRes) {
            case R.string.category_dialog_title:
                detailFragment = new DialogsFragment();
                break;
            case R.string.category_widgets_title:
                detailFragment = new WidgetsFragment();
                break;
            case R.string.category_settings_title:
                // Use a settings activity instead of details activity.
                startActivity(new Intent(this, SettingsActivity.class));
                detailFragment = null;
                break;
            case R.string.category_showcase_title:
                detailFragment = new TransitionsFragment();
                break;
            case R.string.category_menu_title:
                detailFragment = new MenuFragment();
                break;
            case R.string.category_spec_title:
                detailFragment = new SpecFragment();
                break;
            case R.string.category_coordinator_title:
                detailFragment = new CoordinatorFragment();
                break;
            default:
                detailFragment = null;
                break;
        }

        if (detailFragment instanceof ListFragment) {
            ((ListFragment) detailFragment).setTitle(getString(titleRes));
        }

        if (detailFragment != null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, detailFragment)
                    .commitAllowingStateLoss();
        } else {
            finish();
        }
    }

}

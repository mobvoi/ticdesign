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

import com.mobvoi.design.demo.fragments.DynamicSettingsFragment;
import com.mobvoi.design.demo.fragments.SettingsFragment;
import com.ticwear.design.demo.R;

import java.util.List;

import ticwear.design.preference.PreferenceActivity;

/**
 * Created by tankery on 3/1/16.
 *
 * Settings activity to show headers with sub fragments.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName) ||
                DynamicSettingsFragment.class.getName().equals(fragmentName);
    }
}

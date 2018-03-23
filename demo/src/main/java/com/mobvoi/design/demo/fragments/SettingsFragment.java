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

import android.os.Bundle;
import android.widget.Toast;

import com.ticwear.design.demo.R;

import ticwear.design.preference.PreferenceFragment;

/**
 * Created by tankery on 2/25/16.
 *
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String settings = getArguments().getString("settings");
        if ("preference".equals(settings)) {
            addPreferencesFromResource(R.xml.preferences_widget);
        } else if ("about".equals(settings)) {
            addPreferencesFromResource(R.xml.preferences_about);
        } else {
            Toast.makeText(getActivity(), "No such settings.", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }
}
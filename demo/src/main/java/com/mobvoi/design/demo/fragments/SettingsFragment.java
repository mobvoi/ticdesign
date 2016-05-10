package com.mobvoi.design.demo.fragments;

import android.os.Bundle;

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
            addPreferencesFromResource(R.xml.preferences);
        } else if ("about".equals(settings)) {
            addPreferencesFromResource(R.xml.preferences_about);
        }
    }
}
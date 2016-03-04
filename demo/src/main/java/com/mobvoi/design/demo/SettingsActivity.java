package com.mobvoi.design.demo;

import com.mobvoi.design.demo.fragments.SettingsFragment;
import mobvoi.design.preference.PreferenceActivity;
import com.ticwear.design.demo.R;

import java.util.List;

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
        return SettingsFragment.class.getName().equals(fragmentName);
    }
}

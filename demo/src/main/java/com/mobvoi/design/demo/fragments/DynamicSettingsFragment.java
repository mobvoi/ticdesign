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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ticwear.design.demo.R;

import java.util.ArrayList;
import java.util.List;

import ticwear.design.preference.Preference;
import ticwear.design.preference.Preference.OnPreferenceClickListener;
import ticwear.design.preference.PreferenceFragment;
import ticwear.design.preference.SwitchPreference;

/**
 * Created by tankery on 2/25/16.
 *
 */
public class DynamicSettingsFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {

    public static final String WIFI_SWITCH = "wifi_switch";
    public static final String WIFI_DESCRIPTION = "wifi_description";
    public static final String WIFI_ADDING = "wifi_adding";

    private Preference descriptionPref;
    private Preference addingPref;
    private List<Preference> dynamicPrefs = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_dynamic);
        descriptionPref = createDescPref();
        addingPref = createAddingPref();
        addingPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                addPreference();
                return true;
            }
        });
        updatePreference();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (WIFI_SWITCH.equals(key)) {
            updatePreference();
        }
    }

    @NonNull
    private Preference createDescPref() {
        Preference pref = new Preference(getActivity(), null, android.R.attr.preferenceInformationStyle);
        pref.setSummary(R.string.preference_ambientmode_tips);
        pref.setKey(WIFI_DESCRIPTION);
        return pref;
    }

    @NonNull
    private Preference createAddingPref() {
        Preference pref = new Preference(getActivity());
        pref.setTitle(R.string.preference_adding_pref);
        pref.setKey(WIFI_ADDING);
        return pref;
    }

    @NonNull
    private Preference createDynamicPref(int num) {
        Preference pref = new Preference(getActivity());
        pref.setTitle(getString(R.string.preference_dynamic_pref, num));
        return pref;
    }

    private void addPreference() {
        Preference pref = createDynamicPref(dynamicPrefs.size() + 1);
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                removePreference(preference);
                return true;
            }
        });
        dynamicPrefs.add(pref);
        getPreferenceScreen().addPreference(pref);
        addingPref.setOrder(getPreferenceScreen().getPreferenceCount());
    }

    private void removePreference(Preference pref) {
        if (dynamicPrefs.remove(pref)) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    private void updatePreference() {
        Preference preference = findPreference(WIFI_SWITCH);
        if (preference instanceof SwitchPreference) {
            SwitchPreference connectionPref = (SwitchPreference) preference;
            boolean checked = connectionPref.isChecked();

            if (checked) {
                enableDynamicPrefs();
            } else {
                disableDynamicPrefs();
            }
        }
    }

    private void enableDynamicPrefs() {
        if (descriptionPref != null) {
            getPreferenceScreen().removePreference(descriptionPref);
        }
        if (addingPref != null) {
            getPreferenceScreen().addPreference(addingPref);
        }

        for (Preference pref : dynamicPrefs) {
            getPreferenceScreen().addPreference(pref);
        }
    }

    private void disableDynamicPrefs() {
        if (addingPref != null) {
            getPreferenceScreen().removePreference(addingPref);
        }
        if (descriptionPref != null) {
            getPreferenceScreen().addPreference(descriptionPref);
        }

        List<Preference> tobeRemove = new ArrayList<>(dynamicPrefs);
        for (Preference pref : tobeRemove) {
            removePreference(pref);
        }
    }

}
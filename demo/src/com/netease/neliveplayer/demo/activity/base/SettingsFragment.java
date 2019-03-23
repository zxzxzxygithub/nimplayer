package com.netease.neliveplayer.demo.activity.base;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import com.netease.neliveplayer.demo.BuildConfig;
import com.netease.neliveplayer.demo.R;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting_pref);



        bindPreferenceSummaryToValueString(findPreference(getString(R.string.about_nelp_demo_version_name_key)), BuildConfig.VERSION_NAME);
        bindPreferenceSummaryToValueString(findPreference(getString(R.string.about_nelp_demo_version_code_key)), BuildConfig.VERSION_CODE + "");
        bindPreferenceSummaryToValueString(findPreference(getString(R.string.about_nelp_demo_build_date_key)), BuildConfig.BUILD_DATE);
        bindPreferenceSummaryToValueString(findPreference(getString(R.string.about_nelp_demo_git_revision_key)), BuildConfig.GIT_REVISION);

    }




    private void bindPreferenceSummaryToValueString(Preference preference, String summary) {

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(summary);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(summary);
        }
    }
    private void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof SwitchPreference) {
            //ignore
        } else {
            preference.setSummary(stringValue);
        }

        return true;
    }

}

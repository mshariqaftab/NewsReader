package com.ms.newsreader.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.ms.newsreader.R;
import com.ms.newsreader.util.Constant;
import com.ms.newsreader.util.Preferences;

/**
 * Created by Mohd. Shariq on 21/03/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    SharedPreferences sharedPreferences;
    ListPreference countryListPreference;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.country_preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.country_list_preference_key));
    }


    @Override
    public void onResume() {
        super.onResume();
        //unregister the preferenceChange listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        countryListPreference = (ListPreference) findPreference(getString(R.string.country_list_preference_key));
        countryListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = countryListPreference.findIndexOfValue(newValue.toString());
                Log.d(TAG, "Country + " + countryListPreference.getEntryValues()[index]);
                Preferences.putString(Constant.NEWS_SOURCE_COUNTRY, countryListPreference.getEntryValues()[index].toString());
                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if(key.equals(getString(R.string.country_list_preference_key))) {
                countryListPreference = (ListPreference) preference;
                final int prefIndex = countryListPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
                if (prefIndex >= 0) {
                    countryListPreference.setValueIndex(prefIndex);
                    preference.setSummary(countryListPreference.getEntries()[prefIndex]);
                }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}

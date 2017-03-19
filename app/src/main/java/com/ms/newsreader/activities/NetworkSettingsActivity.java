package com.ms.newsreader.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.ms.newsreader.fragments.NetworkSettingsFragment;

public class NetworkSettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new NetworkSettingsFragment()).commit();
    }
}



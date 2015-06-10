package com.neykov.spotifystreamer.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.neykov.spotifystreamer.PreferenceConstants;
import com.neykov.spotifystreamer.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PreferenceConstants.SHARED_PREFERENCES_FILENAME);
        addPreferencesFromResource(R.xml.preferences);
    }
}

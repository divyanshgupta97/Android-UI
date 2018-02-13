package com.example.user.androidui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.string_preferences);
    }
}

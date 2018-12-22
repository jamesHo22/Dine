package com.example.dine.dine;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // associate fragment with this fragment the preferences.xml.
        // Sets the shared preference with value from fragment
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}

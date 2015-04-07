package com.example.acurguzchin.earthquake;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by acurguzchin on 07.04.15.
 */
public class UserPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userpreferences);
    }
}

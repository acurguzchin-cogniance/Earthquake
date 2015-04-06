package com.example.acurguzchin.earthquake;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * Created by acurguzchin on 06.04.15.
 */
public class PreferencesActivity extends Activity {
    public static final String USER_PREFERENCE = "USER_PREFERENCE";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG_INDEX = "PREF_MIN_MAG_INDEX";
    public static final String PREF_UPDATE_FREQ_INDEX = "PREF_UPDATE_FREQ_INDEX";

    private SharedPreferences prefs;
    
    private CheckBox autoUpdate;
    private Spinner updateFrequencySpinner;
    private Spinner magnitudeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        autoUpdate = (CheckBox) findViewById(R.id.checkbox_auto_update);
        updateFrequencySpinner = (Spinner) findViewById(R.id.spinner_update_freq);
        magnitudeSpinner = (Spinner) findViewById(R.id.spinner_quake_mag);
        
        populateSpinners();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateUiFromPreferences();

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                PreferencesActivity.this.setResult(RESULT_OK);
                finish();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesActivity.this.setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void savePreferences() {
        int updateFrequencyIndex = updateFrequencySpinner.getSelectedItemPosition();
        int magnitudeIndex = magnitudeSpinner.getSelectedItemPosition();
        boolean autoUpdateChecked = autoUpdate.isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_AUTO_UPDATE, autoUpdateChecked);
        editor.putInt(PREF_UPDATE_FREQ_INDEX, updateFrequencyIndex);
        editor.putInt(PREF_MIN_MAG_INDEX, magnitudeIndex);
        editor.commit();
    }

    private void updateUiFromPreferences() {
        autoUpdate.setChecked(prefs.getBoolean(PREF_AUTO_UPDATE, false));
        updateFrequencySpinner.setSelection(prefs.getInt(PREF_UPDATE_FREQ_INDEX, 2));
        magnitudeSpinner.setSelection(prefs.getInt(PREF_MIN_MAG_INDEX, 0));
    }

    private void populateSpinners() {
        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(this, R.array.update_freq_options, android.R.layout.simple_spinner_item);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        updateFrequencySpinner.setAdapter(frequencyAdapter);

        ArrayAdapter<CharSequence> magnitudeAdapter = ArrayAdapter.createFromResource(this, R.array.magnitude_options, android.R.layout.simple_spinner_item);
        magnitudeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        magnitudeSpinner.setAdapter(magnitudeAdapter);
    }
}

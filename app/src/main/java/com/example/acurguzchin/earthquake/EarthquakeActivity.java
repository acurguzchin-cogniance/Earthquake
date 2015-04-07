package com.example.acurguzchin.earthquake;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class EarthquakeActivity extends ActionBarActivity {
    static final private int MENU_PREFERENCES = Menu.FIRST + 1;
    static final private int MENU_UPDATE = Menu.FIRST + 2;
    private static final int SHOW_PREFERENCES = 1;

    public int minimumMagnitude = 0;
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        updateFromPreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == MENU_PREFERENCES) {
            Intent intent = new Intent(this, UserPreferenceActivity.class);
            startActivityForResult(intent, SHOW_PREFERENCES);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_PREFERENCES) {
            updateFromPreferences();

            FragmentManager fm = getFragmentManager();
            final EarthquakeListFragment earthquakeList = (EarthquakeListFragment) fm.findFragmentById(R.id.EarthquakeListFragment);
            Thread t = new Thread(new Runnable() {
                public void run() {
                    earthquakeList.refreshEarthquakes();
                }
            });
            t.start();
        }
    }

    private void updateFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        minimumMagnitude = Integer.parseInt(prefs.getString(UserPreferenceActivity.PREF_MIN_MAG, "0"));
        updateFreq = Integer.parseInt(prefs.getString(UserPreferenceActivity.PREF_UPDATE_FREQ, "60"));
        autoUpdateChecked = prefs.getBoolean(UserPreferenceActivity.PREF_AUTO_UPDATE, false);
    }
}

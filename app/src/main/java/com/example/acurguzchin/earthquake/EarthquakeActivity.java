package com.example.acurguzchin.earthquake;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class EarthquakeActivity extends Activity {
    private static final int SHOW_PREFERENCES = 1;
    private static String ACTION_BAR_INDEX = "ACTION_BAR_INDEX";

    public int minimumMagnitude = 0;
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;

    TabListener<EarthquakeListFragment> listTabListener;
    TabListener<EarthquakeMapFragment> mapTabListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        updateFromPreferences();

        if (!isTabletLayout()) {
            ActionBar actionBar = getActionBar();

            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setDisplayShowTitleEnabled(false);

            ActionBar.Tab listTab = actionBar.newTab();
            listTabListener = new TabListener<>(this, R.id.EarthquakeFragmentContainer, EarthquakeListFragment.class);
            listTab.setText("List").
                    setContentDescription("List of earthquakes").
                    setTabListener(listTabListener);
            actionBar.addTab(listTab);

            ActionBar.Tab mapTab = actionBar.newTab();
            mapTabListener = new TabListener<>(this, R.id.EarthquakeFragmentContainer, EarthquakeMapFragment.class);
            mapTab.setText("Map").
                    setContentDescription("Map of earthquakes").
                    setTabListener(mapTabListener);
            actionBar.addTab(mapTab);
        }

//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
//        SearchView searchView = (SearchView) findViewById(R.id.searchView);
//        searchView.setSearchableInfo(searchableInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                startService(new Intent(this, EarthquakeUpdateService.class));
                return true;
            case R.id.menu_preferences:
                Intent intent = new Intent(this, UserPreferenceActivity.class);
                startActivityForResult(intent, SHOW_PREFERENCES);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_PREFERENCES) {
            updateFromPreferences();
            startService(new Intent(this, EarthquakeUpdateService.class));
        }
    }

    private void updateFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        minimumMagnitude = Integer.parseInt(prefs.getString(UserPreferenceActivity.PREF_MIN_MAG, "0"));
        updateFreq = Integer.parseInt(prefs.getString(UserPreferenceActivity.PREF_UPDATE_FREQ, "60"));
        autoUpdateChecked = prefs.getBoolean(UserPreferenceActivity.PREF_AUTO_UPDATE, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!isTabletLayout()) {
            int actionBarIndex = getActionBar().getSelectedTab().getPosition();
            SharedPreferences.Editor editor = getPreferences(Activity.MODE_PRIVATE).edit();
            editor.putInt(ACTION_BAR_INDEX, actionBarIndex);
            editor.apply();

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (listTabListener != null) {
                ft.detach(listTabListener.fragment);
            }
            if (mapTabListener != null) {
                ft.detach(mapTabListener.fragment);
            }
            ft.commit();
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (!isTabletLayout()) {
            FragmentManager fragmentManager = getFragmentManager();
            listTabListener.fragment = fragmentManager.findFragmentById(R.id.EarthquakeListFragment);
            mapTabListener.fragment = fragmentManager.findFragmentById(R.id.EarthquakeMapFragment);

            SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
            int actionBarIndex = prefs.getInt(ACTION_BAR_INDEX, 0);
            getActionBar().setSelectedNavigationItem(actionBarIndex);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isTabletLayout()) {
            SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
            int actionBarIndex = prefs.getInt(ACTION_BAR_INDEX, 0);
            getActionBar().setSelectedNavigationItem(actionBarIndex);
        }
    }

    private boolean isTabletLayout() {
        View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
        return fragmentContainer == null;
    }

    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment fragment;
        private Activity activity;
        private Class<T> fragmentClass;
        private int fragmentContainer;

        public TabListener(Activity activity, int fragmentContainer, Class<T> fragmentClass) {
            this.activity = activity;
            this.fragmentContainer = fragmentContainer;
            this.fragmentClass = fragmentClass;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (fragment == null) {
                String fragmentName = fragmentClass.getName();
                fragment = Fragment.instantiate(activity, fragmentName);
                ft.add(fragmentContainer, fragment, fragmentName);
            }
            else {
                ft.attach(fragment);
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (fragment != null) {
                ft.detach(fragment);
            }
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (fragment != null) {
                ft.attach(fragment);
            }
        }
    }
}

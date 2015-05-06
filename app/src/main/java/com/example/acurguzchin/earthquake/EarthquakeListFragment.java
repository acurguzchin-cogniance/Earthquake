package com.example.acurguzchin.earthquake;

import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Date;

/**
 * Created by acurguzchin on 03.04.15.
 */
public class EarthquakeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;

    private static final String TAG = "EARTHQUAKE";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new SimpleCursorAdapter(
                getActivity(), android.R.layout.simple_list_item_1, null,
                new String[] { EarthquakeProvider.KEY_SUMMARY },
                new int[] { android.R.id.text1 },
            0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);

        refreshEarthquakes();
    }

    public void refreshEarthquakes() {
        getLoaderManager().restartLoader(0, null, EarthquakeListFragment.this);
        getActivity().startService(new Intent(getActivity(), EarthquakeUpdateService.class));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[] { EarthquakeProvider.KEY_ID, EarthquakeProvider.KEY_SUMMARY };
        EarthquakeActivity activity = (EarthquakeActivity) getActivity();
        String where = EarthquakeProvider.KEY_MAGNITUDE + " >= " + activity.minimumMagnitude;
        return new CursorLoader(getActivity(), EarthquakeProvider.CONTENT_URI, projection, where, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = ContentUris.withAppendedId(EarthquakeProvider.CONTENT_URI, id);
        Cursor result = cr.query(uri, null, null, null, null);

        if (result.moveToFirst()) {
            Date date = new Date(result.getLong(result.getColumnIndex(EarthquakeProvider.KEY_DATE)));
            String details = result.getString(result.getColumnIndex(EarthquakeProvider.KEY_DETAILS));
            double magnitude = result.getDouble(result.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE));
            String linkString = result.getString(result.getColumnIndex(EarthquakeProvider.KEY_LINK));

            double lat = result.getDouble(result.getColumnIndex(EarthquakeProvider.KEY_LOCATION_LAT));
            double lng = result.getDouble(result.getColumnIndex(EarthquakeProvider.KEY_LOCATION_LNG));
            Location location = new Location("db");
            location.setLatitude(lat);
            location.setLongitude(lng);

            Quake quake = new Quake(date, details, location, magnitude, linkString);

            DialogFragment newFragment = EarthquakeDialog.newInstance(getActivity(), quake);
            newFragment.show(getFragmentManager(), "dialog");
        }
    }
}

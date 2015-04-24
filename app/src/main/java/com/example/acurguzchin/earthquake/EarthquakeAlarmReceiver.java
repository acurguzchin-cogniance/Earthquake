package com.example.acurguzchin.earthquake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by acurguzchin on 24.04.15.
 */
public class EarthquakeAlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_REFRESH_EARTHQUAKE_ALARM = "com.example.acurguzchin.earthquake.ACTION_REFRESH_EARTHQUAKE_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, EarthquakeUpdateService.class));
    }
}

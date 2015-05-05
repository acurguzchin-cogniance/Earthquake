package com.example.acurguzchin.earthquake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by alex on 5/5/15.
 */
public class ToastsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("MSG");
    }
}

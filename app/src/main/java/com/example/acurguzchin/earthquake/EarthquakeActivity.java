package com.example.acurguzchin.earthquake;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class EarthquakeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String serviceString = Context.DOWNLOAD_SERVICE;
        DownloadManager downloadManager;
        downloadManager = (DownloadManager) getSystemService(serviceString);
        Uri uri = Uri.parse("http://developer.android.com/shareables/icon_templates-v4.0.zip");
        DownloadManager.Request request = new DownloadManager.Request(uri);
        long reference = downloadManager.enqueue(request);
    }
}

package com.example.acurguzchin.earthquake;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by acurguzchin on 23.04.15.
 */
public class EarthquakeUpdateService extends IntentService {
    public static String TAG = "EARTHQUAKE_UPDATE_SERVICE";

    private static final int NOTIFICATION_ID = 1;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private Notification.Builder notifBuilder;

    public EarthquakeUpdateService() {
        super("EarthquakeUpdateService");
    }

    public EarthquakeUpdateService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int updateFreq = Integer.parseInt(prefs.getString(UserPreferenceActivity.PREF_UPDATE_FREQ, "60"));
        boolean autoUpdateChecked = prefs.getBoolean(UserPreferenceActivity.PREF_AUTO_UPDATE, false);

        if (autoUpdateChecked) {
            long refreshPeriod = updateFreq * 60 * 1000;
            long timeToRefresh = SystemClock.elapsedRealtime() + refreshPeriod;
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeToRefresh, refreshPeriod, alarmIntent);
        }
        else {
            alarmManager.cancel(alarmIntent);
        }

        refreshEarthquakes();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentToFire = new Intent(EarthquakeAlarmReceiver.ACTION_REFRESH_EARTHQUAKE_ALARM);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);

        notifBuilder = new Notification.Builder(this);
        notifBuilder.setAutoCancel(true).setTicker("Earthquake detected").setSmallIcon(R.drawable.notification_icon);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void refreshEarthquakes() {
        Log.i(TAG, "start refreshing");
        URL url;
        try {
            String quakeFeed = getString(R.string.quake_feed);
            url = new URL(quakeFeed);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection)connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConnection.getInputStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document dom = db.parse(inputStream);
                Element docElement = dom.getDocumentElement();

                NodeList nodeList = docElement.getElementsByTagName("entry");
                if (nodeList != null && nodeList.getLength() > 0) {
                    for (int i = 0 ; i < nodeList.getLength(); i++) {
                        Element entry = (Element) nodeList.item(i);
                        Element title = (Element) entry.getElementsByTagName("title").item(0);
                        Element geo = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        if (details.equals("Data Feed Deprecated")) {
                            continue;
                        }

                        String hostname = "http://earthquake.usgs.gov";
                        String absLinkString = hostname + link.getAttribute("href");
                        String point = geo.getFirstChild().getNodeValue();
                        String date = when.getFirstChild().getNodeValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
                        Date qdate = new GregorianCalendar(0,0,0).getTime();
                        try {
                            qdate = sdf.parse(date);
                        } catch (ParseException e) {
                            Log.d(TAG, "Date parsing exception.", e);
                        }
                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));
                        String magnitudeString = details.split(" ")[1];
                        int end = magnitudeString.length() - 1;
                        double magnitude = Double.parseDouble(magnitudeString.substring(0, end));
                        details = details.split(",")[1].trim();

                        final Quake quake = new Quake(qdate, details, l, magnitude, absLinkString);

                        addNewQuake(quake);
                    }
                }
            }
        } catch (MalformedURLException e) {
            Log.d(TAG, "MalformedURLException");
        } catch (IOException e) {
            Log.d(TAG, "IOException");
        } catch (ParserConfigurationException e) {
            Log.d(TAG, "Parser Configuration Exception");
        } catch (SAXException e) {
            Log.d(TAG, "SAX Exception");
        }
        finally {
            final Context context = getApplicationContext();
            Handler handler = new Handler(context.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(context, "Earthquakes are up-to-date", Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }

    private void addNewQuake(Quake quake) {
        ContentResolver contentResolver = getContentResolver();

        String where = EarthquakeProvider.KEY_DATE + "=" + quake.getDate().getTime();
        Cursor cursor = contentResolver.query(EarthquakeProvider.CONTENT_URI, null, where, null, null);
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(EarthquakeProvider.KEY_DATE, quake.getDate().getTime());
            values.put(EarthquakeProvider.KEY_DETAILS, quake.getDetails());
            values.put(EarthquakeProvider.KEY_SUMMARY, quake.toString());

            double lat = quake.getLocation().getLatitude();
            double lng = quake.getLocation().getLongitude();
            values.put(EarthquakeProvider.KEY_LOCATION_LAT, lat);
            values.put(EarthquakeProvider.KEY_LOCATION_LNG, lng);
            values.put(EarthquakeProvider.KEY_LINK, quake.getLink());
            values.put(EarthquakeProvider.KEY_MAGNITUDE, quake.getMagnitude());

            contentResolver.insert(EarthquakeProvider.CONTENT_URI, values);


        }
        broadcastNotification(quake);
        cursor.close();
    }

    private void broadcastNotification(Quake quake) {
        Intent startActivityIntent = new Intent(this, EarthquakeActivity.class);
        PendingIntent launchIntent = PendingIntent.getActivity(this, 0, startActivityIntent, 0);

        notifBuilder.setContentIntent(launchIntent).
                setWhen(quake.getDate().getTime()).
                setContentTitle("M: " + quake.getMagnitude()).
                setContentText(quake.getDetails());

        Log.i(TAG, quake.getDetails());

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(NOTIFICATION_ID, notifBuilder.getNotification());
    }
}

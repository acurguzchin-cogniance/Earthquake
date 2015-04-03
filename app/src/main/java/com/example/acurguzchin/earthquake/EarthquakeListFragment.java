package com.example.acurguzchin.earthquake;

import android.app.ListFragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by acurguzchin on 03.04.15.
 */
public class EarthquakeListFragment extends ListFragment {
    private ArrayList<Quake> quakes = new ArrayList<>();
    private ArrayAdapter<Quake> aa;

    private static final String TAG = "EARCHQUAKE";
    private Handler handler = new Handler();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        aa = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, quakes);
        setListAdapter(aa);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                refreshEarthquakes();
            }
        });
        thread.start();
    }

    public void refreshEarthquakes() {
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

                quakes.clear();

                NodeList nodeList = docElement.getElementsByTagName("entry");
                if (nodeList != null && nodeList.getLength() > 0) {
                    for (int i = 0 ; i < nodeList.getLength(); i++) {
                        Element entry = (Element) nodeList.item(i);
                        Element title = (Element) entry.getElementsByTagName("title").item(0);
                        Element geo = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);
                        String details = title.getFirstChild().getNodeValue();
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
                        handler.post(new Runnable() {
                            public void run() {
                                addNewQuake(quake);
                            }
                        });
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
        }
    }
    private void addNewQuake(Quake quake) {
        quakes.add(quake);
        aa.notifyDataSetChanged();
    }
}

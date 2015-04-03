package com.example.acurguzchin.earthquake;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by acurguzchin on 03.04.15.
 */
public class Quake {
    private Date date;
    private String detals;
    private Location location;
    private double magnitude;
    private String link;

    public Quake(Date date, String detals, Location location, double magnitude, String link) {
        this.date = date;
        this.detals = detals;
        this.location = location;
        this.magnitude = magnitude;
        this.link = link;
    }

    public Date getDate() {
        return date;
    }

    public String getDetals() {
        return detals;
    }

    public Location getLocation() {
        return location;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(date) + ": " + magnitude + " " + detals;
    }
}

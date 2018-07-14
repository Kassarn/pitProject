package com.example.ivan.pitproject;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ivan on 9.4.2018 г..
 */

public class HoleSize extends Application {
    String size;
    String depth;
    String lattitude;
    String longitude;
    LatLng latLng;

    public LatLng getLatLng() {
        return latLng;
    }

    public HoleSize() {

    }

    public HoleSize(String size,String depth, LatLng latLng) {
        this.size = size;
        this.depth =depth;
        this.latLng = latLng;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }

    public String getLattitude() {
        return lattitude;
    }

    public void setLattitude(String lattitude) {
        this.lattitude = lattitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}

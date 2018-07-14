package com.example.ivan.pitproject;

import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

/**
 * Created by Ivan on 19.4.2018 г..
 */

public class SortMarkers implements Comparator<HoleSize> {
    LatLng currentLocation;

    public SortMarkers(LatLng current) {
        this.currentLocation = current;
    }





    @Override
    public int compare(final HoleSize hole1, final HoleSize hole2) {
        double lat1 = hole1.getLatLng().latitude;
        double lon1 = hole1.getLatLng().longitude;
        double lat2 = hole2.getLatLng().latitude;
        double lon2 = hole2.getLatLng().longitude;


        double distanceToPlace1 = distance(currentLocation.latitude, currentLocation.longitude, lat1, lon1);
        double distanceToPlace2 = distance(currentLocation.latitude, currentLocation.longitude, lat2, lon2);
        return (int) (distanceToPlace1 - distanceToPlace2);
    }


    public double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon / 2), 2)));
        return radius * angle;
    }

}

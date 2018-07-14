package com.example.ivan.pitproject;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.ivan.pitproject.MapActivity.activityA;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    static MapActivity activityA;
    double meters;
    String size;
    String depth;
    TextView infoView;
    TextView sizeView;
    TextView depthView;
    public List<HoleSize> markers = new ArrayList<>();

    private static final String TAG = "MapActivity";
    private static final float DEFAULT_ZOOM = 15f;
    LocationManager locManager;
    private static final int REQUEST_LOCATION = 1;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    @BindView(R.id.pop)
    Button button;
    @BindView(R.id.landpop)
    Button button1;

    DataBaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_screen);

        button = (Button) findViewById(R.id.pop);
        button1 = (Button) findViewById(R.id.landpop);
        initMap();

        myDB = new DataBaseHelper(this);
        ButterKnife.bind(this);


        activityA = this;
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            infoView = (TextView) findViewById(R.id.info);
            sizeView = (TextView) findViewById(R.id.size);
            depthView = (TextView) findViewById(R.id.depth);
            Cursor cursor = myDB.getAllData();
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    double ltd = Double.valueOf(cursor.getString(3));
                    double lng = Double.valueOf(cursor.getString(4));
                    size = cursor.getString(1);
                    depth = cursor.getString(2);
                    LatLng hole = new LatLng(ltd, lng);
                    markers.add(new HoleSize(size, depth, hole));
                }


                Collections.sort(markers, new SortMarkers(getDeviceLocation()));
                String size1 = "";
                String depth1 = "";
                switch (markers.get(0).getSize()) {
                    case "1":
                        size = "Малка";
                        break;
                    case "2":
                        size = "Средна";
                        break;
                    case "3":
                        size = "Голяма";
                        break;
                }

                switch (markers.get(0).getDepth()) {
                    case "1":
                        depth = "Плитка";
                        break;
                    case "2":
                        depth = "Средно-Дълбока";
                        break;
                    case "3":
                        depth = "Дълбока";
                        break;
                }
                meters = SphericalUtil.computeDistanceBetween(getDeviceLocation(), markers.get(0).getLatLng());
                infoView.setText("Разстояние до следваща дупка: " + String.format("%.2f М", meters));
                sizeView.setText("Големина на дупката: " + size1);
                depthView.setText("Дълбочина на дупката: \n" + depth1);
            }
        } else {
            buildAlertMessageNoGps();

        }


        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    Cursor res = myDB.getAllData();
                    mMap.clear();
                    while (res.moveToNext()) {

                        double ltd = Double.valueOf(res.getString(3));
                        double lng = Double.valueOf(res.getString(4));
                        LatLng hole = new LatLng(ltd, lng);
                        String holeType = getHoleType(res.getString(1), res.getString(2));
                        BitmapDescriptor markerColour = getMarkerColor(res.getString(1));
                        Marker marker = mMap.addMarker(new MarkerOptions().position(hole)
                                .title(res.getString(1)).visible(false).title(holeType)
                                .icon(markerColour));

                        if (SphericalUtil.computeDistanceBetween(getDeviceLocation(), marker.getPosition()) < 400) {
                            marker.setVisible(true);

                        }
                    }
                    Cursor cursor1 = myDB.getAllData();
                    markers.clear();
                    while (cursor1.moveToNext()) {
                        double ltd = Double.valueOf(cursor1.getString(3));
                        double lng = Double.valueOf(cursor1.getString(4));
                        size = cursor1.getString(1);
                        depth = cursor1.getString(2);
                        LatLng hole = new LatLng(ltd, lng);
                        markers.add(new HoleSize(size, depth, hole));
                    }
                    if (cursor1.getCount() != 0) {
                        Collections.sort(markers, new SortMarkers(getDeviceLocation()));

                        String size = "";
                        String depth = "";
                        switch (markers.get(0).getSize()) {
                            case "1":
                                size = "Малка";
                                break;
                            case "2":
                                size = "Средна";
                                break;
                            case "3":
                                size = "Голяма";
                                break;
                        }

                        switch (markers.get(0).getDepth()) {
                            case "1":
                                depth = "Плитка";
                                break;
                            case "2":
                                depth = "Средно-Дълбока";
                                break;
                            case "3":
                                depth = "Дълбока";
                                break;
                        }
                        meters = SphericalUtil.computeDistanceBetween(getDeviceLocation(), markers.get(0).getLatLng());
                        infoView.setText("Разстояние до следваща дупка: " + String.format("%.2f М", meters));
                        sizeView.setText("Големина на дупката: " + size);
                        depthView.setText("Дълбочина на дупката: \n" + depth);

                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } else if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {


                    Cursor res = myDB.getAllData();
                    mMap.clear();
                    while (res.moveToNext()) {

                        double ltd = Double.valueOf(res.getString(3));
                        double lng = Double.valueOf(res.getString(4));
                        LatLng hole = new LatLng(ltd, lng);
                        String holeType = getHoleType(res.getString(1), res.getString(2));
                        BitmapDescriptor markerColour = getMarkerColor(res.getString(1));
                        Marker marker = mMap.addMarker(new MarkerOptions().position(hole)
                                .title(res.getString(1)).visible(false).title(holeType)
                                .icon(markerColour));

                        if (SphericalUtil.computeDistanceBetween(getDeviceLocation(), marker.getPosition()) < 400) {
                            marker.setVisible(true);

                        }
                    }


                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }


    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        finish();
                        startActivity(getIntent());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @OnClick(R.id.pop)
    public void popButton() {
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        } else {
            Intent intent = new Intent(MapActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.landpop)
    public void popLandButton() {
        Intent intent = new Intent(MapActivity.this, Pop.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
       // Log.d(TAG, "onMapReady: map is ready");

        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        final Cursor res = myDB.getAllData();
        if (res.getCount() == 0) {
            //show message
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
            moveCamera(getDeviceLocation(), DEFAULT_ZOOM);
        }


        while (res.moveToNext()) {

            double ltd = Double.valueOf(res.getString(3));
            double lng = Double.valueOf(res.getString(4));
            LatLng hole = new LatLng(ltd, lng);
            BitmapDescriptor markerColour = getMarkerColor(res.getString(1));
            String holeType = getHoleType(res.getString(1), res.getString(2));
            Marker marker = googleMap.addMarker(new MarkerOptions().position(hole)
                    .title(res.getString(1)).visible(false).title(holeType).icon(markerColour));

            moveCamera(getDeviceLocation(), DEFAULT_ZOOM);


            if (SphericalUtil.computeDistanceBetween(getDeviceLocation(), marker.getPosition()) < 400) {
                marker.setVisible(true);
            }

        }


    }

    BitmapDescriptor getMarkerColor(String hole) {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.defaultMarker();
        switch (hole) {
            case "1":
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                break;
            case "2":
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                break;
            case "3":
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                break;
            default:
                break;
        }
        return bitmapDescriptor;
    }

    String getHoleType(String size, String depth) {
        String holeDescription;
        switch (size) {
            case "1":
                switch (depth) {
                    case "1":
                        holeDescription = "Малка плитка";
                        break;
                    case "2":
                        holeDescription = "Малка-средно дълбока";
                        break;
                    case "3":
                        holeDescription = "Малка дълбока";
                        break;
                    default:
                        holeDescription = "Грешно име";
                        break;

                }
                break;
            case "2":
                switch (depth) {
                    case "1":
                        holeDescription = "Средна плитка";
                        break;
                    case "2":
                        holeDescription = "Средна-средно дълбока";
                        break;
                    case "3":
                        holeDescription = "Средна дълбока";
                        break;
                    default:
                        holeDescription = "Грешно име";
                        break;

                }
                break;
            case "3":
                switch (depth) {
                    case "1":
                        holeDescription = "Голяма плитка";
                        break;
                    case "2":
                        holeDescription = "Голяма-средно дълбока";
                        break;
                    case "3":
                        holeDescription = "Голяма дълбока";
                        break;
                    default:
                        holeDescription = "Грешно име";
                        break;

                }
                break;
            default:
                holeDescription = "Грешно име";
                break;
        }
        return holeDescription;
    }

    public LatLng getDeviceLocation() {
        double latti = 27;
        double longi = 42;
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {

                latti = location.getLatitude();
                longi = location.getLongitude();

            } else if (location1 != null) {

                latti = location1.getLatitude();
                longi = location1.getLongitude();

            } else if (location2 != null) {
                latti = location2.getLatitude();
                longi = location2.getLongitude();

            }
        }

        return new LatLng(latti, longi);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

}
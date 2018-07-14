package com.example.ivan.pitproject;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import org.json.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
        MapActivity mapActivity = new MapActivity();
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public double area;
    String JSON_STRING;
    double meters;
    String size;
    String depth;
    public List<HoleSize>  markers =  new ArrayList<>();
    public  JSONObject postData = new JSONObject();

   DataBaseHelper myDB;
    private static final int REQUEST_LOCATION = 1;
    TextView infoView;
    TextView sizeView;
    TextView depthView;


            LocationManager locationManager;
    String unique_id;
    @BindView(R.id.get_xy) Button button;
    @BindView(R.id.close)Button close;
    @BindView(R.id.small) Button small;
    @BindView(R.id.medium) Button medium;
    @BindView(R.id.large) Button large;

    @BindView(R.id.small_shallow) Button small_shallow;
    @BindView(R.id.medium_shallow) Button medium_shallow;
    @BindView(R.id.large_shallow) Button large_shallow;

    @BindView(R.id.viewdata) Button showData;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        infoView = (TextView) findViewById(R.id.info);
       sizeView = (TextView) findViewById(R.id.size);
       depthView = (TextView) findViewById(R.id.depth);


        button = (Button) findViewById(R.id.get_xy);
        ButterKnife.bind(this);
        myDB = new DataBaseHelper(this);





        Cursor cursor = myDB.getAllData();
        while (cursor.moveToNext()) {
            double ltd = Double.valueOf(cursor.getString(3));
            double lng = Double.valueOf(cursor.getString(4));
            size = cursor.getString(1);
            depth = cursor.getString(2);
            LatLng hole = new LatLng(ltd, lng);
            markers.add(new HoleSize(size,depth, hole));
        }

        if (cursor.getCount() != 0) {
            Collections.sort(markers, new SortMarkers(getDevLoc()));
            meters = SphericalUtil.computeDistanceBetween(getDevLoc(), markers.get(0).getLatLng());
           String size1="";
           String depth2 ="";
            switch (markers.get(0).getSize()){
                case"1":
                    size ="Малка";
                    break;
                case"2":
                    size ="Средна";
                    break;
                case"3":
                    size ="Голяма";
                    break;
            }

            switch (markers.get(0).getDepth()){
                case"1":
                    depth ="Плитка";
                    break;
                case"2":
                    depth ="Средно-Дълбока";
                    break;
                case"3":
                    depth ="Дълбока";
                    break;
            }
            infoView.setText("Разстояние до следваща дупка: "+String.format("%.2f М",meters) );
            sizeView.setText("Големина на дупката: "+size1);
            depthView.setText("Дълбочина на дупката: \n"+depth2);
        }



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                 Cursor cursor1 = myDB.getAllData();
                    markers.clear();
                    while (cursor1.moveToNext()) {
                        double ltd = Double.valueOf(cursor1.getString(3));
                        double lng = Double.valueOf(cursor1.getString(4));
                        size = cursor1.getString(1);
                        depth = cursor1.getString(2);
                        LatLng hole = new LatLng(ltd, lng);
                        markers.add(new HoleSize(size,depth, hole));
                    }
                    if (cursor1.getCount() != 0) {
                        Collections.sort(markers, new SortMarkers(getDevLoc()));

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
                        meters = SphericalUtil.computeDistanceBetween(getDevLoc(), markers.get(0).getLatLng());
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
        }
    }



        @OnClick(R.id.close)
        public void closeButton() {
            finish();


        }



    private boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }




    @OnClick(R.id.viewdata)
    public void ViewData() {
        Cursor res = myDB.getAllData();

        if(res.getCount() == 0) {
           //show message
            showMessage("Error","No data found");
            return;
        }
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append("ID :" + res.getString(0)+"\n");
            buffer.append("Name :" + res.getString(1)+"\n");
            buffer.append("DEPTH :" + res.getString(2)+"\n");
            buffer.append("LATTITUDE:" + res.getString(3)+"\n");
            buffer.append("LONGITUDE :" + res.getString(4)+"\n\n");
        }
        // show all data
        showMessage("Data",buffer.toString());
    }

    public void showMessage(String title,String message) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    @OnClick(R.id.small)
    public  void smallClick () {

        ((HoleSize) this.getApplication()).setSize("1");
    }
    @OnClick(R.id.medium)
    public  void  mediumClick () {
        ((HoleSize) this.getApplication()).setSize("2");
    }
    @OnClick(R.id.large)
    public  void largeClick () {
        ((HoleSize) this.getApplication()).setSize("3");
    }
    @OnClick(R.id.small_shallow)
    public  void smallShallowClick () {
        if (((HoleSize) this.getApplication()).getSize() == null) {
            Toast.makeText(this.getApplicationContext(), "First choose size", Toast.LENGTH_SHORT).show();
        } else {
            ((HoleSize) this.getApplication()).setDepth("1");
       /*    if(  String.valueOf(((HoleSize) this.getApplication()).getSize()).equalsIgnoreCase("small")) {
                ((HoleSize) this.getApplication()).setSize("small_shallow");
            } else if ( ((HoleSize) this.getApplication()).getSize().equalsIgnoreCase("medium")) {
                ((HoleSize) this.getApplication()).setSize("medium_shallow");
            } else if ( ((HoleSize) this.getApplication()).getSize().equalsIgnoreCase("large")) {
                ((HoleSize) this.getApplication()).setSize("large_shallow");
            }
        }*/

        }
    }
    @OnClick(R.id.medium_shallow)
    public void  mediumShallowClick() {
        if( ((HoleSize) this.getApplication()).getSize()== null)  {
            Toast.makeText(this.getApplicationContext(),"First choose size",Toast.LENGTH_SHORT).show();
        } else {
           ((HoleSize) this.getApplication()).setDepth("2");
         /*   if( ((HoleSize) this.getApplication()).getSize().equalsIgnoreCase("small")) {
                ((HoleSize) this.getApplication()).setSize("small_medium shallow");
            } else if ( ((HoleSize) this.getApplication()).getSize().equalsIgnoreCase("medium")) {
                ((HoleSize) this.getApplication()).setSize("medium_medium shallow");
            } else if ( ((HoleSize) this.getApplication()).getSize().equalsIgnoreCase("large")) {
                ((HoleSize) this.getApplication()).setSize("large_medium shallow");
            }*/
        }
    }
    @OnClick(R.id.large_shallow)
    public void largeShallowClick() {
        if( ((HoleSize) this.getApplication()).getSize()== null)  {
            Toast.makeText(this.getApplicationContext(),"First choose size",Toast.LENGTH_SHORT).show();
        } else {
          ((HoleSize) this.getApplication()).setDepth("3");
          /* if( ((HoleSize) this.getApplication()).getSize().equalsIgnoreCase("small")) {
                ((HoleSize) this.getApplication()).setSize("small_large shallow");
            } else if ( ((HoleSize) this.getApplication()).getSize().equalsIgnoreCase("medium")) {
                ((HoleSize) this.getApplication()).setSize("medium_large shallow");
            } else if ( ((HoleSize) this.getApplication()).getSize().equalsIgnoreCase("large")) {
                ((HoleSize) this.getApplication()).setSize("large_large shallow");
            }*/
        }
    }




    @OnClick(R.id.get_xy)
    public void onButtonClick() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
            if(((HoleSize) this.getApplication()).getSize() == null  ||  ((HoleSize) this.getApplication()).getDepth() ==null) {
                Toast.makeText(this.getApplicationContext(),"Choose size and depth.",Toast.LENGTH_SHORT).show();
            } else {
                sendData();
               new RecieveData().execute();
                ((HoleSize)this.getApplication()).setSize(null);
                ((HoleSize)this.getApplication()).setDepth(null);

            }
        }



    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {

                double latti = location.getLatitude();
                double longi = location.getLongitude();
                ((HoleSize)this.getApplication()).setLattitude(String.valueOf(latti));
                ((HoleSize)this.getApplication()).setLongitude(String.valueOf(longi));
                unique_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);

            } else if (location1 != null) {

                double latti = location1.getLatitude();
                double longi = location1.getLongitude();
                ((HoleSize)this.getApplication()).setLattitude(String.valueOf(latti));
                ((HoleSize)this.getApplication()).setLongitude(String.valueOf(longi));
                unique_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);

            } else if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                ((HoleSize)this.getApplication()).setLattitude(String.valueOf(latti));
                ((HoleSize)this.getApplication()).setLongitude(String.valueOf(longi));
                unique_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
            } else {
                Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show();
            }
        }





    }

    private void sendData() {
        try {


            JSONObject locationObject = new JSONObject();
            postData.put("deviceID", unique_id);
            postData.put("depth",((HoleSize)this.getApplication()).getDepth());
            postData.put("location",locationObject);
            locationObject.put("xCord", ((HoleSize)this.getApplication()).getLattitude());
            locationObject.put("yCord", ((HoleSize)this.getApplication()).getLongitude());
            postData.put("size",((HoleSize)this.getApplication()).getSize());
            String url = "http://viraltest.eu/holes/?action=addNew&hole="+postData.toString();

            System.out.println(postData.toString());
            new SendData().execute(url,url);



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


        public double calculateHoleArea(DataBaseHelper db) {
            double r;
            double totalSize = 0;
            Cursor cursor = db.getAllData();
            while (cursor.moveToNext()) {
                String size = cursor.getString(1);
                switch (size) {
                    case "1":
                        r = 10;
                        totalSize += Math.PI * (Math.pow(r, 2));
                        break;
                    case "2":
                        r = 20;
                        totalSize += Math.PI * (Math.pow(r, 2));
                        break;
                    case "3":
                        r = 30;
                        totalSize += Math.PI * (Math.pow(r, 2));
                        break;
                    default:
                        break;
                }

            }
            return totalSize;
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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


       class RecieveData  extends AsyncTask<Void, Void, String> {
           ProgressDialog m_Dialog = new ProgressDialog(MainActivity.this);
           String JSON_URL;
           @Override
           protected void onPreExecute()
           {

               m_Dialog.setMessage("Please wait ...");
               m_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
               m_Dialog.setCancelable(false);
               m_Dialog.show();
               JSON_URL ="http://viraltest.eu/holes/?action=listing";
           }

           @Override
           protected String doInBackground(Void... voids) {
               try {
                   StringBuilder JSON_DATA = new StringBuilder();
                   URL url = new URL(JSON_URL);
                   HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                   httpURLConnection.setRequestMethod("GET");
                   InputStream in = httpURLConnection.getInputStream();
                   BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                   while ((JSON_STRING = reader.readLine())!=null) {
                       JSON_DATA.append(JSON_STRING).append("\n");
                   }
                   return JSON_DATA.toString().trim();
               } catch (Exception e) {
                   e.printStackTrace();
               }
               return null;
           }
           @Override
           protected void onPostExecute(String result) {

               try {

                     JSONArray myJson = new JSONArray(result);
                     boolean isInserted = false;
                   for (int i = 0; i <myJson.length() ; i++) {

                       try {
                           JSONObject oneObject = myJson.getJSONObject(i);
                           JSONObject location = myJson.getJSONObject(i).getJSONObject("location");

                           String size= oneObject.getString("size");
                           String depth= oneObject.getString("depth");
                           String xCord = location.getString("xCord");
                           String yCord = location.getString("yCord");

                          if(insertData(size,depth,xCord,yCord)){
                              isInserted =true;
                          }
                       } catch (JSONException e) {
                           // Oops
                       }
                   }
                   if(isInserted){
                       Toast.makeText(MainActivity.this,"Data Inserted",Toast.LENGTH_SHORT).show();
                   } else {
                       Toast.makeText(MainActivity.this,"Data is not Inserted",Toast.LENGTH_SHORT).show();
                   }
                m_Dialog.dismiss();
               } catch (JSONException e) {
                   e.printStackTrace();
               }


           }

           @Override
           protected void onProgressUpdate(Void... values) {
               super.onProgressUpdate(values);


           }
       }

       public boolean insertData(String size,String depth,String xCord,String yCord){
           boolean isInserted =   myDB.insertData(size,depth,xCord,yCord);
           if(isInserted) {
               return true;
           }
           return false;
       }

       public LatLng getDevLoc(){
           double latti = 27;
           double longi = 42;
           locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
           if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                   != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                   (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

               ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

           } else {
               Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

               Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

               Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

               if (location != null) {

                   latti = location.getLatitude();
                   longi = location.getLongitude();

               } else if (location1 != null) {

                   latti = location1.getLatitude();
                   longi = location1.getLongitude();

               } else if (location2 != null) {
                   latti = location2.getLatitude();
                   longi = location2.getLongitude();

               } else {
                   Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show();
               }
           }

           return new LatLng(latti, longi);
       }


}



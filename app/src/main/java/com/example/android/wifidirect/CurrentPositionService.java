package com.example.android.wifidirect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;

/**
 * Created by ergyspuka on 11/05/2017.
 */

public class CurrentPositionService extends Service implements LocationListener, GpsStatus.Listener{


    private final IBinder mBinder = new CurrentPositionServiceBinder();


    private Context mContext;
    private static final String TAG = CurrentPositionService.class.getSimpleName();

    // flag for GPS status
    boolean isGPSEnabled = false;


    private Location location; // location
    private Iterable<GpsSatellite> satellites;

    private double latitude = -1; // latitude
    private double longitude = -1; // longitude


    public void setLocation(Location location) {
        this.location = location;
    }


    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meter

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 800; // 0.8 seconds

    // Declaring a Location Manager
    protected LocationManager locationManager;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(getBaseContext(), "The CurrentPosition Service is running ...", Toast.LENGTH_SHORT).show();

        mContext = this.getApplicationContext();
        getLocation();
        return START_STICKY;
    }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            locationManager.addGpsStatusListener(this);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                //System.out.println("The GPS system is enabled");
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                //Log.d("Android Location:", "GPS enabled");
                if (locationManager != null) {
                    setLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        //System.out.println("isGPSEnabled --- latitude: " + latitude + ", longitude: " + longitude);
                    }
                }
            } else {
                System.out.println("The GPS system is not enabled");
            }
        } catch (SecurityException e) {
            Log.d("Android Location:", "Security exception: " + e.getMessage());
        } catch (Exception e) {
            Log.d("Android Location:", "General exception: " + e.getMessage());
        }

        return location;
    }



    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed in the CurrentPositionService!");

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        Log.d(TAG, "latitude = " + latitude + ", longitude = " + longitude);
    }


    @Override
    public void onGpsStatusChanged(int event) {
        Log.d(TAG, "CurrentPositionService - onGpsStatusChanged ...");
        satellites = null;

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            GpsStatus gpsStatus = locationManager.getGpsStatus(null);
            if(gpsStatus != null) {
                satellites = gpsStatus.getSatellites();
                Iterator<GpsSatellite> sat = satellites.iterator();
                int isatellites = 0;
                int satellitesInFix = 0;
                String lSatellites = null;
                int i = 0;
                while (sat.hasNext()) {

                    GpsSatellite satellite = sat.next();

                    if(satellite.usedInFix()) {
                        satellitesInFix++;
                    }
                    isatellites++;

                    /*
                    lSatellites = "Satellite" + (i++) + ": "
                            + satellite.getPrn() + ","
                            + satellite.usedInFix() + ","
                            + satellite.getSnr() + ","
                            + satellite.getAzimuth() + ","
                            + satellite.getElevation()+ "\n\n";

                    Log.d("SATELLITE",lSatellites);
                    Log.i(TAG, isatellites + " Used In Last Fix ("+satellitesInFix+")");
                    */
                }
                //Log.i(TAG, isatellites + " Used In Last Fix ("+satellitesInFix+")");
            }

        }
        else{
            Toast.makeText(getBaseContext(), "Location permissions should be enabled ...", Toast.LENGTH_SHORT).show();
        }

    }



    public Iterable<GpsSatellite> getCurrentSatellites(){
        return this.satellites;
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




    @Override
    public void onDestroy() {
        Toast.makeText(getBaseContext(), "The CurrentPosition Service is stopped ...", Toast.LENGTH_SHORT).show();

        locationManager.removeUpdates(this);
        //this.stopSelf();
        //super.onDestroy();
    }


    public IBinder onBind(Intent intent) {
        return mBinder;
    }




    public class CurrentPositionServiceBinder extends Binder {
        public CurrentPositionService getService() {
            return CurrentPositionService.this;
        }
    }

}

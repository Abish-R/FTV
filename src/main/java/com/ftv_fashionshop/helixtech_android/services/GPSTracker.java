package com.ftv_fashionshop.helixtech_android.services;

/**
 * Created by Aman Agrawal on 23/06/2016.
 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.ftv_fashionshop.helixtech_android.AndroidGPSTrackingActivity;
import com.ftv_fashionshop.helixtech_android.SignUpActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;

public class GPSTracker extends Service implements LocationListener {
    private static final String TAG = "LocationAddress";
    String locality;
    String postalCode;
    String city;
    String state;
    String country;
    String result = null;

    AndroidGPSTrackingActivity.LocationReceiver locationReceiver;
    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context, AndroidGPSTrackingActivity.LocationReceiver locationReceiver) {
        this.mContext = context;
        this.locationReceiver=locationReceiver;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                locationReceiver.GPSOFF();
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
/*                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling

                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                        }*/
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
                else {
                    locationReceiver.GPSOFF();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void getAddressFromLocation(final double latitude, final double longitude,
                                       final Context context, final SignUpActivity.GeocoderHandler handler) {

//        Thread thread = new Thread() {
//            @Override
//            public void run() {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addressList = geocoder.getFromLocation(
                    latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }
                locationReceiver.onLocationReceived(address);
                locality = (address.getAddressLine(0) + " " + address.getAddressLine(1));
                postalCode = (address.getPostalCode());
                city = (address.getSubAdminArea());
                state = (address.getAdminArea());
                country = (address.getCountryName());

                sb.append(address.getLocality()).append("\n");
                sb.append(address.getPostalCode()).append("\n");
                sb.append(address.getCountryName());
                result = sb.toString();
                Message message = Message.obtain();
                message.setTarget(handler);

                if (result != null) {
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    result = "Locality:\n" + locality + "Postal Code:\n " + postalCode + " Country:\n" + country + " City:\n" + city + "State:\n" + state;
                    bundle.putString("address", result);
                    message.setData(bundle);

                } else {
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    result = "Latitude: " + latitude + " Longitude: " + longitude +
                            "\n Unable to get address for this lat-long.";
                    bundle.putString("address", result);
                    message.setData(bundle);
                }
                message.sendToTarget();

            }
        } catch (IOException e) {
            Log.e(TAG, "Unable connect to Geocoder", e);
        }
    }

    public void getAddressFromLocation(final double latitude, final double longitude,
                                       final Context context, final AndroidGPSTrackingActivity.GeocoderHandler handler) {

//        Thread thread = new Thread() {
//            @Override
//            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)).append("\n");
                        }
                        locationReceiver.onLocationReceived(address);
                        locality = (address.getAddressLine(0) + " " + address.getAddressLine(1));
                        postalCode = (address.getPostalCode());
                        city = (address.getSubAdminArea());
                        state = (address.getAdminArea());
                        country = (address.getCountryName());

                        sb.append(address.getLocality()).append("\n");
                        sb.append(address.getPostalCode()).append("\n");
                        sb.append(address.getCountryName());
                        result = sb.toString();
                        Message message = Message.obtain();
                        message.setTarget(handler);

                        if (result != null) {
                            message.what = 1;
                            Bundle bundle = new Bundle();
                            result = "Locality:\n" + locality + "Postal Code:\n " + postalCode + " Country:\n" + country + " City:\n" + city + "State:\n" + state;
                            bundle.putString("address", result);
                            message.setData(bundle);

                        } else {
                            message.what = 1;
                            Bundle bundle = new Bundle();
                            result = "Latitude: " + latitude + " Longitude: " + longitude +
                                    "\n Unable to get address for this lat-long.";
                            bundle.putString("address", result);
                            message.setData(bundle);
                        }
                        message.sendToTarget();

                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                }
            }
//        };
//
//        thread.start();
//
//    }
    public String getLocality() {
        return locality;
    }
}


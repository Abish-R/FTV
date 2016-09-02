package com.ftv_fashionshop.helixtech_android;


/**
 * Created by  : Aman Agrawal
 * Created on  : 23/06/2016.
 * Verified by:
 * Verified on:
 * This activity is used to get the location of the user via GPS service of the mobile.
 * The GPS returns the latitude and longitude of the location and the address is retrieved by applying reverse geocoding to it in the GPSTravker class.
 * This class is also used to inflate the various address fields with the appropriate data using the data passed via interfaces from GPSTracker class.
 */

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.services.GPSTracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.Constants;
import app.UrlConstants;
import app.VolleySingleton;
import helper.MyCustomProgressDialog;

public class AndroidGPSTrackingActivity extends AppCompatActivity {


    private static final int LAST_LOCATION_REQUEST = 4123;
    Button btnShowLocation;
    Button btnShowAddress;
    Button submit;
    TextView tvAddress;
    String stname1, city1, state1, country1, pin1;
    EditText stname;
    EditText city;
    EditText country;
    private static final String MY_PREFS_NAME = "USER";
    EditText pin;
    EditText state;
    RelativeLayout second;
    ProgressDialog dialog;
    // GPSTracker class
    GPSTracker gps;

    LocationReceiver locationReceiver = new LocationReceiver() {
        /**Used to set address into various edit text fields by getting address via interfaces from GPS Tracker activity.
         * @param address
         */
        @Override
        public void onLocationReceived(Address address) {
            dialog.hide();
            stname.setText(address.getAddressLine(0) + " " + address.getAddressLine(1));
            city.setText(address.getSubAdminArea());
            state.setText(address.getAdminArea());
            country.setText(address.getCountryName());
            pin.setText(address.getPostalCode());


        }

        /**
         * Used to show alert to enable GPS on device if it is switched off.
         */
        @Override
        public void GPSOFF() {
            dialog.hide();
            showSettingsAlert();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_location);

        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setInverseBackgroundForced(false);

        tvAddress = (TextView) findViewById(R.id.tvAddress);
        stname = (EditText) findViewById(R.id.stname);
        state = (EditText) findViewById(R.id.state);
        city = (EditText) findViewById(R.id.city);
        country = (EditText) findViewById(R.id.country);
        pin = (EditText) findViewById(R.id.pin);
        second = (RelativeLayout) findViewById(R.id.second);
        submit = (Button) findViewById(R.id.submit);

        SharedPreferences editor = AndroidGPSTrackingActivity.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
       /* editor.putString("street", stname);
        editor.putString("city", city);
        editor.putString("country", country);
        editor.putString("pincode", pin);
        editor.putString("state", state);*/

        stname.setText(   editor.getString("street", ""));
        state.setText(editor.getString("state",""));
        country.setText(editor.getString("country",""));
        pin.setText(editor.getString("pincode",""));
        city.setText(editor.getString("city",""));



        // show location button click event

        btnShowAddress = (Button) findViewById(R.id.btnShowAddress);
        btnShowAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                dialog.show();

                startGPSClass();
                //Toast.makeText(getApplicationContext(),gps.getLocality(),Toast.LENGTH_SHORT).show();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                stname1 = stname.getText().toString();
                city1 = city.getText().toString();
                state1 = state.getText().toString();
                country1 = country.getText().toString();
                pin1 = pin.getText().toString();
                if (pin1.length() != 6) {
                    Toast.makeText(getApplicationContext(), "Postal Code lenght must be 6 characters", Toast.LENGTH_SHORT).show();
                } else if ((city1.equals("")) && (country1.equals("")) && (pin1.equals("")) && (stname1.equals("")) && (state1.equals(""))) {
                    Toast.makeText(getApplicationContext(), "Address fields cannot be left empty", Toast.LENGTH_SHORT).show();
                    // new AsyncSignup(AndroidGPSTrackingActivity.this).execute(String.valueOf(Constants.designer_id), fname1, lname1, username, password1, email1, mobNo1, gender, selectedText, stname1, city1, state1, country1, pin1);
                } else if ((!stname1.equals("")) && (!city1.equals("")) && (!country1.equals("")) && (!pin1.equals("")) && (!state1.equals(""))) {
                 //   Toast.makeText(getApplicationContext(), "Address fields input taken", Toast.LENGTH_SHORT).show();

                    sendRequest();

                    // new AsyncSignup(AndroidGPSTrackingActivity.this).execute(String.valueOf(Constants.designer_id), fname1, lname1, username, password1, email1, mobNo1, gender, selectedText, stname1, city1, state1, country1, pin1);

                } else if ((!city1.equals("")) || (!country1.equals("")) || (!pin1.equals("")) || (!stname1.equals("")) || (!state1.equals(""))) {
                    Toast.makeText(getApplicationContext(), "None of the address field can be left empty", Toast.LENGTH_SHORT).show();
                }

            }


        });
    }

    /**
     * Used to send address details like strret name, city, state, country and postal code to backend using the webservice {@link UrlConstants#editDetails}
     */
    private void saveResults() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("tag", "address");
        setResult(RESULT_OK, resultIntent);

    }
    private void sendRequest() {
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.editDetails, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hide();
                try {
                    JSONObject object = new JSONObject(s);
                    if (object.getInt("response") == 1) {

                        accounts.AccountManager.updateAddress(stname.getText().toString(), city.getText().toString(), country.getText().toString(), pin.getText().toString(), state.getText().toString(), AndroidGPSTrackingActivity.this);
                        int callingActivity = getIntent().getIntExtra("calling-activity", 0);

                        switch (callingActivity) {
                            case Constants.ACTIVITY_2:
                                startActivity(new Intent(AndroidGPSTrackingActivity.this, MainActivity.class));
                                AndroidGPSTrackingActivity.this.finish();

                                // Activity2 is started from Activity1
                                break;
                            case Constants.ACTIVITY_1:
                                saveResults();
                                AndroidGPSTrackingActivity.this.finish();
                                // Activity2 is started from Activity3
                                break;
                        }


                    } else {
                        Toast.makeText(AndroidGPSTrackingActivity.this, "Nothing to change", Toast.LENGTH_SHORT).show();
                        finish();
                    }


                } catch (JSONException e) {
                    Toast.makeText(AndroidGPSTrackingActivity.this, "Unknown Error Occurred", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hide();
                Snackbar.make(findViewById(R.id.layout), "No internet connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendRequest();
                            }
                        }).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("designer_id", String.valueOf(Constants.designer_id));

                params.put("customer_id", String.valueOf(accounts.AccountManager.getId(AndroidGPSTrackingActivity.this)));
                params.put("street", stname.getText().toString());
                params.put("city", city.getText().toString());
                params.put("country", country.getText().toString());
                params.put("pincode", pin.getText().toString());
                params.put("state", state.getText().toString());


                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);

    }

    /**
     * Used to provide run time permissions to run GPS service on Android M and above. Its also used to get the longitude and latitude and retrieve address via reverse geocoding using the function {@link GPSTracker#getAddressFromLocation(double, double, Context, GeocoderHandler)}
     */
    private void startGPSClass() {
        if (ActivityCompat.checkSelfPermission(AndroidGPSTrackingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AndroidGPSTrackingActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(AndroidGPSTrackingActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, LAST_LOCATION_REQUEST);
            return;
        }
        gps = new GPSTracker(AndroidGPSTrackingActivity.this, locationReceiver);

        //you can hard-code the lat & long if you have issues with getting it
        //remove the below if-condition and use the following couple of lines
        //double latitude = 37.422005;
        //double longitude = -122.084095


        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();


        gps.getAddressFromLocation(latitude, longitude,
                getApplicationContext(), new GeocoderHandler());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**Used to grant runtime permissons to use GPS service.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LAST_LOCATION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startGPSClass();
            } else {
                startGPSClass();
            }
        }
    }


    /**
     * Used to show settings alert dialog box which redirects to GPS settings asking user to enable the GPS.
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                AndroidGPSTrackingActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        AndroidGPSTrackingActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    /**
     *
     */
    public interface LocationReceiver {
        /**This funtion is called when the address is successfully received.
         * @param address address recieved is passed as a parameter to this function.
         */
        public void onLocationReceived(Address address);

        void GPSOFF();
    }

    /**
     * This class is used to retreive the address from the bundle that was stored in GPSTracker class using the Message objectx    */
    public class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;


            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();


                    locationAddress = bundle.getString("address");


                    break;
                default:
                    locationAddress = null;
            }
            tvAddress.setText(locationAddress);

        }
    }


}
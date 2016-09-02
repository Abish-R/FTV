package com.ftv_fashionshop.helixtech_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import accounts.AccountManager;
import app.Constants;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;

/**
 * Created by HARSH GOYNKA on 5/30/2016.
 */
public class SplashScreen extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "Xi5ZQE6v8GBMNyLUZI5wKRkhM";
    private static final String TWITTER_SECRET = "szNBTCOmCFTsvz5BoYEJsFUCEosua9YCnvZQaAdg8uAtDi5F7P";

    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    @Override

    public void onCreate(Bundle SavedInstanceState) {
        setTheme(ThemeSetter.getDesiredNoActionBarTheme(this));
        super.onCreate(SavedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_splash_screen);
        getThemeColor();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Constants.imageNumber = assignImageNumber(metrics.densityDpi);
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isMobile;
        if(activeNetwork==null){
            isMobile=true;
        }
        else
         isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        Log.e("Conectivity",isMobile+"");
        if(isMobile){
            Constants.imageNumberHigh=Constants.imageNumber;
        }
        else{
            Constants.imageNumberHigh=Constants.imageNumber+4;
        }

        Log.e("dimensions",metrics.widthPixels+" " +metrics.heightPixels);
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, LoginControllerActivity.class);
                startActivity(i);
                // close this activity
                finish();
            }
        }, 3000);
    }

    private int assignImageNumber(int densityDpi) {
        if (densityDpi < DisplayMetrics.DENSITY_MEDIUM) {
            return 0;
        } else if (densityDpi < DisplayMetrics.DENSITY_HIGH) {
            return 1;
        } else if (densityDpi < DisplayMetrics.DENSITY_XHIGH) {
            return 2;
        } else
            return 3;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    private void getThemeColor() {
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.getColorCode, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject object = new JSONObject(s);

                    int colour = object.getInt("colour");
                    int reader = SplashScreen.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).getInt("colour", -1);
                    if (colour != reader) {

                        SharedPreferences.Editor editor = SplashScreen.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
                        editor.putInt("colour", colour);
                        editor.commit();

                      //  restartActivity();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("designer_id", String.valueOf(Constants.designer_id ));
                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }
    private void restartActivity() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(0, 0);
    }
}

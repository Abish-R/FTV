package com.ftv_fashionshop.helixtech_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.UrlConstants;
import app.VolleySingleton;

/**
 * Created by Aman Agrawal on 13/06/2016.
 */
public class CategoryFilter extends AppCompatActivity {
    LinearLayout container;
    JSONArray data;
    int theme;
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getDesiredTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_category);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_cancel);
        setTitle("Filters");

        TextView name = (TextView) findViewById(R.id.name);
        container = (LinearLayout) findViewById(R.id.container);
        sendRequest();

    }
    private int getDesiredTheme() {
        SharedPreferences editor = CategoryFilter.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        int pos = editor.getInt("colour", -1);

        switch (pos) {
            case 0:
                theme = R.style.AppTheme;
                break;
            case 1:
                theme = R.style.AppTheme2;
                break;
            case 2:
                theme = R.style.AppTheme3;
                break;
            case 3:
                theme = R.style.AppTheme4;
                break;
            case 4:
                theme = R.style.AppTheme5;
                break;
            case 5:
                theme = R.style.AppTheme6;
                break;
            case 6:
                theme = R.style.AppTheme7;
                break;

            case 7:
                theme = R.style.AppTheme8;
                break;
            default:
                theme = R.style.AppTheme;
                break;
        }
        return theme;

    }
    private void sendRequest() {
        // final JSONObject jsonObject = new JSONObject(String.valueOf(data));

        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.categoryFilter, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                container.removeAllViews();
                JSONArray array = null;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    array = jsonObject.getJSONArray("All Categories");

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object;
//                    try {

                        View v = createView(array.getString(i), container);
                        container.addView(v);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(CategoryFilter.this, "Please  check your Internet Connection!", Toast.LENGTH_SHORT).show();
            }

        })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("designer_id", String.valueOf(app.Constants.designer_id));
                return map;


            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }


    private View createView(String object, LinearLayout parent) throws JSONException {

        View v;
        TextView name;

        v = LayoutInflater.from(this).inflate(R.layout.filter_category, parent, false);
        name = (TextView) v.findViewById(R.id.name);
        name.setText(object);


        return v;
    }


}

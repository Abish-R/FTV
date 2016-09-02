package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.adapter.ReviewAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.Constants;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import helper.DividerItemDecoration;
import helper.MyCustomProgressDialog;

public class AllReview extends AppCompatActivity {
    RecyclerView reviews;
    ProgressDialog dialog;
    ReviewAdapter mAdapter;

    int theme;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_review2);

        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);
        reviews = (RecyclerView) findViewById(R.id.recyclerView);
        FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllReview.this, AdReview.class));
            }
        });

        mAdapter = new ReviewAdapter(this);
        reviews.setAdapter(mAdapter);
        reviews.setLayoutManager(new LinearLayoutManager(this));
        reviews.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        sendRequest();
    }


    private void sendRequest() {
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.allReviews, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hide();
                try {
                    JSONObject object = new JSONObject(s);
                    updateUI(object);

                } catch (JSONException e) {
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
                params.put("page_no", "0");

                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    private void updateUI(JSONObject object) throws JSONException {
        mAdapter.setData(object.getJSONArray("data"));
    }
}

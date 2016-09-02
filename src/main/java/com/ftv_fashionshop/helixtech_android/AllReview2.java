package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.adapter.ReviewAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.UrlConstants;
import app.VolleySingleton;
import accounts.AccountManager;
import helper.DividerItemDecoration;

public class AllReview2 extends AppCompatActivity {
    RatingBar averageRatingBar;
    RecyclerView reviews;
    ProgressDialog dialog;
    ReviewAdapter mAdapter;
    int theme;
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    int product_id, customer_id;
    int page_no = 0;
    TextView name;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getDesiredTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_review);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        product_id = getIntent().getIntExtra("product_id", -1);
        customer_id = AccountManager.getId(this);

        if (product_id == -1 || customer_id == -1) {
            Toast.makeText(this, "Unknown Error Occurred!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Reviews");
        initializeViews();

        mAdapter = new ReviewAdapter(this);
        reviews.setAdapter(mAdapter);
        reviews.setLayoutManager(new LinearLayoutManager(this));
        reviews.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        reviews.setNestedScrollingEnabled(false);

        sendRequest();

    }
    /** Used to change the theme by dynamically selecting the theme from desginer's side and store it in local database.Further it also used the {@link #setTheme(int)} to set the theme dynamically by using the theme stored in the shared preference.
     * @return theme stored in integer
     */
    private int getDesiredTheme() {
        SharedPreferences editor = AllReview2.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        int pos = editor.getInt("colour", -1);

        switch (pos) {
            case 0:
                theme = R.style.AppTheme_NoActionBar;
                break;
            case 1:
                theme = R.style.AppTheme2_NoActionBar;
                break;
            case 2:
                theme = R.style.AppTheme3_NoActionBar;
                break;
            case 3:
                theme = R.style.AppTheme4_NoActionBar;
                break;
            case 4:
                theme = R.style.AppTheme5_NoActionBar;
                break;
            case 5:
                theme = R.style.AppTheme6_NoActionBar;
                break;
            case 6:
                theme = R.style.AppTheme7_NoActionBar;
                break;

            case 7:
                theme = R.style.AppTheme8_NoActionBar;
                break;
            default:
                theme = R.style.AppTheme_NoActionBar;
                break;
        }
        return theme;

    }
    private void sendRequest() {
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.allReviews, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hide();
                page_no++;
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
                Snackbar.make(findViewById(R.id.linearLayout), "No internet connection", Snackbar.LENGTH_INDEFINITE)
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
                params.put("product_id", String.valueOf(product_id));
                params.put("page_no", String.valueOf(page_no));

                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    private void updateUI(JSONObject object) throws JSONException {
        averageRatingBar.setRating(Float.parseFloat(object.getString("average rating")));

        mAdapter.setData(object.getJSONArray("data"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void initializeViews() {
        name = (TextView) findViewById(R.id.name);
        image = (ImageView) findViewById(R.id.product_image);

        averageRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        reviews = (RecyclerView) findViewById(R.id.recyclerView);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);

        name.setText(getIntent().getStringExtra("product_name"));
        Picasso.with(this).load(getIntent().getStringExtra("product_url")).into(image);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null)
            dialog.dismiss();
    }
}

package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.adapter.WishlistPageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.Constants;
import app.UrlConstants;
import app.VolleySingleton;
import accounts.AccountManager;
import helper.MyCustomProgressDialog;
import helper.VerticalSpaceItemDecoration;

/**
 * Created by  : Aman Agrawal
 * Created on  : 7/06/2016.
 * Verified by:
 * Verified on:
 * This activity is used to display the wish listed products of the customer by displaying the card fo wish listed products in a recycler view.
 * */
public class Wishlist extends AppCompatActivity {

    AccountManager accountManager;
    ProgressDialog dialog;
    RecyclerView mRecyclerView;
    int theme;
    boolean itemsAvailable=true;
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    WishlistPageAdapter mAdapter;

    boolean contentIsLoaded = false;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    int pageNumber = 0;
    JSONArray data;
    // SwipeRefreshLayout mSwipeRefreshLayout;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getDesiredTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
// toolbar


        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new WishlistPageAdapter(this);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading &&  itemsAvailable &&(totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached

                    Log.i("Yaeye!", "end called");

                    // Do something

                    loading = true;
                    requestProducts();
                }
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(30));


        /*mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                pageNumber = 0;
                AppController.getInstance().cancelAllPendingRequests();
//                mSwipeRefreshLayout.setRefreshing(true);
                requestProducts();
            }
        });*/

        requestProducts();

        if (pageNumber == 0)
            dialog.show();


    }
    /** Used to change the theme by dynamically selecting the theme from desginer's side and store it in local database.Further it also used the {@link #setTheme(int)} to set the theme dynamically by using the theme stored in the shared preference.
     * @return theme stored in integer
     */
    private int getDesiredTheme() {
        SharedPreferences editor = Wishlist.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
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
    private void requestProducts() {
        // mSwipeRefreshLayout.setRefreshing(true);
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.
                wishListURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("Response", s);
                //mSwipeRefreshLayout.setRefreshing(false);
                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray temp = object.getJSONArray("data");
                    if (temp.length() == 0) {
                        itemsAvailable=false;
                        dialog.hide();
                    }
                    if (pageNumber == 0) {
                        data = temp;
                        mAdapter.setData(data);
                        mRecyclerView.setAdapter(mAdapter);
                        dialog.hide();
                        if(data.length()!=0){
                            toggleEmpty(View.GONE);
                        }
                        // fab.show();
                    } else {
                        for (int i = 0; i < temp.length(); i++) {

                            data.put(temp.getJSONObject(i));
                            mAdapter.notifyItemInserted(data.length() - 1);
                        }
                    }
                    contentIsLoaded = true;
                    pageNumber++;
                    loading = false;


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hide();
                //todo show onscreen tap
                Snackbar.make(findViewById(R.id.wish_list_container), "No internet connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestProducts();
                            }
                        }).show();
//                Toast.makeText(MainActivity.this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("designer_id", String.valueOf(Constants.designer_id));
                params.put("customer_id", String.valueOf(AccountManager.getId(Wishlist.this)));
                params.put("pageno", String.valueOf(pageNumber));
                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    public void toggleEmpty(int visible) {
        findViewById(R.id.empty).setVisibility(visible);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();

        }

        return super.onOptionsItemSelected(item);
    }


}





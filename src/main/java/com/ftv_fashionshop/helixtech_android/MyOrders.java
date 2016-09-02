package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.adapter.MyOrdersAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import accounts.AccountManager;
import app.Constants;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import helper.MyCustomProgressDialog;
import helper.VerticalSpaceItemDecoration;


/**
 * Created by  : Aman Agrawal
 * Created on  : 08/06/2016.
 * Verified by:
 * Verified on:
 * This activity is used to display the various order made by a customer by implementing a recycler view and inflating it with cards containing details about the order and also implementing pagenation.
 */
public class MyOrders extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    AccountManager accountManager;
    ProgressDialog dialog;
    RecyclerView mRecyclerView;
    int theme;
    MyOrdersAdapter mAdapter;
    Menu menu;

    boolean contentIsLoaded = false;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    int pageNumber = 0;
    JSONArray data;
    JSONObject object;
    // SwipeRefreshLayout mSwipeRefreshLayout;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorders);

// toolbar


        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new MyOrdersAdapter(this);
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
                if (!loading && (totalItemCount - visibleItemCount)
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


    /**
     * Used to send details like designer id and customer id to backend using the webservice {@link UrlConstants#myOrdersURL} and implement pagenation.
     */
    private void requestProducts() {
        // mSwipeRefreshLayout.setRefreshing(true);
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.
                myOrdersURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("Response", s);
                //mSwipeRefreshLayout.setRefreshing(false);
                try {
                    object = new JSONObject(s);
                    JSONArray temp = object.getJSONArray("data");
                    if (temp.length() == 0) {
                        dialog.hide();
                    }
                    if (pageNumber == 0) {
                        data = temp;
                        mAdapter.setData(data);
                        mRecyclerView.setAdapter(mAdapter);
                        dialog.hide();
                        if(data.length()==0){
                            findViewById(R.id.empty).setVisibility(View.VISIBLE);
                        }
                        // fab.show();
                    } else {
                        for (int i = 0; i < temp.length(); i++) {
                            int flag = 0;
                            for (int j = 0; j < data.length(); j++) {
                                if (data.getJSONObject(j).getInt("product_id") == temp.getJSONObject(i).getInt("product_id")) {
                                    flag = 1;
                                    break;
                                }
                            }
                            if (flag == 1) {
                                continue;
                            }
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
                Snackbar.make(findViewById(R.id.offer_container), "No internet connection", Snackbar.LENGTH_INDEFINITE)
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
                params.put("customer_id", String.valueOf(AccountManager.getId(MyOrders.this)));
                params.put("pageno", String.valueOf(pageNumber));
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


}

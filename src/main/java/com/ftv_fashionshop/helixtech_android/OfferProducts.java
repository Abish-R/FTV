package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.adapter.ViewAllActivityPageAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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

public class OfferProducts extends AppCompatActivity {
    int offer_id;
    ProgressDialog dialog;
    RecyclerView mRecyclerView;
    int theme;
    int scrollDirections;
    boolean scrolling;
    LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
    int currentItem;
    int previousItemNumber;
    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (!scrolling && previousItemNumber == currentItem) {
                try {
                    loadHighResImage();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    android.os.Handler handler;
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    ViewAllActivityPageAdapter mAdapter;
    boolean contentIsLoaded = false;
    int firstVisibleItem, visibleItemCount, totalItemCount, LikeListStatus;
    int pageNumber = 0;
    JSONArray data;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all);
        handler = new android.os.Handler();
        offer_id = getIntent().getIntExtra("offer_id", -1);
        if (offer_id == -1) {
            return;
        }
        setTitle(getIntent().getStringExtra("offer_name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new MyCustomProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new ViewAllActivityPageAdapter(this);
        mRecyclerView.setVisibility(View.GONE);
        mLayoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    scrollDirections = 1; //upward
                } else if (dy < 0)
                    scrollDirections = -1;

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
                    loading = true;
                    requestProducts();
                }
            }


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                    int completeVisibleItem = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if (completeVisibleItem != RecyclerView.NO_POSITION) {
                        currentItem = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                        mLayoutManager.smoothScrollToPosition(mRecyclerView, null, mLayoutManager.findFirstCompletelyVisibleItemPosition());
                    } else {
                        int firstItemPos = mLayoutManager.findFirstVisibleItemPosition();
                        View firstItemView = mLayoutManager.findViewByPosition(firstItemPos);
                        int lastItemPos = mLayoutManager.findLastVisibleItemPosition();
                        View lastItemView = mLayoutManager.findViewByPosition(lastItemPos);
                        if (lastItemView != null && firstItemView != null) {
                            if (scrollDirections == -1) {
                                if (Math.abs(lastItemView.getY()) / recyclerView.getHeight() > 0.4) {
                                    mLayoutManager.smoothScrollToPosition(mRecyclerView, null, firstItemPos);
                                    currentItem = firstItemPos;
                                } else {
                                    mLayoutManager.smoothScrollToPosition(mRecyclerView, null, lastItemPos);
                                    currentItem = lastItemPos;
                                }
                            } else {
                                if (Math.abs(firstItemView.getY()) / recyclerView.getHeight() < 0.4) {
                                    mLayoutManager.smoothScrollToPosition(mRecyclerView, null, firstItemPos);
                                    currentItem = firstItemPos;
                                } else {
                                    mLayoutManager.smoothScrollToPosition(mRecyclerView, null, lastItemPos);
                                    currentItem = lastItemPos;
                                }
                            }
                        }
                    }
                    previousItemNumber = currentItem;

                    handler.postDelayed(r, 800);

                } else {
                    scrolling = true;
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(30));
        requestProducts();

        if (pageNumber == 0)
            dialog.show();

    }
    private void loadHighResImage() throws JSONException {
        final View v = mLayoutManager.findViewByPosition(currentItem);

        if (mLayoutManager.getItemViewType(v) == 1) {
            Log.e("Image loaded for:", data.getJSONObject(currentItem).getString("product_name") +" "+ data.getJSONObject(currentItem).getJSONArray("images").getString(Constants.imageNumber + 4));
            v.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            Picasso.with(this)
                    .load(data.getJSONObject(currentItem).getJSONArray("images").getString(Constants.imageNumberHigh))
                    .into((ImageView) v.findViewById(R.id.imageView), new Callback() {
                        @Override
                        public void onSuccess() {
                            v.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            v.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                            ((ImageView) v.findViewById(R.id.imageView)).setImageResource(R.drawable.ic_error);
                        }
                    });
        }


    }
    private void requestProducts() {
        // mSwipeRefreshLayout.setRefreshing(true);
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.productForOffer, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("Response", s);
                //mSwipeRefreshLayout.setRefreshing(false);
                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray temp = object.getJSONArray("data");
                    if (temp.length() == 0) {
                        dialog.hide();
                    }
                    if (pageNumber == 0) {
                        dialog.hide();
                        if (temp.length() == 1) {
                            int product_id = temp.getJSONObject(0).getInt("product_id");
                            Intent intent = new Intent(OfferProducts.this, ProductDescriptionActivity.class);
                            intent.putExtra("product_id", product_id);
                            startActivity(intent);
                            OfferProducts.this.finish();
                            return;
                        }
                        else {
                            data = temp;
                            mAdapter.setData(data);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mRecyclerView.setAdapter(mAdapter);

                        }
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

                Toast.makeText(OfferProducts.this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("offer_id", String.valueOf(offer_id));
                params.put("customer_id", String.valueOf(AccountManager.getId(OfferProducts.this)));
                params.put("designer_id", String.valueOf(Constants.designer_id));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

}

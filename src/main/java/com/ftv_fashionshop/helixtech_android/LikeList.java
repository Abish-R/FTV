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
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.adapter.LikelistPageAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.Constants;
import app.ThemeSetter;
import app.UrlConstants;
import app.VolleySingleton;
import accounts.AccountManager;
import helper.MyCustomProgressDialog;
import helper.VerticalSpaceItemDecoration;

/**
 * Created by Aman Agrawal on 07/06/2016.
 */
public class LikeList extends AppCompatActivity {


    AccountManager accountManager;
    ProgressDialog dialog;
    RecyclerView mRecyclerView;
    LikelistPageAdapter mAdapter;
    int theme;
    int scrollDirections;
    boolean scrolling;
    LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
    int currentItem;
    int previousItemNumber;
    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (!scrolling && previousItemNumber == currentItem && previousItemNumber == Constants.loadedImageNumber) {
                try {
                    loadHighResImage();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    android.os.Handler handler;
    private int position;

    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    int customer_id;
    boolean contentIsLoaded = false;
    int firstVisibleItem, visibleItemCount, totalItemCount, LikeListStatus;
    int pageNumber = 0;
    JSONArray data;
    // SwipeRefreshLayout mSwipeRefreshLayout;
    private int previousTotal = 0;
    private boolean loading = true;

    private int visibleThreshold = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSetter.getDesiredTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likelist);
// toolbar
        customer_id = AccountManager.getId(this);

        handler = new android.os.Handler();
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
        mAdapter = new LikelistPageAdapter(this);
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
    private void loadHighResImage() throws JSONException {
        final View v = mLayoutManager.findViewByPosition(currentItem);

        if (mLayoutManager.getItemViewType(v) == 1) {
            Log.e("Image loaded for:", data.getJSONObject(currentItem).getString("product_name") +" "+ data.getJSONObject(currentItem).getJSONArray("images").getString(Constants.imageNumber + 4));
            v.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            Picasso.with(this)
                    .load(data.getJSONObject(currentItem).getJSONArray("images").getString(Constants.imageNumberHigh))
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
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
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.
                likeListURL, new Response.Listener<String>() {
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
                        data = temp;
                        mAdapter.setData(data);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mRecyclerView.setAdapter(mAdapter);
                        dialog.hide();
                        if(data.length()!=0){
                            toggleEmpty(View.GONE);
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
                Snackbar.make(findViewById(R.id.like_list_container), "No internet connection", Snackbar.LENGTH_INDEFINITE)
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
                params.put("customer_id", String.valueOf(customer_id));
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


    public void saveInfo(String insertionResult, int response, int status) {

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("insertionResult", insertionResult);
        editor.putInt("response", response);
        editor.putInt("status", status);
        editor.apply();
    }

    public void toggleEmpty(int visible) {
        findViewById(R.id.empty).setVisibility(visible);

    }
}

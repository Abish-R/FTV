package com.ftv_fashionshop.helixtech_android;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.adapter.LandingPageAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import accounts.AccountManager;
import app.AppController;
import app.Constants;
import app.UrlConstants;
import app.VolleySingleton;
import helper.VerticalSpaceItemDecoration;

public class LandingFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    RecyclerView mRecyclerView;
    LandingPageAdapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;


    int firstVisibleItem, visibleItemCount, totalItemCount;
    int pageNumber;
    JSONArray data;
    ProgressBar progressBar;
    String url = "";
    int scrollDirections;
    boolean scrolling;
    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
    int currentItem;
    int previousItemNumber;
    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (!scrolling && previousItemNumber == currentItem && currentItem == Constants.loadedImageNumber) {
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
    private boolean loading;
    private int visibleThreshold = 2;
    private int previousTotal;

    public LandingFragment() {
        // Required empty public constructor
    }

    public static LandingFragment newInstance(int pos) {
        LandingFragment fragment = new LandingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, pos);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_PARAM1);
        }
        switch (position) {
            case 0:
                url = UrlConstants.whatsNew;
                break;
            case 1:
                url = UrlConstants.readyToWear;
                break;
            case 2:
                url = UrlConstants.couture;
        }

    }

    boolean itemsAvailable=true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_landing, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pageNumber = 0;
        previousTotal = 0;
        loading = true;
        handler = new android.os.Handler();
        if (data == null) {
            data = new JSONArray();
        }
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mAdapter = new LandingPageAdapter(getActivity());

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
                if (!loading && itemsAvailable && (totalItemCount - visibleItemCount)
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

                    handler.postDelayed(r, 1500);

                } else {
                    scrolling = true;
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(30));

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                pageNumber = 0;
                itemsAvailable=true;
                AppController.getInstance().cancelAllPendingRequests();
//                mSwipeRefreshLayout.setRefreshing(true);
                requestProducts();
            }
        });
        if (data.length() == 0) {
            progressBar.setVisibility(View.VISIBLE);
        }
        requestProducts();


    }

    private void loadHighResImage() throws JSONException {
        final View v = mLayoutManager.findViewByPosition(currentItem);

        if (mLayoutManager.getItemViewType(v) == 1) {
            ImageView iv= (ImageView) v.findViewById(R.id.imageView);
            Log.e("Image loaded for:", data.getJSONObject(currentItem).getString("product_name") +" "+ data.getJSONObject(currentItem).getJSONArray("images").getString(Constants.imageNumber + 4));
            //Toast.makeText(getActivity(),data.getJSONObject(currentItem).getJSONArray("images").length(),
             //       Toast.LENGTH_LONG).show();
            Picasso.with(getActivity())
                    .load(data.getJSONObject(currentItem).getJSONArray("images").getString(Constants.imageNumberHigh))
                    .placeholder(iv.getDrawable())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(iv, new Callback() {
                        @Override
                        public void onSuccess() {
                            v.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            v.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                            //((ImageView) v.findViewById(R.id.imageView)).setImageResource(R.drawable.ic_error);
                        }
                    });
        }
    }

    private void requestProducts() {
        mSwipeRefreshLayout.setRefreshing(true);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("Response", s);
                mSwipeRefreshLayout.setRefreshing(false);
                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray temp = object.getJSONArray("data");
                    if(temp.length()==0){
                        itemsAvailable=false;
                    }
                    if (pageNumber == 0) {
                        data = temp;
                        mAdapter.setData(data);
                        progressBar.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);

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

                    pageNumber++;
                    loading = false;


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //todo show onscreen tap

                loading = false;
                mSwipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), "No internet connection", Snackbar.LENGTH_INDEFINITE)
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
                params.put("customer_id", String.valueOf(AccountManager.getId(getActivity())));
                params.put("pageno", String.valueOf(pageNumber));
                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }
/*
    public class AsyncLoadImage extends AsyncTask<Void, Void, Void> {

        *//**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     *//*
        @Override
        protected Void doInBackground(Void... params) {
            thread.run();
            return null;
        }
    }*/


}

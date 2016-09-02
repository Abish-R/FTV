package com.ftv_fashionshop.helixtech_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ftv_fashionshop.helixtech_android.adapter.LandingPageAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.AppController;
import app.Constants;
import app.UrlConstants;
import app.VolleySingleton;
import de.hdodenhof.circleimageview.CircleImageView;
import accounts.AccountManager;
import helper.VerticalSpaceItemDecoration;


public class MainActivity2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AccountManager.LogoutCallback {

    AccountManager accountManager;
    ProgressDialog dialog;
    RecyclerView mRecyclerView;
    int theme;
    private static final String MY_PREFS_NAME = "DESIGNER_THEME";
    LandingPageAdapter mAdapter;
    FloatingActionButton fab;
    boolean contentIsLoaded = false;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    int pageNumber = 0;
    JSONArray data;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Menu menu;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getDesiredTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        accountManager = new AccountManager(this);
        accountManager.setLogoutCallback(this);

        AccountManager.sendToken(this);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Logging out");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setInverseBackgroundForced(false);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, FilterActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        fab.hide();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset == 0 && contentIsLoaded) {

                    fab.show();
                }
                fab.hide();
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (contentIsLoaded)
                    fab.show();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView userName = (TextView) headerView.findViewById(R.id.userName);
        TextView email = (TextView) headerView.findViewById(R.id.email);
        CircleImageView profilePic = (CircleImageView) headerView.findViewById(R.id.profile_image);
        String imageUrl = AccountManager.getImageURL(this);
        if (!imageUrl.isEmpty()) {
            Picasso.with(this).load(imageUrl).into(profilePic);
        } else
            profilePic.setImageDrawable(null);
        userName.setText(AccountManager.getName(this));
        email.setText(AccountManager.getEmail(this));

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new LandingPageAdapter(this);

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


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                pageNumber = 0;
                AppController.getInstance().cancelAllPendingRequests();
//                mSwipeRefreshLayout.setRefreshing(true);
                requestProducts();
            }
        });

        requestProducts();
        dialog.setMessage("Loading Products...");
        if (pageNumber == 0)
            dialog.show();

        getCartCount();

    }
    /** Used to change the theme by dynamically selecting the theme from desginer's side and store it in local database.Further it also used the {@link #setTheme(int)} to set the theme dynamically by using the theme stored in the shared preference.
     * @return theme stored in integer
     */
    private int getDesiredTheme() {
        SharedPreferences editor = MainActivity2.this.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
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
    private void getCartCount() {
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.getCartCount, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    Constants.cartNumbers = object.getInt("data");
                    MenuItem item = menu.findItem(R.id.badge);
                    RelativeLayout notifCount = (RelativeLayout) MenuItemCompat.getActionView(item);
                    TextView tv = (TextView) notifCount.findViewById(R.id.actionbar_notifcation_textview);
                    tv.setText(""+Constants.cartNumbers);

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
                params.put("customer_id", String.valueOf(AccountManager.getId(MainActivity2.this)));
                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    private void requestProducts() {
        mSwipeRefreshLayout.setRefreshing(true);
        StringRequest request = new StringRequest(Request.Method.POST, UrlConstants.feedURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("Response", s);
                mSwipeRefreshLayout.setRefreshing(false);
                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray temp = object.getJSONArray("data");
                    if (temp.length() == 0) {
                        dialog.hide();
                    }
                    if (pageNumber == 0) {
                        data = temp;
                        mAdapter.setData(data);
                        mRecyclerView.setAdapter(mAdapter);
                        dialog.hide();
                        fab.show();
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
                Snackbar.make(findViewById(R.id.coordinatorLayout), "No internet connection", Snackbar.LENGTH_INDEFINITE)
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
                params.put("customer_id", String.valueOf(AccountManager.getId(MainActivity2.this)));
                params.put("pageno", String.valueOf(pageNumber));
                return params;

            }
        };
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.logout:
                dialog.setMessage("Logging out...");
                dialog.show();
                accountManager.logout();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.order_history:
                startActivity(new Intent(this, MyOrders.class));
                break;
            case R.id.wish_list:
                startActivity(new Intent(this, Wishlist.class));
                break;
            case R.id.like_list:
                startActivity(new Intent(this, LikeList.class));
                break;
            case R.id.review:
                startActivity(new Intent(this, MyOrders.class));
                break;
            case R.id.my_offers:
                startActivity(new Intent(this, MyOffers.class));
                break;

            default:
                Toast.makeText(this, "This part of the app is under progress", Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void loggedOut() {
        startActivity(new Intent(this, LoginControllerActivity.class));
        finish();
    }

    @Override
    public void logOutError(int error) {
        dialog.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.badge);
        MenuItemCompat.setActionView(item, R.layout.badge_menu_item);
        RelativeLayout notifCount = (RelativeLayout) MenuItemCompat.getActionView(item);
        TextView tv = (TextView) notifCount.findViewById(R.id.actionbar_notifcation_textview);
        tv.setText("0");
        this.menu = menu;
        menu.getItem(0).getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity2.this, CartActivity.class));
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.badge) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
